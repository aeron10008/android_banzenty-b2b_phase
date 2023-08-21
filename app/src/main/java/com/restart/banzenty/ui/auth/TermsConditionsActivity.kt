package com.restart.banzenty.ui.auth

import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import com.restart.banzenty.R
import com.restart.banzenty.base.BaseActivity
import com.restart.banzenty.databinding.ActivityTermsConditionsBinding
import com.restart.banzenty.utils.Constants
import com.restart.banzenty.viewModels.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class TermsConditionsActivity : BaseActivity() {
    val TAG = "TermsConditionsActivity"

    private lateinit var binding: ActivityTermsConditionsBinding

    val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTermsConditionsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initToolbar()
        initViews()
        getTermsConditions()
    }


    private fun initToolbar() {
        binding.toolbar.root.setNavigationIcon(R.drawable.back_ic)
        binding.toolbar.root.setNavigationOnClickListener { finish() }
        binding.toolbar.textViewToolbarTitle.text = getString(R.string.terms_conditions)
    }

    private fun initViews() {
        binding.tvTerms.movementMethod = ScrollingMovementMethod()
    }

    private fun getTermsConditions() {
        viewModel.getTermsConditions().observe(this) {
            if (it != null) {
                it.error?.getContentIfNotHandled()?.let { errorState ->
                    //check if internet connection is lost
                    if (errorState.response.message
                        == Constants.UNABLE_TODO_OPERATION_WO_INTERNET
                        || errorState.response.message == Constants.UNABLE_TO_RESOLVE_HOST)
                        showErrorUI(
                            show = true,
                            title = getString(R.string.no_internet),
                            desc = null,
                            image = R.drawable.ic_no_internet,
                            showButton = true
                        )
                    else
                        showErrorUI(
                            show = true,
                            title = getString(R.string.something_went_wrong),
                            desc = null,
                            image = R.drawable.ic_something_wrong,
                            showButton = true
                        )
                }

                showProgressBar(it.loading.isLoading)

                it.data?.data?.getContentIfNotHandled()?.let { terms ->
                    if (terms.isNotEmpty()) {
                        showErrorUI(false)

                        binding.tvTerms.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            Html.fromHtml(terms, Html.FROM_HTML_MODE_COMPACT)
                        }
                        else {
                            Html.fromHtml(terms)
                        }
                        Log.i(TAG, "getTermsConditions: " + Html.fromHtml(terms, Html.FROM_HTML_MODE_COMPACT))
                    }
                    else {
                        showErrorUI(
                            show = true, title = getString(R.string.no_data), desc = null,
                            image = R.drawable.ic_no_data, showButton = false
                        )
                    }
                }
            }
        }
    }

    override fun showProgressBar(show: Boolean) {
        if (show) {
            binding.shimmer.visibility = View.VISIBLE
            binding.tvTerms.visibility = View.GONE
        } else {
            binding.tvTerms.visibility = View.VISIBLE
            binding.shimmer.visibility = View.GONE
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
            binding.tvTerms.visibility = View.GONE
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
                    getTermsConditions()
                }
            } else binding.errorLayout.btnRetry.visibility = View.GONE

        } else {
            binding.tvTerms.visibility = View.VISIBLE
            binding.errorLayout.root.visibility = View.GONE
        }
    }
}