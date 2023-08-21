package com.restart.banzenty.presistence

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.restart.banzenty.data.models.FuelRequestModel
import com.restart.banzenty.data.models.NotificationModel
import com.restart.banzenty.data.models.RewardsModel
import com.restart.banzenty.data.models.StationModel
import com.restart.banzenty.utils.Constants

@Dao
interface AppDBDao {

    /*Notifications*/
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationModel.Notification>)

    @Query("SELECT * FROM notifications ORDER BY id DESC LIMIT (:page * ${Constants.PAGINATION_PAGE_SIZE})")
    fun getNotifications(
        page: Int
    ): LiveData<List<NotificationModel.Notification>>

    /*PlanRequests*/
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFuelRequests(notifications: List<FuelRequestModel.FuelRequest>)

    @Query("SELECT * FROM requests ORDER BY id DESC LIMIT (:page * ${Constants.PAGINATION_PAGE_SIZE})")
    fun getPlanRequests(
        page: Int
    ): LiveData<List<FuelRequestModel.FuelRequest>>


    /*Stations*/
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStations(stations: List<StationModel.Station>)

    @Query("SELECT * FROM stations")
//    @Query("SELECT * FROM stations ORDER BY id DESC LIMIT (:page * ${Constants.PAGINATION_PAGE_SIZE})")
    fun getStations(): LiveData<List<StationModel.Station>>


    /*Rewards*/
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRewards(rewards: List<RewardsModel.Reward>)

    @Query("SELECT * FROM rewards")
    fun getRewards(): LiveData<List<RewardsModel.Reward>>
}