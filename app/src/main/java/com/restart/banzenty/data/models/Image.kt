package com.restart.banzenty.data.models

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Image(
    @SerializedName("url")
    @Expose
    val url: String?,
    @SerializedName("thumbnail")
    @Expose
    val thumbnail: String?,
    @SerializedName("preview")
    @Expose
    val preview: String?
) : Parcelable