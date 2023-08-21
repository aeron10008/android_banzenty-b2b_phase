package com.restart.banzenty.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import com.restart.banzenty.R
import com.restart.banzenty.base.BaseActivity
import com.restart.banzenty.data.models.UserModel
import com.restart.banzenty.databinding.ActivityRegisterBinding
import com.restart.banzenty.utils.Validation
import com.restart.banzenty.viewModels.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterActivity : BaseActivity(), View.OnClickListener {

    private var socialUser: UserModel.User? = null
    val TAG = "RegisterActivity"

    private lateinit var binding: ActivityRegisterBinding

    val viewModel: AuthViewModel by viewModels()

    var agreedToTerms = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViews()
        observeData()
    }

    private fun initViews() {

        if (intent.hasExtra("socialUser")) {
            socialUser = intent.getParcelableExtra("socialUser")
            binding.editTextEmail.setText(socialUser?.email ?: "")
            binding.editTextFullName.setText(socialUser?.name ?: "")
            binding.editTextPassword.setText("************")
            binding.editTextConfirmPassword.setText("************")
            binding.textViewPassword.visibility = View.GONE
            binding.relativeLayoutPassword.visibility = View.GONE
            binding.textViewConfirmPassword.visibility = View.GONE
            binding.relativeLayoutConfirmPassword.visibility = View.GONE
            binding.editTextPassword.isEnabled = false
            binding.editTextConfirmPassword.isEnabled = false

        }

        binding.editTextCarNumbers.doOnTextChanged { text, _, _, _ ->
            if (text!!.isNotEmpty()) {
                val lastChar: Char? = text?.get(text.length - 1)
                if (lastChar == '0' ||
                    lastChar == '1' ||
                    lastChar == '2' ||
                    lastChar == '3' ||
                    lastChar == '4' ||
                    lastChar == '5' ||
                    lastChar == '6' ||
                    lastChar == '7' ||
                    lastChar == '8' ||
                    lastChar == '9'
                ) {
                    val changeChar = when (lastChar) {
                        '1' -> {
                            '۱'
                        }
                        '2' -> {
                            '۲'
                        }
                        '3' -> {
                            '۳'
                        }
                        '4' -> {
                            '٤'
                        }
                        '5' -> {
                            '٥'
                        }
                        '6' -> {
                            '٦'
                        }
                        '7' -> {
                            '٧'
                        }
                        '8' -> {
                            '۸'
                        }
                        '9' -> {
                            '۹'
                        }
                        else -> {
                            '۰'
                        }
                    }
                    val builder = java.lang.StringBuilder()
                    text.forEachIndexed { i, c ->
                        if (i != text.length - 1) {
                            builder.append(c)
                        }
                    }
                    builder.append(changeChar)
                    val result = builder.toString()
                    binding.editTextCarNumbers.setText(result)
                    binding.editTextCarNumbers.setSelection(result.length)
                }
            }

        }

        binding.registerCountryCodePicker.registerPhoneNumberTextView(binding.editTextPhoneNum)

//        binding.otpCarAlphabets.setOtpCompletionListener(this)

        val afterTextChangedListener: TextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // ignore
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // ignore
            }

            override fun afterTextChanged(s: Editable) {
                //car_plate_digits, car_plate_chars
                Log.d(TAG, "afterTextChanged")
                viewModel.registerDataChanged(
                    binding.editTextFullName.text.toString(),
                    binding.editTextEmail.text.toString(),
                    binding.registerCountryCodePicker,
                    binding.editTextCarNumbers.text.toString(),
//                    binding.otpCarAlphabets.text.toString(),
                    "${binding.etAlp1.text.toString().trim()}${
                        binding.etAlp2.text.toString().trim()
                    }${binding.etAlp3.text.toString().trim()}",
                    binding.editTextPassword.text.toString(),
                    binding.editTextConfirmPassword.text.toString(),
                    socialUser?.social_id, socialUser?.social_type,
                    agreedToTerms
                )

                /*  if (binding.etAlp1.isFocusable && binding.etAlp1.text.toString().isNotEmpty())
                      binding.etAlp2.requestFocus()
                  else if (binding.etAlp2.isFocusable && binding.etAlp2.text.toString().isNotEmpty())
                      binding.etAlp3.requestFocus()*/
            }
        }

        binding.editTextFullName.addTextChangedListener(afterTextChangedListener)
        binding.editTextEmail.addTextChangedListener(afterTextChangedListener)
        binding.editTextPhoneNum.addTextChangedListener(afterTextChangedListener)
        binding.editTextPassword.addTextChangedListener(afterTextChangedListener)
        binding.editTextConfirmPassword.addTextChangedListener(afterTextChangedListener)

        binding.radioButtonTermsAndConditions.setOnClickListener(this)
        binding.textViewAgreeToTerms.setOnClickListener(this)
        binding.buttonRegister.setOnClickListener(this)

        binding.etAlp1.doOnTextChanged { text, _, _, _ ->
            if (text?.length == 1) {
                if (binding.etAlp2.text.isEmpty()) {
                    binding.etAlp2.requestFocus()
                }

            }
        }
        binding.etAlp2.doOnTextChanged { text, _, _, _ ->
            if (text?.length == 1) {
                if (binding.etAlp3.text.isEmpty()) {
                    binding.etAlp3.requestFocus()
                }
            }
        }
    }

    private fun observeData() {
        viewModel.registerFormState.observe(this) { registerFromState ->
            if (registerFromState == null) {
                return@observe
            }

            binding.buttonRegister.isEnabled = registerFromState.isDataValid

            if (registerFromState.isDataValid) {
                binding.buttonRegister.background = ContextCompat
                    .getDrawable(this, R.drawable.custom_button_red_orange_r10)
            } else {
                binding.buttonRegister.background = ContextCompat
                    .getDrawable(this, R.drawable.custom_button_dove_gray_r10)
            }

            if (registerFromState.userNameError != null) {
                binding.editTextFullName.error = getString(registerFromState.userNameError!!)
                return@observe
            }

            if (registerFromState.userEmailError != null) {
                binding.editTextEmail.error = getString(registerFromState.userEmailError!!)
                return@observe
            }

            if (registerFromState.userPhoneError != null) {
                binding.editTextPhoneNum.error = getString(registerFromState.userPhoneError!!)
                return@observe
            }

            if (registerFromState.userCarPlatesError != null) {
                binding.editTextCarNumbers.error = getString(registerFromState.userCarPlatesError!!)
                binding.etAlp1.error = getString(registerFromState.userCarPlatesError!!)
                return@observe
            }

            if (registerFromState.passwordError != null) {
                binding.editTextPassword.error = getString(registerFromState.passwordError!!)
                return@observe
            }

            if (registerFromState.passwordConfirmationError != null) {
                binding.editTextConfirmPassword.error =
                    getString(registerFromState.passwordConfirmationError!!)
                return@observe
            }
        }
    }

    private fun startRegistration() {
        Validation.validatePhoneEditText(binding.editTextPhoneNum)
        viewModel.register(
            binding.editTextFullName.text.toString().trim(),
            binding.editTextEmail.text.toString().trim(),
            binding.registerCountryCodePicker.fullNumberWithPlus.toString().trim(),
            binding.editTextCarNumbers.text.toString().trim(),
            "${binding.etAlp1.text.toString().trim()}${
                binding.etAlp2.text.toString().trim()
            }${binding.etAlp3.text.toString().trim()}",
            binding.editTextPassword.text.toString().trim(),
            binding.editTextConfirmPassword.text.toString().trim(),
            socialUser?.social_id,
            socialUser?.social_type,
            socialUser?.image?.url
        ).observe(this) {
            if (it != null) {
                it.error?.getContentIfNotHandled()?.let { errorState ->
                    onErrorStateChange(errorState)
                }
            }
            showProgressBar(it.loading.isLoading)

            it.data?.data?.getContentIfNotHandled()?.let { userDataModel ->
                if (agreedToTerms) {
//                        sharedPreferencesUtils.saveUser(userDataModel)
                    val intent = Intent(this@RegisterActivity, VerificationActivity::class.java)
//                        intent.putExtra("is_forget_password", false)
                    intent.putExtra(
                        "phone_number", binding.registerCountryCodePicker.fullNumberWithPlus
                    )
                    startActivity(intent)
                }
            }
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.radioButtonTermsAndConditions -> {
                agreedToTerms = binding.radioButtonTermsAndConditions.isChecked
                viewModel.registerDataChanged(
                    binding.editTextFullName.text.toString(),
                    binding.editTextEmail.text.toString(),
                    binding.registerCountryCodePicker,
                    binding.editTextCarNumbers.text.toString(),
                    "${binding.etAlp1.text.toString().trim()}${
                        binding.etAlp2.text.toString().trim()
                    }${binding.etAlp3.text.toString().trim()}",
                    binding.editTextPassword.text.toString(),
                    binding.editTextConfirmPassword.text.toString(),
                    socialUser?.social_id, socialUser?.social_type,
                    agreedToTerms
                )
            }

            binding.textViewAgreeToTerms -> {
                startActivity(Intent(this, TermsConditionsActivity::class.java))
            }

            binding.buttonRegister -> {
                startRegistration()
                hideSoftKeyboard()
            }
        }
    }


    override fun showProgressBar(show: Boolean) {
        if (show) {
            binding.progressBar.visibility = View.VISIBLE
            binding.buttonRegister.visibility = View.GONE
        } else {
            binding.buttonRegister.visibility = View.VISIBLE
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