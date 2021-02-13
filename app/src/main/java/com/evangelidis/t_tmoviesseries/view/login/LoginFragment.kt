package com.evangelidis.t_tmoviesseries.view.login

import android.annotation.SuppressLint
import android.app.ActionBar
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.evangelidis.t_tmoviesseries.R
import com.evangelidis.t_tmoviesseries.databinding.EnterEmailLayoutBinding
import com.evangelidis.t_tmoviesseries.databinding.FragmentLoginBinding
import com.evangelidis.t_tmoviesseries.view.login.LoginRegisterMethods.isEmailValid
import com.evangelidis.t_tmoviesseries.view.login.LoginRegisterMethods.isPasswordValid
import com.evangelidis.t_tmoviesseries.view.login.LoginRegisterMethods.verifyAvailableNetwork
import com.evangelidis.t_tmoviesseries.view.main.MainActivity
import com.evangelidis.tantintoast.TanTinToast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class LoginFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var fragmentContext: Context
    private var typeface: Int = R.font.montserrat_regular

    private val binding: FragmentLoginBinding by lazy { FragmentLoginBinding.inflate(layoutInflater) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        fragmentContext = inflater.context

        binding.forgotPassword.setOnClickListener { performForgotPassword() }
        binding.btnLogin.setOnClickListener { performLogin() }

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        return binding.root
    }

    private fun performLogin() {
        val email = binding.emailEditText.text.toString()
        val password = binding.passwordEditText.text.toString()

        if (verifyAvailableNetwork(context)) {
            if (isEmailValid(email)) {
                if (isPasswordValid(password)) {
                    loginUser(email, password)
                } else {
                    TanTinToast.Warning(fragmentContext).text("Pass must be at least 6 characters").typeface(typeface).show()
                }
            } else {
                TanTinToast.Warning(fragmentContext).text("The email is not valid").typeface(typeface).show()
            }
        } else {
            TanTinToast.Warning(fragmentContext).text("There is no internet connection").typeface(typeface).show()
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
                    TanTinToast.Warning(fragmentContext).text("There is a problem. Please try again").typeface(typeface).show()
                }
            }
    }

    @SuppressLint("InflateParams")
    private fun performForgotPassword() {
        val popUpView = EnterEmailLayoutBinding.inflate(layoutInflater)

        val popup = PopupWindow(
            popUpView.root,
            ActionBar.LayoutParams.MATCH_PARENT,
            ActionBar.LayoutParams.WRAP_CONTENT,
            true
        )
        popup.showAtLocation(popUpView.root, Gravity.CENTER, 0, 0)

        popUpView.declineMessage.setOnClickListener { popup.dismiss() }
        popUpView.submitMessage.setOnClickListener {
            if (verifyAvailableNetwork(context)) {
                if (isEmailValid(popUpView.emailReset.text.toString())) {
                    auth.sendPasswordResetEmail(popUpView.emailReset.text.toString())
                        .addOnSuccessListener {
                            popup.dismiss()
                            TanTinToast.Info(fragmentContext).text("You will receive shortly an email to reset your password. Please check your inbox.").typeface(typeface).show()
                        }
                        .addOnFailureListener {
                            TanTinToast.Error(fragmentContext).text(popUpView.emailReset.text.toString() + (" does not exist. Please try to Sign up first.")).typeface(typeface).show()
                        }
                } else {
                    TanTinToast.Warning(fragmentContext).text("The email is not valid").typeface(typeface).show()
                }
            } else {
                TanTinToast.Warning(fragmentContext).text("There is no internet connection").typeface(typeface).show()
            }
        }
    }
}
