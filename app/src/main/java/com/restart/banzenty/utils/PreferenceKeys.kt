package com.restart.banzenty.utils

import com.restart.banzenty.BuildConfig

class PreferenceKeys {

    companion object {
        //shared preference files:
        const val APP_PREFERENCES: String = "${BuildConfig.APPLICATION_ID}.APP_PREFERENCES"

        //shared preference keys here:
        const val FIRST_TIME: String = "${BuildConfig.APPLICATION_ID}.FIRST_TIME"
        const val USER_LANGUAGE: String = "${BuildConfig.APPLICATION_ID}.USER_LANGUAGE"
        const val USER_NIGHT_MODE: String = "${BuildConfig.APPLICATION_ID}.USER_NIGHT_MODE"
        const val DEVICE_TOKEN: String = "${BuildConfig.APPLICATION_ID}.DEVICE_TOKEN"
        const val USER_TOKEN: String = "${BuildConfig.APPLICATION_ID}.USER_TOKEN"
        const val USER_ID: String = "${BuildConfig.APPLICATION_ID}.USER_ID"
        const val USER_NAME: String = "${BuildConfig.APPLICATION_ID}.USER_NAME"
        const val USER_EMAIL: String = "${BuildConfig.APPLICATION_ID}.USER_EMAIL"
        const val USER_MOBILE: String = "${BuildConfig.APPLICATION_ID}.USER_MOBILE"
        const val USER_IMAGE: String = "${BuildConfig.APPLICATION_ID}.USER_IMAGE"
        const val SOCIAL_ID: String = "${BuildConfig.APPLICATION_ID}.SOCIAL_ID"
        const val USER_CAR_PLATE_DIGITS: String =
            "${BuildConfig.APPLICATION_ID}.USER_CAR_PLATE_DIGITS"
        const val USER_CAR_PLATE_CHARACTERS: String =
            "${BuildConfig.APPLICATION_ID}.USER_CAR_PLATE_CHARACTERS"
        const val USER_SUBSCRIBED: String = "${BuildConfig.APPLICATION_ID}.USER_SUBSCRIBED"
        const val USER_REMAINING_LITERS: String =
            "${BuildConfig.APPLICATION_ID}.USER_REMAINING_LITERS"
        const val USER_REMAINING_CASH: String =
            "${BuildConfig.APPLICATION_ID}.USER_REMAINING_CASH"
    }
}