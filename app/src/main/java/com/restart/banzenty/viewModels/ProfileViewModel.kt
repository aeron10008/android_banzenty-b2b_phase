package com.restart.banzenty.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.restart.banzenty.data.models.CarModel
import com.restart.banzenty.data.models.FuelRequestModel
import com.restart.banzenty.data.models.NotificationModel
import com.restart.banzenty.data.models.UserModel
import com.restart.banzenty.network.DataState
import com.restart.banzenty.repositories.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel
@Inject constructor(private val profileRepository: ProfileRepository) : ViewModel() {
    var isQueryExhausted: Boolean = false
    var isProfileDataLoaded: Boolean = false
    var page: Int = 1


    fun getNotifications(page: Int): LiveData<DataState<List<NotificationModel.Notification>>> {
        return profileRepository.getNotifications(page)
    }

    fun getFuelRequests(page: Int): LiveData<DataState<List<FuelRequestModel.FuelRequest>>> {
        return profileRepository.getFuelRequests(page)
    }

    fun getProfileDetails(): LiveData<DataState<UserModel.User>> {
        return profileRepository.getProfileDetails()
    }

    fun changePassword(oldPassword: String, newPassword: String):
            LiveData<DataState<String>> {
        return profileRepository.changePassword(oldPassword, newPassword)
    }

    fun updateProfile(
        name: String,
        phone: String,
        email: String,
        image: File?,
        carPlateDigits: String,
        carPlateCharacters: String
    ): LiveData<DataState<UserModel.User>> {
        return profileRepository.updateProfile(
            name,
            phone,
            email,
            image,
            carPlateDigits,
            carPlateCharacters
        )
    }

    fun addCar(carPlateDigits: String, carPlateCharacters: String): LiveData<DataState<String>> {
        return profileRepository.addCar(carPlateDigits, carPlateCharacters)
    }

    fun getCars(): LiveData<DataState<CarModel>> {
        return profileRepository.getCars()
    }

    fun deleteCar(carId: Int): LiveData<DataState<String>> {
        return profileRepository.deleteCar(carId)
    }

    fun cancelActiveJobs() {
        profileRepository.cancelActiveJobs()
    }

    override fun onCleared() {
        super.onCleared()
        profileRepository.cancelActiveJobs()
    }
}