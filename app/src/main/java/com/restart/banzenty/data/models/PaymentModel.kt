package com.restart.banzenty.data.models

import android.os.Parcelable
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class PaymentModel(

    @SerializedName("payments")
    @Expose
    val payments: List<Payment>
) : Parcelable {
    @Parcelize
    data class Payment(
        @SerializedName("id")
        @Expose
        val id: Int,

        @SerializedName("merchantRefNumber")
        @Expose
        val merchantRefNumber: String,

        @SerializedName("user_id")
        @Expose
        val user_id: Int,

        @SerializedName("created_at")
        @Expose
        val created_at: String,

        @SerializedName("paid_amount")
        @Expose
        val paid_amount: Float
    ) : Parcelable
}
