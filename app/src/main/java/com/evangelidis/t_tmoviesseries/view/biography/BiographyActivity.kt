package com.evangelidis.t_tmoviesseries.view.biography

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.evangelidis.t_tmoviesseries.R
import com.evangelidis.t_tmoviesseries.databinding.ActivityBiographyBinding
import com.evangelidis.t_tmoviesseries.utils.Constants.ACTOR_NAME
import com.evangelidis.t_tmoviesseries.utils.Constants.BIOGRAPHY_TEXT
import com.evangelidis.t_tmoviesseries.view.main.MainActivity
import com.evangelidis.t_tmoviesseries.view.search.SearchActivity

class BiographyActivity : AppCompatActivity() {

    private val binding: ActivityBiographyBinding by lazy { ActivityBiographyBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val biography = intent.getStringExtra(BIOGRAPHY_TEXT)
        val actorName = intent.getStringExtra(ACTOR_NAME)

        binding.actorName.text = actorName
        binding.actorBioText.text = biography

        binding.toolbar.toolbarTitle.text = resources.getString(R.string.biography_label)

        binding.toolbar.imageToMain.setOnClickListener {
            val intent = Intent(this@BiographyActivity, MainActivity::class.java)
            startActivity(intent)
        }

        binding.toolbar.searchIcn.setOnClickListener {
            val intent = Intent(this@BiographyActivity, SearchActivity::class.java)
            startActivity(intent)
        }
    }
}
