package com.restart.banzenty.presistence

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.restart.banzenty.data.models.FuelRequestModel
import com.restart.banzenty.data.models.NotificationModel
import com.restart.banzenty.data.models.RewardsModel
import com.restart.banzenty.data.models.StationModel
import com.restart.banzenty.utils.Constants.Companion.DATABASE_VERSION

@Database(
    entities = [NotificationModel.Notification::class, FuelRequestModel.FuelRequest::class,
        StationModel.Station::class, RewardsModel.Reward::class],
    version = DATABASE_VERSION
)
@TypeConverters(
    StationCompanyConverter::class,
    ServiceTypeConverter::class,
    SingleServiceTypeConverter::class,
    StationConverter::class,
    ImageConverter::class,
    SingleImageConverter::class
)

abstract class AppDatabase : RoomDatabase() {

    abstract fun getAppDBDao(): AppDBDao


}