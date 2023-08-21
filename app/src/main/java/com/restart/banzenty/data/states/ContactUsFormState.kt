package com.restart.banzenty.data.states

class ContactUsFormState {

    var nameError: Int?
        private set

    var emailError: Int?
        private set

    var messageError: Int?
        private set

    var isDataValid: Boolean
        private set

    constructor(nameError: Int?, emailError: Int?, messageError: Int?) {
        this.nameError = nameError
        this.emailError = emailError
        this.messageError = messageError
        isDataValid = false
    }

    constructor(isDataValid: Boolean) {
        nameError = null
        emailError = null
        messageError = null
        this.isDataValid = isDataValid
    }
}