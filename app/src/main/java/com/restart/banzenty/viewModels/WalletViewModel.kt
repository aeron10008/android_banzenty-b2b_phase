package com.restart.banzenty.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.restart.banzenty.data.models.ChargeModel
import com.restart.banzenty.data.models.PaymentModel
import com.restart.banzenty.data.models.WalletModel
import com.restart.banzenty.network.DataState
import com.restart.banzenty.repositories.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class WalletViewModel
@Inject constructor(private val walletRepository: WalletRepository) : ViewModel() {
    var isQueryExhausted: Boolean = false
    var page: Int = 1

    fun getWalletDetails(): LiveData<DataState<WalletModel.Wallet>> {
        return walletRepository.getWalletDetails()
    }

    fun getTransactionList(): LiveData<DataState<PaymentModel>> {
        return walletRepository.getTransactionList()
    }

    fun addMoneyToWallet(orderAmount: String?, merchantRefNumber: String?, statusCode: String?): LiveData<DataState<ChargeModel>> {
        return walletRepository.addMoneyToWallet(orderAmount, merchantRefNumber, statusCode)
    }
}