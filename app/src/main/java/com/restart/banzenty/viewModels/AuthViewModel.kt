package com.restart.banzenty.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.restart.banzenty.R
import com.restart.banzenty.data.models.UserModel
import com.restart.banzenty.data.states.LoginFormState
import com.restart.banzenty.data.states.RegisterFormState
import com.restart.banzenty.data.states.ResetPasswordFormState
import com.restart.banzenty.data.states.VerifyFormState
import com.restart.banzenty.network.DataState
import com.restart.banzenty.repositories.AuthRepository
import com.restart.banzenty.utils.Validation.Companion.isValidEmail
import com.restart.banzenty.utils.Validation.Companion.isValidPassword
import com.rilixtech.widget.countrycodepicker.CountryCodePicker
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AuthViewModel
@Inject constructor(private val authRepository: AuthRepository) : ViewModel() {

    /* Registration */
    val registerFormState = MutableLiveData<RegisterFormState>()

    fun register(
        fullName: String, email: String, phone: String,
        carPlateDigits: String,
        carPlateChars: String,
        password: String, confirmPassword: String,
        socialId: String?,
        socialType: String?,
        image: String?
    ): LiveData<DataState<UserModel>> {
        return authRepository.register(
            fullName, email, phone, carPlateDigits, carPlateChars, password,
            confirmPassword, socialId, socialType, image
        )
    }

    fun registerDataChanged(
        fullName: String?,
        email: String?,
        phone: CountryCodePicker,
        carPlatedigits: String?,
        carPlateChars: String?,
        password: String?,
        confirmPassword: String?,
        socialId: String?,
        socialType: String?,
        agreedToTerms: Boolean
    ) {
        if (fullName.isNullOrBlank()) {
            registerFormState.value = RegisterFormState(
                R.string.invalid_name,
                null, null, null, null,
                null, false
            )
        } else if (!isValidEmail(email)) {
            registerFormState.value = RegisterFormState(
                null, R.string.invalid_email,
                null, null, null,
                null, false
            )
        } else if (!phone.isValid) {
            registerFormState.value = RegisterFormState(
                null, null,
                R.string.invalid_phone, null, null,
                null, false
            )
        } else if (carPlatedigits.isNullOrBlank()) {
            registerFormState.value = RegisterFormState(
                null, null,
                null, R.string.invalid_car_plates, null,
                null, false
            )
        } else if (carPlateChars.isNullOrBlank()) {
            registerFormState.value = RegisterFormState(
                null, null,
                null, R.string.invalid_car_plates, null,
                null, false
            )
        } else if (socialId == null && !isValidPassword(password)) {
            registerFormState.value = RegisterFormState(
                null, null,
                null, null, R.string.invalid_password,
                null, false
            )
        } else if (socialId == null && !password.equals(confirmPassword)) {
            registerFormState.value = RegisterFormState(
                null, null,
                null, null, null,
                R.string.password_not_matched, false
            )
        } else if (!agreedToTerms) {
            registerFormState.value = RegisterFormState(
                null, null,
                null, null, null,
                null, false
            )
        } else {
            registerFormState.value = RegisterFormState(
                null, null,
                null, null, null, null,
                true
            )
        }
    }

    /* login */
    val loginFormState = MutableLiveData<LoginFormState>()

    fun login(phone: String?, password: String?, socialId: String?, socialType: String?):
            LiveData<DataState<UserModel.User>> {
        return authRepository.login(phone, password, socialId, socialType)
    }

    fun loginDataChanged(userPhone: String?, userPassword: String?) {
        if (userPhone.isNullOrBlank()) {
            loginFormState.value = LoginFormState(R.string.invalid_phone, null)
        } else if (!isValidPassword(userPassword)) {
            loginFormState.value = LoginFormState(null, R.string.invalid_password)
        } else {
            loginFormState.value = LoginFormState(true)
        }
    }

    /* Verify */
    val verifyFormState = MutableLiveData<VerifyFormState>()

    fun verifyCode(phone: String?, code: String?): LiveData<DataState<UserModel>> {
        return authRepository.verify(phone, code)
    }

    fun verifyCodeDataChanged(code: String?) {
        if (code.isNullOrBlank() || code.length < 4) {
            verifyFormState.value = VerifyFormState(R.string.invalid_code)
        } else {
            verifyFormState.value = VerifyFormState(true)
        }
    }

    fun resendCode(phone: String, type: String): LiveData<DataState<String>> {
        return authRepository.resendCode(phone, type)
    }

    fun resendCodeDataChanged(phone: String?) {
        if (phone.isNullOrBlank()) {
            registerFormState.value = RegisterFormState(
                null, null,
                R.string.invalid_phone, null, null,
                null, false
            )
        } else {
            registerFormState.value = RegisterFormState(
                null, null,
                null, null, null,
                null, true
            )
        }
    }

    /* reset password */
    fun requestPasswordReset(phone: String): LiveData<DataState<String>> {
        return authRepository.requestPasswordReset(phone)
    }

    fun requestPasswordResetDataChanged(phone: String?) {
        if (phone.isNullOrBlank()) {
            registerFormState.value = RegisterFormState(
                null, null,
                R.string.invalid_phone, null, null,
                null, false
            )
        } else {
            registerFormState.value = RegisterFormState(
                null, null,
                null, null, null,
                null, true
            )
        }
    }

    val resetPasswordFormState = MutableLiveData<ResetPasswordFormState>()

    fun resetPassword(token: String?, password: String):
            LiveData<DataState<String>> {
        return authRepository.resetPassword(token, password)
    }

    fun getTermsConditions(): LiveData<DataState<String>> {
        return authRepository.termsConditions()
    }

    fun resetPasswordDataChanged(
        oldPassword: String?,
        password: String?,
        passwordConfirmation: String?,
        forgetPassword: Boolean
    ) {
        if (!forgetPassword && !isValidPassword(oldPassword)) {
            resetPasswordFormState.value = ResetPasswordFormState(
                R.string.invalid_password,
                null,
                null
            )
        } else if (!isValidPassword(password)) {
            resetPasswordFormState.value = ResetPasswordFormState(
                null,
                R.string.invalid_password,
                null
            )
        } else if (passwordConfirmation.toString() != password.toString()) {
            resetPasswordFormState.value = ResetPasswordFormState(
                null,
                null,
                R.string.invalid_password
            )
        } else {
            resetPasswordFormState.setValue(ResetPasswordFormState(true))
        }
    }

    fun cancelActiveJobs() {
        authRepository.cancelActiveJobs()
    }

    override fun onCleared() {
        super.onCleared()
        authRepository.cancelActiveJobs()
    }
}