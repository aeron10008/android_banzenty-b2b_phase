package com.restart.banzenty.data.models

import android.os.Parcelable
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class StationFilterModel(
    @SerializedName("services")
    @Expose
    val services : List<StationService>,

    @SerializedName("companies")
    @Expose
    val companies: List<StationModel.Station.StationCompany>,

    @SerializedName("fuel_types")
    @Expose
    val fuelTypes : List<PlanModel.Plan.Fuel>?=null,
) : Parcelable {

/*    @Parcelize
    data class OtherOptions(
        @PrimaryKey
        @SerializedName("id")
        @Expose
        val id: Int,

        @SerializedName("name")
        @Expose
        val name: String,
        
        @SerializedName("category_id")
        @Expose
        val categoryId: Int,

        @SerializedName("image")
        @Expose
        val image: Image?,

        var isChecked: Boolean = false
    ): Parcelable*/
}
