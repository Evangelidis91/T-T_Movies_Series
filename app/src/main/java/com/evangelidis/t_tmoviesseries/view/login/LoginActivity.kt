package com.evangelidis.t_tmoviesseries.view.login

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.evangelidis.t_tmoviesseries.R
import com.evangelidis.t_tmoviesseries.databinding.ActivityLoginBinding
import com.evangelidis.t_tmoviesseries.extensions.hide
import com.evangelidis.t_tmoviesseries.extensions.show
import com.evangelidis.t_tmoviesseries.utils.Constants.IS_LOGIN_SKIPPED
import com.evangelidis.t_tmoviesseries.view.main.MainActivity
import com.google.firebase.auth.FirebaseAuth
import es.dmoral.prefs.Prefs

class LoginActivity : AppCompatActivity() {

    private var isLogin = true
    private var mAuth: FirebaseAuth? = null

    private val binding: ActivityLoginBinding by lazy { ActivityLoginBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()
        val topLoginFragment = LoginFragment()
        val topSignUpFragment = SignUpFragment()

        supportFragmentManager.beginTransaction()
            .replace(R.id.login_fragment, topLoginFragment)
            .replace(R.id.sign_up_fragment, topSignUpFragment)
            .commit()

        binding.loginFragment.rotation = -90f

        binding.button.apply {
            setOnButtonSwitched(object : OnButtonSwitchedListener {
                override fun onButtonSwitched(isLogin: Boolean) {
                    binding.loginLayout.setBackgroundColor(ContextCompat.getColor(this@LoginActivity, if (isLogin) R.color.colorPrimary else R.color.secondPage))
                }
            })
            setOnClickListener {
                switchFragment()
            }
        }

        binding.loginFragment.hide()
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        if ((mAuth?.currentUser != null) || (Prefs.with(this).readBoolean(IS_LOGIN_SKIPPED, false))) {
            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
            finish()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        binding.loginFragment.apply {
            pivotX = this.width / 2.toFloat()
            pivotY = this.height.toFloat()
        }
        binding.signUpFragment.apply {
            pivotX = this.width / 2.toFloat()
            pivotY = this.height.toFloat()
        }
    }

    private fun switchFragment() {
        if (isLogin) {
            binding.loginFragment.apply {
                show()
                animate().rotation(0f).setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        binding.signUpFragment.apply {
                            hide()
                            rotation = 90f
                        }
                        binding.wrapper.setDrawOrder(FlexibleFrameLayout.ORDER_LOGIN_STATE)
                    }
                })
            }
        } else {
            binding.signUpFragment.apply {
                show()
                animate().rotation(0f).setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        binding.loginFragment.apply {
                            hide()
                            rotation = -90f
                        }
                        binding.wrapper.setDrawOrder(FlexibleFrameLayout.ORDER_SIGN_UP_STATE)
                    }
                })
            }
        }
        isLogin = !isLogin
        binding.button.startAnimation()
    }
}
