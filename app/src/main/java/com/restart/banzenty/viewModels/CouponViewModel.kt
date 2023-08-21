package com.restart.banzenty.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.restart.banzenty.data.models.CouponModel
import com.restart.banzenty.network.DataState
import com.restart.banzenty.repositories.RewardsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CouponViewModel
@Inject constructor(private val rewardsRepository: RewardsRepository) : ViewModel() {
    var isQueryExhausted: Boolean = false
    var isCouponDataLoaded: Boolean = false

    fun redeemCode(rewardId: Int): LiveData<DataState<CouponModel>> {
        return rewardsRepository.redeemCode(rewardId)
    }

    fun cancelActiveJobs() {
        rewardsRepository.cancelActiveJobs()
    }

    override fun onCleared() {
        super.onCleared()
        rewardsRepository.cancelActiveJobs()
    }
}