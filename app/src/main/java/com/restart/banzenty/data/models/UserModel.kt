package com.restart.banzenty.data.models

import android.os.Parcelable
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserModel(

    @SerializedName("token")
    @Expose
    val token: String,

    @SerializedName("reset_token")
    @Expose
    val resetToken: String,

    @SerializedName("user")
    @Expose
    val user: User,

    var isVerified: Boolean = true
) : Parcelable {

    @Parcelize
    data class User(
        @SerializedName("id")
        @Expose
        val id: Int,

        @SerializedName("name")
        @Expose
        val name: String?,

        @SerializedName("phone")
        @Expose
        val phone: String?,

        @SerializedName("email")
        @Expose
        val email: String?,

        @SerializedName("password")
        @Expose
        val password: String?,

        @SerializedName("password_confirmation")
        @Expose
        val password_confirmation: String?,

        @SerializedName("social_id")
        @Expose
        val social_id: String?,

        @SerializedName("social_type")
        @Expose
        val social_type: String?,

        @SerializedName("image")
        @Expose
        val image: Image?,

        @SerializedName("car")
        @Expose
        val userCarPlate: UserCarPlate?,

        @SerializedName("active_subscription")
        @Expose
        var activeSubscription: ActiveSubscription? = null,

        @SerializedName("subscription")
        @Expose
        val renewedSubscription: ActiveSubscription? = null,

        var isVerified: Boolean = true
    ) : Parcelable


    @Parcelize
    data class UserCarPlate(
        @PrimaryKey
        @SerializedName("id")
        @Expose
        val id: Int,

        @SerializedName("plate_number_digits")
        @Expose
        val plateNumberDigits: String?,

        @SerializedName("plate_number_characters")
        @Expose
        val plateNumberCharacters: String?,

        var isDeleting: Boolean = false
    ) : Parcelable

    @Parcelize
    data class ActiveSubscription(
        @PrimaryKey
        @SerializedName("id")
        @Expose
        val id: Int,
        @SerializedName("renew_at")
        @Expose
        var renewAt: String? = null,

        @SerializedName("litres")
        @Expose
        var remainingLiters: Double?=0.0,


        @SerializedName("remaining")
        @Expose
        var remainingCash: Double?=0.0,

        @SerializedName("plan")
        @Expose
        val plan: PlanModel.Plan?
    ) : Parcelable

}
