package com.evangelidis.t_tmoviesseries.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import com.evangelidis.t_tmoviesseries.utils.Constants.IS_LOGGEDIN
import com.evangelidis.t_tmoviesseries.utils.Constants.IS_LOGIN_SKIPPED
import com.evangelidis.t_tmoviesseries.view.MainActivity
import com.evangelidis.t_tmoviesseries.R
import com.evangelidis.t_tmoviesseries.login.LoginRegisterMethods.arePasswordsEquals
import com.evangelidis.t_tmoviesseries.login.LoginRegisterMethods.isEmailValid
import com.evangelidis.t_tmoviesseries.login.LoginRegisterMethods.isPasswordValid
import com.evangelidis.t_tmoviesseries.login.LoginRegisterMethods.verifyAvailableNetwork
import com.evangelidis.t_tmoviesseries.login.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import es.dmoral.prefs.Prefs
import java.text.SimpleDateFormat
import java.util.*

class SignUpFragment : Fragment() {

    lateinit var inflate: View
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var fragmentContext: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        inflate = inflater.inflate(R.layout.fragment_signup, container, false)
        inflate.findViewById<AppCompatButton>(R.id.btn_signup)
            .setOnClickListener { performSignUp() }
        inflate.findViewById<AppCompatButton>(R.id.btn_skip).setOnClickListener {
            Prefs.with(fragmentContext).writeBoolean(IS_LOGIN_SKIPPED, true)
            startActivity(Intent(this.context, MainActivity::class.java))
            activity?.finish()
        }
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        return inflate
    }

    private fun performSignUp() {
        val email = inflate.findViewById<EditText>(R.id.email_editText).text.toString()
        val password = inflate.findViewById<EditText>(R.id.password_editText).text.toString()
        val confirmPassword =
            inflate.findViewById<EditText>(R.id.confirm_password_editText).text.toString()

        if (verifyAvailableNetwork(context)) {
            if (isEmailValid(email)) {
                if (isPasswordValid(password)) {
                    if (arePasswordsEquals(password, confirmPassword)) {
                        createUser(email, password)
                    } else {
                        Toast.makeText(context, "Passwords are not match", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(context, "Pass must be at least 6 characters", Toast.LENGTH_LONG)
                        .show()
                }
            } else {
                Toast.makeText(context, "The email is not valid", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(context, "There is no internet connection", Toast.LENGTH_LONG).show()
        }
    }

    private fun createUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onAuthSuccess(task.result?.user, email)
                    sendVerificationEmail(task.result?.user)
                } else {
                    Toast.makeText(context, task.result.toString(), Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun sendVerificationEmail(user: FirebaseUser?) {
        user?.sendEmailVerification()
    }

    private fun onAuthSuccess(
        user: FirebaseUser?,
        email: String
    ) {
        val username: String = usernameFromEmail(email)
        writeNewUser(user?.uid, username, email)
        Prefs.with(fragmentContext).writeBoolean(IS_LOGGEDIN, true)
        startActivity(Intent(fragmentContext, MainActivity::class.java))
        activity?.finish()
    }

    private fun usernameFromEmail(email: String): String {
        return if (email.contains("@")) {
            email.split("@").toTypedArray()[0]
        } else {
            email
        }
    }

    private fun writeNewUser(userid: String?, name: String, email: String) {
        val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
        val currentDate = sdf.format(Date())
        val user = User(userid, name, email, currentDate)
        if (userid != null) {
            database.child("users").child(userid).setValue(user)
        }
    }
}