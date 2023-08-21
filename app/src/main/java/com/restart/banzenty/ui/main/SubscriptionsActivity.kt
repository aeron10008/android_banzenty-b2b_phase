package com.restart.banzenty.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.restart.banzenty.R
import com.restart.banzenty.adapters.SubscriptionsAdapter
import com.restart.banzenty.base.BaseActivity
import com.restart.banzenty.data.models.PlanModel
import com.restart.banzenty.databinding.ActivitySubscriptionsBinding
import com.restart.banzenty.utils.Constants
import com.restart.banzenty.utils.displayInfoDialog
import com.restart.banzenty.viewModels.SubscriptionsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SubscriptionsActivity : BaseActivity(), SubscriptionsAdapter.OnItemClickedInterface {

    private var subscriptionsData: PlanModel? = null
    private val TAG = "SubscriptionsActivity"
    private var activeSubscriptionId: Int = -1
    private var selectedSubscriptionId: Int = -1

    lateinit var binding: ActivitySubscriptionsBinding

    private val viewModel: SubscriptionsViewModel by viewModels()
    var subscriptionPlansList: ArrayList<PlanModel.Plan> = ArrayList()
    private var subscriptionPlanAdapter: SubscriptionsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubscriptionsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getIntentExtras()
        toolbarSetup()
        getSubscriptions()
        setDetails()
    }

    private fun getIntentExtras() {
        activeSubscriptionId = intent.getIntExtra(
            "active_subscription_id",
            -1
        )
    }

    private val openSubscriptionIntent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            run {
                if (it.resultCode == RESULT_OK) {
//                    val returnIntent = Intent()
//                    setResult(RESULT_OK, returnIntent)
                    setDetails()
                    getSubscriptions()
//                    subscriptionPlanAdapter?.setPendingSubscriptionId(
//                        selectedSubscriptionId
//                    )
//                    subscriptionPlansList.clear()
//                    subscriptionPlansList.addAll(subscriptionsData?.plans ?: arrayListOf())
//                    subscriptionPlanAdapter?.submitList(subscriptionPlansList)
//                    finish()
                }
            }
        }

    private fun toolbarSetup() {
        binding.toolbar.textViewToolbarTitle.text = getString(R.string.subscriptions)

        binding.toolbar.root.setNavigationIcon(R.drawable.back_ic)
        binding.toolbar.root.setNavigationOnClickListener { finish() }
    }

    private fun getSubscriptions() {
        viewModel.getSubscriptionPlans().observe(this) {
            if (it != null) {
                it.error?.getContentIfNotHandled()?.let { errorState ->
                    //check if internet connection is lost
                    if (subscriptionPlansList.isEmpty()) {
                        if (errorState.response.message == Constants.UNABLE_TODO_OPERATION_WO_INTERNET
                            || errorState.response.message == Constants.UNABLE_TO_RESOLVE_HOST
                        ) {
                            showErrorUI(
                                show = true, title = getString(R.string.no_internet),
                                desc = null, image = R.drawable.ic_no_internet, showButton = true
                            )
                        } else {
                            showErrorUI(
                                show = true,
                                title = getString(R.string.something_went_wrong),
                                desc = null,
                                image = R.drawable.ic_something_wrong,
                                showButton = true
                            )
                        }
                    } else {
                        onErrorStateChange(errorState)
                    }
                }

                showProgressBar(it.loading.isLoading)

                it.data?.data?.getContentIfNotHandled()?.let { subscriptions ->
                    if (subscriptions.plans.isNotEmpty()) {
                        subscriptionsData = subscriptions
                        subscriptionPlansList.clear()
                        subscriptionPlansList.addAll(subscriptions.plans)
                        subscriptionPlanAdapter?.setPendingSubscriptionId(
                            subscriptionsData?.pendingSubscriptionId ?: -1
                        )
                        subscriptionPlanAdapter?.submitList(subscriptionPlansList.toList())

                        viewModel.page = subscriptionPlansList.size / Constants.PAGINATION_PAGE_SIZE

                        Log.d(TAG, "getSubscriptions: page: ${viewModel.page}")
                        viewModel.isQueryExhausted = subscriptions.plans.size < 50

                        if (subscriptionPlansList.isEmpty()) {
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
                        subscriptionPlansList.clear()
                        subscriptionPlanAdapter?.submitList(subscriptionPlansList.toList())

                        if (subscriptionPlansList.isEmpty()) {
                            //show error UI
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

    private fun setDetails() {
        binding.rvSubscriptions.apply {
            subscriptionPlanAdapter =
                SubscriptionsAdapter(requestManager, this@SubscriptionsActivity)
            subscriptionPlanAdapter?.setActiveSubscriptionId(
                activeSubscriptionId
            )
            adapter = subscriptionPlanAdapter
        }
    }

    override fun showProgressBar(show: Boolean) {
        if (show) {
            showErrorUI(false)
            binding.shimmer.visibility = View.VISIBLE
        } else {
            binding.shimmer.visibility = View.GONE
        }
    }

    override fun showErrorUI(
        show: Boolean, image: Int?, title: String?, desc: String?,
        showButton: Boolean?
    ) {
        if (show) {
            binding.rvSubscriptions.visibility = View.GONE
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
                binding.errorLayout.btnRetry.setOnClickListener { getSubscriptions() }
            } else
                binding.errorLayout.btnRetry.visibility = View.GONE

        } else {
            binding.rvSubscriptions.visibility = View.VISIBLE
            binding.errorLayout.root.visibility = View.GONE
        }
    }

    override fun onSubscribeClicked(isSubscribed: Boolean, selectedSubscription: PlanModel.Plan) {
        if (isSubscribed) {
            val returnIntent = Intent()
            setResult(RESULT_OK, returnIntent)
            finish()
        } else {
            if (activeSubscriptionId == -1
            ) {
                if ((subscriptionsData?.pendingSubscriptionId ?: -1) != -1) {
                    if (subscriptionsData?.pendingSubscriptionId == selectedSubscription.id)
                        displayInfoDialog(subscriptionsData?.requestedSubscriptionMessage)
                    else
                        displayInfoDialog(subscriptionsData?.pendingSubscriptionMessage)
                } else {
//                    subscriptionsData?.pendingSubscriptionId = selectedSubscription.id
                    this.selectedSubscriptionId = selectedSubscription.id
                    val subscriptionsIntent =
                        Intent(this, SubscriptionOptionsActivity::class.java)
                    subscriptionsIntent.putExtra("selected_subscription", selectedSubscription)
                    subscriptionsIntent.putExtra(
                        "requested_subscription_message",
                        subscriptionsData?.requestedSubscriptionMessage
                    )
                    openSubscriptionIntent.launch(subscriptionsIntent)
                }
            }
        }
    }
}