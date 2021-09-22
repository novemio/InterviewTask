package com.test.android.digitalpassserver

import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.text.format.Formatter
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException
import java.util.*

private const val TAG= "MainActivity"
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(Intent(this,DigitalServerService::class.java))
        }else{
            startService(Intent(this,DigitalServerService::class.java))
        }

        val wm = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        val ip: String = Formatter.formatIpAddress(wm.connectionInfo.ipAddress)

        findViewById<TextView>(R.id.ipAddress).text = ip.plus(":8080")
        Log.d(TAG,"ip first = $ip")



    }
    fun getLocalIpAddress(): String? {
        try {
            val en: Enumeration<NetworkInterface> = NetworkInterface.getNetworkInterfaces()
            while (en.hasMoreElements()) {
                val intf: NetworkInterface = en.nextElement()
                val enumIpAddr: Enumeration<InetAddress> = intf.inetAddresses
                while (enumIpAddr.hasMoreElements()) {
                    val inetAddress: InetAddress = enumIpAddr.nextElement()
                    if (!inetAddress.isLoopbackAddress) {
                        val ip: String = Formatter.formatIpAddress(inetAddress.hashCode())
                        Log.i(TAG, "***** IP=$ip")
                        return ip
                    }
                }
            }
        } catch (ex: SocketException) {
            Log.e(TAG, ex.toString())
        }
        return null
    }


    override fun onDestroy() {
        super.onDestroy()
        stopService(Intent(this,DigitalServerService::class.java))
    }
}