package com.restart.banzenty.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.restart.banzenty.data.models.HomeModel
import com.restart.banzenty.data.models.PlanModel
import com.restart.banzenty.data.models.UserModel
import com.restart.banzenty.network.DataState
import com.restart.banzenty.repositories.SubscriptionsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SubscriptionsViewModel
@Inject constructor(private val subscriptionsRepository: SubscriptionsRepository) : ViewModel() {
    var isQueryExhausted: Boolean = false
    var page: Int = 1

    fun getSubscriptionPlans(): LiveData<DataState<PlanModel>> {
        return subscriptionsRepository.getSubscriptionPlans()
    }

    fun subscribe(planId: Int, stationIDs: Array<Int>?): LiveData<DataState<String>> {
        return subscriptionsRepository.subscribe(planId, stationIDs)
    }

    fun cancelSubscription(): LiveData<DataState<String>> {
        return subscriptionsRepository.cancelSubscription()
    }

    fun renewSubscription(): LiveData<DataState<String>> {
        return subscriptionsRepository.renewSubscription()
    }
    fun getMySubscription(): LiveData<DataState<HomeModel>> {
        return subscriptionsRepository.getMySubscription()
    }
    fun cancelActiveJobs() {
        subscriptionsRepository.cancelActiveJobs()
    }

    override fun onCleared() {
        super.onCleared()
        subscriptionsRepository.cancelActiveJobs()
    }
}