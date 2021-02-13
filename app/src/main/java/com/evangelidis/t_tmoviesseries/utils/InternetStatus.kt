package com.evangelidis.t_tmoviesseries.utils

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log

object InternetStatus {
    fun isConnected(context: Context): Boolean {
        try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.isAvailable && networkInfo.isConnected
        } catch (e: Exception) {
            Log.v("connectivity", e.toString())
        }
        return false
    }

}
