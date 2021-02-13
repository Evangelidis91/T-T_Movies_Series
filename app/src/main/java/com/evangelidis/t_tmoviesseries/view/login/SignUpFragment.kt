package com.evangelidis.t_tmoviesseries.view.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.evangelidis.t_tmoviesseries.R
import com.evangelidis.t_tmoviesseries.databinding.FragmentSignupBinding
import com.evangelidis.t_tmoviesseries.utils.Constants.FIREBASE_NEW_USER_DATE_FORMAT
import com.evangelidis.t_tmoviesseries.utils.Constants.FIREBASE_USER_DATABASE_PATH
import com.evangelidis.t_tmoviesseries.utils.Constants.IS_LOGGED_IN
import com.evangelidis.t_tmoviesseries.utils.Constants.IS_LOGIN_SKIPPED
import com.evangelidis.t_tmoviesseries.view.login.LoginRegisterMethods.arePasswordsEquals
import com.evangelidis.t_tmoviesseries.view.login.LoginRegisterMethods.isEmailValid
import com.evangelidis.t_tmoviesseries.view.login.LoginRegisterMethods.isPasswordValid
import com.evangelidis.t_tmoviesseries.view.login.LoginRegisterMethods.sendVerificationEmail
import com.evangelidis.t_tmoviesseries.view.login.LoginRegisterMethods.verifyAvailableNetwork
import com.evangelidis.t_tmoviesseries.view.login.model.User
import com.evangelidis.t_tmoviesseries.view.main.MainActivity
import com.evangelidis.tantintoast.TanTinToast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import es.dmoral.prefs.Prefs
import java.text.SimpleDateFormat
import java.util.*

class SignUpFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var fragmentContext: Context
    private var typeface: Int = R.font.montserrat_regular

    private val binding: FragmentSignupBinding by lazy { FragmentSignupBinding.inflate(layoutInflater) }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentContext = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding.btnSignUp.setOnClickListener { performSignUp() }
        binding.btnSkip.setOnClickListener {
            Prefs.with(fragmentContext).writeBoolean(IS_LOGIN_SKIPPED, true)
            startActivity(Intent(this.context, MainActivity::class.java))
            activity?.finish()
        }
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        return binding.root
    }

    private fun performSignUp() {
        val email = binding.emailEditText.text.toString()
        val password = binding.passwordEditText.text.toString()
        val confirmPassword = binding.confirmPasswordEditText.text.toString()

        if (verifyAvailableNetwork(context)) {
            if (isEmailValid(email)) {
                if (isPasswordValid(password)) {
                    if (arePasswordsEquals(password, confirmPassword)) {
                        createUser(email, password)
                    } else {
                        TanTinToast.Warning(fragmentContext).text("Passwords are not match").typeface(typeface).show()
                    }
                } else {
                    TanTinToast.Warning(fragmentContext).text("Password must be at least 6 characters").typeface(typeface).show()
                }
            } else {
                TanTinToast.Warning(fragmentContext).text("The email is not valid").typeface(typeface).show()
            }
        } else {
            TanTinToast.Warning(fragmentContext).text("There is no internet connection").typeface(typeface).show()
        }
    }

    private fun createUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onAuthSuccess(task.result?.user, email)
                    sendVerificationEmail(task.result?.user)
                    TanTinToast.Success(fragmentContext).text("Your account created successfully!").typeface(typeface).show()
                } else {
                    TanTinToast.Warning(fragmentContext).text(task.result.toString()).typeface(typeface).show()
                }
            }
    }

    private fun onAuthSuccess(user: FirebaseUser?, email: String) {
        val username: String = usernameFromEmail(email)
        writeNewUser(user?.uid, username, email)
        Prefs.with(fragmentContext).writeBoolean(IS_LOGGED_IN, true)
        startActivity(Intent(fragmentContext, MainActivity::class.java))
        activity?.finish()
    }

    private fun usernameFromEmail(email: String): String {
        return if (email.contains("@")) {
            email.substringBefore("@")
        } else {
            email
        }
    }

    private fun writeNewUser(userId: String?, name: String, email: String) {
        val sdf = SimpleDateFormat(FIREBASE_NEW_USER_DATE_FORMAT, Locale.UK)
        val currentDate = sdf.format(Date())
        val user = User(userId, name, email, currentDate)

        userId?.let {
            database.child(FIREBASE_USER_DATABASE_PATH).child(it).setValue(user)
        }
    }
}
