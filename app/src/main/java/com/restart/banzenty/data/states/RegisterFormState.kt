package com.restart.banzenty.data.states

data class RegisterFormState(
    var userNameError: Int?,

    var userEmailError: Int?,

    var userPhoneError: Int?,

    var userCarPlatesError: Int?,

    var passwordError: Int?,

    var passwordConfirmationError: Int?,

    var isDataValid: Boolean
)
