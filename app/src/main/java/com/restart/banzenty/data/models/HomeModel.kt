package com.restart.banzenty.data.models

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class HomeModel(

    @SerializedName("user")
    @Expose
    val user: UserModel.User?,
    @SerializedName("banners")
    @Expose
    val banners: List<Banner>,
    @SerializedName("latest_request")
    @Expose
    val latestRequest: FuelRequestModel.FuelRequest?,
    @SerializedName("unread_notifications_count")
    @Expose
    val unReadNotificationCount: Int = 0,

    @SerializedName("subscribe_message")
    @Expose
    val subscribeMessages: SubscribeMessages?,

) : Parcelable {

    @Parcelize
    data class Banner(
        @SerializedName("id")
        @Expose
        val id: Int,
        @SerializedName("target_type")
        @Expose
        val targetType: String,
        @SerializedName("target_id")
        @Expose
        val targetId: String,
//        @SerializedName("target")
//        @Expose
//        val target: String,
        @SerializedName("image")
        @Expose
        val image: Image?
    ) : Parcelable

    @Parcelize
    data class SubscribeMessages(
        @SerializedName("renew")
        @Expose
        val renew: String?,
        @SerializedName("cancel")
        @Expose
        val cancel: String?
    ) : Parcelable

}
