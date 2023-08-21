package com.restart.banzenty.data.states

class LoginFormState {

    var userPhoneError: Int?
    private set

    var userPasswordError: Int?
    private set

    var isDataValid: Boolean
    private set

    constructor(userPhoneError: Int?, userPasswordError: Int?) {
        this.userPhoneError = userPhoneError
        this.userPasswordError = userPasswordError
        isDataValid = false
    }

    constructor(isDataValid: Boolean) {
        userPhoneError = null
        userPasswordError = null
        this.isDataValid = isDataValid
    }
}