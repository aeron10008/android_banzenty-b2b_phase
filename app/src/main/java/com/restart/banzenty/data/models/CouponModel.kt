package com.restart.banzenty.data.models

import android.os.Parcelable
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class CouponModel(
    @SerializedName("coupon")
    @Expose
    val coupon: Coupon,

    @SerializedName("coupon_help_text")
    @Expose
    val couponHelpText : String
) : Parcelable {
    @Parcelize
    data class Coupon(
        @PrimaryKey
        @SerializedName("id")
        @Expose
        val id: Int,

        @SerializedName("code")
        @Expose
        val code: String,

        @SerializedName("used")
        @Expose
        val used: Boolean,

        @SerializedName("created_at")
        @Expose
        val createdAt: String,

        @SerializedName("reward")
        @Expose
        var reward: RewardsModel.Reward
    ) : Parcelable
}
