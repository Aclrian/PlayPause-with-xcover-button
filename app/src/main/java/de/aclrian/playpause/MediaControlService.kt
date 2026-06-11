package de.aclrian.playpause

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.IBinder

class MediaControlService : Service() {
    lateinit var receiver: MediaControlReceiver
        private set

    inner class LocalBinder : Binder() {
        fun getService(): MediaControlService = this@MediaControlService
    }

    private lateinit var networkChecker: NetworkChecker

    private val binder = LocalBinder()

    override fun onCreate() {
        super.onCreate()
        networkChecker = NetworkChecker(this)
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

        receiver = MediaControlReceiver(networkChecker)
        val filter = IntentFilter("com.samsung.android.knox.intent.action.HARD_KEY_REPORT")
        registerReceiver(receiver, filter, RECEIVER_EXPORTED)
    }

    override fun onDestroy() {
        unregisterReceiver(receiver)
        networkChecker.unregister()
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    fun configChanged() {
        networkChecker.updateRegistration()
    }
}