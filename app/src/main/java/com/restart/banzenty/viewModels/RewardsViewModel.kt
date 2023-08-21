package com.restart.banzenty.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.restart.banzenty.data.models.RewardsModel
import com.restart.banzenty.network.DataState
import com.restart.banzenty.repositories.RewardsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RewardsViewModel
@Inject constructor(private val rewardsRepository: RewardsRepository) : ViewModel() {
    var isQueryExhausted: Boolean = false
    var isRewardsDataLoaded: Boolean = false

    fun getRewards(): LiveData<DataState<RewardsModel>> {
        return rewardsRepository.getRewards()
    }

    fun cancelActiveJobs() {
        rewardsRepository.cancelActiveJobs()
    }

    override fun onCleared() {
        super.onCleared()
        rewardsRepository.cancelActiveJobs()
    }
}
