package com.restart.banzenty.data.models

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class StationService(
    @SerializedName("id")
    @Expose
    val id: Int,
    @SerializedName("name")
    @Expose
    var name: String? = null,
    @SerializedName("category_id")
    @Expose
    var categoryId: Int? = null,
    @SerializedName("image")
    @Expose
    var image: Image? = null,

    @SerializedName("discount")
    @Expose
    var discount: Int? = 0,

    @SerializedName("limit")
    @Expose
    var limit: Int? = null,

    @SerializedName("used")
    @Expose
    var used: Int? = null,

    var isChecked: Boolean = false
) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StationService

        if (id != other.id) return false
        if (name != other.name) return false
        if (categoryId != other.categoryId) return false
        if (image != other.image) return false
        if (isChecked != other.isChecked) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + (categoryId ?: 0)
        result = 31 * result + (image?.hashCode() ?: 0)
        result = 31 * result + isChecked.hashCode()
        return result
    }
}
