package com.restart.banzenty.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.restart.banzenty.data.models.StationFilterModel
import com.restart.banzenty.data.models.StationModel
import com.restart.banzenty.network.DataState
import com.restart.banzenty.repositories.StationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StationViewModel
@Inject constructor(private val stationRepository: StationRepository) : ViewModel() {
    var isQueryExhausted: Boolean = false
    var page: Int = 1


    fun getStations(
        fuelTypeIds: ArrayList<Int>?,
        companyIds: ArrayList<Int>?,
        serviceIds: ArrayList<Int>?,
        distance_min: Int?,
        distance_max: Int?,
        lat: Double?,
        lng: Double?
    ): LiveData<DataState<List<StationModel.Station>>> {
        return stationRepository.getStations(
            fuelTypeIds,
            companyIds,
            serviceIds,
            distance_min,
            distance_max,
            lat,
            lng
        )
    }

    fun getStationsFilter(): LiveData<DataState<StationFilterModel>> {
        return stationRepository.getStationFilters()
    }

    fun getStationsList(
        lat: Double?,
        lng: Double?,
        hasContract: Int
    ): LiveData<DataState<List<StationModel.Station>>> {
        return stationRepository.getStationsList(lat, lng, hasContract)
    }

    fun cancelActiveJobs() {
        stationRepository.cancelActiveJobs()
    }

    override fun onCleared() {
        super.onCleared()
        stationRepository.cancelActiveJobs()
    }
}