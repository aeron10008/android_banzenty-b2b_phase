package com.restart.banzenty.data.states

class ResetPasswordFormState {
    var oldPassword: Int?
        private set
    var passwordError: Int?
        private set
    var passwordConfirmationError: Int?
        private set
    var isDataValid: Boolean
        private set

    constructor(oldPassword: Int?, passwordError: Int?, passwordConfirmationError: Int?) {
        this.oldPassword = oldPassword
        this.passwordError = passwordError
        this.passwordConfirmationError = passwordConfirmationError
        isDataValid = false
    }

    constructor(isDataValid: Boolean) {
        oldPassword = null
        passwordError = null
        passwordConfirmationError = null
        this.isDataValid = isDataValid
    }
}