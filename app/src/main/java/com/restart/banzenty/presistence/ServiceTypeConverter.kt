package com.restart.banzenty.presistence

import androidx.room.TypeConverter
import com.restart.banzenty.data.models.StationService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ServiceTypeConverter {
      @TypeConverter
      fun fromString(value: String?): List<StationService?>? {
          val listType = object : TypeToken<ArrayList<StationService?>?>() {}.type
          return Gson().fromJson<List<StationService?>>(value, listType)
      }

      @TypeConverter
      fun fromArrayList(list: List<StationService?>?): String? {
          val gson = Gson()
          return gson.toJson(list)
      }

}