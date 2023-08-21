package com.restart.banzenty.ui.station

import android.os.Bundle
import android.view.View
import com.restart.banzenty.R
import com.restart.banzenty.adapters.StationServicesAdapter
import com.restart.banzenty.base.BaseActivity
import com.restart.banzenty.data.models.StationModel
import com.restart.banzenty.databinding.ActivityStationBinding
import com.restart.banzenty.utils.MainUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StationDetailsActivity : BaseActivity(), View.OnClickListener {

    lateinit var binding: ActivityStationBinding
    private var stationServicesAdapter: StationServicesAdapter? = null
    private var stationDetails: StationModel.Station? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViews()
    }

    private fun initViews() {
        stationDetails = intent.getParcelableExtra("station")
        requestManager.load(stationDetails?.company?.image?.url).into(binding.imageViewStationPic)
        binding.textViewStationName.text = stationDetails?.name
        binding.tvOpening.text = stationDetails?.workingHours
        binding.ivPartner.visibility =
            if (stationDetails?.hasContract == 1) View.VISIBLE else View.GONE
        if ((stationDetails?.distance ?: "0.0").toFloat() < 1) {
            val meters = stationDetails?.distance!!.split(".")
            binding.textViewStationDistance.text = getString(
                R.string.d_meters,
                if (meters[1].length > 3) meters[1].substring(0, 3).removePrefix("0") else meters[1]
            )
        } else {
            binding.textViewStationDistance.text =
                getString(R.string.distance_km, (stationDetails?.distance ?: "0.0").toFloat())
        }

        binding.imageButtonBack.setOnClickListener(this)
        binding.buttonStationDirection.setOnClickListener(this)
        if (stationDetails?.address != null) {
            binding.tvAddress.visibility = View.VISIBLE
            binding.tvAddress.text = stationDetails?.address
        } else {
            binding.tvAddress.visibility = View.INVISIBLE
        }
        binding.recyclerViewAvailableServices.apply {
            stationServicesAdapter = StationServicesAdapter(requestManager)
            stationServicesAdapter?.submitList(stationDetails?.services ?: ArrayList())
//            addOnScrollListener(object : RecyclerView.OnScrollListener() {
//                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                    if (!binding.rvNotifications.canScrollVertically(1))
//                        if (!isSwipLoading) {
//                            if (!viewModel.isQueryExhausted) {
//                                isSwipLoading = true
//                                val currentPage = viewModel.page
//                                getNotifications(currentPage + 1)
//                            }
//                        }
//                }
//            })
            adapter = stationServicesAdapter
        }
        if (stationServicesAdapter?.itemCount == 0)
            binding.tvAvailableServices.visibility = View.GONE
    }

    override fun showProgressBar(show: Boolean) {
//       Not used here
    }

    override fun showErrorUI(
        show: Boolean,
        image: Int?,
        title: String?,
        desc: String?,
        showButton: Boolean?
    ) {
//       Not used here
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.imageButtonBack -> finish()

            binding.buttonStationDirection -> try {
                MainUtils.openGoogleMap(
                    this,
                    stationDetails?.lat ?: 0.0,
                    stationDetails?.lng ?: 0.0
                )
            } catch (e: Exception) {
            }
        }
    }
}