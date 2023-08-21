package com.restart.banzenty.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.restart.banzenty.R
import com.restart.banzenty.base.BaseActivity
import com.restart.banzenty.data.models.Image
import com.restart.banzenty.data.models.UserModel
import com.restart.banzenty.databinding.ActivityLoginBinding
import com.restart.banzenty.ui.main.MainActivity
import com.restart.banzenty.utils.Validation
import com.restart.banzenty.utils.displayToast
import com.restart.banzenty.viewModels.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.net.URL

@AndroidEntryPoint
class LoginActivity : BaseActivity(), View.OnClickListener {
    val TAG = "LoginActivity"
    private lateinit var binding: ActivityLoginBinding
    val viewModel: AuthViewModel by viewModels()
    private var callbackManager: CallbackManager? = null
    lateinit var mGoogleSignInClient: GoogleSignInClient
    private var socialUser: UserModel.User? = null
    var socialLogin = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        initializeGoogle()
        initFB()
        setContentView(binding.root)
        setListeners()
        initViews()
        observeData()
    }

    private fun initFB() {
        callbackManager = CallbackManager.Factory.create()
        binding.loginButton.setPermissions(listOf("public_profile,email"));
//        "/me?fields=id,name,email,picture.type(large)",
        binding.loginButton.registerCallback(
            callbackManager,
            object :
                FacebookCallback<LoginResult> {
                override fun onCancel() {
                    Log.d(TAG, "onCancel: ")
                }

                override fun onError(error: FacebookException) {
                    Log.d(TAG, "onError: ${error.message}")
                }

                override fun onSuccess(result: LoginResult) {
                    val request = GraphRequest(
                        AccessToken.getCurrentAccessToken(),
                        "/me?fields=id,name,email,picture.width(500).height(500)",
                        null,
                        HttpMethod.GET,
                        { response ->
                            Log.d(TAG, "onSuccess: $response")
                            socialUser = UserModel.User(
                                id = 0,
                                email = response.jsonObject?.getString("email") ?: "",
                                name = response.jsonObject?.getString("name") ?: "",
                                image = Image(
                                    url = response.jsonObject?.getJSONObject("picture")
                                        ?.getJSONObject("data")
                                        ?.getString("url"), null, null
                                ),
                                social_id = response.jsonObject?.getString("id") ?: "",
                                social_type = "fb",
                                password = null,
                                password_confirmation = null,
                                phone = null,
                                userCarPlate = null
                            )
                            LoginManager.getInstance().logOut()
                            startLoginProcess(
                                phone = null,
                                password = null,
                                socialId = response.jsonObject?.getString("id"),
                                socialType = "fb"
                            )

                        }
                    ).executeAsync()
                }

            })
    }

    private val googleResultIntent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            run {
                if (it.resultCode == RESULT_OK) {
                    val task: Task<GoogleSignInAccount> =
                        GoogleSignIn.getSignedInAccountFromIntent(it.data)
                    getGoogleAccountInfo(task)
                }
            }
        }


    private fun initializeGoogle() {
        val gso =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        mGoogleSignInClient.signOut()
    }

    private fun getGoogleAccountInfo(completedTask: Task<GoogleSignInAccount>) {
        val account: GoogleSignInAccount = completedTask.getResult(ApiException::class.java)!!
        var imageUrl = ""
        if (account.photoUrl != null) imageUrl = URL(account.photoUrl.toString()).toString()
        socialUser = UserModel.User(
            id = 0,
            email = account.email ?: "",
            name = account.displayName ?: "",
            image = Image(url = imageUrl, null, null),
            social_id = account.id ?: "",
            social_type = "google",
            password = null,
            password_confirmation = null,
            phone = null,
            userCarPlate = null
        )
//
//        var map = HashMap<String, String>()
//        map["google_id"] = account.id ?: ""
//        map["device_token"] = sessionManager.getDeviceToken() ?: ""
//        map["device_type"] = "android"
        startLoginProcess(
            phone = null, password = null, socialId = account.id, socialType = "google"
        )
        mGoogleSignInClient.signOut()
    }

    private fun initViews() {
        binding.loginCountryCodePicker.registerPhoneNumberTextView(binding.editTextPhoneNum)

        val afterTextChangedListener: TextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // ignore
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // ignore
            }

            override fun afterTextChanged(s: Editable) {
                viewModel.loginDataChanged(
                    binding.editTextPhoneNum.text.toString(),
                    binding.editTextPassword.text.toString()
                )
            }
        }

        binding.editTextPhoneNum.addTextChangedListener(afterTextChangedListener)
        binding.editTextPassword.addTextChangedListener(afterTextChangedListener)
        binding.editTextPassword.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                binding.buttonLogin.performClick()
            }
            true
        }
    }

    private fun setListeners() {
        binding.textViewRecoveryPassword.setOnClickListener(this)
        binding.buttonLogin.setOnClickListener(this)
        binding.textViewLoginAsGuest.setOnClickListener(this)
        binding.imageButtonGoogle.setOnClickListener(this)
        binding.imageButtonFacebook.setOnClickListener(this)
        binding.textViewRegisterNow.setOnClickListener(this)
    }

    private fun observeData() {
        viewModel.loginFormState.observe(this) { loginFormState ->
            if (loginFormState == null) {
                return@observe
            }

            Log.d(TAG, "observeData: loginFromState")

            binding.buttonLogin.isEnabled = loginFormState.isDataValid

            if (loginFormState.isDataValid) {
                binding.buttonLogin.background = ContextCompat.getDrawable(
                    this, R.drawable.custom_button_red_orange_r10
                )
            } else {
                binding.buttonLogin.background = ContextCompat.getDrawable(
                    this, R.drawable.custom_button_dove_gray_r10
                )
            }

            if (loginFormState.userPhoneError != null) {
                binding.editTextPhoneNum.error = getString(loginFormState.userPhoneError!!)
            } else {
                binding.editTextPhoneNum.error = null
            }

            if (loginFormState.userPasswordError != null) {
                binding.editTextPassword.error = getString(loginFormState.userPasswordError!!)
            } else {
                binding.editTextPassword.error = null
            }
        }
    }

    private fun startLoginProcess(
        phone: String?,
        password: String?,
        socialId: String?,
        socialType: String?
    ) {
        viewModel.login(
            phone,
            password,
            socialId,
            socialType
        ).observe(this) {
            if (it != null) {
                it.error?.getContentIfNotHandled()?.let { stateError ->
                    if (socialId != null && socialUser != null) {
                        val intent = Intent(this, RegisterActivity::class.java)
                        intent.putExtra("socialUser", socialUser)
                        startActivity(intent)
                    } else onErrorStateChange(stateError)
                }

                showProgressBar(it.loading.isLoading)

                it.data?.data?.getContentIfNotHandled()?.let { userDataModel ->
                    if (userDataModel.isVerified) {
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        intent.putExtra("installSplash", false)
                        startActivity(intent)
                        finish()
                    } else {
                        displayToast(R.string.verify_phone_num)
                        val intent = Intent(this@LoginActivity, VerificationActivity::class.java)
                        intent.putExtra("is_forget_password", false)
                        intent.putExtra("phone_number", userDataModel.phone)
                        startActivity(intent)
                    }

                }
            }
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.textViewRecoveryPassword -> {
                val intent = Intent(this, RequestResetPasswordActivity::class.java)
                startActivity(intent)
            }

            binding.buttonLogin -> {
                Validation.validatePhoneEditText(binding.editTextPhoneNum)
                socialLogin = false
                startLoginProcess(
                    binding.loginCountryCodePicker.fullNumberWithPlus.toString().trim(),
                    binding.editTextPassword.text.toString().trim(), null, null
                )
                hideSoftKeyboard()
            }

            binding.textViewLoginAsGuest -> {
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.putExtra("isGuest", true)
                startActivity(intent)
            }

            binding.imageButtonGoogle -> {
                socialLogin = true
                googleResultIntent.launch(mGoogleSignInClient.signInIntent)
            }


            binding.imageButtonFacebook -> {
                Log.d(TAG, "onClick: imageButtonFacebook")
                socialLogin = true
                binding.loginButton.performClick()
            }


            binding.textViewRegisterNow -> {
                val intent = Intent(this, RegisterActivity::class.java)
                startActivity(intent)
            }
        }
    }

    override fun showProgressBar(show: Boolean) {
        if (show) {
            binding.progressBar.visibility = View.VISIBLE
            binding.buttonLogin.visibility = View.GONE
        } else {
            binding.buttonLogin.visibility = View.VISIBLE
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