package com.restart.banzenty.ui.main

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.restart.banzenty.R
import com.restart.banzenty.adapters.NotificationsAdapter
import com.restart.banzenty.base.BaseActivity
import com.restart.banzenty.data.models.NotificationModel
import com.restart.banzenty.databinding.ActivityNotificationsBinding
import com.restart.banzenty.utils.Constants.Companion.PAGINATION_PAGE_SIZE
import com.restart.banzenty.utils.Constants.Companion.UNABLE_TODO_OPERATION_WO_INTERNET
import com.restart.banzenty.utils.Constants.Companion.UNABLE_TO_RESOLVE_HOST
import com.restart.banzenty.viewModels.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotificationsActivity : BaseActivity(), SwipeRefreshLayout.OnRefreshListener {

    private val TAG = "NotificationsActivity"
    lateinit var binding: ActivityNotificationsBinding

    private val viewModel: ProfileViewModel by viewModels()

    var notificationsList: ArrayList<NotificationModel.Notification> = ArrayList()
    private var isSwipLoading = false
    private var notificationAdapter: NotificationsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initToolbar()
        initViews()
        getNotifications(1)
    }

    private fun initToolbar() {
        binding.toolbar.textViewToolbarTitle.text = getString(R.string.notifications)
        binding.toolbar.root.setNavigationIcon(R.drawable.back_ic)
        binding.toolbar.root.setNavigationOnClickListener { finish() }
    }

    private fun initViews() {
        binding.swipeRefresh.setOnRefreshListener(this)

        binding.rvNotifications.apply {
            notificationAdapter = NotificationsAdapter(requestManager)

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (!binding.rvNotifications.canScrollVertically(1))
                        if (!isSwipLoading) {
                            if (!viewModel.isQueryExhausted) {
                                isSwipLoading = true
                                val currentPage = viewModel.page
                                getNotifications(currentPage + 1)
                            }
                        }
                }
            })
            adapter = notificationAdapter
        }
    }

    private fun getNotifications(page: Int) {
        viewModel.getNotifications(page).observe(this) {
            if (it != null) {
                it.error?.getContentIfNotHandled()?.let { errorState ->
                    //check if internet connection is lost
                    if (notificationsList.isEmpty())
                        if (errorState.response.message == UNABLE_TODO_OPERATION_WO_INTERNET || errorState.response.message == UNABLE_TO_RESOLVE_HOST)
                            showErrorUI(
                                show = true,
                                title = getString(R.string.no_internet),
                                desc = null,
                                image = R.drawable.ic_no_internet,
                                showButton = true
                            ) else
                            showErrorUI(
                                show = true,
                                title = getString(R.string.something_went_wrong),
                                desc = null,
                                image = R.drawable.ic_something_wrong,
                                showButton = true
                            )
                    else onErrorStateChange(errorState)
                }
                it.loading.let { isLoading ->
                    showProgressBar(isLoading.isLoading)
                }
                it.data?.data?.getContentIfNotHandled()?.let { notifications ->
                    if (notifications.isNotEmpty()) {
                        if (page == 1)
                            notificationsList.clear()

                        notificationsList.addAll(notifications)

                        notificationAdapter?.apply {
                            preloadGlideImages(
                                requestManager,
                                notificationsList.toList()
                            )
                        }
                        notificationAdapter?.submitList(notificationsList.toList())



                        viewModel.page =
                            notificationsList.size / PAGINATION_PAGE_SIZE

                        Log.d(TAG, "page: ${viewModel.page}")
                        viewModel.isQueryExhausted = notifications.size < 50

                        if (notificationsList.isEmpty()) {
                            //show error UI
                            showErrorUI(
                                show = true, title = getString(R.string.no_data), desc = null,
                                image = R.drawable.ic_no_notifications, showButton = false
                            )
                        } else {
                            //hide error UI
                            showErrorUI(false)
                        }
                    } else {
//                        notificationsList.clear()
//                        notificationAdapter?.submitList(notificationsList.toList())
                        if (notificationsList.isEmpty()) {
                            //show error UI
                            showErrorUI(
                                show = true, title = getString(R.string.no_data), desc = null,
                                image = R.drawable.ic_no_notifications, showButton = false
                            )
                        } else {
                            //hide error UI
                            showErrorUI(false)
                        }
                    }
                }
            }
        }
    }

    override fun showProgressBar(show: Boolean) {
        if (show) {
            showErrorUI(false)
            if ((notificationAdapter?.itemCount ?: 0) == 0) {
                binding.shimmer.visibility = View.VISIBLE
            } else {
                isSwipLoading = true
                binding.swipeRefresh.isRefreshing = isSwipLoading
            }
        } else {
            binding.shimmer.visibility = View.GONE
            isSwipLoading = false
            binding.swipeRefresh.isRefreshing = isSwipLoading
        }
    }

    override fun showErrorUI(
        show: Boolean,
        image: Int?,
        title: String?,
        desc: String?,
        showButton: Boolean?
    ) {
        if (show) {
            binding.swipeRefresh.visibility = View.GONE
            binding.errorLayout.tvDesc.visibility = View.GONE
            binding.errorLayout.root.visibility = View.VISIBLE
            binding.errorLayout.tvTitle.text = title
            binding.errorLayout.ivPic.setBackgroundResource(image!!)
            desc?.let {
                binding.errorLayout.tvDesc.visibility = View.VISIBLE
                binding.errorLayout.tvDesc.text = it
            }
            binding.errorLayout.tvTitle.text = title
            if (showButton == true) {
                binding.errorLayout.btnRetry.visibility = View.VISIBLE
                binding.errorLayout.btnRetry.setOnClickListener { getNotifications(1) }
            } else binding.errorLayout.btnRetry.visibility = View.GONE

        } else {
            binding.swipeRefresh.visibility = View.VISIBLE
            binding.errorLayout.root.visibility = View.GONE
        }
    }


    override fun onRefresh() {
        if (!isSwipLoading) {
            getNotifications(1)
        }
    }
}