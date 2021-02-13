package com.evangelidis.t_tmoviesseries.view.biography

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.evangelidis.t_tmoviesseries.R
import com.evangelidis.t_tmoviesseries.databinding.ActivityBiographyBinding
import com.evangelidis.t_tmoviesseries.utils.InternetStatus
import com.evangelidis.t_tmoviesseries.view.main.MainActivity
import com.evangelidis.t_tmoviesseries.view.search.SearchActivity
import com.evangelidis.tantintoast.TanTinToast

class BiographyActivity : AppCompatActivity() {

    companion object {
        const val BIOGRAPHY_TEXT = "BIOGRAPHY_TEXT"
        const val ACTOR_NAME = "ACTOR_NAME"

        fun createIntent(context: Context, biography: String, actorName: String): Intent =
            Intent(context, BiographyActivity::class.java)
                .putExtra(BIOGRAPHY_TEXT, biography)
                .putExtra(ACTOR_NAME, actorName)
    }

    private val binding: ActivityBiographyBinding by lazy { ActivityBiographyBinding.inflate(layoutInflater) }
    private var typeface: Int = R.font.montserrat_regular

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setToolbar(intent.getStringExtra(ACTOR_NAME).orEmpty())
        binding.actorBioText.text = intent.getStringExtra(BIOGRAPHY_TEXT)
    }

    private fun setToolbar(actorName: String) {
        with(binding.toolbar) {
            if (actorName.isEmpty()) {
                toolbarTitle.text = resources.getString(R.string.biography_label)
            } else {
                toolbarTitle.text = actorName
            }
            imageToMain.setOnClickListener {
                startActivity(MainActivity.createIntent(this@BiographyActivity))
            }
            searchIcn.setOnClickListener {
                if (InternetStatus.isConnected(this@BiographyActivity)) {
                    startActivity(SearchActivity.createIntent(this@BiographyActivity))
                } else {
                    TanTinToast.Warning(this@BiographyActivity).text(getString(R.string.no_internet)).typeface(typeface).show()
                }
            }
        }
    }
}
