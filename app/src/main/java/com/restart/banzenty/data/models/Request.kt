package com.restart.banzenty.data.models

import java.io.Serializable

data class Request(
    val id : String,
    val requestType: String,
    val station: String,
    val date: String,
    val time: String,
    val amount: String,
    val total: String
) :Serializable
