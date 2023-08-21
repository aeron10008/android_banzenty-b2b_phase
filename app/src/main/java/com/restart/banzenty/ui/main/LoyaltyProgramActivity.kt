package com.restart.banzenty.ui.main

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import com.restart.banzenty.R
import com.restart.banzenty.adapters.RewardsRecyclerAdapter
import com.restart.banzenty.base.BaseActivity
import com.restart.banzenty.data.models.RewardsModel
import com.restart.banzenty.databinding.ActivityLoyalityProgramBinding
import com.restart.banzenty.utils.Constants.Companion.UNABLE_TODO_OPERATION_WO_INTERNET
import com.restart.banzenty.utils.Constants.Companion.UNABLE_TO_RESOLVE_HOST
import com.restart.banzenty.viewModels.RewardsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoyaltyProgramActivity : BaseActivity() {

    private val TAG = "LoyaltyProgramActivity"

    lateinit var binding: ActivityLoyalityProgramBinding
    private val viewModel: RewardsViewModel by viewModels()
    var rewardList: ArrayList<RewardsModel.Reward> = ArrayList()
    private var rewardsAdapter: RewardsRecyclerAdapter? = null
    var theUserPoints: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoyalityProgramBinding.inflate(layoutInflater)
        setContentView(binding.root)

        toolbarSetup()
        Log.i(TAG, "onCreate: points = $theUserPoints")
        setDetails()
    }

    private fun toolbarSetup() {
        binding.toolbar.textViewToolbarTitle.text = getString(R.string.loyality_program)

        binding.toolbar.root.setNavigationIcon(R.drawable.back_ic)
        binding.toolbar.root.setNavigationOnClickListener { finish() }
    }

    private fun setDetails() {
        Log.i(TAG, "setDetails:  points = $theUserPoints")
        binding.textViewNoOfPoints.text = getString(R.string.no_of_points, theUserPoints)
        binding.rvRewards.apply {
            rewardsAdapter = RewardsRecyclerAdapter(requestManager, this@LoyaltyProgramActivity)

            adapter = rewardsAdapter
        }
    }

    private fun getRewards() {
        viewModel.getRewards().observe(this) {
            if (it != null) {
                it.error?.getContentIfNotHandled()?.let { stateError ->
                    //check if internet connection is lost
                    if (rewardList.isEmpty()) {
                        if (stateError.response.message == UNABLE_TODO_OPERATION_WO_INTERNET
                            || stateError.response.message == UNABLE_TO_RESOLVE_HOST
                        ) {
                            showErrorUI(
                                show = true,
                                title = getString(R.string.no_internet),
                                desc = null,
                                image = R.drawable.ic_no_internet,
                                showButton = true
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
                        onErrorStateChange(stateError)
                    }
                }

                it.loading.let { isLoading ->
                    showProgressBar(isLoading.isLoading)
                }

                it.data?.data?.getContentIfNotHandled()?.let { data ->
                    theUserPoints = data.userPoints

                    requestManager
                        .load(data.qrCode)
                        .fitCenter()
                        .into(binding.imageViewQr)

                    binding.textViewNoOfPoints.text =
                        getString(R.string.no_of_points, theUserPoints)

                    if (data.rewards.isNotEmpty()) {
                        rewardList.clear()

                        rewardList.addAll(data.rewards)

                        rewardsAdapter?.apply {
                            preloadGlideImages(
                                requestManager,
                                rewardList.toList()
                            )
                        }
                        rewardsAdapter?.submitList(rewardList.toList())

                        if (rewardList.isEmpty()) {
                            //show error UI
                            showErrorUI(
                                show = true,
                                title = getString(R.string.no_data),
                                desc = null,
                                image = R.drawable.ic_no_data,
                                showButton = false
                            )
                        } else {
                            //hide error UI
                            showErrorUI(false)
                        }
                    } else {
                        rewardList.clear()
                        rewardsAdapter?.submitList(rewardList.toList())

                        if (rewardList.isEmpty()) {
                            //show error UI
                            showErrorUI(
                                show = true,
                                title = getString(R.string.no_data),
                                desc = null,
                                image = R.drawable.ic_no_data,
                                showButton = false
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

    override fun onResume() {
        super.onResume()
        getRewards()
    }

    override fun showProgressBar(show: Boolean) {
        if (show) {
            showErrorUI(false)
            if ((rewardsAdapter?.itemCount ?: 0) == 0) {
                binding.shimmer.visibility = View.VISIBLE
                binding.parent1.visibility = View.GONE
                binding.cardViewPoints.visibility = View.GONE
                binding.textViewRedeemPointsTitle.visibility = View.GONE
                binding.rvRewards.visibility = View.GONE
            }
        } else {
            binding.shimmer.visibility = View.GONE
            binding.parent1.visibility = View.VISIBLE
            binding.cardViewPoints.visibility = View.VISIBLE
            binding.textViewRedeemPointsTitle.visibility = View.VISIBLE
            binding.rvRewards.visibility = View.VISIBLE
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
                binding.errorLayout.btnRetry.setOnClickListener { getRewards() }
            } else binding.errorLayout.btnRetry.visibility = View.GONE
            binding.rvRewards.visibility = View.GONE
        } else {
            binding.errorLayout.root.visibility = View.GONE
            binding.rvRewards.visibility = View.VISIBLE
        }
    }
}