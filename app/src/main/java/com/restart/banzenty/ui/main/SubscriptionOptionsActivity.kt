package com.restart.banzenty.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.widget.doOnTextChanged
import com.restart.banzenty.R
import com.restart.banzenty.adapters.CarsRecyclerAdapter
import com.restart.banzenty.adapters.SelectedStationsRecyclerAdapter
import com.restart.banzenty.adapters.ViewStationsAdapter
import com.restart.banzenty.base.BaseActivity
import com.restart.banzenty.data.models.PlanModel
import com.restart.banzenty.data.models.StationModel
import com.restart.banzenty.data.models.UserModel
import com.restart.banzenty.databinding.ActivitySubscriptionOptionsBinding
import com.restart.banzenty.utils.*
import com.restart.banzenty.viewModels.ProfileViewModel
import com.restart.banzenty.viewModels.SubscriptionsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SubscriptionOptionsActivity : BaseActivity(),
    SelectedStationsRecyclerAdapter.OnRemoveItemInterface,
    CarsRecyclerAdapter.OnCarDeletedInterface, View.OnClickListener {
    private var deleteCarPos: Int = -1
    private val TAG = "SubscriptionOptionsActivityLog"

    lateinit var binding: ActivitySubscriptionOptionsBinding

    private lateinit var selectedSubscription: PlanModel.Plan
    private var requestedSubscriptionMessage: String? = ""

    private val profileViewModel: ProfileViewModel by viewModels()
    private val subscriptionsViewModel: SubscriptionsViewModel by viewModels()

    private var stationsList: ArrayList<StationModel.Station> = ArrayList()
    private var selectedStationsList: ArrayList<StationModel.Station> = ArrayList()


    private var carsList: ArrayList<UserModel.UserCarPlate> = ArrayList()
    private var carsRecyclerAdapter: CarsRecyclerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubscriptionOptionsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViews()
        toolbarSetup()
        getCars()
    }

    private fun initViews() {
        val viewStationsAdapter = ViewStationsAdapter(requestManager)
        viewStationsAdapter.submitList(stationsList)
        binding.tvShowStations.setOnClickListener {
            showListDialog(
                getString(R.string.fuel_stations),
                viewStationsAdapter
            )
        }


        if (intent.extras != null) {
            selectedSubscription =
                intent.getParcelableExtra<PlanModel.Plan>("selected_subscription")
                        as PlanModel.Plan

            requestedSubscriptionMessage =
                intent.getStringExtra("requested_subscription_message")
        }

        binding.rvDeleteCar.apply {
            carsRecyclerAdapter = CarsRecyclerAdapter(this@SubscriptionOptionsActivity)
            adapter = carsRecyclerAdapter
        }
        binding.editTextCarNumbers.doOnTextChanged { text, _, _, _ ->
            if(text!!.isNotEmpty())
            {
                val lastChar: Char? = text?.get(text.length -1)
                if (lastChar == '0' ||
                    lastChar == '1'||
                    lastChar == '2'||
                    lastChar == '3'||
                    lastChar == '4'||
                    lastChar == '5'||
                    lastChar == '6'||
                    lastChar == '7'||
                    lastChar == '8'||
                    lastChar == '9')
                {
                    val changeChar = when(lastChar)
                    {
                        '1' ->{'۱'}
                        '2' -> {'۲'}
                        '3' -> {'۳'}
                        '4' -> {'٤'}
                        '5' -> {'٥'}
                        '6' -> {'٦'}
                        '7' -> {'٧'}
                        '8' -> {'۸'}
                        '9' -> {'۹'}
                        else -> {'۰'}
                    }
                    val builder = java.lang.StringBuilder()
                    text.forEachIndexed{ i, c -> if(i != text.length -1) {builder.append(c)} }
                    builder.append(changeChar)
                    val result = builder.toString()
                    binding.editTextCarNumbers.setText(result)
                    binding.editTextCarNumbers.setSelection(result.length)
                }
            }

        }
    }


    private fun toolbarSetup() {
        binding.toolbar.textViewToolbarTitle.text = getString(R.string.subscription_members)
        binding.toolbar.root.setNavigationIcon(R.drawable.back_ic)
        binding.toolbar.root.setNavigationOnClickListener { finish() }
    }

    private fun setDetails() {
        //adding car
        binding.btnAddCar.setOnClickListener {
            binding.apply {
                if (editTextCarNumbers.text.toString().trim().isEmpty()) {
                    editTextCarNumbers.error = getString(R.string.invalid_car_plates)
                    return@apply
                }

                if (etAlp1.text.toString().trim().isEmpty()) {
                    etAlp1.error = getString(R.string.invalid_car_plates)
                    return@apply
                }

                if (etAlp2.text.toString().trim().isEmpty()) {
                    etAlp2.error = getString(R.string.invalid_car_plates)
                    return@apply
                }
                hideSoftKeyboard()
                addCar()
            }
        }
        // subscribe
        binding.btnSubscribe.setOnClickListener(this)
    }

    private fun isStationSelected(selectedItem: String): Boolean {
        for (station in selectedStationsList) {
            Log.d(TAG, "isStationSelected: ${selectedItem}==${station.name}")
            if (selectedItem == station.name) {
                return true
            }
        }
        return false
    }

    private fun subscribe(planId: Int, stationIDs: Array<Int>?) {
        //todo go to payment way and check if successful
        //then call subscribe api
        subscriptionsViewModel.subscribe(planId, null).observe(this) {
            if (it != null) {
                it.error?.getContentIfNotHandled()?.let { errorState ->
                    onErrorStateChange(errorState)
                }
                if (it.loading.isLoading) {
                    binding.btnSubscribe.visibility = View.GONE
                    binding.progressBarSubscribe.visibility = View.VISIBLE
                } else {
                    binding.btnSubscribe.visibility = View.VISIBLE
                    binding.progressBarSubscribe.visibility = View.GONE
                }

                it.data?.data?.getContentIfNotHandled()?.let {
//                    showDialog()
                    displayInfoDialog(requestedSubscriptionMessage ?: "",
                        object : AreYouSureCallback {
                            override fun proceed() {
                                val returnIntent = Intent()
                                setResult(RESULT_OK, returnIntent)
                                finish()
                            }

                            override fun cancel() {
//                            TODO("Not yet implemented")
                            }

                        })

                }
            }
        }
    }

/*
    private fun showDialog() {

        //show subscription completed dialog
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)

        val viewGroup = this.findViewById<ViewGroup>(android.R.id.content)

        val dialogView: View = LayoutInflater.from(this)
            .inflate(R.layout.custom_dialog_view, viewGroup, false)

        val dialogTitle = dialogView.findViewById<TextView>(R.id.textViewDialogTitle)
        dialogTitle.text = getString(R.string.subscription_completed)

        val dialogContent = dialogView.findViewById<TextView>(R.id.textViewDialogContent)
        dialogContent.text = getString(R.string.subscription_completed_msg)

        builder.setView(dialogView)

        val alertDialog: AlertDialog = builder.create()

        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        alertDialog.show()

        val doneButton = dialogView.findViewById<Button>(R.id.buttonDone)

        doneButton.setOnClickListener {
            sessionManager.setIsUserSubscribed(true)
            val returnIntent = Intent()
//            returnIntent.putExtra("active_subscription_id", selectedSubscription.id)
            setResult(RESULT_OK, returnIntent)
            alertDialog.dismiss()
            finish()
        }
    }
*/


    private fun addCar() {
        profileViewModel.addCar(
            binding.editTextCarNumbers.text.toString().trim(),
            "${binding.etAlp1.text.toString().trim()}${
                binding.etAlp2.text.toString().trim()
            }${binding.etAlp3.text.toString().trim()}",
        ).observe(this) {
            if (it != null) {
                it.error?.getContentIfNotHandled()?.let { errorState ->
                    onErrorStateChange(errorState)
                }
                if (it.loading.isLoading) {
                    binding.btnAddCar.visibility = View.GONE
                    binding.progressBarAddCar.visibility = View.VISIBLE
                } else {
                    binding.btnAddCar.visibility = View.VISIBLE
                    binding.progressBarAddCar.visibility = View.GONE
                }
                it.data?.data?.getContentIfNotHandled()?.let {
                    Toast.makeText(this, getString(R.string.car_added), Toast.LENGTH_LONG).show()
                    binding.editTextCarNumbers.text.clear()
                    binding.etAlp1.text?.clear()
                    binding.etAlp2.text?.clear()
                    binding.etAlp3.text?.clear()
                    getCars()
                }
            }
        }
    }

    private fun getCars() {
        Log.d(TAG, "getCars: Called")
        profileViewModel.getCars().observe(this) {
            if (it != null) {
                showProgressBar(it.loading.isLoading)
                it.error?.getContentIfNotHandled()?.let { errorState ->
                    //check if internet connection is lost
                    Log.d(TAG, "getCars: $errorState")
                    if (errorState.response.message == Constants.UNABLE_TODO_OPERATION_WO_INTERNET
                        || errorState.response.message == Constants.UNABLE_TO_RESOLVE_HOST
                    ) {
                        showErrorUI(
                            show = true, title = getString(R.string.no_internet),
                            desc = null, image = R.drawable.ic_no_internet, showButton = true
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

                it.data?.data?.getContentIfNotHandled()?.let { carModel ->
                    if (carModel.cars.isNotEmpty()) {
                        setDetails()
                        carsList.clear()
                        carsList.addAll(carModel.cars)

                        carsRecyclerAdapter?.submitList(carsList.toList())

                        if (carsList.isEmpty()) {
                            //show error UI
                            showErrorUI(
                                show = true, title = getString(R.string.no_data), desc = null,
                                image = R.drawable.ic_no_data, showButton = false
                            )
                        } else {
                            //hide error UI
                            showErrorUI(false)
                        }
                    } else {
                        if (carsList.isEmpty()) {
                            //show error UI
                            showErrorUI(
                                show = true, title = getString(R.string.no_data), desc = null,
                                image = R.drawable.ic_no_data, showButton = false
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

    private fun deleteCar(carId: Int) {
        profileViewModel.deleteCar(carId).observe(this) {
            if (it != null) {
                it.error?.getContentIfNotHandled()?.let { errorState ->
                    onErrorStateChange(errorState)
                }
                carsList[deleteCarPos].isDeleting = it.loading.isLoading
                carsRecyclerAdapter?.notifyItemChanged(deleteCarPos)
                it.data?.data?.getContentIfNotHandled()?.let {
                    if (deleteCarPos != -1) {
                        carsList.removeAt(deleteCarPos)
                        carsRecyclerAdapter?.submitList(carsList.toList())
                        deleteCarPos = -1
                        displayToast(getString(R.string.deleted_successfully))
                    }
                }
            }
        }
    }

/*
    override fun onOtpCompleted(otp: String?) {
        carAlphabets = otp!!
    }
*/

    override fun showProgressBar(show: Boolean) {
        if (show) {
            showErrorUI(false)
            binding.shimmer.visibility = View.VISIBLE
            binding.parent.visibility = View.GONE
            binding.btnSubscribe.visibility = View.GONE
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
            } else binding.errorLayout.btnRetry.visibility = View.GONE

            binding.errorLayout.btnRetry.setOnClickListener {
                Log.d(TAG, "showErrorUI: btnRetryClicked")
                getCars()
            }
        } else {
            binding.parent.visibility = View.VISIBLE
            binding.btnSubscribe.visibility = View.VISIBLE
            binding.errorLayout.root.visibility = View.GONE
        }
    }

    override fun onItemRemoved(pos: Int) {
//        binding.rvSelectedStations.apply {
//            selectedStationsList.removeAt(pos)
//            selectedStationsNamesList.removeAt(pos)
//            selectedStationsAdapter?.submitList(selectedStationsList.toList())
//        }
    }

    override fun onCarDeleted(pos: Int) {
        binding.rvDeleteCar.apply {
            deleteCarPos = pos
            val carID = carsList[pos].id
            areYouSureDialog(getString(R.string.you_want_to_delete_car).replace(
                "xxx",
                "${carsList[pos].plateNumberCharacters} - ${carsList[pos].plateNumberDigits}"
            ), object : AreYouSureCallback {
                override fun proceed() {
                    deleteCar(carID)
                }

                override fun cancel() {
                }

            })
        }
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.btnSubscribe -> {
                areYouSureDialog(getString(R.string.subscription_confrimation_message),
                    object : AreYouSureCallback {
                        override fun proceed() {
                            subscribe(selectedSubscription.id, null)
                        }

                        override fun cancel() {
//                   TODO("Not yet implemented")
                        }
                    })
            }
        }
    }

}