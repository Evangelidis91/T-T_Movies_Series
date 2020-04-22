package com.evangelidis.t_tmoviesseries.utils

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log

class InternetStatus {
    private lateinit var connectivityManager: ConnectivityManager
    private var connected = false

    val isOnline: Boolean
        get() {
            try {
                connectivityManager = context
                    .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

                val networkInfo = connectivityManager.activeNetworkInfo
                connected = networkInfo != null && networkInfo.isAvailable &&
                        networkInfo.isConnected
                return connected

            } catch (e: Exception) {
                Log.v("connectivity", e.toString())
            }

            return connected
        }

    companion object {

        lateinit var context: Context
        /**
         * Class to detect if there is internet connection
         */

        private val instance = InternetStatus()

        fun getInstance(ctx: Context): InternetStatus {
            context = ctx.applicationContext
            return instance
        }
    }
}