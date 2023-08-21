package com.restart.banzenty.presistence

import androidx.room.TypeConverter
import com.restart.banzenty.data.models.Image
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SingleImageConverter {
    @TypeConverter
    fun fromString(value: String?): Image? {
        val listType = object : TypeToken<Image?>() {}.type
        return Gson().fromJson<Image?>(value, listType)
    }

    @TypeConverter
    fun fromObject(o: Image?): String? {
        val gson = Gson()
        return gson.toJson(o)
    }
}