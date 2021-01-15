package com.evangelidis.t_tmoviesseries.view.seasons

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.evangelidis.t_tmoviesseries.databinding.ActivitySeasonsBinding
import com.evangelidis.t_tmoviesseries.model.TvShowSeasonResponse
import com.evangelidis.t_tmoviesseries.utils.Constants.TOTAL_SEASONS
import com.evangelidis.t_tmoviesseries.utils.Constants.TV_SHOW_ID
import com.evangelidis.t_tmoviesseries.utils.Constants.TV_SHOW_NAME

class SeasonsActivity : AppCompatActivity() {

    private var numberOfSeasons = 1
    private var tvShowId = 0

    private lateinit var viewModel: ViewModelSeasons

    private var listOfSeasons: MutableList<TvShowSeasonResponse> = mutableListOf()

    val adapter = SeasonsViewPagerAdapter(supportFragmentManager)

    private val binding: ActivitySeasonsBinding by lazy { ActivitySeasonsBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        numberOfSeasons = intent.getIntExtra(TOTAL_SEASONS, 1)
        tvShowId = intent.getIntExtra(TV_SHOW_ID, tvShowId)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = intent.getStringExtra(TV_SHOW_NAME)

        binding.tabs.setupWithViewPager(binding.viewpager)

        viewModel = ViewModelProviders.of(this).get(ViewModelSeasons::class.java)

        for (x in 1..numberOfSeasons) {
            viewModel.getTvShowSeasonDetails(tvShowId, x)
        }

        observeViewModel()
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

    private fun setAdapter() {
        listOfSeasons.sortBy { it.seasonNumber }
        for (x in 0 until listOfSeasons.size) {
            adapter.addFragment(SeasonEpisodesFragment(listOfSeasons[x]), listOfSeasons[x].seasonNumber.toString())
            binding.viewpager.adapter = adapter
        }
    }

    private fun observeViewModel() {
        viewModel.tvShowSeasonDetails.observe(this, Observer { data ->
            data?.let {
                listOfSeasons.add(it)
                if (listOfSeasons.size == numberOfSeasons) {
                    setAdapter()
                }
            }
        })
    }
}
