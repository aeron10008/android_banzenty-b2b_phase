package com.restart.banzenty.ui.station

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import com.mohammedalaa.seekbar.OnRangeSeekBarChangeListener
import com.mohammedalaa.seekbar.RangeSeekBarView
import com.restart.banzenty.R
import com.restart.banzenty.adapters.CompanyFilterAdapter
import com.restart.banzenty.adapters.FuelFilterAdapter
import com.restart.banzenty.adapters.ServicesFilterAdapter
import com.restart.banzenty.base.BaseActivity
import com.restart.banzenty.data.models.PlanModel
import com.restart.banzenty.data.models.StationModel
import com.restart.banzenty.data.models.StationService
import com.restart.banzenty.databinding.ActivityFilterBinding
import com.restart.banzenty.utils.Constants
import com.restart.banzenty.viewModels.StationViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StationFilterActivity : BaseActivity(), View.OnClickListener,
    FuelFilterAdapter.OnItemClickedInterface, CompanyFilterAdapter.OnItemClickedInterface,
    ServicesFilterAdapter.OnItemClickedInterface {

    private var TAG = "StationFilterActivity"
    private var fuelFilterAdapter: FuelFilterAdapter? = null
    private var companyFilterAdapter: CompanyFilterAdapter? = null
    private var servicesFilterAdapter: ServicesFilterAdapter? = null

    lateinit var binding: ActivityFilterBinding

    private val viewModel: StationViewModel by viewModels()

    var fuelTypesList: ArrayList<PlanModel.Plan.Fuel> = ArrayList()
    var companiesList: ArrayList<StationModel.Station.StationCompany> = ArrayList()
    var servicesList: ArrayList<StationService> = ArrayList()

    //    var minDistance: Int? = null
    var distance: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFilterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initToolbar()
        initViews()
        getStationFilters()
    }

    private fun initToolbar() {
        binding.toolbar.textViewToolbarTitle.text = getString(R.string.filter)
        binding.toolbar.root.setNavigationIcon(R.drawable.back_ic)
        binding.toolbar.root.setNavigationOnClickListener { finish() }
    }

    private fun initViews() {

        binding.buttonApply.setOnClickListener(this)
        binding.servicesRecyclerView.apply {
            fuelFilterAdapter = FuelFilterAdapter(this@StationFilterActivity)
            adapter = fuelFilterAdapter
        }

        binding.companiesRecyclerView.apply {
            companyFilterAdapter = CompanyFilterAdapter(this@StationFilterActivity)
            adapter = companyFilterAdapter
        }

        binding.otherOptionsRecyclerView.apply {
            servicesFilterAdapter = ServicesFilterAdapter(this@StationFilterActivity)
            adapter = servicesFilterAdapter
        }

        binding.rangeSeekBar.setOnRangeSeekBarViewChangeListener(object :
            OnRangeSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: RangeSeekBarView?,
                progress: Int,
                fromUser: Boolean
            ) {
                distance = progress
//                binding.textViewMaxDistance.text =
//                    getString(R.string.distance_km, 25)
                binding.textViewMinDistance.text =
                    getString(R.string.distance_km, distance!!.toFloat())
            }

            override fun onStartTrackingTouch(seekBar: RangeSeekBarView?, progress: Int) {
            }

            override fun onStopTrackingTouch(seekBar: RangeSeekBarView?, progress: Int) {

            }
        })
    }

    private fun getStationFilters() {
        viewModel.getStationsFilter().observe(this) {
            if (it != null) {
                it.error?.getContentIfNotHandled()?.let { errorState ->
                    //check if internet connection is lost
                    if (errorState.response.message == Constants.UNABLE_TODO_OPERATION_WO_INTERNET
                        || errorState.response.message == Constants.UNABLE_TO_RESOLVE_HOST
                    ) {
                        showErrorUI(
                            show = true,
                            title = getString(R.string.no_internet),
                            desc = null,
                            image = R.drawable.ic_no_internet,
                            showButton = true
                        )
                    } else {
                        showErrorUI(
                            show = true,
                            title = getString(R.string.something_went_wrong),
                            desc = null,
                            image = R.drawable.ic_something_wrong,
                            showButton = true
                        )
                    }
                }

                showProgressBar(it.loading.isLoading)

                it.data?.data?.getContentIfNotHandled()?.let { filters ->
                    fuelTypesList.clear()
                    companiesList.clear()
                    servicesList.clear()

                    fuelTypesList.addAll(filters.fuelTypes ?: arrayListOf())
                    for (item in fuelTypesList) {
                        Log.i(TAG, "getStationFilters: services = " + item.name)
                    }

                    companiesList.addAll(filters.companies)
                    for (item in companiesList) {
                        Log.i(TAG, "getStationFilters: companies = " + item.name)
                    }

                    servicesList.addAll(filters.services)
                    for (item in servicesList) {
                        Log.i(TAG, "getStationFilters: other options = " + item.name)
                    }

                    setOldFilterData()

//                    serviceFilterAdapter?.submitList(servicesList.toList())
//                    companyFilterAdapter?.submitList(companiesList.toList())
//                    otherOptionsFilterAdapter?.submitList(otherOptionsList.toList())

                    if (fuelTypesList.isEmpty())
                        binding.servicesLayout.visibility = View.GONE
                    else
                        binding.servicesLayout.visibility = View.VISIBLE

                    Log.i(TAG, "getStationFilters: service list size = ${fuelTypesList.size}")

                    if (companiesList.isEmpty())
                        binding.stationsLayout.visibility = View.GONE
                    else
                        binding.stationsLayout.visibility = View.VISIBLE

                    Log.i(TAG, "getStationFilters: companies list size = ${companiesList.size}")

                    if (servicesList.isEmpty())
                        binding.otherOptionsLayout.visibility = View.GONE
                    else
                        binding.otherOptionsLayout.visibility = View.VISIBLE

                    Log.i(
                        TAG,
                        "getStationFilters: other options list size = ${servicesList.size}"
                    )
                    if (fuelTypesList.isEmpty() && companiesList.isEmpty() && servicesList.isEmpty()) {
                        //show error UI
                        showErrorUI(
                            show = true,
                            title = getString(R.string.no_data),
                            desc = null,
                            image = R.drawable.ic_no_data,
                            showButton = false
                        )
                    } else {
                        //hide error UI
                        showErrorUI(false)
                    }
                }
            }
        }
    }

    private fun setOldFilterData() {
        val oldSelectedStationsIds =
            intent.extras?.getIntegerArrayList("selectedStationsIds") ?: arrayListOf()

        for (oldStationsId in oldSelectedStationsIds) {
            for (station in companiesList)
                if (station.id == oldStationsId)
                    station.isChecked = true
        }
        companyFilterAdapter?.submitList(companiesList.toList())

        val oldSelectedFuelTypeIds =
            intent.extras?.getIntegerArrayList("selectedFuelTypeIds") ?: arrayListOf()

        for (fuelTypeId in oldSelectedFuelTypeIds) {
            for (fuelType in fuelTypesList)
                if (fuelType.id == fuelTypeId)
                    fuelType.isChecked = true
        }
        fuelFilterAdapter?.submitList(fuelTypesList.toList())

        val oldSelectedServiceIds =
            intent.extras?.getIntegerArrayList("selectedServicesIds") ?: arrayListOf()

        for (oldOptionId in oldSelectedServiceIds) {
            for (option in servicesList)
                if (option.id == oldOptionId)
                    option.isChecked = true
        }
        servicesFilterAdapter?.submitList(servicesList.toList())


        if (intent.extras?.containsKey("distance") == true) {
            distance = intent.extras?.getInt("distance")
            binding.rangeSeekBar.currentValue = distance!!
        } else distance = null
/*        distance =
            if (intent.extras?.containsKey("distance") == true) intent.extras?.getInt(
                "distance"
            ) else null*/

/*        if (intent.extras?.containsKey("minDistance") == true) {
            minDistance = intent.extras?.getInt("minDistance")
            binding.rangeSeekBar.currentValue = minDistance!!
        }*/


    }

    override fun showProgressBar(show: Boolean) {
        if (show) {
            showErrorUI(false)
            binding.servicesLayout.visibility = View.GONE
            binding.stationsLayout.visibility = View.GONE
            binding.distanceLayout.visibility = View.GONE
            binding.otherOptionsLayout.visibility = View.GONE
            binding.buttonApply.visibility = View.GONE
            binding.shimmer.visibility = View.VISIBLE
        } else {
            binding.servicesLayout.visibility = View.VISIBLE
            binding.stationsLayout.visibility = View.VISIBLE
            binding.distanceLayout.visibility = View.VISIBLE
            binding.otherOptionsLayout.visibility = View.VISIBLE
            binding.buttonApply.visibility = View.VISIBLE
            binding.shimmer.visibility = View.GONE
        }
    }

    override fun showErrorUI(
        show: Boolean, image: Int?, title: String?, desc: String?,
        showButton: Boolean?
    ) {
        if (show) {
            binding.servicesLayout.visibility = View.GONE
            binding.distanceLayout.visibility = View.GONE
            binding.stationsLayout.visibility = View.GONE
            binding.otherOptionsLayout.visibility = View.GONE
            binding.errorLayout.tvDesc.visibility = View.GONE
            binding.errorLayout.root.visibility = View.VISIBLE
            binding.errorLayout.tvTitle.text = title
            binding.errorLayout.ivPic.setBackgroundResource(image!!)
            desc?.let {
                binding.errorLayout.tvDesc.visibility = View.VISIBLE
                binding.errorLayout.tvDesc.text = it
            }
            binding.errorLayout.tvTitle.text = title

            if (showButton == true) {
                binding.errorLayout.btnRetry.visibility = View.VISIBLE
                binding.errorLayout.btnRetry.setOnClickListener { getStationFilters() }
            } else binding.errorLayout.btnRetry.visibility = View.GONE

        } else {
            binding.servicesLayout.visibility = View.VISIBLE
            binding.distanceLayout.visibility = View.VISIBLE
            binding.stationsLayout.visibility = View.VISIBLE
            binding.otherOptionsLayout.visibility = View.VISIBLE
            binding.errorLayout.root.visibility = View.GONE
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.buttonApply -> {
                //TODO
                /*
                *for loop on services
                * create arraylist of the checked service ids
                 */

                val selectedStationsIds = ArrayList<Int>()
                val selectedFuelTypeIds = ArrayList<Int>()
                val selectedServiceIds = ArrayList<Int>()

                for (station in companiesList)
                    if (station.isChecked) selectedStationsIds.add(station.id)
                for (service in fuelTypesList)
                    if (service.isChecked) selectedFuelTypeIds.add(service.id)
                for (option in servicesList)
                    if (option.isChecked) selectedServiceIds.add(option.id)
                val returnIntent = Intent()

                if (selectedStationsIds.isNotEmpty())
                    returnIntent.putExtra(
                        "selectedStationsIds",
                        selectedStationsIds
                    )
                if (selectedFuelTypeIds.isNotEmpty())
                    returnIntent.putExtra(
                        "selectedFuelTypeIds",
                        selectedFuelTypeIds
                    )
                if (selectedServiceIds.isNotEmpty())
                    returnIntent.putExtra("selectedServicesIds", selectedServiceIds)

//                if (minDistance != null)
//                    returnIntent.putExtra("minDistance", minDistance)
                if (distance != null)
                    returnIntent.putExtra("distance", distance)

                setResult(RESULT_OK, returnIntent)
                finish()
            }
        }
    }

    override fun onServiceItemClicked(pos: Int) {
        fuelTypesList[pos].isChecked = !fuelTypesList[pos].isChecked
        fuelFilterAdapter?.submitList(fuelTypesList.toList())
        fuelFilterAdapter?.notifyItemChanged(pos)
    }

    override fun onCompanyItemClicked(pos: Int) {
        companiesList[pos].isChecked = !companiesList[pos].isChecked
        companyFilterAdapter?.submitList(companiesList.toList())
        companyFilterAdapter?.notifyItemChanged(pos)
    }

    override fun onOtherOptionsItemClicked(pos: Int) {
        servicesList[pos].isChecked = !servicesList[pos].isChecked
        servicesFilterAdapter?.submitList(servicesList.toList())
        servicesFilterAdapter?.notifyItemChanged(pos)
    }
}