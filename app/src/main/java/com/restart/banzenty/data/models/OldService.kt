package com.restart.banzenty.data.models

import android.graphics.drawable.Drawable
import java.io.Serializable

data class OldService(
    var serviceLogo: Drawable?,
    var serviceName: String
): Serializable
