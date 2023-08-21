package com.restart.banzenty.network


import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.restart.banzenty.R
import com.restart.banzenty.ui.main.MainActivity
import com.restart.banzenty.utils.Constants
import com.restart.banzenty.utils.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class CustomFirebaseMessagingService : FirebaseMessagingService() {
    @Inject
    lateinit var sessionManager: SessionManager

    override fun onNewToken(s: String) {
        super.onNewToken(s)
        Log.d(TAG, "onNewToken: $s")
        sessionManager.setDeviceToken(s)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val broadcastIntent = Intent()
        broadcastIntent.action = Constants.NEW_NOTIFICATION_ACTION
        sendBroadcast(broadcastIntent)
        Log.d(
            TAG,
            "onMessageReceived Data: ${remoteMessage.notification} \n Notification: ${remoteMessage.data}"
        )
        if (remoteMessage.notification != null) {
            showNotification(
                this.applicationContext,
                remoteMessage.notification?.title,
                remoteMessage.notification?.body
            )
        } else {
            showNotification(
                this.applicationContext,
                remoteMessage.data["title"],
                remoteMessage.data["body"]
            )
        }
    }

    companion object {
        private const val TAG = "CustomFirebaseMessaging"
        private const val BANZENTY_PENDING_INTENT = 89777
        private const val BANZENTY_NOTIFICATION_CHANNEL_ID = "banzenty_channel_id"
        private var carRequestId = 0
        fun showNotification(
            applicationContext: Context,
            title: String?, body: String?
        ) {
            val notificationManager = applicationContext
                .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationChannel = NotificationChannel(
                    BANZENTY_NOTIFICATION_CHANNEL_ID,
                    "Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                notificationChannel.setShowBadge(true)
//                notificationChannel.description = "Case Status"
                notificationChannel.enableLights(true)
                notificationChannel.lightColor =
                    ContextCompat.getColor(applicationContext, R.color.colorAccent)
                notificationChannel.vibrationPattern = longArrayOf(0, 1000, 500, 1000)
                notificationManager.createNotificationChannel(notificationChannel)
            }
            val builder = NotificationCompat.Builder(
                applicationContext,
                BANZENTY_NOTIFICATION_CHANNEL_ID
            )
            val defaultSoundUri: Uri =
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            builder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_notification_app)
                .setColor(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.flamingo
                    )
                )
                .setSound(defaultSoundUri)
                .setContentTitle(title)
                .setVibrate(longArrayOf(0, 1000, 500, 1000))
                .setContentIntent(contentIntent(applicationContext))
                .setContentText(body)
            notificationManager.notify(Random().nextInt(), builder.build())
        }

        private fun contentIntent(
            applicationContext: Context
        ): PendingIntent {
            return PendingIntent.getActivity(
                applicationContext,
                BANZENTY_PENDING_INTENT,
                Intent(
                    applicationContext, MainActivity::class.java
                ), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
    }
}