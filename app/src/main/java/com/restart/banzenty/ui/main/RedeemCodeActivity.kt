package com.restart.banzenty.ui.main

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import com.restart.banzenty.R
import com.restart.banzenty.base.BaseActivity
import com.restart.banzenty.data.models.CouponModel
import com.restart.banzenty.databinding.ActivityRedeemCodeBinding
import com.restart.banzenty.utils.Constants
import com.restart.banzenty.viewModels.CouponViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RedeemCodeActivity : BaseActivity() {

    lateinit var binding: ActivityRedeemCodeBinding
    var rewardId: Int = 0

    private val viewModel: CouponViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRedeemCodeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initToolbar()
        init()
    }

    private fun initToolbar() {
        binding.toolbar.textViewToolbarTitle.text = getString(R.string.redeem_code)
        binding.toolbar.root.setNavigationIcon(R.drawable.back_ic)
        binding.toolbar.root.setNavigationOnClickListener { finish() }
    }

    private fun init() {
        if (intent.extras != null) {
            rewardId = intent.getIntExtra("reward_id", 0)
        }

        redeemCode(rewardId)
    }

    private fun setViewDetails(data: CouponModel) {
        requestManager.load(data.coupon.reward.image?.url).into(binding.ivRedeemPic)

        binding.tvServiceName.text = data.coupon.reward.name

        binding.tvCode.text = data.coupon.code

        binding.btnCopyIc.setOnClickListener {

            if (binding.tvCode.text.isNotEmpty() && binding.tvCode.text != null) {
                val myClipboard =
                    this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val myClip: ClipData = ClipData.newPlainText("code", binding.tvCode.text)

                myClipboard.setPrimaryClip(myClip)

                Toast.makeText(this, getString(R.string.copied_to_clipboard), Toast.LENGTH_LONG)
                    .show()
            }
        }

        binding.tvUsingCodeDescription.text = data.couponHelpText
    }

    private fun redeemCode(rewardId: Int) {
        viewModel.redeemCode(rewardId).observe(this) {
            if (it != null) {
                it.error?.getContentIfNotHandled()?.let { stateError ->
                    //check if internet connection is lost
                    if (stateError.response.message == Constants.UNABLE_TODO_OPERATION_WO_INTERNET
                        || stateError.response.message == Constants.UNABLE_TO_RESOLVE_HOST
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
                }

                showProgressBar(it.loading.isLoading)

                it.data?.data?.getContentIfNotHandled()?.let { data ->
                    viewModel.isCouponDataLoaded = true
                    showErrorUI(false)
                    setViewDetails(data)
                }
            }
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
            binding.svContent.visibility = View.GONE
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
                binding.errorLayout.btnRetry.setOnClickListener { redeemCode(rewardId) }
            } else binding.errorLayout.btnRetry.visibility = View.GONE

        } else {
            binding.svContent.visibility = View.VISIBLE
            binding.errorLayout.root.visibility = View.GONE
        }
    }

    override fun showProgressBar(show: Boolean) {
        if (show) {
            showErrorUI(false)
            binding.svContent.visibility = View.GONE
            if (binding.tvCode.text.isEmpty()) {
                binding.shimmer.visibility = View.VISIBLE
            }

        } else {
            binding.shimmer.visibility = View.GONE
        }
    }
}