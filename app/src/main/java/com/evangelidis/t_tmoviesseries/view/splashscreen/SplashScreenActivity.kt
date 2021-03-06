package com.evangelidis.t_tmoviesseries.view.splashscreen

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.evangelidis.t_tmoviesseries.R
import com.evangelidis.t_tmoviesseries.utils.Constants.IS_NOTIFICATION_ON
import com.evangelidis.t_tmoviesseries.utils.Constants.SPLASHSCREEN_TIME
import com.evangelidis.t_tmoviesseries.view.login.LoginActivity
import es.dmoral.prefs.Prefs

class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splah)

        this.window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        if (!Prefs.with(applicationContext).contains(IS_NOTIFICATION_ON)) {
            Prefs.with(applicationContext).writeBoolean(IS_NOTIFICATION_ON, true)
        }

        val handler = Handler()
        handler.postDelayed({
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }, SPLASHSCREEN_TIME)
    }

    override fun onBackPressed() {}
}
