package com.restart.banzenty.utils

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.restart.banzenty.data.models.UserModel
import com.restart.banzenty.presistence.AppDBDao
import com.restart.banzenty.utils.Constants.Companion.BASE_URL
import javax.inject.Inject

class SessionManager
@Inject constructor(
    val context: Context,
    val appDBDao: AppDBDao,
    val sharedPreferences: SharedPreferences
) {
    private val TAG = "SessionManager"
    fun isInternetAvailable(): Boolean {
        var result = false
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = connectivityManager.activeNetwork ?: return false
            val actNw =
                connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
            result = when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            connectivityManager.run {
                connectivityManager.activeNetworkInfo?.run {
                    result = when (type) {
                        ConnectivityManager.TYPE_WIFI -> true
                        ConnectivityManager.TYPE_MOBILE -> true
                        ConnectivityManager.TYPE_ETHERNET -> true
                        else -> false
                    }

                }
            }
        }

        return result
    }

    fun saveUser(userDataModel: UserModel.User, token: String?) {
        sharedPreferences.edit().putInt(PreferenceKeys.USER_ID, userDataModel.id).apply()
        sharedPreferences.edit().putString(PreferenceKeys.USER_NAME, userDataModel.name).apply()
        sharedPreferences.edit().putString(PreferenceKeys.USER_EMAIL, userDataModel.email).apply()
        sharedPreferences.edit().putString(PreferenceKeys.USER_MOBILE, userDataModel.phone).apply()
        sharedPreferences.edit().putString(PreferenceKeys.USER_IMAGE, userDataModel.image?.url)
            .apply()
        sharedPreferences.edit().putString(PreferenceKeys.SOCIAL_ID, userDataModel.social_id)
            .apply()
        sharedPreferences.edit().putString(
            PreferenceKeys.USER_CAR_PLATE_DIGITS,
            userDataModel.userCarPlate?.plateNumberDigits
        ).apply()
        sharedPreferences.edit().putString(
            PreferenceKeys.USER_CAR_PLATE_CHARACTERS,
            userDataModel.userCarPlate?.plateNumberCharacters
        ).apply()

        if (token != null) sharedPreferences.edit().putString(PreferenceKeys.USER_TOKEN, token)
            .apply()
    }

    fun getNightMode(): Int {
        return sharedPreferences.getInt(
            PreferenceKeys.USER_NIGHT_MODE,
            AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    fun setNightMode(nightMode: Int) {
        sharedPreferences.edit().putInt(PreferenceKeys.USER_NIGHT_MODE, nightMode).apply()
    }

    fun isUserSubscribed(): Boolean {
        return sharedPreferences.getBoolean(
            PreferenceKeys.USER_SUBSCRIBED,
            false
        )
    }

    fun setIsUserSubscribed(isUserSubscribed: Boolean) {
        sharedPreferences.edit().putBoolean(PreferenceKeys.USER_SUBSCRIBED, isUserSubscribed)
            .apply()
    }

    fun getRemainingLiters(): Float {
        return sharedPreferences.getFloat(
            PreferenceKeys.USER_REMAINING_LITERS,
            0.0f
        )
    }

    fun setRemainingCash(liters: Float) {
        sharedPreferences.edit().putFloat(PreferenceKeys.USER_REMAINING_CASH, liters).apply()
    }

    fun getRemainingCash(): Float {
        return sharedPreferences.getFloat(
            PreferenceKeys.USER_REMAINING_CASH,
            0.0f
        )
    }

    fun setRemainingLiters(liters: Float) {
        sharedPreferences.edit().putFloat(PreferenceKeys.USER_REMAINING_LITERS, liters).apply()
    }

    fun isFirstTime(): Boolean {
        return sharedPreferences.getBoolean(PreferenceKeys.FIRST_TIME, true)
    }

    fun setFirstTime(firstTime: Boolean) {
        sharedPreferences.edit().putBoolean(PreferenceKeys.FIRST_TIME, firstTime).apply()
    }

    fun getLanguage(): String {
        return sharedPreferences.getString(PreferenceKeys.USER_LANGUAGE, "en").toString()
    }

    fun setLanguage(language: String) {
        sharedPreferences.edit().putString(PreferenceKeys.USER_LANGUAGE, language).apply()
    }

    fun getDeviceToken(): String {
        return sharedPreferences.getString(PreferenceKeys.DEVICE_TOKEN, "").toString()
    }

    fun setDeviceToken(deviceToken: String) {
        sharedPreferences.edit().putString(PreferenceKeys.DEVICE_TOKEN, deviceToken).apply()
    }

    fun getToken(): String {
        return sharedPreferences.getString(PreferenceKeys.USER_TOKEN, "").toString()
    }

    fun setToken(token: String) {
        sharedPreferences.edit().putString(PreferenceKeys.USER_TOKEN, token).apply()
    }

    fun getUserId(): String {
        return sharedPreferences.getInt(PreferenceKeys.USER_ID, -1).toString()
    }

    fun getUserName(): String {
        return sharedPreferences.getString(PreferenceKeys.USER_NAME, "").toString()
    }

    fun getUserEmail(): String {
        return sharedPreferences.getString(PreferenceKeys.USER_EMAIL, "").toString()
    }

    fun getUserPhone(): String {
        return sharedPreferences.getString(PreferenceKeys.USER_MOBILE, "").toString()
    }

    fun getSocialId(): String? {
        return sharedPreferences.getString(PreferenceKeys.SOCIAL_ID, null)
    }

    fun getUserImage(): String {
        return sharedPreferences.getString(
            PreferenceKeys.USER_IMAGE,
            "${BASE_URL.replace("/api/", "")}/images/profile-picture.png"
        ).toString()
    }

    fun getCarPlateDigits(): String {
        return sharedPreferences.getString(PreferenceKeys.USER_CAR_PLATE_DIGITS, "").toString()
    }

    fun getCarPlateCharacters(): String {
        return sharedPreferences.getString(PreferenceKeys.USER_CAR_PLATE_CHARACTERS, "")
            .toString()
    }

    fun logout() {
        FirebaseMessaging.getInstance().deleteToken().addOnSuccessListener {
            Log.d(TAG, "oldToken: ${getDeviceToken()} ")
            fetchToken()
        }
        val language = getLanguage()
        sharedPreferences.edit().clear().apply()
        setFirstTime(false)
        setLanguage(language)
    }

    fun fetchToken() {
        FirebaseApp.initializeApp(context)
        FirebaseMessaging.getInstance().token.addOnSuccessListener { newToken: String? ->
            Log.d(TAG, "newToken: $newToken")
            if (newToken != null) setDeviceToken(newToken)
        }
    }
}