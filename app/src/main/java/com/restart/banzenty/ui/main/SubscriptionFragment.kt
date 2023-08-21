package com.restart.banzenty.ui.main

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.restart.banzenty.R
import com.restart.banzenty.adapters.SubscriptionFeatureAdapter
import com.restart.banzenty.base.BaseFragment
import com.restart.banzenty.data.models.HomeModel
import com.restart.banzenty.data.models.UserModel
import com.restart.banzenty.databinding.FragmentSubscriptionBinding
import com.restart.banzenty.utils.*
import com.restart.banzenty.viewModels.SubscriptionsViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SubscriptionFragment : BaseFragment(), View.OnClickListener,
    SwipeRefreshLayout.OnRefreshListener {
    private var subscribeMessages: HomeModel.SubscribeMessages? = null
    private val TAG = "MySubscriptionFragment"
    private lateinit var binding: FragmentSubscriptionBinding
    private var stateChangeListener: DataStateChangeListener? = null
    private val viewModel: SubscriptionsViewModel by viewModels()
    private var renewSubscriptionClicked = false
    private var cancelSubscriptionClicked = false
    private var activeSubscription: UserModel.ActiveSubscription? = null
    private var isLoadSuccess = false

    private val openSubscriptionIntent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            run {
                if (it.resultCode == Activity.RESULT_OK) {
                    //open subscription fragment
                    (activity as MainActivity).navigateToHome()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSubscriptionBinding.inflate(inflater, container, false)
        initViews()
        return binding.root
    }

    private fun initViews() {
        binding.btnCancel.setOnClickListener(this)
        binding.btnRenew.setOnClickListener(this)
        binding.swipeRefresh.setOnRefreshListener(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeObservers()
    }

    private fun getMySubscription() {
        renewSubscriptionClicked = false
        cancelSubscriptionClicked = false
        viewModel.getMySubscription().observe(viewLifecycleOwner) {
            if (it != null) {
                if ((sessionManager.isUserSubscribed() && activeSubscription == null) || !isLoadSuccess)
                    showProgressBar(it.loading.isLoading)
                it.error?.getContentIfNotHandled()?.let { stateError ->
                    //check if internet connection is lost
                    isLoadSuccess = false
                    if (stateError.response.message == Constants.UNABLE_TODO_OPERATION_WO_INTERNET || stateError.response.message == Constants.UNABLE_TO_RESOLVE_HOST)
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
                }
                it.data?.data?.getContentIfNotHandled()?.let { homeModel ->
                    this.subscribeMessages = homeModel.subscribeMessages
                    isLoadSuccess = true
                    (activity as MainActivity).activeSubscription.value =
                        homeModel.user?.activeSubscription
                }
            }
        }
    }

    private fun subscribeObservers() {
        showProgressBar(true)
        (activity as MainActivity).activeSubscription.observe(viewLifecycleOwner) { subscription ->
            showProgressBar(false)
            if (subscription == null) {
                activeSubscription = null
                showErrorUI(
                    show = true, title = getString(R.string.no_subscription_found), desc = null,
                    image = R.drawable.ic_no_data, showButton = true
                )
            } else {
                showErrorUI(false)
                this.activeSubscription = subscription
                populateCurrentSubscription()
            }
        }
    }

    private fun populateCurrentSubscription() {
        binding.tvSubscriptionName.text = activeSubscription?.plan?.name
//        binding.tvRenewDate.text =
//            getString(
//                R.string.renew_date_s,
//                MainUtils.getDateTime(activeSubscription?.renewAt)
//            )

        /*      binding.tvRenewDate.text =
                  getString(
                      R.string.renew_date_s,
                      (activeSubscription?.renewAt ?: "").split(" ")[0]
                  )*/
        binding.tvPrice.text =
            getString(R.string.total_price_s_egp, activeSubscription?.plan?.price)
//        binding.tvPeriod.text =
//            getString(R.string.period_s, activeSubscription?.plan?.period)
        binding.tvTotalLitres.text = getString(
            R.string.estimated_capacity_s_litres,
            activeSubscription?.plan?.liters.toString()
        )
        binding.tvFuelType.text =
            getString(R.string.fuel_type_s, activeSubscription?.plan?.fuel?.name)

/*        binding.tvNoLiters.text =
            resources.getString(
                R.string.remaining_liters,
                activeSubscription?.remainingLiters
            )
        binding.progressBarFuel.max = (activeSubscription?.plan?.liters ?: 0.0).toInt()
        binding.progressBarFuel.progress =
            (activeSubscription?.remainingLiters ?: 0.0).toInt()*/
        binding.tvRemainingCash.text =
            resources.getString(R.string.remaining_cash, activeSubscription?.remainingCash)
        binding.progressBarCash.max = (activeSubscription?.plan?.price ?: "0.0").toFloat().toInt()
        binding.progressBarCash.progress = (activeSubscription?.remainingCash ?: 0.0).toInt()

        if (activeSubscription?.plan?.subscriptionFeatures.isNullOrEmpty()) {
            binding.rvSubscriptionFeatures.visibility = View.GONE
            binding.tvBeneifts.visibility = View.GONE
        } else {
            binding.tvBeneifts.visibility = View.VISIBLE
            binding.rvSubscriptionFeatures.visibility = View.VISIBLE
            binding.rvSubscriptionFeatures.apply {
                val subscriptionFeaturesAdapter = SubscriptionFeatureAdapter()
                subscriptionFeaturesAdapter.submitList(
                    activeSubscription?.plan?.subscriptionFeatures ?: arrayListOf()
                )
                adapter = subscriptionFeaturesAdapter
            }
        }
    }

    override fun showProgressBar(show: Boolean) {
        if (show) {
            showErrorUI(false)
            if (renewSubscriptionClicked) {
                binding.btnRenew.visibility = View.GONE
                binding.btnCancel.isEnabled = false
                binding.progressBar.visibility = View.VISIBLE
            } else if (cancelSubscriptionClicked) {
                binding.btnCancel.visibility = View.GONE
                binding.btnRenew.isEnabled = false
                binding.progressBar.visibility = View.VISIBLE
            } else {
                binding.llSubInfo.visibility = View.GONE
                binding.tvAboutSub.visibility = View.GONE
//                binding.tvRenewDate.visibility = View.GONE
                binding.tvTotalLitres.visibility = View.GONE
                binding.tvFuelType.visibility = View.GONE
//                binding.tvPeriod.visibility = View.GONE
                binding.tvPrice.visibility = View.GONE
                binding.tvBeneifts.visibility = View.GONE
                binding.rvSubscriptionFeatures.visibility = View.GONE
                binding.llActions.visibility = View.GONE
                binding.shimmer.visibility = View.VISIBLE
            }
        } else {
            binding.btnRenew.visibility = View.VISIBLE
            binding.btnCancel.isEnabled = true
            binding.btnRenew.isEnabled = true
            binding.btnCancel.visibility = View.VISIBLE
            binding.progressBar.visibility = View.GONE
            binding.shimmer.visibility = View.GONE
            binding.swipeRefresh.isRefreshing = false
        }
    }

    override fun showErrorUI(
        show: Boolean, image: Int?, title: String?, desc: String?,
        showButton: Boolean?
    ) {
        if (show) {
            if (isLoadSuccess)
                binding.errorLayout.btnRetry.setText(R.string.subscribe_now)
            else binding.errorLayout.btnRetry.setText(R.string.text_retry)

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
                binding.errorLayout.btnRetry.setOnClickListener {
                    if (isLoadSuccess) {
//                        openSubscriptionIntent.launch(
                        startActivity(
                            Intent(
                                activity,
                                SubscriptionsActivity::class.java
                            )
                        )
//                        )
                    } else {
                        getMySubscription()
                    }
                }
            } else binding.errorLayout.btnRetry.visibility = View.GONE
        } else {
            binding.errorLayout.root.visibility = View.GONE

            binding.llSubInfo.visibility = View.VISIBLE
            binding.tvAboutSub.visibility = View.VISIBLE
//            binding.tvRenewDate.visibility = View.VISIBLE
            binding.tvTotalLitres.visibility = View.VISIBLE
            binding.tvFuelType.visibility = View.VISIBLE
//            binding.tvPeriod.visibility = View.VISIBLE
            binding.tvPrice.visibility = View.VISIBLE
            if (binding.rvSubscriptionFeatures.adapter != null && binding.rvSubscriptionFeatures.adapter?.itemCount != 0) {
                binding.tvBeneifts.visibility =
                    View.VISIBLE
                binding.rvSubscriptionFeatures.visibility = View.VISIBLE
            }
            binding.llActions.visibility = View.VISIBLE
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            stateChangeListener = context as DataStateChangeListener
        } catch (e: ClassCastException) {
        }
    }

    override fun onClick(v: View) {
        when (v) {
            binding.btnCancel -> {
                activity?.displayInfoDialog(subscribeMessages?.cancel)
            }
            /*     activity?.areYouSureDialog(
                     getString(R.string.you_want_to_cancel_subscription),
                     object : AreYouSureCallback {
                         override fun proceed() {
                             cancelSubscription()
                         }

                         override fun cancel() {
                         }

                     })*/
            binding.btnRenew -> {
                activity?.displayInfoDialog(subscribeMessages?.renew)

                /*    activity?.areYouSureDialog(
              getString(R.string.you_want_to_renew_subscription),
              object : AreYouSureCallback {
                  override fun proceed() {
                      renewSubscription()
                  }

                  override fun cancel() {
                  }

              })*/

            }

        }
    }

    private fun renewSubscription() {
        renewSubscriptionClicked = true
        viewModel.renewSubscription().observe(viewLifecycleOwner) {
            if (it != null) {
                showProgressBar(it.loading.isLoading)
                it.error?.getContentIfNotHandled()?.let { stateError ->
                    //check if internet connection is lost
                    stateChangeListener?.onErrorStateChange(stateError)
                    renewSubscriptionClicked = false
                }
                it.data?.data?.getContentIfNotHandled()?.let { renewedDate ->
                    sessionManager.setIsUserSubscribed(true)
                    renewSubscriptionClicked = false
                    activeSubscription?.renewAt = MainUtils.getDateTime(renewedDate)
                    activeSubscription?.remainingLiters = activeSubscription?.plan?.liters
                    activity?.displayToast(R.string.renewed_successfully)
                    (activity as MainActivity).activeSubscription.value = activeSubscription
                }
            }
        }
    }

    private fun cancelSubscription() {
        cancelSubscriptionClicked = true
        viewModel.cancelSubscription().observe(viewLifecycleOwner) {
            if (it != null) {
                showProgressBar(it.loading.isLoading)
                it.error?.getContentIfNotHandled()?.let { stateError ->
                    //check if internet connection is lost
                    stateChangeListener?.onErrorStateChange(stateError)
                    cancelSubscriptionClicked = false
                }
                it.data?.data?.getContentIfNotHandled()?.let { homeModel ->
                    sessionManager.setIsUserSubscribed(true)
                    (activity as MainActivity).activeSubscription.value = null
                    cancelSubscriptionClicked = false
                    (activity as MainActivity).navigateToHome()
                    activity?.displayToast(R.string.cancelled_successfully)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        getMySubscription()
    }

    override fun onRefresh() {
//        if (!isSwipLoading) {
        getMySubscription()
//        }
    }
}