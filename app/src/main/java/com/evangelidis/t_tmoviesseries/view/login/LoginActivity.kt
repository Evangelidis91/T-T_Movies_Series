package com.evangelidis.t_tmoviesseries.view.login

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.evangelidis.t_tmoviesseries.R
import com.evangelidis.t_tmoviesseries.extensions.hide
import com.evangelidis.t_tmoviesseries.extensions.show
import com.evangelidis.t_tmoviesseries.utils.Constants.IS_LOGIN_SKIPPED
import com.evangelidis.t_tmoviesseries.view.main.MainActivity
import com.google.firebase.auth.FirebaseAuth
import es.dmoral.prefs.Prefs
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private var isLogin = true
    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mAuth = FirebaseAuth.getInstance()
        val topLoginFragment = LoginFragment()
        val topSignUpFragment = SignUpFragment()

        supportFragmentManager.beginTransaction()
            .replace(R.id.login_fragment, topLoginFragment)
            .replace(R.id.sign_up_fragment, topSignUpFragment)
            .commit()

        login_fragment.rotation = -90f

        button.apply {
            setOnButtonSwitched(object : OnButtonSwitchedListener {
                override fun onButtonSwitched(isLogin: Boolean) {
                    login_layout.setBackgroundColor(ContextCompat.getColor(applicationContext, if (isLogin) R.color.colorPrimary else R.color.secondPage))
                }
            })

            setOnClickListener {
                switchFragment()
            }
        }

        login_fragment.hide()
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        if (mAuth?.currentUser != null) {
            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
            finish()
        } else if (Prefs.with(this).readBoolean(IS_LOGIN_SKIPPED, false)) {
            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
            finish()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        login_fragment.apply {
            pivotX = login_fragment.width / 2.toFloat()
            pivotY = login_fragment.height.toFloat()
        }
        sign_up_fragment.apply {
            pivotX = sign_up_fragment.width / 2.toFloat()
            pivotY = sign_up_fragment.height.toFloat()
        }
    }

    private fun switchFragment() {
        if (isLogin) {
            login_fragment.show()
            login_fragment.animate().rotation(0f).setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    sign_up_fragment.apply {
                        hide()
                        rotation = 90f
                    }
                    wrapper.setDrawOrder(FlexibleFrameLayout.ORDER_LOGIN_STATE)
                }
            })
        } else {
            sign_up_fragment.show()
            sign_up_fragment.animate().rotation(0f).setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    login_fragment.apply {
                        hide()
                        rotation = -90f
                    }
                    wrapper.setDrawOrder(FlexibleFrameLayout.ORDER_SIGN_UP_STATE)
                }
            })
        }
        isLogin = !isLogin
        button.startAnimation()
    }
}