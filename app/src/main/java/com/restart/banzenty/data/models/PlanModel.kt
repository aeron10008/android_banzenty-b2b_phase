package com.restart.banzenty.data.models

import android.os.Parcelable
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class PlanModel(

    @SerializedName("plans")
    @Expose
    val plans: List<Plan>,

    @SerializedName("subscribed")
    @Expose
    val subscribed: Boolean,

    @SerializedName("pending_subscription_id")
    @Expose
    var pendingSubscriptionId: Int? = null,

    @SerializedName("pending_subscription_message")
    @Expose
    val pendingSubscriptionMessage: String?="",
    @SerializedName("requested_subscription_message")
    @Expose
    val requestedSubscriptionMessage: String?=""

) : Parcelable {

    @Parcelize
    data class Plan(
        @PrimaryKey
        @SerializedName("id")
        @Expose
        val id: Int,

        @SerializedName("name")
        @Expose
        val name: String,

        @SerializedName("litres")
        @Expose
        val liters: Double? = 0.0,

        @SerializedName("price")
        @Expose
        val price: String?,

        @SerializedName("period")
        @Expose
        val period: String,

        @SerializedName("fuel")
        @Expose
        val fuel: Fuel,

        @SerializedName("services")
        @Expose
        val subscriptionFeatures: ArrayList<StationService>? = null

    ) : Parcelable {

        @Parcelize
        data class Fuel(
            @SerializedName("id")
            @Expose
            val id: Int,

            @SerializedName("name")
            @Expose
            val name: String,

            @SerializedName("price")
            @Expose
            val fuelPrice: Double,

            var isChecked: Boolean = false
        ) : Parcelable
//
//        @Parcelize
//        data class SubscriptionFeature(
//            @SerializedName("id")
//            @Expose
//            val id: Int,
//            @SerializedName("service")
//            @Expose
//            val service: StationService,
//            @SerializedName("discount")
//            @Expose
//            val discount: Double
//        ) : Parcelable
    }
}
