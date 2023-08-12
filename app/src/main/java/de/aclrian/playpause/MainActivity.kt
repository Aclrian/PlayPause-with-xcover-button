package de.aclrian.playpause

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!isForegroundServiceRunning(this)) {
            startForegroundService(Intent(this, MediaControlService::class.java))
        }
    }

    companion object {
        fun isForegroundServiceRunning(context: Context): Boolean {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            @Suppress("DEPRECATION") // to get the running services of this application
            return am.getRunningServices(Int.MAX_VALUE).any {
                MediaControlService::class.java.name.equals(it.service.className)
            }
        }
    }
}
