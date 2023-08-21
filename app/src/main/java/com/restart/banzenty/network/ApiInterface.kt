package com.restart.banzenty.network

import androidx.lifecycle.LiveData
import com.restart.banzenty.data.models.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface ApiInterface {
    /* Auth */
    @POST("auth/register")
    @FormUrlEncoded
    fun register(
        @Field("name") name: String,
        @Field("phone") phone: String,
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("password_confirmation") password_confirmation: String,
        @Field("social_id") social_id: String?,
        @Field("social_type") social_type: String?,
        @Field("car_plate_digits") carPlateDigits: String,
        @Field("car_plate_characters") carPlateChars: String,
        @Field("fcm_token") fcm_token: String,
        @Field("image") image: String?,
        @Field("fleet") fliet: String? = "none"
    ): LiveData<GenericApiResponse<GenericResponse<UserModel>>>

    @POST("auth/login")
    @FormUrlEncoded
    fun login(
        @Field("phone") phone: String?,
        @Field("password") password: String?,
        @Field("social_id") social_id: String?,
        @Field("social_type") social_type: String?,
        @Field("fcm_token") fcm_token: String
    ): LiveData<GenericApiResponse<GenericResponse<UserModel>>>

    @POST("auth/verify")
    @FormUrlEncoded
    fun verify(
        @Field("code") code: String?,
        @Field("phone") phone: String?
    ): LiveData<GenericApiResponse<GenericResponse<UserModel>>>

    @POST("auth/resend-code")
    @FormUrlEncoded
    fun resendCode(
        @Field("phone") phone: String,
        @Field("type") type: String
    ): LiveData<GenericApiResponse<GenericResponse<UserModel>>>

//    @POST("auth/logout")
//    @FormUrlEncoded
//    fun logout(
//    ): LiveData<GenericApiResponse<GenericResponse<AuthUser>>>

    @GET("auth/terms-and-conditions")
    fun termsConditions(): LiveData<GenericApiResponse<GenericResponse<TermsConditionModel>>>

    @POST("auth/request-password-reset")
    @FormUrlEncoded
    fun requestPasswordReset(
        @Field("phone") phone: String
    ): LiveData<GenericApiResponse<GenericResponse<UserModel>>>

    @POST("auth/reset-password")
    @FormUrlEncoded
    fun resetPassword(
        @Field("token") token: String?,
        @Field("password") password: String,
        @Field("password_confirmation") passwordConfirmation: String
    ): LiveData<GenericApiResponse<GenericResponse<UserModel>>>

    /**********************/

    /*Profile*/

    @GET("profile")
    fun getProfileDetails(): LiveData<GenericApiResponse<GenericResponse<UserModel>>>

    //    @POST("profile/update")
//    @FormUrlEncoded
//    fun updateProfile(
//        @Field("name") name: String,
//        @Field("phone") phone: String,
//        @Field("email") email: String,
//        @Field("image") image: File?
//    ): LiveData<GenericApiResponse<GenericResponse<UserModel>>>
    @POST("profile/update")
    @Multipart
    fun updateProfile(
        @Part("name") name: RequestBody,
        @Part("phone") phone: RequestBody,
        @Part("email") email: RequestBody,
        @Part("car_plate_digits") carPlateDigits: RequestBody,
        @Part("car_plate_characters") carPlateChars: RequestBody,
        @Part image: MultipartBody.Part?
    ): LiveData<GenericApiResponse<GenericResponse<UserModel>>>

    @POST("profile/password/change")
    @FormUrlEncoded
    fun changePassword(
        @Field("password_old") oldPassword: String,
        @Field("password") password: String,
        @Field("password_confirmation") passwordConfirmation: String
    ): LiveData<GenericApiResponse<GenericResponse<UserModel>>>

    @GET("profile/notifications")
    fun getNotifications(@Query("page") page: Int): LiveData<GenericApiResponse<GenericResponse<NotificationModel>>>

    //TODO profile/unread-notifications-count

    @GET("profile/requests")
    fun getFuelRequests(@Query("page") page: Int): LiveData<GenericApiResponse<GenericResponse<FuelRequestModel>>>

    @POST("profile/cars/add")
    @FormUrlEncoded
    fun addCar(
        @Field("plate_number_digits") plateNumberDigits: String,
        @Field("plate_number_characters") plateNumberCharacters: String
    ): LiveData<GenericApiResponse<GenericResponse<CarModel>>>

    @GET("profile/cars")
    fun getCars(): LiveData<GenericApiResponse<GenericResponse<CarModel>>>

    @POST("profile/cars/delete")
    @FormUrlEncoded
    fun deleteCar(
        @Field("car_id") carId: Int
    ): LiveData<GenericApiResponse<GenericResponse<Int>>>

    /**********************/

    /*Home*/
    @GET("home")
    fun getHomeDetails(): LiveData<GenericApiResponse<GenericResponse<HomeModel>>>

    /**********************/

    /*Contact Us*/
    @POST("contact-us")
    @FormUrlEncoded
    fun contactUs(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("text") text: String
    ): LiveData<GenericApiResponse<GenericResponse<UserModel>>>

    /**********************/

    /*Stations*/
    @GET("stations")
    fun getStations(
        @Query("fuel_ids[]") serviceIds: ArrayList<Int>?,
        @Query("company_ids[]") companyIds: ArrayList<Int>?,
        @Query("service_ids[]") optionsIds: ArrayList<Int>?,
        @Query("distance_min") distance_min: Int?,
        @Query("distance_max") distance_max: Int?,
        @Query("lat") lat: Double?,
        @Query("lng") lng: Double?
    ): LiveData<GenericApiResponse<GenericResponse<StationModel>>>

    @GET("stations/filters")
    fun getStationsFilter(): LiveData<GenericApiResponse<GenericResponse<StationFilterModel>>>

    /*Subscription Options*/
    @GET("stations/list")
    fun getStationsList(
        @Query("lat") lat: Double?,
        @Query("lng") lng: Double?,
        @Query("has_contract") hasContract: Int? = 0
    ): LiveData<GenericApiResponse<GenericResponse<StationModel>>>

    /**********************/

    /*Rewards*/
    @GET("rewards")
    fun getRewards(): LiveData<GenericApiResponse<GenericResponse<RewardsModel>>>

    @POST("rewards/redeem")
    @FormUrlEncoded
    fun redeemCode(
        @Field("reward_id") rewardId: Int
    ): LiveData<GenericApiResponse<GenericResponse<CouponModel>>>

    /************************/

    /*Subscriptions*/
    @GET("plans")
    fun getSubscriptionPlans(): LiveData<GenericApiResponse<GenericResponse<PlanModel>>>

    @POST("plans/subscribe")
    @FormUrlEncoded
    fun subscribe(
        @Field("plan_id") planId: Int,
        @Field("station_ids[]") stationIDs: Array<Int>?
    ): LiveData<GenericApiResponse<GenericResponse<String>>>

    @POST("plans/cancel")
    fun cancelSubscription(): LiveData<GenericApiResponse<GenericResponse<String>>>

    @POST("plans/renew")
    fun renewSubscription(): LiveData<GenericApiResponse<GenericResponse<UserModel.User>>>

    /************************/

    /*Wallet*/
    @GET("profile/myWallet")
    fun getWalletDetails(): LiveData<GenericApiResponse<GenericResponse<WalletModel>>>

    /************************/

    /*Transactions*/
    @GET("profile/myPayments")
    fun getTransactionList(): LiveData<GenericApiResponse<GenericResponse<PaymentModel>>>

    /*AddMoneyToWallet*/
    @GET("profile/addMoneyToWallet")
    fun addMoneyToWallet(
        @Query("orderAmount") orderAmount: String?,
        @Query("merchantRefNumber") merchantRefNumber: String?,
        @Query("statusCode") statusCode: String?
    ): LiveData<GenericApiResponse<GenericResponse<ChargeModel>>>
}