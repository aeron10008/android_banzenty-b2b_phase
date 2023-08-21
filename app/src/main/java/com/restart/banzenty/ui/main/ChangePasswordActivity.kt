package com.restart.banzenty.ui.main

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.restart.banzenty.R
import com.restart.banzenty.base.BaseActivity
import com.restart.banzenty.databinding.ActivityChangePasswordBinding
import com.restart.banzenty.ui.auth.LoginActivity
import com.restart.banzenty.viewModels.AuthViewModel
import com.restart.banzenty.viewModels.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChangePasswordActivity : BaseActivity() {
    private val TAG = "ChangePasswordActivity"
    lateinit var binding: ActivityChangePasswordBinding
    val viewModel: AuthViewModel by viewModels()
    val profileViewModel: ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initToolbar()
        initViews()
        observeData()
    }


    private fun initToolbar() {
        binding.toolbar.root.setNavigationIcon(R.drawable.back_ic)
        binding.toolbar.root.setNavigationOnClickListener { finish() }
    }

    private fun initViews() {
        if (intent.hasExtra("is_forget_password")) {
            binding.toolbar.textViewToolbarTitle.text = getString(R.string.password_reset)
            binding.currentPasswordLayout.visibility = View.GONE
        } else {
            binding.toolbar.textViewToolbarTitle.text = getString(R.string.change_password)
            binding.currentPasswordLayout.visibility = View.VISIBLE
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
                viewModel.resetPasswordDataChanged(
                    binding.etCurrentPassword.text.toString(),
                    binding.etNewPassword.text.toString(),
                    binding.etConfirmPassword.text.toString(),
                    intent.hasExtra("is_forget_password")
                )
            }
        }

        binding.buttonChangePassword.setOnClickListener {
            if (!intent.hasExtra("is_forget_password")) {
                startChangePassProcess()
            } else {
                startResetPassword()
            }
            hideSoftKeyboard()
        }


        if (!intent.hasExtra("if_forgot_password"))
            binding.etCurrentPassword.addTextChangedListener(
                afterTextChangedListener
            )
        binding.etNewPassword.addTextChangedListener(afterTextChangedListener)
        binding.etConfirmPassword.addTextChangedListener(afterTextChangedListener)
    }

    private fun observeData() {
        viewModel.resetPasswordFormState.observe(this) { changePassword ->
            if (changePassword == null) {
                return@observe
            }
            binding.buttonChangePassword.isEnabled = changePassword.isDataValid

            if (changePassword.isDataValid) {
                binding.buttonChangePassword.background = ContextCompat.getDrawable(
                    this,
                    R.drawable.custom_button_red_orange_r14
                )
            } else {
                binding.buttonChangePassword.background = ContextCompat.getDrawable(
                    this,
                    R.drawable.custom_button_french_gray_r14
                )
            }

            if (changePassword.oldPassword != null) {
                binding.etCurrentPassword.error = getString(changePassword.oldPassword!!)
            }
            if (changePassword.passwordError != null) {
                binding.etNewPassword.error = getString(changePassword.passwordError!!)
            }
            if (changePassword.passwordConfirmationError != null) {
                binding.etConfirmPassword.error =
                    getString(changePassword.passwordConfirmationError!!)
            }
        }
    }

    private fun startChangePassProcess() {
        profileViewModel.changePassword(
            binding.etCurrentPassword.text.toString().trim(),
            binding.etNewPassword.text.toString().trim()
        ).observe(this) {
            if (it != null) {
                it.error?.getContentIfNotHandled()?.let { errorState ->
                    onErrorStateChange(errorState)
                }
                showProgressBar(it.loading.isLoading)
                it.data?.data?.getContentIfNotHandled()?.let { userModel ->
                    showCustomDialog(
                        getString(R.string.password_changed),
                        getString(R.string.password_changed_msg)
                    )
                }
            }
        }
    }

    private fun startResetPassword() {
        viewModel.resetPassword(
            intent.getStringExtra("token"),
            binding.etNewPassword.text.toString().trim()
        ).observe(this) {
            if (it != null) {
                it.error?.getContentIfNotHandled()?.let { errorState ->
                    onErrorStateChange(errorState)
                }
                showProgressBar(it.loading.isLoading)
                it.data?.data?.getContentIfNotHandled()?.let { userModel ->
                    showCustomDialog(
                        getString(R.string.password_reset),
                        getString(R.string.password_reset_msg)
                    )
                }
            }
        }
    }


    private fun showCustomDialog(title: String, content: String) {
        //show password changed dialog
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)

        val viewGroup = this.findViewById<ViewGroup>(android.R.id.content)

        val dialogView: View = LayoutInflater.from(this)
            .inflate(R.layout.custom_dialog_view, viewGroup, false)

        val dialogTitle = dialogView.findViewById<TextView>(R.id.textViewDialogTitle)
        dialogTitle.text = title

        val dialogContent = dialogView.findViewById<TextView>(R.id.textViewDialogContent)
        dialogContent.text = content

        builder.setView(dialogView)

        val alertDialog: AlertDialog = builder.create()

        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        alertDialog.show()

        val doneButton = dialogView.findViewById<Button>(R.id.buttonDone)

        doneButton.setOnClickListener {
            alertDialog.dismiss()
            if (intent.hasExtra("is_forget_password")) {
                val intent = Intent(this, LoginActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                finish()
            } else {
                finish()
            }
        }
    }


    override fun showProgressBar(show: Boolean) {
        if (show) {
            binding.progressBar.visibility = View.VISIBLE
            binding.buttonChangePassword.visibility = View.GONE
        } else {
            binding.buttonChangePassword.visibility = View.VISIBLE
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
//       Not used here
    }
}