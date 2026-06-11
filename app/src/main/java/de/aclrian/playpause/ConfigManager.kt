package de.aclrian.playpause

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class ConfigManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("SSID", Context.MODE_PRIVATE)

    fun saveSsid(ssid: String) {
        val ssidSet =
            prefs.getStringSet("ssidSet", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        ssidSet.add(ssid)
        prefs.edit { putStringSet("ssidSet", ssidSet) }
    }

    fun getSsids(): Set<String> {
        return prefs.getStringSet("ssidSet", emptySet()) ?: emptySet()
    }

    fun removeSsid(ssid: String) {
        val ssidSet = getSsids().minus(ssid)
        prefs.edit { putStringSet("ssidSet", ssidSet) }
    }
}
