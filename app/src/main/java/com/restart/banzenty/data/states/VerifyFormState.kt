package com.restart.banzenty.data.states

class VerifyFormState {
    var codeError: Int?
        private set

    var isDataValid: Boolean
        private set

    constructor(codeError: Int?) {
        this.codeError = codeError
        isDataValid = false
    }

    constructor(isDataValid: Boolean) {
        codeError = null
        this.isDataValid = isDataValid
    }
}