package com.restart.banzenty.ui.main

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.restart.banzenty.R
import com.restart.banzenty.adapters.FuelRequestsAdapter
import com.restart.banzenty.base.BaseFragment
import com.restart.banzenty.data.models.FuelRequestModel
import com.restart.banzenty.databinding.FragmentFuelRequestsBinding
import com.restart.banzenty.utils.Constants
import com.restart.banzenty.utils.DataStateChangeListener
import com.restart.banzenty.viewModels.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FuelRequestsFragment : BaseFragment(), SwipeRefreshLayout.OnRefreshListener {
    private val TAG = "RequestsFragment"

    lateinit var binding: FragmentFuelRequestsBinding

    private val viewModel: ProfileViewModel by viewModels()

    private var stateChangeListener: DataStateChangeListener? = null


    var fuelRequestsList: ArrayList<FuelRequestModel.FuelRequest> = ArrayList()
    private var isSwipLoading = false
    private var fuelRequestsAdapter: FuelRequestsAdapter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentFuelRequestsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        if (viewModel.isQueryExhausted) {
            if (fuelRequestsList.isEmpty()) {
                showErrorUI(
                    show = true, title = getString(R.string.no_data), desc = null,
                    image = R.drawable.ic_no_data, showButton = false
                )
            } else {
                fuelRequestsAdapter?.submitList(fuelRequestsList)
                binding.rvFuelRequests.visibility = View.VISIBLE
            }
        } else {
            if (fuelRequestsList.isEmpty()) {
                getFuelRequests(1)
            } else {
                fuelRequestsAdapter?.submitList(fuelRequestsList)
                binding.rvFuelRequests.visibility = View.VISIBLE
            }
        }
    }

    private fun initViews() {
        binding.swipeRefresh.setOnRefreshListener(this)

        binding.rvFuelRequests.apply {
            fuelRequestsAdapter = FuelRequestsAdapter(requestManager)

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (!binding.rvFuelRequests.canScrollVertically(1))
                        if (!isSwipLoading) {
                            if (!viewModel.isQueryExhausted) {
                                isSwipLoading = true
                                val currentPage = viewModel.page
                                getFuelRequests(currentPage + 1)
                            }
                        }
                }
            })
            adapter = fuelRequestsAdapter
        }

    }

    fun refreshFragment()
    {
        val position = binding.rvFuelRequests.computeVerticalScrollOffset()
        if(position != 0)
        {
            binding.rvFuelRequests.smoothScrollToPosition(0)
        } else{
            onRefresh()
        }
    }

    private fun getFuelRequests(page: Int) {
        viewModel.getFuelRequests(page).observe(viewLifecycleOwner) {
            if (it != null) {
                showProgressBar(it.loading.isLoading)
                it.error?.getContentIfNotHandled()?.let { errorState ->
                    //check if internet connection is lost
                    if (fuelRequestsList.isEmpty())
                        if (errorState.response.message == Constants.UNABLE_TODO_OPERATION_WO_INTERNET || errorState.response.message == Constants.UNABLE_TO_RESOLVE_HOST)
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
                    else stateChangeListener?.onErrorStateChange(errorState)
                }
                it.data?.data?.getContentIfNotHandled()?.let { requests ->
                    Log.d(TAG, "wallet-transactions: $requests")
                    if (requests.isNotEmpty()) {
                        if (page == 1)
                            fuelRequestsList.clear()

                        fuelRequestsList.addAll(requests)

                        fuelRequestsAdapter?.apply {
                            preloadGlideImages(
                                requestManager,
                                fuelRequestsList.toList()
                            )
                        }
                        fuelRequestsAdapter?.submitList(fuelRequestsList.toList())
                        if (page==1)
                            fuelRequestsAdapter?.notifyDataSetChanged()



                        viewModel.page =
                            fuelRequestsList.size / Constants.PAGINATION_PAGE_SIZE

                        Log.d(TAG, "page: ${viewModel.page}")
                        viewModel.isQueryExhausted = requests.size < 50

                        if (fuelRequestsList.isEmpty()) {
                            //show error UI
                            showErrorUI(
                                show = true, title = getString(R.string.no_data), desc = null,
                                image = R.drawable.ic_no_data, showButton = false
                            )
                        } else {
                            //hide error UI
                            showErrorUI(false)
                        }
                    } else {
//                        notificationsList.clear()
//                        fuelRequestsAdapter?.submitList(notificationsList.toList())
                        if (fuelRequestsList.isEmpty()) {
                            //show error UI
                            viewModel.isQueryExhausted = true
                            showErrorUI(
                                show = true, title = getString(R.string.no_data), desc = null,
                                image = R.drawable.ic_no_data, showButton = false
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
            if ((fuelRequestsAdapter?.itemCount ?: 0) == 0) {
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
            binding.rvFuelRequests.visibility = View.GONE
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
                binding.errorLayout.btnRetry.setOnClickListener { getFuelRequests(1) }
            } else binding.errorLayout.btnRetry.visibility = View.GONE

        } else {
            binding.rvFuelRequests.visibility = View.VISIBLE
            binding.errorLayout.root.visibility = View.GONE
        }
    }

    override fun onRefresh() {
        if (!isSwipLoading) {
            getFuelRequests(1)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            stateChangeListener = context as DataStateChangeListener
        } catch (e: ClassCastException) {
        }
    }
}