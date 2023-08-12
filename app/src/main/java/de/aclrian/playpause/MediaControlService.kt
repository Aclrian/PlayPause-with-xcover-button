package de.aclrian.playpause

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder

class MediaControlService : Service() {
    private lateinit var receiver: MediaControlReceiver

    override fun onCreate() {
        super.onCreate()
        val channelID = "PlayPause Notification"
        val channel = NotificationChannel(channelID, channelID, NotificationManager.IMPORTANCE_MIN)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        val notification = Notification.Builder(this, channelID)
            .setContentText("Service is running")
            .setContentTitle("PlayPause")
            .setOngoing(true)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setCategory(Notification.CATEGORY_SERVICE)
        startForeground(4242, notification.build())

        receiver = MediaControlReceiver()
        val filter = IntentFilter("com.samsung.android.knox.intent.action.HARD_KEY_REPORT")
        registerReceiver(receiver, filter)
    }

    override fun onDestroy() {
        unregisterReceiver(receiver)
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}