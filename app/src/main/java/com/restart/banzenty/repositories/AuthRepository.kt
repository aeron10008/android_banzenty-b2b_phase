package com.restart.banzenty.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import com.restart.banzenty.data.models.TermsConditionModel
import com.restart.banzenty.data.models.UserModel
import com.restart.banzenty.network.*
import com.restart.banzenty.presistence.AppDBDao
import com.restart.banzenty.utils.Response
import com.restart.banzenty.utils.ResponseType
import com.restart.banzenty.utils.SessionManager
import kotlinx.coroutines.Job
import javax.inject.Inject

class AuthRepository
@Inject constructor(
    val appDBDao: AppDBDao,
    val authApiService: ApiInterface,
    val sessionManager: SessionManager
) : JobManager("AuthRepository") {
    private val TAG = "AuthRepository"

    fun register(
        fullName: String,
        email: String,
        phone: String,
        carPlateDigits: String,
        carPlateChars: String,
        password: String,
        passwordConfirmation: String,
        socialId: String?,
        socialType: String?,
        image: String?
    ): LiveData<DataState<UserModel>> {
        return object : NetworkBoundResource<GenericResponse<UserModel>, UserModel>(
            isNetworkAvailable = sessionManager.isInternetAvailable(),
            isNetworkRequest = true,
            shouldLoadFromCache = false,
            shouldCancelIfNoInternet = true
        ) {
            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<GenericResponse<UserModel>>) {
                Log.d(TAG, "register handleApiSuccessResponse: $response")
                if (response.body.status_code == 200) {
                    onCompleteJob(
                        DataState.success(
                            data = response.body.data,
                            response = Response(
                                message = "Jobs",
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

            override fun createCall(): LiveData<GenericApiResponse<GenericResponse<UserModel>>> {
                return authApiService.register(
                    fullName,
                    phone,
                    email,
                    password,
                    passwordConfirmation,
                    socialId,
                    socialType,
                    carPlateDigits,
                    carPlateChars,
                    sessionManager.getDeviceToken(),
                    image
                )
            }

            override fun setJob(job: Job) {
                addJop("signUp", job)
            }

            override suspend fun createCacheRequest() {
                //not used in this case
            }

            override fun loadFromCache(): LiveData<UserModel> {
                //not used in this case
                return AbsentLiveData.create()
            }

            override suspend fun updateLocalDb(cacheObject: UserModel?) {
                //not used in this case
            }
        }.asLiveData()
    }

    fun login(phone: String?, password: String?, socialId: String?, socialType: String?):
            LiveData<DataState<UserModel.User>> {
        return object :
            NetworkBoundResource<GenericResponse<UserModel>, UserModel.User>(
                isNetworkAvailable = sessionManager.isInternetAvailable(),
                isNetworkRequest = true,
                shouldLoadFromCache = false,
                shouldCancelIfNoInternet = true
            ) {
            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<GenericResponse<UserModel>>) {
                Log.d(TAG, "login handleApiSuccessResponse: $response")
                val userModel = response.body.data?.user

                if (response.body.status_code == 200) {
                    userModel?.isVerified = true
                    sessionManager.saveUser(userModel!!, response.body.data?.token!!)
                    onCompleteJob(
                        DataState.success(
                            data = userModel,
                            response = Response(
                                message = "login",
                                responseType = ResponseType.None()
                            )
                        )
                    )
                } else if (response.body.status_code == 801) {
                    //Phone not verified
                    userModel?.isVerified = false
                    onCompleteJob(
                        DataState.success(
                            data = userModel,
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

            override fun createCall(): LiveData<GenericApiResponse<GenericResponse<UserModel>>> {
                return authApiService.login(
                    phone, password, socialId, socialType,
                    sessionManager.getDeviceToken()
                )
            }

            override fun setJob(job: Job) {
                addJop("login", job)
            }

            override suspend fun createCacheRequest() {
                //not used in this case
            }

            override fun loadFromCache(): LiveData<UserModel.User> {
                //not used in this case
                return AbsentLiveData.create()
            }

            override suspend fun updateLocalDb(cacheObject: UserModel.User?) {
                //not used in this case
            }
        }.asLiveData()
    }

    fun verify(phone: String?, code: String?): LiveData<DataState<UserModel>> {
        return object : NetworkBoundResource<GenericResponse<UserModel>, UserModel>(
            isNetworkAvailable = sessionManager.isInternetAvailable(),
            isNetworkRequest = true,
            shouldLoadFromCache = false,
            shouldCancelIfNoInternet = true
        ) {
            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<GenericResponse<UserModel>>) {
                Log.d(TAG, "verify handleApiSuccessResponse: $response")
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

            override fun createCall(): LiveData<GenericApiResponse<GenericResponse<UserModel>>> {
                return authApiService.verify(
                    phone = phone,
                    code = code
                )
            }

            override suspend fun createCacheRequest() {
                //not used in this case
            }

            override fun setJob(job: Job) {
                addJop("verifyMobile", job)
            }

            override fun loadFromCache(): LiveData<UserModel> {
                //not used in this case
                return AbsentLiveData.create()
            }

            override suspend fun updateLocalDb(cacheObject: UserModel?) {
                //not used in this case
            }
        }.asLiveData()
    }

    fun resendCode(phone: String, type: String): LiveData<DataState<String>> {
        return object : NetworkBoundResource<GenericResponse<UserModel>, String>(
            isNetworkAvailable = sessionManager.isInternetAvailable(),
            isNetworkRequest = true,
            shouldLoadFromCache = false,
            shouldCancelIfNoInternet = true
        ) {
            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<GenericResponse<UserModel>>) {
                Log.d(TAG, "resend code handleApiSuccessResponse: $response")

                if (response.body.status_code == 200) {
                    onCompleteJob(
                        DataState.success(
                            data = response.body.data?.user?.phone,
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
                return authApiService.resendCode(
                    phone,
                    type
                )
            }

            override fun setJob(job: Job) {
                addJop("resendCode", job)
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

    fun requestPasswordReset(phone: String): LiveData<DataState<String>> {
        return object : NetworkBoundResource<GenericResponse<UserModel>, String>(
            isNetworkAvailable = sessionManager.isInternetAvailable(),
            isNetworkRequest = true,
            shouldLoadFromCache = false,
            shouldCancelIfNoInternet = true
        ) {
            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<GenericResponse<UserModel>>) {
                Log.d(TAG, "request Password reset handleApiSuccessResponse: $response")

                if (response.body.status_code == 200) {
                    onCompleteJob(
                        DataState.success(
                            data = "Sent",
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
                return authApiService.requestPasswordReset(
                    phone
                )
            }

            override fun setJob(job: Job) {
                addJop("requestPasswordReset", job)
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

    fun resetPassword(token: String?, password: String):
            LiveData<DataState<String>> {
        return object : NetworkBoundResource<GenericResponse<UserModel>, String>(
            isNetworkAvailable = sessionManager.isInternetAvailable(),
            isNetworkRequest = true,
            shouldLoadFromCache = false,
            shouldCancelIfNoInternet = true
        ) {
            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<GenericResponse<UserModel>>) {
                Log.d(TAG, "reset password handleApiSuccessResponse: $response")
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
                return authApiService.resetPassword(
                    token,
                    password,
                    password
                )
            }

            override fun setJob(job: Job) {
                addJop("resetPassword", job)
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

    fun termsConditions(): LiveData<DataState<String>> {
        return object : NetworkBoundResource<GenericResponse<TermsConditionModel>, String>(
            isNetworkAvailable = sessionManager.isInternetAvailable(),
            isNetworkRequest = true,
            shouldLoadFromCache = false,
            shouldCancelIfNoInternet = true
        ) {
            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<GenericResponse<TermsConditionModel>>) {
                Log.d(TAG, "verify handleApiSuccessResponse: $response")
                if (response.body.status_code == 200) {
                    onCompleteJob(
                        DataState.success(
                            data = response.body.data?.termsConditions ?: "",
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

            override fun createCall(): LiveData<GenericApiResponse<GenericResponse<TermsConditionModel>>> {
                return authApiService.termsConditions()
            }

            override suspend fun createCacheRequest() {
                //not used in this case
            }

            override fun setJob(job: Job) {
                addJop("TermsCondition", job)
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