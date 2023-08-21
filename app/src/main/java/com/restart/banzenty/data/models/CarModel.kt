package com.restart.banzenty.data.models

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class CarModel(
    @SerializedName("cars")
    @Expose
    val cars: List<UserModel.UserCarPlate>
) : Parcelable