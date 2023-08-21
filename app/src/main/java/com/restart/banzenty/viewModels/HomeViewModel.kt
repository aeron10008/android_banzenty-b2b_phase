package com.restart.banzenty.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.restart.banzenty.R
import com.restart.banzenty.data.models.HomeModel
import com.restart.banzenty.data.states.ContactUsFormState
import com.restart.banzenty.network.DataState
import com.restart.banzenty.repositories.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel
@Inject constructor(private val homeRepository: HomeRepository) : ViewModel() {
    val contactUsFormState = MutableLiveData<ContactUsFormState>()

    var isHomeDataLoaded: Boolean = false
    var isQueryExhausted: Boolean = false
    var page: Int = 1

    fun getHomeDetails(): LiveData<DataState<HomeModel>> {
        return homeRepository.getHomeDetails()
    }

    fun contactUs(name: String, email: String, text: String):
            LiveData<DataState<String>> {
        return homeRepository.contactUs(name, email, text)
    }

    fun contactUsDataChanged(
        name: String?,
        email: String?,
        message: String?
    ) {
        if (name.isNullOrBlank()) {
            contactUsFormState.value = ContactUsFormState(
                R.string.invalid_name,
                null,
                null
            )
        } else if (email.isNullOrBlank()) {
            contactUsFormState.value = ContactUsFormState(
                null,
                R.string.invalid_email,
                null
            )
        } else if (message.isNullOrBlank()) {
            contactUsFormState.value = ContactUsFormState(
                null,
                null,
                R.string.invalid_message
            )
        } else {
            contactUsFormState.setValue(ContactUsFormState(true))
        }
    }

    fun cancelActiveJobs() {
        homeRepository.cancelActiveJobs()
    }

    override fun onCleared() {
        super.onCleared()
        homeRepository.cancelActiveJobs()
    }
}