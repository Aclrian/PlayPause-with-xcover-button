package de.aclrian.playpause

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat.startForegroundService

class StartServiceReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent!!.action == Intent.ACTION_BOOT_COMPLETED && !MainActivity.isForegroundServiceRunning(context!!)) {
            startForegroundService(context, Intent(context, MediaControlService::class.java))
        }
    }
}