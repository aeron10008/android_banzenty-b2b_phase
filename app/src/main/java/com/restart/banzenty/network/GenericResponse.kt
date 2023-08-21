package com.restart.banzenty.network

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class GenericResponse<T>(
    @SerializedName("status_code")
    @Expose
    var status_code: Int,

    @SerializedName("message")
    @Expose
    var message: String,

    @SerializedName("data")
    @Expose
    var data: T?
)