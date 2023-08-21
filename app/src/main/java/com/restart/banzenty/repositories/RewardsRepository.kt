package com.restart.banzenty.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import com.restart.banzenty.data.models.CouponModel
import com.restart.banzenty.data.models.RewardsModel
import com.restart.banzenty.network.*
import com.restart.banzenty.presistence.AppDBDao
import com.restart.banzenty.utils.Response
import com.restart.banzenty.utils.ResponseType
import com.restart.banzenty.utils.SessionManager
import kotlinx.coroutines.Job
import javax.inject.Inject

class RewardsRepository
@Inject constructor(
    val appDBDao: AppDBDao,
    val authApiService: ApiInterface,
    val sessionManager: SessionManager
) : JobManager("RewardsRepository") {
    private val TAG = "RewardsRepository"

    fun getRewards(): LiveData<DataState<RewardsModel>> {
        return object : NetworkBoundResource<GenericResponse<RewardsModel>, RewardsModel>(
            isNetworkAvailable = sessionManager.isInternetAvailable(),
            isNetworkRequest = true,
            shouldLoadFromCache = true,
            shouldCancelIfNoInternet = false
        ) {
            override suspend fun handleApiSuccessResponse(
                response: ApiSuccessResponse<GenericResponse<RewardsModel>>) {

                if (response.body.status_code == 200) {
                    onCompleteJob(
                        DataState.success(
                            data = response.body.data,
                            response = Response(
                                message = "Rewards",
                                responseType = ResponseType.None()
                            )
                        )
                    )
                }
                else {
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

            override fun createCall(): LiveData<GenericApiResponse<GenericResponse<RewardsModel>>> {
                Log.d(TAG, "createCall: ")
                return authApiService.getRewards()
            }

            override fun setJob(job: Job) {
                addJop("getRewards", job)
            }

            override suspend fun createCacheRequest() {
/*
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
*/
            }

            override fun loadFromCache(): LiveData<RewardsModel> {
/*
                return appDBDao.getRewards().switchMap {
                    object : LiveData<List<RewardsModel.Reward>>() {
                        override fun onActive() {
                            super.onActive()
                            value = it
                        }
                    }
                }
*/
                return AbsentLiveData.create()
            }

            override suspend fun updateLocalDb(cacheObject: RewardsModel?) {
                /*if (cacheObject != null) {
                    withContext(Dispatchers.IO) {
                        appDBDao.insertRewards(cacheObject)
                    }
                }*/
            }
        }.asLiveData()
    }

    fun redeemCode(rewardId: Int): LiveData<DataState<CouponModel>> {
        return object : NetworkBoundResource<GenericResponse<CouponModel>, CouponModel>(
            isNetworkAvailable = sessionManager.isInternetAvailable(),
            isNetworkRequest = true,
            shouldLoadFromCache = false,
            shouldCancelIfNoInternet = true
        ) {
            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<GenericResponse<CouponModel>>) {
                if (response.body.status_code == 200) {
                    onCompleteJob(
                        DataState.success(
                            data = response.body.data,
                            response = Response(
                                message = "Coupon",
                                responseType = ResponseType.None()
                            )
                        )
                    )
                }
                else {
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

            override fun createCall(): LiveData<GenericApiResponse<GenericResponse<CouponModel>>> {
                Log.d(TAG, "createCall: ")
                return authApiService.redeemCode(rewardId)
            }

            override suspend fun createCacheRequest() {
                //Not used here
            }

            override fun setJob(job: Job) {
                addJop("Coupon", job)
            }

            override fun loadFromCache(): LiveData<CouponModel> {
                return AbsentLiveData.create()
            }

            override suspend fun updateLocalDb(cacheObject: CouponModel?) {
                //Not used here
            }
        }.asLiveData()
    }
}