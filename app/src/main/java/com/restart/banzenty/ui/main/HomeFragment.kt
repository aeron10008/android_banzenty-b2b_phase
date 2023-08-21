package com.restart.banzenty.ui.main

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.restart.banzenty.R
import com.restart.banzenty.adapters.AdsAdapter
import com.restart.banzenty.base.BaseFragment
import com.restart.banzenty.data.models.HomeModel
import com.restart.banzenty.databinding.FragmentHomeBinding
import com.restart.banzenty.ui.station.StationFinderActivity
import com.restart.banzenty.utils.Constants
import com.restart.banzenty.utils.DataStateChangeListener
import com.restart.banzenty.utils.MainUtils
import com.restart.banzenty.viewModels.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : BaseFragment(), View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {
    private val TAG = "HomeFragment"
    private lateinit var binding: FragmentHomeBinding
    private val viewModel: HomeViewModel by viewModels()
    private var stateChangeListener: DataStateChangeListener? = null

    private var homeDataDetails: HomeModel? = null
    private lateinit var adsAdapter: AdsAdapter
    private var isSwipLoading = false

    private val openSubscriptionIntent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            run {
                if (it.resultCode == RESULT_OK) {
                    //open subscription fragment
                    (activity as MainActivity).navigateToMySubscription()
//                    getHomeDetails()
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
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initViews()
        if (!viewModel.isHomeDataLoaded)
            getHomeDetails()
        else setHomeUI()

        subscribeObservers()
    }

    private fun subscribeObservers() {
        (activity as MainActivity).activeSubscription.observe(viewLifecycleOwner) { subscription ->
            homeDataDetails?.user?.activeSubscription = subscription
            if (viewModel.isHomeDataLoaded) setHomeUI()
        }
    }


    private fun getHomeDetails() {
        viewModel.getHomeDetails().observe(viewLifecycleOwner) {
            if (it != null) {
                showProgressBar(it.loading.isLoading)
                it.error?.getContentIfNotHandled()?.let { stateError ->
                    //check if internet connection is lost
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
                    viewModel.isHomeDataLoaded = true
                    if (homeModel.user != null) sessionManager.saveUser(homeModel.user, null)
                    this.homeDataDetails = homeModel
                    setHomeUI()
                    showErrorUI(false)
                    (activity as MainActivity).populateData()
                    (activity as MainActivity).activeSubscription.value =
                        homeModel.user?.activeSubscription
                }
            }
        }
    }

    private fun setHomeUI() {
        Log.d(TAG, "setHomeUI: ")
        if (homeDataDetails?.user?.activeSubscription == null) {
            if (sessionManager.getUserName().isEmpty())
                binding.tvWelcomeUser.text =
                    getString(R.string.hi_user, getString(R.string.guest))
            else binding.tvWelcomeUser.text =
                getString(R.string.hi_user, sessionManager.getUserName())
            binding.tvRemainingCash.visibility = View.GONE
            binding.progressBarCash.visibility = View.GONE
        } else {
            sessionManager.setRemainingLiters(
                (homeDataDetails?.user?.activeSubscription?.remainingLiters ?: 0.0).toFloat()
            )
            sessionManager.setRemainingCash(
                (homeDataDetails?.user?.activeSubscription?.remainingCash ?: 0.0).toFloat()
            )
            binding.tvRemainingCash.text =
                resources.getString(R.string.remaining_cash, sessionManager.getRemainingCash())
            binding.tvWelcomeUser.text = getString(R.string.hi_user, sessionManager.getUserName())
            val progress: Int =
                (homeDataDetails?.user?.activeSubscription?.plan?.price ?: "0.0").toFloat().toInt()
            binding.progressBarCash.max = progress
            binding.progressBarCash.progress = sessionManager.getRemainingCash().toInt()
            binding.tvRemainingCash.visibility = View.VISIBLE
            binding.progressBarCash.visibility = View.VISIBLE
            /*
            Litres old Logic
     binding.tvNoLiters.text =
          resources.getString(R.string.remaining_liters, sessionManager.getRemainingLiters())
                 binding.progressBarFuel.max =
         (homeDataDetails?.user?.activeSubscription?.plan?.liters ?: 0).toInt()
     binding.progressBarFuel.progress = sessionManager.getRemainingLiters().toInt()
           */

        }
        requestManager
            .load(sessionManager.getUserImage())
            .transform(CenterCrop(), CircleCrop())
            .into(binding.ivProfile)

        if (homeDataDetails?.latestRequest == null) {
            binding.tvLatestRequestLabel.visibility = View.GONE
            binding.lastRequestLayout.root.visibility = View.GONE
        } else {
            binding.tvLatestRequestLabel.visibility = View.VISIBLE
            binding.lastRequestLayout.root.visibility = View.VISIBLE
            requestManager.load(homeDataDetails?.latestRequest?.serviceType?.image?.url)
                .into(binding.lastRequestLayout.ivService)
            binding.lastRequestLayout.textViewRequestPlanName.text =
                homeDataDetails?.latestRequest?.serviceType?.name
            binding.lastRequestLayout.textViewRequestId.text =
                homeDataDetails?.latestRequest?.externalNumber
            binding.lastRequestLayout.textViewStationName.text =
                homeDataDetails?.latestRequest?.station?.name
            binding.lastRequestLayout.textViewDateAndTime.text =
                MainUtils.getDateTime(homeDataDetails?.latestRequest?.createdAt)

            if ((homeDataDetails?.latestRequest?.amount ?: "0").toDouble() != 0.0) {
                binding.lastRequestLayout.rlAmount.visibility = View.VISIBLE
                binding.lastRequestLayout.textViewAmount.text =
                    getString(
                        R.string.f_litres,
                        (homeDataDetails?.latestRequest?.amount ?: "0.0").toDouble()
                    )
            } else {
                binding.lastRequestLayout.rlAmount.visibility = View.GONE
            }


            binding.lastRequestLayout.rlSubscriptionCredit.visibility = View.GONE

            binding.lastRequestLayout.textViewTotal.text =
                getString(R.string.egp, homeDataDetails?.latestRequest?.price ?: "0.0")

            if ((homeDataDetails?.latestRequest?.price ?: "0.0").toDouble() != 0.0) {
                binding.lastRequestLayout.tvPaymentType.setText(R.string.subscription)
            } else {
                binding.lastRequestLayout.tvPaymentType.setText(R.string.reward)
            }
        }


        /*         binding.lastRequestLayout.textViewTotal.text =
                getString(R.string.egp, homeDataDetails?.latestRequest?.price ?: "")

            if (homeDataDetails?.latestRequest?.fromSubscription != null) {
                binding.lastRequestLayout.tvSubscriptionCredit.text =
                    getString(R.string.egp, homeDataDetails?.latestRequest?.fromSubscription)
                binding.lastRequestLayout.rlSubscriptionCredit.visibility = View.VISIBLE
            } else binding.lastRequestLayout.rlSubscriptionCredit.visibility = View.GONE
            if (homeDataDetails?.latestRequest?.subscriptionName != null) {
                binding.lastRequestLayout.tvPaymentType.setText(R.string.subscription)
            } else {
                if ((homeDataDetails?.latestRequest?.price ?: "0.0").toDouble() != 0.0)
                    binding.lastRequestLayout.tvPaymentType.setText(R.string.cash)
                else
                    binding.lastRequestLayout.tvPaymentType.setText(R.string.reward)
            }*/


        if (homeDataDetails?.banners.isNullOrEmpty()) {
            binding.svAds.visibility = View.GONE
        } else {
            binding.svAds.visibility = View.VISIBLE
            adsAdapter.submitList(homeDataDetails?.banners!!)
            binding.svAds.setSliderAdapter(adsAdapter)
            binding.svAds.startAutoCycle()
        }

        (activity as MainActivity).setNotificationCount(
            homeDataDetails?.unReadNotificationCount ?: 0
        )
    }

    private fun initViews() {
        binding.swipeRefresh.setOnRefreshListener(this)
        binding.ivProfile.setOnClickListener(this)
        binding.tvStation.setOnClickListener(this)
        binding.tvSubscription.setOnClickListener(this)
        binding.tvOffer.setOnClickListener(this)
        adsAdapter = AdsAdapter(requestManager)
    }

    override fun showProgressBar(show: Boolean) {
        if (show) {
            showErrorUI(false)
            if (homeDataDetails == null) {
                binding.shimmer.visibility = View.VISIBLE
                binding.relativeLayoutUserInfo.visibility = View.GONE
                binding.svAds.visibility = View.GONE
                binding.tvServiceLabel.visibility = View.GONE
                binding.relativeLayoutServices.visibility = View.GONE
                binding.tvSubscription.visibility = View.GONE
                binding.tvLatestRequestLabel.visibility = View.GONE
                binding.lastRequestLayout.root.visibility = View.GONE
            }
        } else {
            binding.shimmer.visibility = View.GONE
            binding.relativeLayoutUserInfo.visibility = View.VISIBLE
            binding.svAds.visibility = View.VISIBLE
            binding.tvServiceLabel.visibility = View.VISIBLE
            binding.relativeLayoutServices.visibility = View.VISIBLE
            binding.tvSubscription.visibility = View.VISIBLE
            binding.tvLatestRequestLabel.visibility = View.VISIBLE
            binding.lastRequestLayout.root.visibility = View.VISIBLE
            binding.swipeRefresh.isRefreshing = false
        }
    }

    override fun showErrorUI(
        show: Boolean,
        image: Int?,
        title: String?,
        desc: String?,
        showButton: Boolean?
    ) {
        Log.d(TAG, "showErrorUI: $show")
        if (show) {
            binding.svAds.visibility = View.GONE
            binding.relativeLayoutUserInfo.visibility = View.GONE
            binding.tvServiceLabel.visibility = View.GONE
            binding.relativeLayoutServices.visibility = View.GONE
            binding.tvSubscription.visibility = View.GONE
            binding.tvLatestRequestLabel.visibility = View.GONE
            binding.lastRequestLayout.root.visibility = View.GONE


            binding.errorLayout.root.visibility = View.VISIBLE
            binding.errorLayout.tvDesc.visibility = View.GONE
            binding.errorLayout.tvTitle.text = title
            binding.errorLayout.ivPic.setBackgroundResource(image!!)
            desc?.let {
                binding.errorLayout.tvDesc.visibility = View.VISIBLE
                binding.errorLayout.tvDesc.text = it
            }
            binding.errorLayout.tvTitle.text = title
            if (showButton == true) {
                binding.errorLayout.btnRetry.visibility = View.VISIBLE
                binding.errorLayout.btnRetry.setOnClickListener { getHomeDetails() }
            } else binding.errorLayout.btnRetry.visibility = View.GONE

        } else {
            binding.errorLayout.root.visibility = View.GONE
        }
    }

    override fun onClick(v: View) {

        when (v) {
            binding.tvStation -> startActivity(Intent(activity, StationFinderActivity::class.java))

            binding.ivProfile -> (activity as MainActivity).navigateToProfile()

            binding.tvSubscription ->
                if (sessionManager.getToken().isEmpty())
                    (activity as MainActivity).showLoginDialog()
                else {
                    val subscriptionsIntent = Intent(
                        activity,
                        SubscriptionsActivity::class.java
                    )
                    subscriptionsIntent.putExtra(
                        "active_subscription_id",
                        homeDataDetails?.user?.activeSubscription?.plan?.id ?: -1
                    )

                    openSubscriptionIntent.launch(subscriptionsIntent)
//                    startActivity(subscriptionsIntent)
                }

            binding.tvOffer -> if (sessionManager.getToken().isEmpty())
                (activity as MainActivity).showLoginDialog()
            else startActivity(Intent(activity, LoyaltyProgramActivity::class.java))
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            stateChangeListener = context as DataStateChangeListener
        } catch (e: ClassCastException) {
        }
    }

    override fun onRefresh() {
        binding.nsvContainer.smoothScrollTo(0, 0, 300)
        if (!isSwipLoading) {
            getHomeDetails()
        }
    }

    override fun onResume() {
        super.onResume()
        if (sessionManager.isUserSubscribed()) {
            getHomeDetails()
            sessionManager.setIsUserSubscribed(false)
        }
    }
}