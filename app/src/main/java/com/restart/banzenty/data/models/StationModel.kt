package com.restart.banzenty.data.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.restart.banzenty.presistence.ImageConverter
import com.restart.banzenty.presistence.ServiceTypeConverter
import com.restart.banzenty.presistence.StationCompanyConverter
import kotlinx.parcelize.Parcelize

@Parcelize
data class StationModel(
    @SerializedName("stations")
    @Expose
    val stations: List<Station>
) : Parcelable {
    @Entity(tableName = "stations")
    @Parcelize
    //TODO add isChecked flag inside Station
    data class Station(
        @PrimaryKey
        @SerializedName("id")
        @Expose
        val id: Int,
        @SerializedName("name")
        @Expose
        val name: String,
        @SerializedName("icon")
        @Expose
        val companyImage: Image?,
        @SerializedName("address")
        @Expose
        val address: String? = null,
        @SerializedName("working_hours")
        @Expose
        val workingHours: String,

        @SerializedName("lat")
        @Expose
        val lat: Double,

        @SerializedName("lng")
        @Expose
        val lng: Double,

        @SerializedName("distance")
        @Expose
        var distance: String?,

        @SerializedName("has_contract")
        @Expose
        val hasContract: Int?=0,

        @TypeConverters(StationCompanyConverter::class)
        @SerializedName("company")
        @Expose
        val company: StationCompany?,

        @SerializedName("services")
        @Expose
        @TypeConverters(ServiceTypeConverter::class)
        var services: List<StationService>? = null,

        @TypeConverters(ImageConverter::class)
        @SerializedName("image")
        @Expose
        val image: List<Image>?,

        var isChecked: Boolean = false


    ) : Parcelable {
        @Parcelize
        data class StationCompany(
            @SerializedName("id")
            @Expose
            val id: Int,

            @SerializedName("name")
            @Expose
            val name: String,

            @SerializedName("icon")
            @Expose
            val image: Image,

            var isChecked: Boolean = false
        ) : Parcelable

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Station

            if (id != other.id) return false

            return true
        }

        override fun hashCode(): Int {
            return id
        }
    }
}
