package com.restart.banzenty.data.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class NotificationModel(
/*{
    "status_code": 200,
    "message": "success",
    "data": {
    "current_page": 1,
    "data": [],
    "from": null,
    "to": null,
    "total": 0,
    "per_page": 20,
    "last_page": 1,
    "next_page_url": null,
    "prev_page_url": null,
    "first_page_url": "http://localhost:8000/api/profile/notifications?page=1",
    "last_page_url": "http://localhost:8000/api/profile/notifications?page=1",
    "path": "http://localhost:8000/api/profile/notifications"
}
}*/
    @field:SerializedName("current_page")
    @Expose
    var current_page: Int = 0,
    @field:SerializedName("first_page_url")
    @Expose
    var firstPageUrl: String? = null,
    @field:SerializedName("from")
    @Expose
    var from: Int = 0,
    @field:SerializedName("last_page")
    @Expose
    var lastPage: Int = 0,
    @field:SerializedName("last_page_url")
    @Expose
    var lastPageUrl: String? = null,
    @field:SerializedName("next_page_url")
    @Expose
    var nextPageUrl: String? = null,
    @field:SerializedName("path")
    @Expose
    var path: String? = null,
    @field:SerializedName("per_page")
    @Expose
    var perPage: Int = 0,
    @field:SerializedName("prev_page_url")
    @Expose
    var pretPageUrl: String? = null,
    @field:SerializedName("to")
    @Expose
    var to: Int = 0,
    @field:SerializedName("total")
    @Expose
    var total: Int = 0,

    @field:SerializedName("data")
    @Expose
    var notifications: List<Notification>? = null
) : Parcelable {

    @Entity(tableName = "notifications")
    @Parcelize
    data class Notification(
        @PrimaryKey
        @SerializedName("id")
        @Expose
        val id: Int,
        @SerializedName("title")
        @Expose
        val title: String,
        @SerializedName("image")
        @Expose
        val image: String,
        @SerializedName("read")
        @Expose
        val isRead: Int = 0
    ) : Parcelable

}
