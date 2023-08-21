package com.restart.banzenty.data.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class TermsConditionModel(
    @SerializedName("terms-and-conditions")
    @Expose
    val termsConditions: String
)
