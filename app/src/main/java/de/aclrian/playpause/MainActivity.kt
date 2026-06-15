package de.aclrian.playpause

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isEmpty

class MainActivity : AppCompatActivity() {
    private lateinit var configManager: ConfigManager
    private var mediaControlService: MediaControlService? = null
    private var isBound = false

    private val connection =
        object : ServiceConnection {
            override fun onServiceConnected(
                className: ComponentName,
                service: IBinder,
            ) {
                val binder = service as MediaControlService.LocalBinder
                mediaControlService = binder.getService()
                isBound = true
            }

            override fun onServiceDisconnected(arg0: ComponentName) {
                mediaControlService = null
                isBound = false
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        configManager = ConfigManager(this)

        if (!isForegroundServiceRunning(this)) {
            startForegroundService(Intent(this, MediaControlService::class.java))
        }
        val ssids = configManager.getSsids()
        if (ssids.isNotEmpty()) {
            buildWifiList(ssids)
        }
    }

    override fun onStart() {
        super.onStart()
        Intent(this, MediaControlService::class.java).also { intent ->
            bindService(intent, connection, BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        if (isBound) {
            unbindService(connection)
            isBound = false
        }
    }

    fun buildWifiList(ssids: Set<String>) {
        val wifiList = findViewById<LinearLayout>(R.id.wifiList)
        wifiList.removeAllViews()
        for (ssid in ssids) {
            var ssidName = ssid
            if (ssid.startsWith("\"") and ssid.endsWith("\"")) {
                ssidName = ssid.substring(1, ssid.length - 1)
            }
            wifiList.addView(
                createSsidTextItem(
                    ssidName,
                    {
                        confirmDeletion(ssid)
                        true
                    },
                ),
            )
        }
        if (wifiList.isEmpty()) {
            wifiList.addView(createSsidTextItem(getString(R.string.no_wifi)))
        }
    }

    fun createSsidTextItem(
        text: String,
        longClickListener: View.OnLongClickListener? = null,
        padding: Int = (16 * resources.displayMetrics.density).toInt(),
    ): TextView {
        val ssidView = TextView(this)
        ssidView.layoutParams =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            )
        ssidView.text = text
        ssidView.textSize = 18f
        ssidView.setPadding(padding, padding, padding, padding)
        ssidView.gravity = android.view.Gravity.CENTER_VERTICAL
        ssidView.minHeight = (48 * resources.displayMetrics.density).toInt()

        ssidView.setBackgroundResource(R.drawable.ripple)
        ssidView.isClickable = true
        ssidView.isFocusable = true
        ssidView.setOnClickListener {
            // Empty listener to enable ripple feedback
        }
        ssidView.setOnLongClickListener(longClickListener)
        return ssidView
    }

    fun confirmDeletion(ssid: String) {
        AlertDialog
            .Builder(this)
            .setTitle(getString(R.string.remove_wifi_start) + " $ssid" + getString(R.string.remove_wifi_end))
            .setPositiveButton(
                R.string.ok,
            ) { _, _ ->
                configManager.removeSsid(ssid)
                buildWifiList(configManager.getSsids())
                mediaControlService?.configChanged()
            }.setNegativeButton(R.string.cancel) { _, _ ->
                // omitted
            }.show()
    }

    companion object {
        fun isForegroundServiceRunning(context: Context): Boolean {
            val am = context.getSystemService(ACTIVITY_SERVICE) as ActivityManager
            @Suppress("DEPRECATION") // to get the running services of this application
            return am.getRunningServices(Int.MAX_VALUE).any {
                MediaControlService::class.java.name.equals(it.service.className)
            }
        }
    }

    fun addCurrentNetwork(
        @Suppress("UNUSED_PARAMETER") view: View,
    ) {
        val ssidInsertView = EditText(this)
        ssidInsertView.setText(mediaControlService?.receiver?.networkChecker?.currentSSID ?: "")
        val margin = (30 * resources.displayMetrics.density).toInt()
        val container = FrameLayout(this)
        val params =
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
            )
        params.marginStart = margin
        params.marginEnd = margin
        ssidInsertView.layoutParams = params
        container.addView(ssidInsertView)

        AlertDialog
            .Builder(this)
            .setTitle(getString(R.string.add_wifi))
            .setView(container)
            .setPositiveButton(
                getString(R.string.ok),
            ) { _, _ ->
                val ssid = ssidInsertView.text.toString()
                configManager.saveSsid(ssid)
                buildWifiList(configManager.getSsids())
                mediaControlService?.configChanged()
            }.setNegativeButton(getString(R.string.cancel)) { _, _ ->
                // omitted
            }.show()
    }
}
