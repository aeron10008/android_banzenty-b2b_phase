package com.restart.banzenty.data.models

import android.os.Parcelable
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class WalletModel(

    @SerializedName("wallet")
    @Expose
    val wallet: Wallet
) : Parcelable {
    @Parcelize
    data class Wallet(
        @PrimaryKey
        @SerializedName("id")
        @Expose
        val id: Int,

        @SerializedName("user_id")
        @Expose
        val user_id: Int,

        @SerializedName("money")
        @Expose
        val money: Int
    ) : Parcelable
}
