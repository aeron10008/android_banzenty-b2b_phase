package com.restart.banzenty.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.restart.banzenty.R
import com.restart.banzenty.base.BaseActivity
import com.restart.banzenty.databinding.ActivityRequestResetPasswordBinding
import com.restart.banzenty.utils.Validation
import com.restart.banzenty.viewModels.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RequestResetPasswordActivity : BaseActivity() {

    private val TAG = "ForgetPasswordActivity"

    lateinit var binding: ActivityRequestResetPasswordBinding

    val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRequestResetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        toolbarSetup()
        initViews()
        observeData()
    }

    private fun toolbarSetup() {
        binding.toolbar.textViewToolbarTitle.text = getString(R.string.forget_password)
        binding.toolbar.root.setNavigationIcon(R.drawable.back_ic)
        binding.toolbar.root.setNavigationOnClickListener { finish() }
    }

    private fun initViews() {
        binding.ccp.registerPhoneNumberTextView(binding.editTextPhoneNum)
        binding.buttonConfirm.setOnClickListener {
            hideSoftKeyboard()
            startRequestPasswordResetProcess()
        }

        val afterTextChangedListener: TextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // ignore
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // ignore
            }

            override fun afterTextChanged(s: Editable) {
                Log.d(TAG, "afterTextChanged")
                viewModel.requestPasswordResetDataChanged(
                    binding.ccp.fullNumberWithPlus.toString().trim()
                )
            }
        }

        binding.editTextPhoneNum.addTextChangedListener(afterTextChangedListener)
    }

    private fun observeData() {
        viewModel.registerFormState.observe(this) { loginFormState ->
            if (loginFormState == null) {
                return@observe
            }

            Log.d(TAG, "observeData: loginFormState")

            binding.buttonConfirm.isEnabled = loginFormState.isDataValid

            if (loginFormState.isDataValid) {
                binding.buttonConfirm.background = ContextCompat.getDrawable(
                    this, R.drawable.custom_button_red_orange_r14
                )
            } else {
                binding.buttonConfirm.background = ContextCompat.getDrawable(
                    this, R.drawable.custom_button_french_gray_r14
                )
            }

            if (loginFormState.userPhoneError != null) {
                binding.editTextPhoneNum.error = getString(loginFormState.userPhoneError!!)
            }
        }
    }

    private fun startRequestPasswordResetProcess() {
        Validation.validatePhoneEditText(binding.editTextPhoneNum)
        viewModel.requestPasswordReset(
            binding.ccp.fullNumberWithPlus.toString().trim()
        ).observe(this) {
            if (it != null) {
                it.error?.getContentIfNotHandled()?.let { stateError ->
                    onErrorStateChange(stateError)
                }

                it.loading.let { isLoading ->
                    showProgressBar(isLoading.isLoading)
                }

                it.data?.data?.getContentIfNotHandled()?.let { message ->
                    val intent =
                        Intent(this@RequestResetPasswordActivity, VerificationActivity::class.java)
                    intent.putExtra("is_forget_password", true)
                    intent.putExtra("phone_number", binding.ccp.fullNumberWithPlus)
                    startActivity(intent)
                }
            }
        }
    }

    override fun showProgressBar(show: Boolean) {
        if (show) {
            binding.progressBar.visibility = View.VISIBLE
            binding.buttonConfirm.visibility = View.GONE
        } else {
            binding.buttonConfirm.visibility = View.VISIBLE
            binding.progressBar.visibility = View.GONE
        }
    }

    override fun showErrorUI(
        show: Boolean,
        image: Int?,
        title: String?,
        desc: String?,
        showButton: Boolean?
    ) {
//        TODO("Not yet implemented")
    }
}