package com.restart.banzenty.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.switchMap
import com.restart.banzenty.data.models.StationFilterModel
import com.restart.banzenty.data.models.StationModel
import com.restart.banzenty.network.*
import com.restart.banzenty.presistence.AppDBDao
import com.restart.banzenty.utils.Response
import com.restart.banzenty.utils.ResponseType
import com.restart.banzenty.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import javax.inject.Inject

class StationRepository
@Inject constructor(
    val appDBDao: AppDBDao,
    val apiService: ApiInterface,
    val sessionManager: SessionManager
) : JobManager("StationRepository") {
    private val TAG = "StationRepository"

    fun getStations(
        fuelTypeIds: ArrayList<Int>?,
        companyIds: ArrayList<Int>?,
        serviceIds: ArrayList<Int>?,
        distance_min: Int?,
        distance_max: Int?,
        lat: Double?,
        lng: Double?
    ): LiveData<DataState<List<StationModel.Station>>> {
        return object :
            NetworkBoundResource<GenericResponse<StationModel>, List<StationModel.Station>>(
                isNetworkAvailable = sessionManager.isInternetAvailable(),
                isNetworkRequest = true,
                shouldLoadFromCache = false,
                shouldCancelIfNoInternet = true
            ) {
            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<GenericResponse<StationModel>>) {
                Log.d(TAG, " handleApiSuccessResponse: $response")
                if (response.body.status_code == 200) {
                    onCompleteJob(
                        DataState.success(
                            data = response.body.data?.stations,
                            response = Response(
                                message = response.body.message,
                                responseType = ResponseType.Toast()
                            )
                        )
                    )
                } else {
                    onCompleteJob(
                        DataState.error(
                            response = Response(
                                message = response.body.message,
                                responseType = ResponseType.Toast()
                            )
                        )
                    )
                }
            }

            override fun createCall(): LiveData<GenericApiResponse<GenericResponse<StationModel>>> {
                return apiService.getStations(
                    fuelTypeIds,
                    companyIds,
                    serviceIds,
                    distance_min,
                    distance_max,
                    lat,
                    lng
                )
            }

            override fun setJob(job: Job) {
                addJop("getStations", job)
            }

            override suspend fun createCacheRequest() {
                withContext(Dispatchers.Main) {
//                    //finish by viewing the db cache
                    result.addSource(loadFromCache()) { viewState ->
                        if (viewState != null) {
                            onCompleteJob(
                                DataState.success(
                                    data = viewState,
                                    null
                                )
                            )
                        }

                    }
                }
            }

            override fun loadFromCache(): LiveData<List<StationModel.Station>> {
                return appDBDao.getStations().switchMap {
                    object : LiveData<List<StationModel.Station>>() {
                        override fun onActive() {
                            super.onActive()
                            value = it
                        }
                    }
                }
            }

            override suspend fun updateLocalDb(cacheObject: List<StationModel.Station>?) {
                if (cacheObject != null) {
                    withContext(Dispatchers.IO) {
                        appDBDao.insertStations(cacheObject)
                    }
                }
            }
        }.asLiveData()
    }

    fun getStationFilters(): LiveData<DataState<StationFilterModel>> {
        return object :
            NetworkBoundResource<GenericResponse<StationFilterModel>, StationFilterModel>(
                isNetworkAvailable = sessionManager.isInternetAvailable(),
                isNetworkRequest = true,
                shouldLoadFromCache = false,
                shouldCancelIfNoInternet = true
            ) {
            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<GenericResponse<StationFilterModel>>) {
                Log.d(TAG, " handleApiSuccessResponse: $response")
                if (response.body.status_code == 200) {
                    onCompleteJob(
                        DataState.success(
                            data = response.body.data,
                            response = Response(
                                message = response.body.message,
                                responseType = ResponseType.Toast()
                            )
                        )
                    )
                } else {
                    onCompleteJob(
                        DataState.error(
                            response = Response(
                                message = response.body.message,
                                responseType = ResponseType.Toast()
                            )
                        )
                    )
                }
            }

            override suspend fun createCacheRequest() {
                //not used in this case
            }

            override fun createCall(): LiveData<GenericApiResponse<GenericResponse<StationFilterModel>>> {
                return apiService.getStationsFilter()
            }

            override fun setJob(job: Job) {
                addJop("filterStations", job)
            }

            override fun loadFromCache(): LiveData<StationFilterModel> {
                //not used in this case
                return AbsentLiveData.create()
            }

            override suspend fun updateLocalDb(cacheObject: StationFilterModel?) {
                //not used in this case
            }

        }.asLiveData()
    }

    fun getStationsList(
        lat: Double?,
        lng: Double?,
        hasContract: Int?
    ): LiveData<DataState<List<StationModel.Station>>> {
        return object :
            NetworkBoundResource<GenericResponse<StationModel>, List<StationModel.Station>>(
                isNetworkAvailable = sessionManager.isInternetAvailable(),
                isNetworkRequest = true,
                shouldLoadFromCache = false,
                shouldCancelIfNoInternet = true
            ) {
            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<GenericResponse<StationModel>>) {
                Log.d(TAG, " handleApiSuccessResponse: $response")
                if (response.body.status_code == 200) {
                    onCompleteJob(
                        DataState.success(
                            data = response.body.data?.stations,
                            response = Response(
                                message = response.body.message,
                                responseType = ResponseType.Toast()
                            )
                        )
                    )
                } else {
                    onCompleteJob(
                        DataState.error(
                            response = Response(
                                message = response.body.message,
                                responseType = ResponseType.Toast()
                            )
                        )
                    )
                }
            }

            override suspend fun createCacheRequest() {
                withContext(Dispatchers.Main) {
                    //finish by viewing the db cache
                    result.addSource(loadFromCache()) { viewState ->
                        if (viewState != null) {
                            onCompleteJob(
                                DataState.success(
                                    data = viewState,
                                    null
                                )
                            )
                        }

                    }
                }
            }

            override fun createCall(): LiveData<GenericApiResponse<GenericResponse<StationModel>>> {
                return apiService.getStationsList(lat, lng,hasContract)
            }

            override fun setJob(job: Job) {
                addJop("getStationsList", job)
            }

            override fun loadFromCache(): LiveData<List<StationModel.Station>> {
                return appDBDao.getStations().switchMap {
                    object : LiveData<List<StationModel.Station>>() {
                        override fun onActive() {
                            super.onActive()
                            value = it
                        }
                    }
                }
            }

            override suspend fun updateLocalDb(cacheObject: List<StationModel.Station>?) {
                if (cacheObject != null) {
                    withContext(Dispatchers.IO) {
                        appDBDao.insertStations(cacheObject)
                    }
                }
            }

        }.asLiveData()
    }
}