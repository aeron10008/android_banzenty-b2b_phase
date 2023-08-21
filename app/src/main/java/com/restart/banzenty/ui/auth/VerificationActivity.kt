package com.restart.banzenty.ui.auth

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.restart.banzenty.utils.displayToast
import com.restart.banzenty.R
import com.restart.banzenty.base.BaseActivity
import com.restart.banzenty.databinding.ActivityVerificationBinding
import com.restart.banzenty.ui.main.ChangePasswordActivity
import com.restart.banzenty.ui.main.MainActivity
import com.restart.banzenty.viewModels.AuthViewModel
import com.mukesh.OnOtpCompletionListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VerificationActivity : BaseActivity(), View.OnClickListener, OnOtpCompletionListener {
    val TAG = "VerificationActivityLog"

    lateinit var binding: ActivityVerificationBinding

    val viewModel: AuthViewModel by viewModels()
    var resendCode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        toolbarSetup()
        initViews()
        observeData()
    }

    private fun toolbarSetup() {
        binding.toolbar.textViewToolbarTitle.text = getString(R.string.verify_phone_num)
        binding.toolbar.root.setNavigationIcon(R.drawable.back_ic)
        binding.toolbar.root.setNavigationOnClickListener { finish() }
    }

    private fun initViews() {
        binding.otpView.setOtpCompletionListener(this)
        val afterTextChangedListener: TextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // ignore
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // ignore
            }

            override fun afterTextChanged(s: Editable) {
                Log.d(TAG, "afterTextChanged: ${s.toString()}")
                viewModel.verifyCodeDataChanged(binding.otpView.text.toString())
            }
        }
        binding.otpView.addTextChangedListener(afterTextChangedListener)
        binding.buttonVerify.setOnClickListener(this)
        startTimer()
    }

    private fun observeData() {
        viewModel.verifyFormState.observe(this) { verifyFormState ->
            if (verifyFormState == null) {
                return@observe
            }
            binding.buttonVerify.isEnabled = verifyFormState.isDataValid

            if (verifyFormState.isDataValid) {
                binding.buttonVerify.background = ContextCompat.getDrawable(
                    this,
                    R.drawable.custom_button_red_orange_r14
                )
            } else {
                binding.buttonVerify.background = ContextCompat.getDrawable(
                    this,
                    R.drawable.custom_button_french_gray_r14
                )
            }

            if (verifyFormState.codeError != null) {
                binding.otpView.error = getString(verifyFormState.codeError!!)
            }
        }

    }

    private fun verifyCode() {
        resendCode = false
        viewModel.verifyCode(
            intent.getStringExtra("phone_number"),
            binding.otpView.text.toString().trim()
        ).observe(this) {
            if (it != null) {
                it.error?.getContentIfNotHandled()?.let { stateError ->
                    onErrorStateChange(stateError)
                }
                showProgressBar(it.loading.isLoading)
                it.data?.data?.getContentIfNotHandled()?.let { verified ->
                    /*
                    * Forget Password --> Open changePassword and pass forgot_password to hide oldPassword field
                    * Finish Activity if it's coming from updating profile
                    * Open Home if it's coming from Login or SignUp
                    * */
                    if (intent.hasExtra("is_forget_password")) {
                        //Case 1
                        val intent =
                            Intent(this@VerificationActivity, ChangePasswordActivity::class.java)
                        intent.putExtra("is_forget_password", true)
                        intent.putExtra("token", verified.resetToken)
                        startActivity(intent)
                        finish()
                        //Case 2

                    } else if (intent.hasExtra("change_phone")) {
                        // return and finish with success code
                        sessionManager.saveUser(
                            verified.user,
                            verified.token
                        )
                        val returnIntent = Intent()
                        setResult(RESULT_OK, returnIntent)
                        finish()
                    } else {
                        sessionManager.saveUser(
                            verified.user,
                            verified.token
                        )
                        //Case 3
                        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                        val viewGroup = findViewById<ViewGroup>(android.R.id.content)
                        val dialogView: View = LayoutInflater.from(this@VerificationActivity)
                            .inflate(R.layout.custom_dialog_view, viewGroup, false)
                        builder.setView(dialogView)
                        val alertDialog: AlertDialog = builder.create()
                        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                        alertDialog.show()

                        val doneButton = dialogView.findViewById<Button>(R.id.buttonDone)
                        doneButton.setOnClickListener {
                            alertDialog.dismiss()
                            val intent = Intent(this, MainActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            startActivity(intent)
                        }

                    }
                }
            }
        }
    }

    private fun resendCode() {
        resendCode = true
        startTimer()
        viewModel.resendCode(
            intent.getStringExtra("phone_number")!!,
            if (intent.hasExtra("is_forget_password")) "password" else "phone"
        ).observe(this) {
            if (it != null) {
                it.error?.getContentIfNotHandled()?.let { stateError ->
                    onErrorStateChange(stateError)
                }
                showProgressBar(it.loading.isLoading)
                it.data?.data?.getContentIfNotHandled()?.let { phone ->
                    displayToast(R.string.code_sent)
                }
            }
        }
    }

    private fun startTimer() {

        binding.buttonVerify.background = ContextCompat.getDrawable(
            this,
            R.drawable.custom_button_french_gray_r14
        )
        binding.textViewResendingOtp.setTextColor(
            ContextCompat.getColor(
                this,
                R.color.silver_chalice
            )
        )
        binding.textViewResendingOtp.isClickable = false
        binding.textViewResendingOtp.isEnabled = false

        object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                var diff = millisUntilFinished
                val secondsInMilli: Long = 1000
                val minutesInMilli = secondsInMilli * 60
                val elapsedMinutes = diff / minutesInMilli
                diff %= minutesInMilli
                val elapsedSeconds = diff / secondsInMilli
                binding.textViewResendingOtp.text =
                    "${getString(R.string.resending_otp)} $elapsedMinutes:$elapsedSeconds"
            }

            override fun onFinish() {
                binding.textViewResendingOtp.text = getString(R.string.resending_otp)
                binding.textViewResendingOtp.setTextColor(
                    ContextCompat.getColor(
                        this@VerificationActivity,
                        R.color.red_orange
                    )
                )
                binding.textViewResendingOtp.isClickable = true
                binding.textViewResendingOtp.isEnabled = true
                binding.textViewResendingOtp.setOnClickListener(this@VerificationActivity)
            }
        }.start()
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.textViewResendingOtp -> resendCode()
            binding.buttonVerify -> verifyCode()
        }
    }

    override fun onOtpCompleted(otp: String) {
        viewModel.verifyCodeDataChanged(otp)
        verifyCode()
    }

    override fun showProgressBar(show: Boolean) {
        if (show) {
            binding.progressBar.visibility = View.VISIBLE
            if (resendCode)
                binding.textViewResendingOtp.visibility = View.GONE
            else binding.buttonVerify.visibility = View.GONE
        } else {
            binding.buttonVerify.visibility = View.VISIBLE
            binding.textViewResendingOtp.visibility = View.VISIBLE
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
        TODO("Not yet implemented")
    }

    override fun onBackPressed() {
        super.onBackPressed()
        viewModel.cancelActiveJobs()
    }
}