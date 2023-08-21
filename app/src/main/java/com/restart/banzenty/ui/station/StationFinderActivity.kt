package com.restart.banzenty.ui.station

import android.Manifest
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.HandlerThread
import android.util.Log
import android.view.View
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import com.birjuvachhani.locus.Locus
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.android.gms.tasks.Task
import com.restart.banzenty.R
import com.restart.banzenty.adapters.StationAdapter
import com.restart.banzenty.base.BaseActivity
import com.restart.banzenty.data.models.StationModel
import com.restart.banzenty.databinding.ActivityStationFinderBinding
import com.restart.banzenty.utils.Constants
import com.restart.banzenty.utils.MainUtils
import com.restart.banzenty.viewModels.StationViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class StationFinderActivity : BaseActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private val TAG = "StationFinderActivity"
    private lateinit var binding: ActivityStationFinderBinding

    private val viewModel: StationViewModel by viewModels()

    private lateinit var googleMap: GoogleMap
    private lateinit var mapFragment: SupportMapFragment
    private var currentLocation: Location? = null
    private var locationCallback: LocationCallback? = null
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private lateinit var locationRequest: LocationRequest
    private lateinit var handlerThread: HandlerThread
    private var isPermissionGranted = false
    private var isGpsEnabled = false
    private var stationAdapter: StationAdapter? = null

    var stationList: ArrayList<StationModel.Station> = ArrayList()
    var currentMarkers: ArrayList<Marker> = ArrayList()

    var selectedStationsIds: ArrayList<Int>? = null
    var selectedFuelTypeIds: ArrayList<Int>? = null
    var selectedServicesIds: ArrayList<Int>? = null
    var distance: Int? = null

    //    var minDistance: Int? = null
    private val openFilterActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            run {
                if (it.resultCode == RESULT_OK) {
                    //Todo call filter api again
                    selectedStationsIds =
                        it.data?.extras?.getIntegerArrayList("selectedStationsIds")

                    selectedServicesIds =
                        it.data?.extras?.getIntegerArrayList("selectedServicesIds")

                    selectedFuelTypeIds =
                        it.data?.extras?.getIntegerArrayList("selectedFuelTypeIds")

                    distance =
                        if (it.data?.extras?.containsKey("distance") == true) it.data?.extras?.getInt(
                            "distance"
                        ) else null

/*                    minDistance =
                        if (it.data?.extras?.containsKey("minDistance") == true) it.data?.extras?.getInt(
                            "minDistance"
                        ) else null*/

                    getStations()
                }
            }
        }

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.entries.all {
                it.value
            }
            if (granted) {
                isPermissionGranted = true
                initGPSSettings()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStationFinderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initToolbar()
        setUpMap()
        initLocation()
        initViews()
    }


    private fun setupLocation() {
        Locus.getCurrentLocation(this) { result ->
            result.location?.let {

//                Received location update

                currentLocation = it
                runOnUiThread {
                    mapFragment.getMapAsync(this)
                }
            }
            result.error?.let {
//                Received error !
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
//        Locus.stopLocationUpdates()
    }

    private fun initToolbar() {
        binding.toolbar.root.inflateMenu(R.menu.toolbar_menu)
        binding.toolbar.root.setNavigationIcon(R.drawable.back_ic)
        binding.toolbar.root.setNavigationOnClickListener { finish() }
        binding.toolbar.textViewToolbarTitle.text = getString(R.string.station_finder)
        //hide notification icon from toolbar and show filter icon instead
        binding.toolbar.root.menu.findItem(R.id.action_notification).isVisible = false
        binding.toolbar.root.menu.findItem(R.id.action_filter).isVisible = true

        //set on menu item click listener
        binding.toolbar.root.setOnMenuItemClickListener {
            if (it.itemId == R.id.action_filter) {
                // start activity for result
                val stationIntent = Intent(this, StationFilterActivity::class.java)
                stationIntent.putExtra("selectedStationsIds", selectedStationsIds)
                stationIntent.putExtra("selectedFuelTypeIds", selectedFuelTypeIds)
                stationIntent.putExtra("selectedServicesIds", selectedServicesIds)
//                if (minDistance != null)
//                    stationIntent.putExtra("minDistance", minDistance)
                if (distance != null)
                    stationIntent.putExtra("distance", distance)

                openFilterActivity.launch(stationIntent)
            }
            true
        }
    }

    private fun initViews() {
        binding.recyclerViewNearbyStations.apply {
            stationAdapter = StationAdapter(requestManager)

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
            adapter = stationAdapter
        }
    }

    private fun setUpMap() {
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
    }

    private fun initLocation() {
        createLocationRequest()
        setUpFusedLocationClient()
        checkLocationPermission()
    }

    private fun setUpFusedLocationClient() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    protected fun createLocationRequest() {
        locationRequest = LocationRequest.create()
//        locationRequest.interval = 2000
//        locationRequest.fastestInterval = 2000/2
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    private fun checkLocationPermission() {
        Log.d(TAG, "checkLocationPermission: ")
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            isPermissionGranted = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestMultiplePermissions.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        } else {
            isPermissionGranted = true
            val manager =
                getSystemService(LOCATION_SERVICE) as LocationManager
            isGpsEnabled = manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            initGPSSettings()
        }
    }


    private fun getCurrentLocation() {
        showProgressBar(true)
        handlerThread = HandlerThread("RequestLocation")
        handlerThread.start()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mFusedLocationClient?.getCurrentLocation(
                LocationRequest.PRIORITY_HIGH_ACCURACY,
                object : CancellationToken() {
                    override fun onCanceledRequested(p0: OnTokenCanceledListener) =
                        CancellationTokenSource().token

                    override fun isCancellationRequested() = false
                })?.addOnSuccessListener { location: Location? ->
                if (location == null)
                    Log.d(TAG, "getLastLocation: location")
                else {
                    setLocation(location)
                }

            }

        }
        /*   mFusedLocationClient?.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY,object : CancellationToken(){
               override fun onCanceledRequested(p0: OnTokenCanceledListener): CancellationToken {
                   Log.d(TAG, "onCanceledRequested: ")
               }

               override fun isCancellationRequested(): Boolean {
                   Log.d(TAG, "isCancellationRequested: ")
               }

           })?.addOnCompleteListener { v ->
               if (v.result == null) {
                   mFusedLocationClient!!.requestLocationUpdates(
                       locationRequest,
                       object : LocationCallback() {
                           override fun onLocationResult(locationResult: LocationResult) {
                               Log.d(TAG, "onLocationResult: ${locationResult.lastLocation}")
                               setLocation(locationResult.lastLocation)
                           }
                       }.also { locationCallback = it },
                       handlerThread.looper
                   )
               } else setLocation(v.result)

           }*/

    }


    private fun initGPSSettings() {
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        val task = LocationServices.getSettingsClient(this)
            .checkLocationSettings(builder.build())
        task.addOnCompleteListener { task1: Task<LocationSettingsResponse?> ->
            try {
                val response =
                    task1.getResult(ApiException::class.java)
                Log.d(TAG, "initGPSSettings: Call get last location if gps already enabled")
//                getCurrentLocation()
                setupLocation()
            } catch (exception: ApiException) {
                when (exception.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->
                        // Location settings are not satisfied. But could be fixed by showing the
                        // user a dialog.
                        try {
                            // Cast to a resolvable exception.
                            val resolvable = exception as ResolvableApiException
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            try {
                                val intentSenderRequest =
                                    IntentSenderRequest.Builder(resolvable.resolution.intentSender)
                                        .build()
                                locationIntentResultLauncher.launch(intentSenderRequest)
                            } catch (e: IntentSender.SendIntentException) {
                                e.printStackTrace()
                            }


//                or for activity
//                            resolvable.startResolutionForResult(
//                                this,
//                                SelectLocationActivity.REQUEST_CHECK_SETTINGS
//                            )
                        } catch (e: IntentSender.SendIntentException) {
                            // Ignore the error.
                        } catch (e: ClassCastException) {
                            // Ignore, should be an impossible error.
                        }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                    }
                }
            }
        }
    }

    private val locationIntentResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result != null) {
                isGpsEnabled = true
                Log.d(TAG, "Call get Last location if gps enabled: ")
                getCurrentLocation()
                setupLocation()
            }
        }

    private fun setLocation(location: Location?) {
        Log.d(TAG, "setLocation: $location")
        if (currentLocation == null) {
            try {
                handlerThread.quitSafely()
                mFusedLocationClient?.removeLocationUpdates(locationCallback!!)
            } catch (e: Exception) {
            }
            currentLocation = location
            runOnUiThread {
                mapFragment.getMapAsync(this)
            }
        }
    }

    private fun getStations() {
        viewModel.getStations(
            selectedFuelTypeIds,
            selectedStationsIds,
            selectedServicesIds,
            0,
            if (distance != null && distance!! > 1) distance else null,
            currentLocation?.latitude ?: 0.0,
            currentLocation?.longitude ?: 0.0
        ).observe(this) {
            if (it != null) {
                it.error?.getContentIfNotHandled()?.let { errorState ->
                    //check if internet connection is lost
                    if (stationList.isEmpty())
                        if (errorState.response.message == Constants.UNABLE_TODO_OPERATION_WO_INTERNET || errorState.response.message == Constants.UNABLE_TO_RESOLVE_HOST)
                            showErrorUI(
                                show = true,
                                title = getString(R.string.no_internet),
                                desc = null,
                                image = R.drawable.ic_no_internet,
                                showButton = true
                            ) else
                            showErrorUI(
                                show = true,
                                title = getString(R.string.something_went_wrong),
                                desc = null,
                                image = R.drawable.ic_something_wrong,
                                showButton = true
                            )
                    else onErrorStateChange(errorState)
                }

                showProgressBar(it.loading.isLoading)

                it.data?.data?.getContentIfNotHandled()?.let { stations ->
                    if (stations.isNotEmpty()) {
                        stationList.clear()

                        stationList.addAll(stations)

                        stationAdapter?.apply {
                            preloadGlideImages(
                                requestManager,
                                stationList.toList()
                            )
                        }
                        stationAdapter?.submitList(stationList.toList())

                        viewModel.page =
                            stationList.size / Constants.PAGINATION_PAGE_SIZE

                        Log.d(TAG, "page: ${viewModel.page}")
                        viewModel.isQueryExhausted = stations.size < 50

                        if (stationList.isEmpty()) {
                            //show error UI
                            showErrorUI(
                                show = true, title = getString(R.string.no_data), desc = null,
                                image = R.drawable.ic_no_notifications, showButton = false
                            )
                        } else {
                            //hide error UI
                            showErrorUI(false)
                        }
                        showStationsOnMap()
                    } else {
                        stationList.clear()
                        stationAdapter?.submitList(stationList.toList())
                        if (stationList.isEmpty()) {
                            //show error UI
                            showErrorUI(
                                show = true, title = getString(R.string.no_data), desc = null,
                                image = R.drawable.ic_no_notifications, showButton = false
                            )
                        } else {
                            //hide error UI
                            showErrorUI(false)
                        }
                    }
                }
            }
        }
    }


    override fun showProgressBar(show: Boolean) {
        if (show) {
            showErrorUI(false)
            binding.shimmer.visibility = View.VISIBLE
        } else {
            binding.shimmer.visibility = View.GONE
        }
    }

    override fun showErrorUI(
        show: Boolean,
        image: Int?,
        title: String?,
        desc: String?,
        showButton: Boolean?
    ) {
        if (show) {
            binding.recyclerViewNearbyStations.visibility = View.GONE
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
                binding.errorLayout.btnRetry.setOnClickListener { getStations() }
            } else binding.errorLayout.btnRetry.visibility = View.GONE

        } else {
            binding.recyclerViewNearbyStations.visibility = View.VISIBLE
            binding.errorLayout.root.visibility = View.GONE
        }
    }


    override fun onMapReady(googleMap: GoogleMap) {
        Log.d(TAG, "onMapReady: ")
        this.googleMap = googleMap
        googleMap.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    currentLocation?.latitude ?: 0.0,
                    currentLocation?.longitude ?: 0.0
                ), 17f
            )
        )
        googleMap.addMarker(
            MarkerOptions()
                .icon(
                    BitmapDescriptorFactory.fromBitmap(
                        MainUtils.getCustomizedMarker(
                            this,
                            R.drawable.ic_marker
                        )!!
                    )
                )
                .title(getString(R.string.your_location))
                .position(
                    LatLng(
                        currentLocation?.latitude ?: 0.0,
                        currentLocation?.longitude ?: 0.0
                    )
                )
        )
        getStations()
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        binding.recyclerViewNearbyStations.smoothScrollToPosition(getCurrentStationIndex(marker.title.toString()))
        return false
    }

    private fun showStationsOnMap() {
        if (currentMarkers.isNotEmpty())
            for (marker in currentMarkers)
                try {
                    marker.remove()
                } catch (e: Exception) {
                }
        currentMarkers.clear()
        googleMap.setOnMarkerClickListener(this)
        for (station in stationList) {
            Glide.with(this)
                .asBitmap()
                .load(station.company?.image?.url)
                .transform(CenterCrop(), CircleCrop())
                .into(object : CustomTarget<Bitmap>(150, 150) {
                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        super.onLoadFailed(errorDrawable)
                        Log.d(TAG, "onLoadFailed: ")
                    }

                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap?>?
                    ) {
                        Log.d(TAG, "onResourceReady: ")
                        currentMarkers.add(
                            googleMap.addMarker(
                                MarkerOptions()
                                    .icon(
                                        BitmapDescriptorFactory.fromBitmap(
                                            resource
                                        )
                                    )
                                    .title(station.name)
                                    .position(
                                        LatLng(
                                            station.lat,
                                            station.lng
                                        )
                                    )
                            )!!
                        )
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        currentMarkers.add(
                            googleMap.addMarker(
                                MarkerOptions()
                                    .icon(
                                        BitmapDescriptorFactory.fromBitmap(
                                            MainUtils.getCustomizedMarker(
                                                this@StationFinderActivity,
                                                R.drawable.ic_fuel_map
                                            )!!
                                        )
                                    )
                                    .title(station.name)
                                    .position(
                                        LatLng(
                                            station.lat,
                                            station.lng
                                        )
                                    )
                            )!!
                        )
                    }
                })
        }

    }

    private fun getCurrentStationIndex(stationTitle: String): Int {
        for (i in stationList.indices) {
            if (stationList[i].name.toString().trim() == stationTitle.trim())
                return i
        }
        return 0
    }
}