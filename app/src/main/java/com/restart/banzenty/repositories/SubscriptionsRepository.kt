package com.restart.banzenty.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import com.restart.banzenty.data.models.HomeModel
import com.restart.banzenty.data.models.PlanModel
import com.restart.banzenty.data.models.UserModel
import com.restart.banzenty.network.*
import com.restart.banzenty.presistence.AppDBDao
import com.restart.banzenty.utils.Response
import com.restart.banzenty.utils.ResponseType
import com.restart.banzenty.utils.SessionManager
import kotlinx.coroutines.Job
import javax.inject.Inject

class SubscriptionsRepository
@Inject constructor(
    val appDBDao: AppDBDao,
    val apiService: ApiInterface,
    val sessionManager: SessionManager
) : JobManager("SubscriptionsRepository") {
    private val TAG = "SubscriptionsRepository"

    fun getMySubscription(): LiveData<DataState<HomeModel>> {
        return object : NetworkBoundResource<GenericResponse<HomeModel>, HomeModel>(
            isNetworkAvailable = sessionManager.isInternetAvailable(),
            isNetworkRequest = true,
            shouldLoadFromCache = false,
            shouldCancelIfNoInternet = true
        ) {
            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<GenericResponse<HomeModel>>) {
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

            override fun createCall(): LiveData<GenericApiResponse<GenericResponse<HomeModel>>> {
                return apiService.getHomeDetails()
            }

            override fun setJob(job: Job) {
                addJop("getMySubscription", job)
            }

            override fun loadFromCache(): LiveData<HomeModel> {
                //not used in this case
                return AbsentLiveData.create()
            }

            override suspend fun updateLocalDb(cacheObject: HomeModel?) {
                //not used in this case
            }

        }.asLiveData()
    }

    fun getSubscriptionPlans(): LiveData<DataState<PlanModel>> {
        return object : NetworkBoundResource<GenericResponse<PlanModel>, PlanModel>(
            isNetworkAvailable = sessionManager.isInternetAvailable(),
            isNetworkRequest = true,
            shouldLoadFromCache = false,
            shouldCancelIfNoInternet = true
        ) {
            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<GenericResponse<PlanModel>>) {
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

            override fun createCall(): LiveData<GenericApiResponse<GenericResponse<PlanModel>>> {
                return apiService.getSubscriptionPlans()
            }

            override fun setJob(job: Job) {
                addJop("getStations", job)
            }

            override fun loadFromCache(): LiveData<PlanModel> {
                return AbsentLiveData.create()
            }

            override suspend fun updateLocalDb(cacheObject: PlanModel?) {
                //not used in this case
            }

        }.asLiveData()
    }

    fun subscribe(planId: Int, stationIDs: Array<Int>?): LiveData<DataState<String>> {
        return object : NetworkBoundResource<GenericResponse<String>, String>(
            isNetworkAvailable = sessionManager.isInternetAvailable(),
            isNetworkRequest = true,
            shouldLoadFromCache = false,
            shouldCancelIfNoInternet = true
        ) {
            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<GenericResponse<String>>) {
                if (response.body.status_code == 200) {
                    onCompleteJob(
                        DataState.success(
                            data = response.body.message,
                            response = Response(
                                message = response.body.message,
                                responseType = ResponseType.None()
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
                //nit used here
            }

            override fun createCall(): LiveData<GenericApiResponse<GenericResponse<String>>> {
                return apiService.subscribe(planId, stationIDs)
            }

            override fun setJob(job: Job) {
                addJop("subscribe", job)
            }

            override fun loadFromCache(): LiveData<String> {
                return AbsentLiveData.create()
            }

            override suspend fun updateLocalDb(cacheObject: String?) {
                //not used here
            }

        }.asLiveData()
    }

    fun cancelSubscription(): LiveData<DataState<String>> {
        return object : NetworkBoundResource<GenericResponse<String>, String>(
            isNetworkAvailable = sessionManager.isInternetAvailable(),
            isNetworkRequest = true,
            shouldLoadFromCache = false,
            shouldCancelIfNoInternet = true
        ) {
            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<GenericResponse<String>>) {
                if (response.body.status_code == 200) {
                    onCompleteJob(
                        DataState.success(
                            data = response.body.message,
                            response = Response(
                                message = response.body.message,
                                responseType = ResponseType.None()
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
                //nit used here
            }

            override fun createCall(): LiveData<GenericApiResponse<GenericResponse<String>>> {
                return apiService.cancelSubscription()
            }

            override fun setJob(job: Job) {
                addJop("unsubscribe", job)
            }

            override fun loadFromCache(): LiveData<String> {
                return AbsentLiveData.create()
            }

            override suspend fun updateLocalDb(cacheObject: String?) {
                //not used here
            }

        }.asLiveData()
    }

    fun renewSubscription(): LiveData<DataState<String>> {
        return object : NetworkBoundResource<GenericResponse<UserModel.User>, String>(
            isNetworkAvailable = sessionManager.isInternetAvailable(),
            isNetworkRequest = true,
            shouldLoadFromCache = false,
            shouldCancelIfNoInternet = true
        ) {
            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<GenericResponse<UserModel.User>>) {
                if (response.body.status_code == 200) {
                    onCompleteJob(
                        DataState.success(
                            data = response.body.data?.renewedSubscription?.renewAt,
                            response = Response(
                                message = response.body.message,
                                responseType = ResponseType.None()
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
                //nit used here
            }

            override fun createCall(): LiveData<GenericApiResponse<GenericResponse<UserModel.User>>> {
                return apiService.renewSubscription()
            }

            override fun setJob(job: Job) {
                addJop("renewSubscribe", job)
            }

            override fun loadFromCache(): LiveData<String> {
                return AbsentLiveData.create()
            }

            override suspend fun updateLocalDb(cacheObject: String?) {
                //not used here
            }

        }.asLiveData()
    }
}