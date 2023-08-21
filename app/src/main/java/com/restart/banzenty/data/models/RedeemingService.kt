package com.restart.banzenty.data.models

import android.graphics.drawable.Drawable
import java.io.Serializable

data class RedeemingService(
    var serviceImage: Drawable,
    var serviceName: String
) :Serializable
