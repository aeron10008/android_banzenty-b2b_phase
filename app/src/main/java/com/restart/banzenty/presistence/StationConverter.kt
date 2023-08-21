package com.restart.banzenty.presistence

import androidx.room.TypeConverter
import com.restart.banzenty.data.models.StationModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class StationConverter {
/*    @TypeConverter
    fun fromString(value: String?): List<Station?>? {
        val listType = object : TypeToken<ArrayList<Station?>?>() {}.type
        return Gson().fromJson<List<Station?>>(value, listType)
    }

    @TypeConverter
    fun fromArrayList(list: List<Station?>?): String? {
        val gson = Gson()
        return gson.toJson(list)
    }*/

    @TypeConverter
    fun fromString(value: String?): StationModel.Station? {
        val listType = object : TypeToken<StationModel.Station?>() {}.type
        return Gson().fromJson<StationModel.Station?>(value, listType)
    }

    @TypeConverter
    fun fromObject(o: StationModel.Station?): String? {
        val gson = Gson()
        return gson.toJson(o)
    }
}