package com.restart.banzenty.ui.main

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.fawry.fawrypay.FawrySdk
import com.fawry.fawrypay.interfaces.FawryPreLaunch
import com.fawry.fawrypay.interfaces.FawrySdkCallbacks
import com.fawry.fawrypay.models.BillItems
import com.fawry.fawrypay.models.FawryLaunchModel
import com.fawry.fawrypay.models.LaunchCustomerModel
import com.fawry.fawrypay.models.LaunchMerchantModel
import com.fawry.fawrypay.models.PayableItem
import com.fawry.fawrypay.models.PaymentMethod
import com.restart.banzenty.R
import com.restart.banzenty.adapters.TransactionsAdapter
import com.restart.banzenty.base.BaseFragment
import com.restart.banzenty.data.models.PaymentModel
import com.restart.banzenty.databinding.FragmentWalletBinding
import com.restart.banzenty.utils.Constants
import com.restart.banzenty.utils.Constants.Companion.BASE_URL
import com.restart.banzenty.utils.Constants.Companion.FAWRY_PAYMENT_URL
import com.restart.banzenty.utils.Constants.Companion.MERCHANTCODE
import com.restart.banzenty.utils.Constants.Companion.MERCHANTSECRETCODE
import com.restart.banzenty.utils.DataStateChangeListener
import com.restart.banzenty.viewModels.WalletViewModel
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException


@AndroidEntryPoint
class WalletFragment : BaseFragment(), SwipeRefreshLayout.OnRefreshListener {
    private val TAG = "WalletFragment"
    private lateinit var binding: FragmentWalletBinding
    private val viewModel: WalletViewModel by viewModels()
    private var stateChangeListener: DataStateChangeListener? = null

    var transactionsList: ArrayList<PaymentModel.Payment> = ArrayList();
    private var isSwipLoading = false
    private var transactionsAdapter: TransactionsAdapter? = null

    private val openSubscriptionIntent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            run {
                if (it.resultCode == RESULT_OK) {
                    //open subscription fragment
                    (activity as MainActivity).navigateToMySubscription()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWalletBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initViews()
        getWalletDetails()
        if (transactionsList.isEmpty()) {
            showNoDataUI(
                show = true,
                icon = R.drawable.ic_no_data,
                caption = getString(R.string.no_transaction_data)
            )
        } else {
            transactionsAdapter?.submitList(transactionsList.toList())
            showNoDataUI(
                show = false,
                icon = R.drawable.ic_no_data,
                caption = getString(R.string.no_transaction_data)
            )
        }
        binding.btnWalletCharge.setOnClickListener {
            showAlertDialogButtonClicked(it)
        }
    }


    private fun getWalletDetails() {
        viewModel.getTransactionList().observe(viewLifecycleOwner) {
            if (it != null) {
                showProgressBar(it.loading.isLoading)
                it.error?.getContentIfNotHandled()?.let { stateError ->
                    //check if internet connection is lost
                    if (transactionsList.isEmpty()) {
                        if (stateError.response.message == Constants.UNABLE_TODO_OPERATION_WO_INTERNET
                            || stateError.response.message == Constants.UNABLE_TO_RESOLVE_HOST
                        ) {
                            showErrorUI(
                                show = true, title = getString(R.string.no_internet),
                                desc = null, image = R.drawable.ic_no_internet, showButton = true
                            )
                        } else {
                            Log.d(TAG, stateError.response.message!!)
                            showErrorUI(
                                show = true,
                                title = getString(R.string.something_went_wrong),
                                desc = null,
                                image = R.drawable.ic_something_wrong,
                                showButton = true
                            )
                        }
                    }
                }
                it.data?.data?.getContentIfNotHandled()?.let { transactions ->
                    setHomeUI()
                    if (transactions.payments.isNotEmpty()) {
                        transactionsList.clear()
                        transactionsList.addAll(transactions.payments)
                        Log.d(TAG, "transactions-list: ${transactionsList.toList()}")
                        transactionsAdapter?.submitList(transactionsList.toList())
                        if (transactionsList.isEmpty()) {
                            showNoDataUI(
                                show = true,
                                icon = R.drawable.ic_no_data,
                                caption = getString(R.string.no_transaction_data)
                            )
                        } else {
                            transactionsAdapter?.submitList(transactionsList.toList())
                            showNoDataUI(
                                show = false,
                                icon = R.drawable.ic_no_data,
                                caption = getString(R.string.no_transaction_data)
                            )
                        }
                    } else {
                        if (transactionsList.isEmpty()) {
                            showNoDataUI(
                                show = true,
                                icon = R.drawable.ic_no_data,
                                caption = getString(R.string.no_transaction_data)
                            )
                        } else {
                            transactionsAdapter?.submitList(transactionsList.toList())
                            showNoDataUI(
                                show = false,
                                icon = R.drawable.ic_no_data,
                                caption = getString(R.string.no_transaction_data)
                            )
                        }
                    }
                    if (transactionsList.isEmpty()) {
                        showNoDataUI(
                            show = true,
                            icon = R.drawable.ic_no_data,
                            caption = getString(R.string.no_transaction_data)
                        )
                    } else {
                        transactionsAdapter?.submitList(transactionsList.toList())
                        showNoDataUI(
                            show = false,
                            icon = R.drawable.ic_no_data,
                            caption = getString(R.string.no_transaction_data)
                        )
                    }
                }
            }
        }
    }

    private fun setHomeUI() {
        binding.tvWalletMoney.text = resources.getString(R.string.wallet_amount, sessionManager.getRemainingCash());
    }

    private fun initViews() {
        binding.swipeRefresh.setOnRefreshListener(this)
        binding.rvWalletList.apply {
            transactionsAdapter = TransactionsAdapter(requestManager)
            adapter = transactionsAdapter
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

    private fun showNoDataUI(
        show: Boolean,
        icon: Int?,
        caption: String?
    ) {
        if (show) {
            binding.rvWalletList.visibility = View.GONE
            binding.noDataLayout.root.visibility = View.VISIBLE
            binding.noDataLayout.tvCaption.text = caption
            binding.noDataLayout.ivIcon.setBackgroundResource(icon!!)
        } else {
            binding.noDataLayout.root.visibility = View.GONE
            binding.rvWalletList.visibility = View.VISIBLE
        }
    }

    override fun showErrorUI(
        show: Boolean,
        image: Int?,
        title: String?,
        desc: String?,
        showButton: Boolean?
    ) {
        Log.d(TAG, "showErrorUI: $show")
        if (show) {
            binding.llSubInfo.visibility = View.GONE
            binding.tvTransactions.visibility = View.GONE
            binding.rvWalletList.visibility = View.GONE
            binding.errorLayout.root.visibility = View.VISIBLE
            binding.errorLayout.tvDesc.visibility = View.GONE
            binding.errorLayout.tvTitle.text = title
            binding.errorLayout.ivPic.setBackgroundResource(image!!)
            desc?.let {
                binding.errorLayout.tvDesc.visibility = View.VISIBLE
                binding.errorLayout.tvDesc.text = it
            }
            binding.errorLayout.tvTitle.text = title
            if (showButton == true) {
                binding.errorLayout.btnRetry.visibility = View.VISIBLE
                binding.errorLayout.btnRetry.setOnClickListener { getWalletDetails() }
            } else binding.errorLayout.btnRetry.visibility = View.GONE

        } else {
            binding.llSubInfo.visibility = View.VISIBLE
            binding.tvTransactions.visibility = View.VISIBLE
            binding.rvWalletList.visibility = View.VISIBLE
            binding.errorLayout.root.visibility = View.GONE
            binding.swipeRefresh.isRefreshing = false
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            stateChangeListener = context as DataStateChangeListener
        } catch (e: ClassCastException) {
        }
    }

    override fun onRefresh() {
        binding.nsvContainer.smoothScrollTo(0, 0, 300)
        if (!isSwipLoading) {
            getWalletDetails()
        }
    }

    override fun onResume() {
        super.onResume()
        if (sessionManager.isUserSubscribed()) {
            getWalletDetails()
            sessionManager.setIsUserSubscribed(false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showAlertDialogButtonClicked(view: View) {
        // Create an alert builder
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.input_order_amount))

        // set the custom layout
        val customLayout: View = layoutInflater.inflate(R.layout.input_dialog_layout, null)
        builder.setView(customLayout)

        val etOrderAmont: EditText = customLayout.findViewById(R.id.tv_order_amount)
        val tvPaymentAmont: TextView = customLayout.findViewById(R.id.tv_payment_amount)
        tvPaymentAmont.text = "0.0"
        etOrderAmont.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val orderAmountValue: Double = if (etOrderAmont.text.toString() != "") {
                    etOrderAmont.text.toString().toDouble()
                } else {
                    0.0
                }
                val paymentAmountValue: Double = orderAmountValue + orderAmountValue * 2.5 / 100
                tvPaymentAmont.text = paymentAmountValue.toString()
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        // add a button
        builder.setPositiveButton(getString(R.string.ok)) { dialog, which ->
            // send data from the AlertDialog to the Activity
            val orderAmount = etOrderAmont.text.toString()
            val paymentAmount = tvPaymentAmont.text.toString()
            startPayment(orderAmount, paymentAmount)
        }
        // create and show the alert dialog
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun startPayment(orderAmount: String, paymentAmount: String) {
        val chargeItems = ArrayList<PayableItem>()
        val billItem = BillItems(
            itemId = "testId",
            description = "",
            quantity = "1",
            price = paymentAmount,
            originalPrice = orderAmount,
        )
        chargeItems.add(billItem)
        var merchantRefNumber = "${System.currentTimeMillis()}"
        Log.d("merchantRefNumber", merchantRefNumber)
        activity?.let {
            FawrySdk.launchAnonymousSDK(
                it, FawrySdk.Languages.ENGLISH, FAWRY_PAYMENT_URL,
                FawryLaunchModel(
                    launchCustomerModel = LaunchCustomerModel(
                        customerName = sessionManager.getUserName(),
                        customerEmail = sessionManager.getUserEmail(),
                        customerMobile = sessionManager.getUserPhone(),
                        customerProfileId = sessionManager.getUserId()
                    ),
                    launchMerchantModel = LaunchMerchantModel(
                        merchantCode = MERCHANTCODE,
                        secretCode = MERCHANTSECRETCODE,
                        merchantRefNum = merchantRefNumber
                    ),
                    allow3DPayment = true,
                    chargeItems = chargeItems,
                    skipReceipt = false,
                    skipLogin = true,
                    payWithCardToken = true,
                    authCaptureMode = false,
                    paymentMethods = FawrySdk.PaymentMethods.CREDIT_CARD
                ),
                object : FawrySdkCallbacks {
                    override fun onPreLaunch(onPreLaunch: FawryPreLaunch) {
                        onPreLaunch.onContinue()
                    }

                    override fun onInit() {

                    }

                    override fun onSuccess(msg: String, data: Any?) {
                        Log.d("SDK_Team","$data")
                        Log.d("pay-status", msg)
                        val jsonObject = JSONObject(data.toString())
                        val orderAmt = jsonObject.getString("orderAmount")
                        val refNum = jsonObject.getString("referenceNumber")
                        val client = OkHttpClient()
                        Log.d("merchantRefNumber", merchantRefNumber)
                        Log.d("refNum", refNum)
                        val url = "https://banzenty.com/api/profile/addMoneyToWallet?orderAmount=${orderAmt}&merchantRefNumber=${refNum}&statusCode=200";
                        val request = Request.Builder()
                            .url(url) // Add query parameters to the URL
                            .header("Accept", "application/json")
                            .header("Accept-Language", sessionManager.getLanguage())
                            .header("authorization", "Bearer ${sessionManager.getToken()}")
                            .build()

                        client.newCall(request).enqueue(object : Callback {
                            override fun onFailure(call: Call, e: IOException) {
                                // Handle the failure
                                e.printStackTrace()
                            }

                            override fun onResponse(call: Call, response: Response) {
                                // Handle the response
                                response.use {
                                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
                                    val responseBody = response.body?.string()
                                    Log.d(TAG, responseBody!!)
                                    println(responseBody)
                                    onRefresh()
                                }
                            }
                        })
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT)
                            .show()
                    }

                    override fun onPaymentCompleted(msg: String, data: Any?) {
                        Log.d("pay-msg", msg)
                        Log.d("pay-data", "$data")
                        Toast.makeText(
                            context,
                            "on payment completed $data",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    override fun onFailure(error: String) {
                        Log.d("pay-error", error)
                        Toast.makeText(
                            context,
                            error,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        }
    }
}