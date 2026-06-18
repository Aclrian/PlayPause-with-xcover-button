package de.aclrian.playpause

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.util.Log

class NetworkChecker(
    context: Context,
    var trustedSSID: Boolean = false,
) {
    val configManager: ConfigManager = ConfigManager(context)
    var currentSSID: String? = null
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val networkCallback = callback()

    init {
        updateRegistration()
    }

    fun updateRegistration() {
        if (configManager.getSsids().isNotEmpty()) {
            register()
        } else {
            unregister()
        }
    }

    fun register() {
        unregister()
        val networkRequest =
            NetworkRequest
                .Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .build()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    fun unregister() {
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (_: IllegalArgumentException) {
            // ignore: NetworkCallback was not registered
        }
    }

    fun callback(): ConnectivityManager.NetworkCallback {
        val callback =
            object : ConnectivityManager.NetworkCallback(FLAG_INCLUDE_LOCATION_INFO) {
                override fun onCapabilitiesChanged(
                    network: Network,
                    capabilities: NetworkCapabilities,
                ) {
                    super.onCapabilitiesChanged(network, capabilities)
                    updateSsid(capabilities)
                }

                override fun onLost(network: Network) {
                    super.onLost(network)
                    trustedSSID = false
                    Log.d(
                        "PlayPause",
                        "Functionality based on current Network deactivated (network lost)",
                    )
                }
            }
        return callback
    }

    private fun updateSsid(capabilities: NetworkCapabilities?) {
        val wifi = capabilities?.transportInfo
        if (wifi is WifiInfo) {
            currentSSID = wifi.ssid

            if (currentSSID != null &&
                currentSSID != WifiManager.UNKNOWN_SSID &&
                !currentSSID!!.equals("<unknown ssid>", ignoreCase = true)
            ) {
                trustedSSID = configManager.getSsids().contains(currentSSID)
                Log.d(
                    "PlayPause",
                    "Functionality based on current Network is " +
                            if (trustedSSID) "activated" else "deactivated",
                )
                return
            }
        }
        trustedSSID = false
        currentSSID = ""
        Log.d(
            "PlayPause",
            "Functionality based on current Network deactivated (unknown network)",
        )
    }
}
