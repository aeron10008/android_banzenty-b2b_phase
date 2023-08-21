package com.restart.banzenty.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import com.restart.banzenty.data.models.ChargeModel
import com.restart.banzenty.data.models.PaymentModel
import com.restart.banzenty.data.models.WalletModel
import com.restart.banzenty.network.*
import com.restart.banzenty.presistence.AppDBDao
import com.restart.banzenty.utils.Response
import com.restart.banzenty.utils.ResponseType
import com.restart.banzenty.utils.SessionManager
import kotlinx.coroutines.Job
import javax.inject.Inject

class WalletRepository
@Inject constructor(
    val appDBDao: AppDBDao,
    val authApiService: ApiInterface,
    val sessionManager: SessionManager
) : JobManager("WalletRepository") {
    private val TAG = "WalletRepository"

    fun getWalletDetails(): LiveData<DataState<WalletModel.Wallet>> {
        return object : NetworkBoundResource<GenericResponse<WalletModel>, WalletModel.Wallet>(
            isNetworkAvailable = sessionManager.isInternetAvailable(),
            isNetworkRequest = true,
            shouldLoadFromCache = false,
            shouldCancelIfNoInternet = true
        ) {
            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<GenericResponse<WalletModel>>) {
                Log.d(TAG, " handleApiSuccessResponse: $response")
                if (response.body.status_code == 200) {
                    onCompleteJob(
                        DataState.success(
                            data = response.body.data?.wallet,
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

            override fun createCall(): LiveData<GenericApiResponse<GenericResponse<WalletModel>>> {
                Log.d(TAG, "money: ${authApiService.getWalletDetails()}")
                return authApiService.getWalletDetails()
            }

            override fun setJob(job: Job) {
                addJop("getWalletDetails", job)
            }

            override fun loadFromCache(): LiveData<WalletModel.Wallet> {
                //not used in this case
                return AbsentLiveData.create()
            }

            override suspend fun updateLocalDb(cacheObject: WalletModel.Wallet?) {
                //not used in this case
            }

        }.asLiveData()
    }

    fun getTransactionList(): LiveData<DataState<PaymentModel>> {
        return object : NetworkBoundResource<GenericResponse<PaymentModel>, PaymentModel>(
            isNetworkAvailable = sessionManager.isInternetAvailable(),
            isNetworkRequest = true,
            shouldLoadFromCache = false,
            shouldCancelIfNoInternet = true
        ) {
            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<GenericResponse<PaymentModel>>) {
                Log.d(TAG, " handleApiSuccessResponse: ${response.body.data}")
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

            override fun createCall(): LiveData<GenericApiResponse<GenericResponse<PaymentModel>>> {
                return authApiService.getTransactionList()
            }

            override fun setJob(job: Job) {
                addJop("getStations", job)
            }

            override fun loadFromCache(): LiveData<PaymentModel> {
                return AbsentLiveData.create()
            }

            override suspend fun updateLocalDb(cacheObject: PaymentModel?) {
                //not used in this case
            }

        }.asLiveData()
    }

    fun addMoneyToWallet(orderAmount: String?, merchantRefNumber: String?, statusCode: String?): LiveData<DataState<ChargeModel>> {
        return object : NetworkBoundResource<GenericResponse<ChargeModel>, ChargeModel>(
            isNetworkAvailable = sessionManager.isInternetAvailable(),
            isNetworkRequest = true,
            shouldLoadFromCache = false,
            shouldCancelIfNoInternet = true
        ) {
            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<GenericResponse<ChargeModel>>) {
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

            override fun createCall(): LiveData<GenericApiResponse<GenericResponse<ChargeModel>>> {
                Log.d(TAG, "money: ${authApiService.getWalletDetails()}")
                return authApiService.addMoneyToWallet(
                    orderAmount, merchantRefNumber, statusCode
                )
            }

            override fun setJob(job: Job) {
                addJop("getWalletDetails", job)
            }

            override fun loadFromCache(): LiveData<ChargeModel> {
                //not used in this case
                return AbsentLiveData.create()
            }

            override suspend fun updateLocalDb(cacheObject: ChargeModel?) {
                //not used in this case
            }

        }.asLiveData()
    }
}