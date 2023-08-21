package com.restart.banzenty.data.models

import android.os.Parcelable
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChargeModel(

    @SerializedName("orderAmount")
    @Expose
    val orderAmount: String?,

    @SerializedName("merchantRefNumber")
    @Expose
    val merchantRefNumber: String?

) : Parcelable
