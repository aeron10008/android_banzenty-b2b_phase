package com.restart.banzenty.presistence

import androidx.room.TypeConverter
import com.restart.banzenty.data.models.StationService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SingleServiceTypeConverter {

    @TypeConverter
    fun fromString(value: String?): StationService? {
        val listType = object : TypeToken<StationService?>() {}.type
        return Gson().fromJson<StationService?>(value, listType)
    }

    @TypeConverter
    fun fromObject(o: StationService?): String? {
        val gson = Gson()
        return gson.toJson(o)
    }
}