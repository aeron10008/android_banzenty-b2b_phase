package com.restart.banzenty.data.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class RewardsModel(

    @SerializedName("points")
    @Expose
    var userPoints: Int,

    @SerializedName("qr_code")
    @Expose
    var qrCode: String?,

    @field:SerializedName("rewards")
    @Expose
    var rewards: List<Reward>

) : Parcelable {
    @Entity(tableName = "rewards")
    @Parcelize
    data class Reward(
        @PrimaryKey
        @SerializedName("id")
        @Expose
        val id: Int,

        @SerializedName("name")
        @Expose
        val name: String,

        @SerializedName("description")
        @Expose
        val description: String,

        @SerializedName("points")
        @Expose
        val points: Int,

        @SerializedName("image")
        @Expose
        val image: Image?,

        @SerializedName("service_id")
        @Expose
        val serviceId: Int
    ) : Parcelable
}
