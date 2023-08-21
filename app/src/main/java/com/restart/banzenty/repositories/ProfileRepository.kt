package com.restart.banzenty.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.switchMap
import com.restart.banzenty.data.models.CarModel
import com.restart.banzenty.data.models.FuelRequestModel
import com.restart.banzenty.data.models.NotificationModel
import com.restart.banzenty.data.models.UserModel
import com.restart.banzenty.network.*
import com.restart.banzenty.presistence.AppDBDao
import com.restart.banzenty.utils.Response
import com.restart.banzenty.utils.ResponseType
import com.restart.banzenty.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject


class ProfileRepository
@Inject constructor(
    val appDBDao: AppDBDao,
    val authApiService: ApiInterface,
    val sessionManager: SessionManager
) : JobManager("ProfileRepository") {
    private val TAG = "ProfileRepository"


    fun getProfileDetails(): LiveData<DataState<UserModel.User>> {
        return object :
            NetworkBoundResource<GenericResponse<UserModel>, UserModel.User>(
                isNetworkAvailable = sessionManager.isInternetAvailable(),
                isNetworkRequest = true,
                shouldLoadFromCache = false,
                shouldCancelIfNoInternet = false
            ) {

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<GenericResponse<UserModel>>) {
                if (response.body.status_code == 200) {
                    onCompleteJob(
                        DataState.success(
                            data = response.body.data?.user,
                            response = Response(
                                message = "User",
                                responseType = ResponseType.None()
                            )
                        )
                    )
                } else {
                    onCompleteJob(
                        DataState.error(
                            response = Response(
                                message = "No Data",
                                responseType = ResponseType.Toast()
                            )
                        )
                    )
                }
            }

            override fun createCall(): LiveData<GenericApiResponse<GenericResponse<UserModel>>> {
                Log.d(TAG, "createCall: ")
                return authApiService.getProfileDetails()
            }

            override fun setJob(job: Job) {
                addJop("getNotifications", job)
            }

            override suspend fun createCacheRequest() {
                //Not used here
            }

            override fun loadFromCache(): LiveData<UserModel.User> {
                return AbsentLiveData.create()
            }

            override suspend fun updateLocalDb(cacheObject: UserModel.User?) {
                //Not used here
            }
        }.asLiveData()
    }

    fun updateProfile(
        name: String,
        phone: String,
        email: String,
        image: File?,
        carPlateDigits: String,
        carPlateCharacters: String
    ):
            LiveData<DataState<UserModel.User>> {
        return object :
            NetworkBoundResource<GenericResponse<UserModel>, UserModel.User>(
                isNetworkAvailable = sessionManager.isInternetAvailable(),
                isNetworkRequest = true,
                shouldLoadFromCache = false,
                shouldCancelIfNoInternet = true
            ) {

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<GenericResponse<UserModel>>) {
                val userModel = response.body.data?.user

                if (response.body.status_code == 200) {
                    userModel?.isVerified = true
                    sessionManager.saveUser(userModel!!, response.body.data?.token)

                    onCompleteJob(
                        DataState.success(
                            data = response.body.data?.user,
                            response = Response(
                                message = response.body.message,
                                responseType = ResponseType.None()
                            )
                        )
                    )
                } else if (response.body.status_code == 801) {
                    //phone updated, and need to be verified
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
                                message = "No Data",
                                responseType = ResponseType.Toast()
                            )
                        )
                    )
                }
            }

            override fun createCall(): LiveData<GenericApiResponse<GenericResponse<UserModel>>> {
                Log.d(TAG, "createCall: ")
                val requestImageFile: RequestBody? =
                    image?.asRequestBody("image/jpeg".toMediaTypeOrNull())
                return authApiService.updateProfile(
                    name = name.toRequestBody("text/plain;charset=utf-8".toMediaTypeOrNull()),
                    phone = phone.toRequestBody("text/plain;charset=utf-8".toMediaTypeOrNull()),
                    email = email.toRequestBody("text/plain;charset=utf-8".toMediaTypeOrNull()),
                    carPlateDigits = carPlateDigits
                        .toRequestBody("text/plain;charset=utf-8".toMediaTypeOrNull()),
                    carPlateChars = carPlateCharacters.toRequestBody("text/plain;charset=utf-8".toMediaTypeOrNull()),
                    image = if (image != null && requestImageFile != null) MultipartBody.Part.createFormData(
                        "image",
                        image.name,
                        requestImageFile
                    ) else null
                )
            }

            override fun setJob(job: Job) {
                addJop("getNotifications", job)
            }

            override suspend fun createCacheRequest() {
                //Not used here
            }

            override fun loadFromCache(): LiveData<UserModel.User> {
                return AbsentLiveData.create()
            }

            override suspend fun updateLocalDb(cacheObject: UserModel.User?) {
                //Not used here
            }
        }.asLiveData()
    }

    fun changePassword(oldPassword: String, password: String):
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
                return authApiService.changePassword(
                    oldPassword,
                    password,
                    password
                )
            }

            override fun setJob(job: Job) {
                addJop("changePassword", job)
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

    fun getNotifications(page: Int): LiveData<DataState<List<NotificationModel.Notification>>> {
        return object :
            NetworkBoundResource<GenericResponse<NotificationModel>, List<NotificationModel.Notification>>(
                isNetworkAvailable = sessionManager.isInternetAvailable(),
                isNetworkRequest = true,
                shouldLoadFromCache = false,
                shouldCancelIfNoInternet = false
            ) {

            override suspend fun handleApiSuccessResponse(
                response: ApiSuccessResponse<GenericResponse<NotificationModel>>
            ) {
//                if (response.body.data?.notifications != null) {
                onCompleteJob(
                    DataState.success(
                        data = response.body.data?.notifications,
                        response = Response(
                            message = "Notifications",
                            responseType = ResponseType.None()
                        )
                    )
                )
//                    updateLocalDb(response.body.data?.notifications)
//                    createCacheRequest()
//                } else {
//                    onCompleteJob(
//                        DataState.error(
//                            response = Response(
//                                message = "No Data",
//                                responseType = ResponseType.Toast()
//                            )
//                        )
//                    )
//                }
            }

            override fun createCall(): LiveData<GenericApiResponse<GenericResponse<NotificationModel>>> {
                Log.d(TAG, "createCall: ")
                return authApiService.getNotifications(
                    page
                )
            }

            override fun setJob(job: Job) {
                addJop("getNotifications", job)
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

            override fun loadFromCache(): LiveData<List<NotificationModel.Notification>> {
                return appDBDao.getNotifications(
                    page = page
                ).switchMap {
                    object : LiveData<List<NotificationModel.Notification>>() {
                        override fun onActive() {
                            super.onActive()
                            value = it
                        }
                    }
                }
            }

            override suspend fun updateLocalDb(cacheObject: List<NotificationModel.Notification>?) {
                if (cacheObject != null) {
                    withContext(Dispatchers.IO) {
                        appDBDao.insertNotifications(cacheObject)
                    }
                }
            }
        }.asLiveData()
    }

    fun getFuelRequests(page: Int): LiveData<DataState<List<FuelRequestModel.FuelRequest>>> {
        return object :
            NetworkBoundResource<GenericResponse<FuelRequestModel>, List<FuelRequestModel.FuelRequest>>(
                isNetworkAvailable = sessionManager.isInternetAvailable(),
                isNetworkRequest = true,
                shouldLoadFromCache = false,
                shouldCancelIfNoInternet = false
            ) {

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<GenericResponse<FuelRequestModel>>) {
//                if (response.body.data?.notifications != null) {
                onCompleteJob(
                    DataState.success(
                        data = response.body.data?.requests,
                        response = Response(
                            message = "Notifications",
                            responseType = ResponseType.None()
                        )
                    )
                )
//                    updateLocalDb(response.body.data?.notifications)
//                    createCacheRequest()
//                } else {
//                    onCompleteJob(
//                        DataState.error(
//                            response = Response(
//                                message = "No Data",
//                                responseType = ResponseType.Toast()
//                            )
//                        )
//                    )
//                }
            }

            override fun createCall(): LiveData<GenericApiResponse<GenericResponse<FuelRequestModel>>> {
                Log.d(TAG, "createCall: ")
                return authApiService.getFuelRequests(
                    page
                )
            }

            override fun setJob(job: Job) {
                addJop("getFuelRequests", job)
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

            override fun loadFromCache(): LiveData<List<FuelRequestModel.FuelRequest>> {
                return appDBDao.getPlanRequests(
                    page = page
                ).switchMap {
                    object : LiveData<List<FuelRequestModel.FuelRequest>>() {
                        override fun onActive() {
                            super.onActive()
                            value = it
                        }
                    }
                }
            }

            override suspend fun updateLocalDb(cacheObject: List<FuelRequestModel.FuelRequest>?) {
                if (cacheObject != null) {
                    withContext(Dispatchers.IO) {
                        appDBDao.insertFuelRequests(cacheObject)
                    }
                }
            }


        }.asLiveData()
    }

    fun addCar(carPlateDigits: String, carPlateCharacters: String): LiveData<DataState<String>> {
        return object : NetworkBoundResource<GenericResponse<CarModel>, String>(
            isNetworkAvailable = sessionManager.isInternetAvailable(),
            isNetworkRequest = true,
            shouldLoadFromCache = false,
            shouldCancelIfNoInternet = false
        ) {
            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<GenericResponse<CarModel>>) {
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

            override fun createCall(): LiveData<GenericApiResponse<GenericResponse<CarModel>>> {
                return authApiService.addCar(carPlateDigits, carPlateCharacters)
            }

            override fun setJob(job: Job) {
                addJop("addCar", job)
            }

            override suspend fun createCacheRequest() {
                //not used here
            }

            override fun loadFromCache(): LiveData<String> {
                return AbsentLiveData.create()
            }

            override suspend fun updateLocalDb(cacheObject: String?) {
                //not used here
            }

        }.asLiveData()
    }

    fun getCars(): LiveData<DataState<CarModel>> {
        return object : NetworkBoundResource<GenericResponse<CarModel>, CarModel>(
            isNetworkAvailable = sessionManager.isInternetAvailable(),
            isNetworkRequest = true,
            shouldLoadFromCache = false,
            shouldCancelIfNoInternet = false
        ) {
            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<GenericResponse<CarModel>>) {
                Log.d(TAG, "get cars handleApiSuccessResponse: $response")
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

            override fun createCall(): LiveData<GenericApiResponse<GenericResponse<CarModel>>> {
                return authApiService.getCars()
            }

            override fun setJob(job: Job) {
                addJop("getCarsList", job)
            }

            override fun loadFromCache(): LiveData<CarModel> {
                return AbsentLiveData.create()
            }

            override suspend fun updateLocalDb(cacheObject: CarModel?) {
                //not used in this case
            }

        }.asLiveData()
    }

    fun deleteCar(carId: Int): LiveData<DataState<String>> {
        return object : NetworkBoundResource<GenericResponse<Int>, String>(
            isNetworkAvailable = sessionManager.isInternetAvailable(),
            isNetworkRequest = true,
            shouldLoadFromCache = false,
            shouldCancelIfNoInternet = true
        ) {
            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<GenericResponse<Int>>) {
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
                //not used here
            }

            override fun createCall(): LiveData<GenericApiResponse<GenericResponse<Int>>> {
                return authApiService.deleteCar(carId)
            }

            override fun setJob(job: Job) {
                addJop("deleteCar", job)
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