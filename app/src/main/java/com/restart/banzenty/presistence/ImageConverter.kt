package com.restart.banzenty.presistence

import androidx.room.TypeConverter
import com.restart.banzenty.data.models.Image
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ImageConverter {
    @TypeConverter
    fun fromString(value: String?): List<Image?>? {
        val listType = object : TypeToken<ArrayList<Image?>?>() {}.type
        return Gson().fromJson<List<Image?>>(value, listType)
    }

    @TypeConverter
    fun fromArrayList(list: List<Image?>?): String? {
        val gson = Gson()
        return gson.toJson(list)
    }
}