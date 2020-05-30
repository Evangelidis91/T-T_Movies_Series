package com.evangelidis.t_tmoviesseries.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.evangelidis.t_tmoviesseries.R
import com.evangelidis.t_tmoviesseries.view.fragments.SeasonEpisodesFragment
import com.evangelidis.t_tmoviesseries.view.adapters.SeasonsViewPagerAdapter
import com.evangelidis.t_tmoviesseries.model.TvShowSeasonResponse
import com.evangelidis.t_tmoviesseries.utils.Constants.TOTAL_SEASONS
import com.evangelidis.t_tmoviesseries.utils.Constants.TV_SHOW_ID
import com.evangelidis.t_tmoviesseries.utils.Constants.TV_SHOW_NAME
import com.evangelidis.t_tmoviesseries.viewmodel.ListViewModel
import com.google.android.material.tabs.TabLayout

class SeasonsActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager

    private var numberOfSeasons = 1
    private var tvShowId = 0

    lateinit var viewModel: ListViewModel

    private var listOfSeasons: MutableList<TvShowSeasonResponse> = mutableListOf()

    val adapter = SeasonsViewPagerAdapter(supportFragmentManager)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seasons)

        numberOfSeasons = intent.getIntExtra(TOTAL_SEASONS, 1)
        tvShowId = intent.getIntExtra(TV_SHOW_ID, tvShowId)

        toolbar = findViewById(R.id.toolbar)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = intent.getStringExtra(TV_SHOW_NAME)

        viewPager = findViewById(R.id.viewpager)

        tabLayout = findViewById(R.id.tabs)
        tabLayout.setupWithViewPager(viewPager)

        viewModel = ViewModelProviders.of(this).get(ListViewModel::class.java)

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
            viewPager.adapter = adapter
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