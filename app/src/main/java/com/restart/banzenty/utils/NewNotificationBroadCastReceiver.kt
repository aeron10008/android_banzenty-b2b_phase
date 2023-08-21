package com.restart.banzenty.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NewNotificationBroadCastReceiver(private var receivingNotificationInterface: ReceivingNotificationInterface?) :
    BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != null && intent.action == Constants.NEW_NOTIFICATION_ACTION && receivingNotificationInterface != null) receivingNotificationInterface!!.onNotificationReceived()
    }

    interface ReceivingNotificationInterface {
        fun onNotificationReceived()
    }
}