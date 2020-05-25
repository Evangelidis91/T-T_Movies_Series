package com.evangelidis.t_tmoviesseries.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.evangelidis.t_tmoviesseries.R
import com.evangelidis.t_tmoviesseries.utils.Constants.ACTOR_NAME
import com.evangelidis.t_tmoviesseries.utils.Constants.BIOGRAPHY_TEXT
import kotlinx.android.synthetic.main.activity_biography.*
import kotlinx.android.synthetic.main.main_toolbar.*

class BiographyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_biography)

        val biography = intent.getStringExtra(BIOGRAPHY_TEXT)
        val actorName = intent.getStringExtra(ACTOR_NAME)

        nameOfActor.text = actorName
        bio.text = biography

        toolbar_title.text = resources.getString(R.string.biography_label)

        imageToMain.setOnClickListener{
            val intent = Intent(this@BiographyActivity, MainActivity::class.java)
            startActivity(intent)
        }

        search_img.setOnClickListener {
            val intent = Intent(this@BiographyActivity, SearchActivity::class.java)
            startActivity(intent)
        }
    }
}