package com.evangelidis.t_tmoviesseries.view.login

import android.content.Context
import android.net.ConnectivityManager
import com.google.firebase.auth.FirebaseUser
import java.util.regex.Matcher
import java.util.regex.Pattern

object LoginRegisterMethods {

    fun verifyAvailableNetwork(context: Context?): Boolean {
        val connectivityManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    fun isPasswordValid(password: String): Boolean {
        if (password.isEmpty() || password.length < 6) {
            return false
        }
        return true
    }

    fun isEmailValid(email: String?): Boolean {
        val expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,7}$"
        val pattern: Pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE)
        val matcher: Matcher = pattern.matcher(email)
        return matcher.matches()
    }

    fun arePasswordsEquals(pass1: String, pass2: String) = (pass1 == pass2)

    fun sendVerificationEmail(user: FirebaseUser?) {
        user?.sendEmailVerification()
    }
}
