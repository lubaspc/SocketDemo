package com.lubaspc.connectios

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiNetworkSuggestion
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

fun Int.toIp() = String.format(
    "%d.%d.%d.%d", this and 0xff, this shr 8 and 0xff,
    this shr 16 and 0xff, this shr 24 and 0xff
)

private fun Context.settingsWifi(): Boolean {
    if (!Settings.System.canWrite(this)) {
        startActivity(
            Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                .setData(Uri.parse("package:$packageName"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
        return false
    }
    return true
}

private fun Context.isConnectedViaWifi(): Boolean {
    val connectivityManager =
        getSystemService(AppCompatActivity.CONNECTIVITY_SERVICE) as ConnectivityManager
    val mWifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
    return mWifi?.isConnected == true
}

private fun MainActivity.connectWifi(ssid: String, pass: String) {
    val conf = WifiConfiguration()
    conf.SSID = "\"$ssid\""
    conf.preSharedKey = "\"$pass\""
    conf.status = WifiConfiguration.Status.ENABLED
    conf.priority = 40
    conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
    conf.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
    conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
    conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
    conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
    conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
    conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
    conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
    conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);

    /* val netId = wifiManager.addNetwork(conf)
     Log.d("connectWifi", "$netId")
     wifiManager.disconnect()
     Log.d("connectWifi", "${wifiManager.disconnect()}")
     wifiManager.enableNetwork(netId, true)
     Log.d("connectWifi", "${wifiManager.enableNetwork(netId, true)}")
     wifiManager.reconnect()
     Log.d("connectWifi", "${wifiManager.reconnect()}")

     */
}

private fun MainActivity.connectWifi29(ssid: String, pass: String) {
    val wifiNetworkSpecifier = WifiNetworkSuggestion.Builder()
        .setSsid(ssid)
        .setWpa2Passphrase(pass)
        .build()
    //val status = wifiManager.addNetworkSuggestions(listOf(wifiNetworkSpecifier))
    //Log.d("connectWifi_status", status.toString())
}

private fun connectWifi(ssid: String, key: String) {
    val wifiConfig = WifiConfiguration()
    wifiConfig.SSID = String.format("\"%s\"", ssid)
    wifiConfig.preSharedKey = String.format("\"%s\"", key)

    /*val netId = wifiManager.addNetwork(wifiConfig)
    wifiManager.disconnect()
    wifiManager.enableNetwork(netId, true)
    wifiManager.reconnect()
     */
}

fun String.getDataWifi(): Pair<String, String>? {
    var ssid: String? = null
    var pass: String? = null
    split(";").forEach {
        var index = it.indexOf("S:")
        if (index >= 0) ssid = it.substring(index + 2 until it.length)
        index = it.indexOf("P:")
        if (index >= 0) pass = it.substring(index + 2 until it.length)
    }
    return (ssid ?: return null) to (pass ?: return null)
}