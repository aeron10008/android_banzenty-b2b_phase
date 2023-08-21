package com.restart.banzenty.presistence

import androidx.room.TypeConverter
import com.restart.banzenty.data.models.StationModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class StationCompanyConverter {
    /*  @TypeConverter
      fun fromString(value: String?): List<ServiceType?>? {
          val listType = object : TypeToken<ArrayList<ServiceType?>?>() {}.type
          return Gson().fromJson<List<ServiceType?>>(value, listType)
      }

      @TypeConverter
      fun fromArrayList(list: List<ServiceType?>?): String? {
          val gson = Gson()
          return gson.toJson(list)
      }*/

    @TypeConverter
    fun fromString(value: String?): StationModel.Station.StationCompany? {
        val listType = object : TypeToken<StationModel.Station.StationCompany?>() {}.type
        return Gson().fromJson<StationModel.Station.StationCompany?>(value, listType)
    }

    @TypeConverter
    fun fromObject(o: StationModel.Station.StationCompany?): String? {
        val gson = Gson()
        return gson.toJson(o)
    }
}