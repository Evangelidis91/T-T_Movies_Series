package com.evangelidis.t_tmoviesseries.view.seasons

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.evangelidis.t_tmoviesseries.databinding.ActivitySeasonsBinding
import com.evangelidis.t_tmoviesseries.model.TvShowSeasonResponse

class SeasonsActivity : AppCompatActivity() {

    companion object {
        const val TOTAL_SEASONS = "TOTAL_SEASONS"
        const val TV_SHOW_ID = "TV_SHOW_ID"
        const val TV_SHOW_NAME = "TV_SHOW_NAME"

        fun createIntent(context: Context, seasonsNumber: Int, showId: Int, name: String): Intent =
            Intent(context, SeasonsActivity::class.java)
                .putExtra(TOTAL_SEASONS, seasonsNumber)
                .putExtra(TV_SHOW_ID, showId)
                .putExtra(TV_SHOW_NAME, name)
    }

    private lateinit var viewModel: ViewModelSeasons
    private var listOfSeasons: MutableList<TvShowSeasonResponse> = mutableListOf()

    private val adapter = SeasonsViewPagerAdapter(supportFragmentManager)
    private val binding: ActivitySeasonsBinding by lazy { ActivitySeasonsBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val numberOfSeasons = intent.getIntExtra(TOTAL_SEASONS, 1)
        val tvShowId = intent.getIntExtra(TV_SHOW_ID, 1)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = intent.getStringExtra(TV_SHOW_NAME)

        binding.tabs.setupWithViewPager(binding.viewpager)

        viewModel = ViewModelProviders.of(this).get(ViewModelSeasons::class.java)

        for (x in 1..numberOfSeasons) {
            viewModel.getTvShowSeasonDetails(tvShowId, x)
        }

        observeViewModel(numberOfSeasons)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun observeViewModel(numberOfSeasons: Int) {
        viewModel.tvShowSeasonDetails.observe(this, Observer { data ->
            data?.let {
                listOfSeasons.add(it)
                if (listOfSeasons.size == numberOfSeasons) {
                    setAdapter()
                }
            }
        })
    }

    private fun setAdapter() {
        listOfSeasons.sortBy { it.seasonNumber }
        for (x in 0 until listOfSeasons.size) {
            adapter.addFragment(SeasonEpisodesFragment(listOfSeasons[x]), listOfSeasons[x].seasonNumber.toString())
            binding.viewpager.adapter = adapter
        }
    }
}
