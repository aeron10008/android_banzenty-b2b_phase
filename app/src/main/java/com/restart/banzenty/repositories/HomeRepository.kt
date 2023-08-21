package com.restart.banzenty.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import com.restart.banzenty.data.models.HomeModel
import com.restart.banzenty.data.models.UserModel
import com.restart.banzenty.network.*
import com.restart.banzenty.presistence.AppDBDao
import com.restart.banzenty.utils.Response
import com.restart.banzenty.utils.ResponseType
import com.restart.banzenty.utils.SessionManager
import kotlinx.coroutines.Job
import javax.inject.Inject

class HomeRepository
@Inject constructor(
    val appDBDao: AppDBDao,
    val authApiService: ApiInterface,
    val sessionManager: SessionManager
) : JobManager("HomeRepository") {
    private val TAG = "HomeRepository"

    fun getHomeDetails(): LiveData<DataState<HomeModel>> {
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
                return authApiService.getHomeDetails()
            }

            override fun setJob(job: Job) {
                addJop("getHomeDetails", job)
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

    fun contactUs(name: String, email: String, text: String):
            LiveData<DataState<String>> {
        return object : NetworkBoundResource<GenericResponse<UserModel>, String>(
            isNetworkAvailable = sessionManager.isInternetAvailable(),
            isNetworkRequest = true,
            shouldLoadFromCache = false,
            shouldCancelIfNoInternet = true
        ) {
            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<GenericResponse<UserModel>>) {
                Log.d(TAG, " handleApiSuccessResponse: $response")
                if (response.body.status_code == 200) {
                    onCompleteJob(
                        DataState.success(
                            data = response.body.message,
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

            override fun createCall(): LiveData<GenericApiResponse<GenericResponse<UserModel>>> {
                return authApiService.contactUs(
                    name, email, text
                )
            }

            override fun setJob(job: Job) {
                addJop("contactUs", job)
            }

            override fun loadFromCache(): LiveData<String> {
                //not used in this case
                return AbsentLiveData.create()
            }

            override suspend fun updateLocalDb(cacheObject: String?) {
                //not used in this case
            }

        }.asLiveData()
    }


}