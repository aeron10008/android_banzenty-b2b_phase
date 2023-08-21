package com.restart.banzenty.ui.main

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.restart.banzenty.databinding.ActivityContactUsBinding
import com.restart.banzenty.viewModels.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ContactUsActivity : BaseActivity() {

    lateinit var binding: ActivityContactUsBinding
    val viewModel: HomeViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactUsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initToolbar()
        initViews()
        observeData()
    }

    private fun observeData() {
        viewModel.contactUsFormState.observe(this) { contactUsFormState ->
            if (contactUsFormState == null) {
                return@observe
            }
            binding.btnSend.isEnabled = contactUsFormState.isDataValid

            if (contactUsFormState.isDataValid) {
                binding.btnSend.background = ContextCompat.getDrawable(
                    this,
                    R.drawable.custom_button_red_orange_r14
                )
            } else {
                binding.btnSend.background = ContextCompat.getDrawable(
                    this,
                    R.drawable.custom_button_french_gray_r14
                )
            }

            if (contactUsFormState.nameError != null) {
                binding.etName.error = getString(contactUsFormState.nameError!!)
            }
            if (contactUsFormState.emailError != null) {
                binding.etEmail.error = getString(contactUsFormState.emailError!!)
            }
            if (contactUsFormState.messageError != null) {
                binding.etMessage.error = getString(contactUsFormState.messageError!!)
            }
        }
    }


    private fun initToolbar() {
        binding.toolbar.textViewToolbarTitle.text = getString(R.string.contact_us)
        binding.toolbar.root.setNavigationIcon(R.drawable.back_ic)
        binding.toolbar.root.setNavigationOnClickListener { finish() }
    }

    private fun initViews() {
        binding.btnSend.setOnClickListener { startContactUsProcess() }
        binding.etName.setText(sessionManager.getUserName())
        binding.etEmail.setText(sessionManager.getUserEmail())
        val afterTextChangedListener: TextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // ignore
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // ignore
            }

            override fun afterTextChanged(s: Editable) {
                viewModel.contactUsDataChanged(
                    binding.etName.text.toString(),
                    binding.etEmail.text.toString(),
                    binding.etMessage.text.toString()
                )
            }
        }

        binding.etName.addTextChangedListener(afterTextChangedListener)
        binding.etEmail.addTextChangedListener(afterTextChangedListener)
        binding.etMessage.addTextChangedListener(afterTextChangedListener)
    }

    private fun startContactUsProcess() {
        viewModel.contactUs(
            binding.etName.text.toString().trim(),
            binding.etEmail.text.toString().trim(),
            binding.etMessage.text.toString().trim()
        ).observe(this) {
            if (it != null) {
                it.error?.getContentIfNotHandled()?.let { errorState ->
                    onErrorStateChange(errorState)
                }
                showProgressBar(it.loading.isLoading)
                it.data?.data?.getContentIfNotHandled()?.let { userModel ->
                    showCustomDialog(
                        getString(R.string.message_received),
                        getString(R.string.your_message_very_imp)
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

            finish()
        }
    }

    override fun showProgressBar(show: Boolean) {
        if (show) {
            binding.progressBar.visibility = View.VISIBLE
            binding.btnSend.visibility = View.GONE
        } else {
            binding.btnSend.visibility = View.VISIBLE
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