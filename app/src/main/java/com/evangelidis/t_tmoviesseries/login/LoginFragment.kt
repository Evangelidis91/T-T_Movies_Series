package com.evangelidis.t_tmoviesseries.login

import android.app.ActionBar
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import com.evangelidis.t_tmoviesseries.view.MainActivity
import com.evangelidis.t_tmoviesseries.R
import com.evangelidis.t_tmoviesseries.login.LoginRegisterMethods.isEmailValid
import com.evangelidis.t_tmoviesseries.login.LoginRegisterMethods.isPasswordValid
import com.evangelidis.t_tmoviesseries.login.LoginRegisterMethods.verifyAvailableNetwork
import com.evangelidis.tantintoast.TanTinToast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class LoginFragment : Fragment() {

    private lateinit var inflate: View
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var fragmentContext: Context

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        fragmentContext = inflater.context

        inflate = inflater.inflate(R.layout.fragment_login, container, false)
        inflate.findViewById<TextView>(R.id.forgot_password).setOnClickListener { performForgotPassword() }
        inflate.findViewById<AppCompatButton>(R.id.btn_login).setOnClickListener { performLogin() }

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        return inflate
    }

    private fun performLogin() {
        val email = inflate.findViewById<EditText>(R.id.email_editText).text.toString()
        val password = inflate.findViewById<EditText>(R.id.password_editText).text.toString()

        if (verifyAvailableNetwork(context)) {
            if (isEmailValid(email)) {
                if (isPasswordValid(password)) {
                    loginUser(email, password)
                } else {
                    TanTinToast.Warning(fragmentContext).text("Pass must be at least 6 characters").show()
                }
            } else {
                TanTinToast.Warning(fragmentContext).text("The email is not valid").show()
            }
        } else {
            TanTinToast.Warning(fragmentContext).text("There is no internet connection").show()
        }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    activity?.let {
                        val intent = Intent(it, MainActivity::class.java)
                        it.startActivity(intent)
                        it.finish()
                    }
                } else {
                    TanTinToast.Warning(fragmentContext).text("There is a problem. Please try again").show()
                }
            }
    }

    private fun performForgotPassword() {
        val mpopup: PopupWindow
        val popUpView: View = layoutInflater.inflate(R.layout.enter_email_layout, null)

        mpopup = PopupWindow(
            popUpView, ActionBar.LayoutParams.FILL_PARENT,
            ActionBar.LayoutParams.WRAP_CONTENT, true
        )
        mpopup.showAtLocation(popUpView, Gravity.CENTER, 0, 0)

        val email = popUpView.findViewById<EditText>(R.id.email_reset_password)

        popUpView.findViewById<Button>(R.id.decline_message).setOnClickListener { mpopup.dismiss() }
        popUpView.findViewById<Button>(R.id.submit_message).setOnClickListener {
            if (verifyAvailableNetwork(context)) {
                if (isEmailValid(email.text.toString())) {
                    auth.sendPasswordResetEmail(email.text.toString())
                    mpopup.dismiss()
                } else {
                    TanTinToast.Warning(fragmentContext).text("The email is not valid").show()
                }
            } else {
                TanTinToast.Warning(fragmentContext).text("There is no internet connection").show()
            }
        }
    }
}