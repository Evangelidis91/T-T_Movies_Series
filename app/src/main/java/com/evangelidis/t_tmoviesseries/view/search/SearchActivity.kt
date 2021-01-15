package com.evangelidis.t_tmoviesseries.view.search

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.evangelidis.t_tmoviesseries.callbacks.OnTrendingClickCallback
import com.evangelidis.t_tmoviesseries.R
import com.evangelidis.t_tmoviesseries.databinding.ActivitySearchBinding
import com.evangelidis.t_tmoviesseries.extensions.gone
import com.evangelidis.t_tmoviesseries.extensions.show
import com.evangelidis.t_tmoviesseries.model.Multisearch
import com.evangelidis.t_tmoviesseries.room.DatabaseQueries
import com.evangelidis.t_tmoviesseries.room.WatchlistDataBase
import com.evangelidis.t_tmoviesseries.utils.Constants.DATABASE_THREAD
import com.evangelidis.t_tmoviesseries.utils.Constants.MOVIE_ID
import com.evangelidis.t_tmoviesseries.utils.Constants.PERSON_ID
import com.evangelidis.t_tmoviesseries.utils.Constants.TV_SHOW_ID
import com.evangelidis.t_tmoviesseries.utils.InternetStatus
import com.evangelidis.t_tmoviesseries.view.movie.MovieActivity
import com.evangelidis.t_tmoviesseries.view.person.PersonActivity
import com.evangelidis.t_tmoviesseries.view.tvshow.TvShowActivity
import com.evangelidis.tantintoast.TanTinToast

class SearchActivity : AppCompatActivity() {

    private var trendCallback: OnTrendingClickCallback = object :
        OnTrendingClickCallback {
        override fun onClick(trend: Multisearch) {
            if (InternetStatus.getInstance(applicationContext).isOnline) {
                when (trend.mediaType) {
                    "tv" -> {
                        val intent = Intent(this@SearchActivity, TvShowActivity::class.java)
                        intent.putExtra(TV_SHOW_ID, trend.id)
                        startActivity(intent)
                    }
                    "movie" -> {
                        val intent = Intent(this@SearchActivity, MovieActivity::class.java)
                        intent.putExtra(MOVIE_ID, trend.id)
                        startActivity(intent)
                    }
                    else -> {
                        val intent = Intent(this@SearchActivity, PersonActivity::class.java)
                        intent.putExtra(PERSON_ID, trend.id)
                        startActivity(intent)
                    }
                }
            } else {
                TanTinToast.Warning(this@SearchActivity).text(getString(R.string.no_internet)).typeface(typeface).show()
            }
        }
    }

    private lateinit var viewModel: ViewModelSearch
    private val trendsAdapter = SearchAdapter(arrayListOf(), trendCallback, mutableListOf())

    private val trendsList = mutableListOf<Multisearch>()

    private var typeface: Typeface? = null

    val binding: ActivitySearchBinding by lazy { ActivitySearchBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        typeface = ResourcesCompat.getFont(this, R.font.montserrat_regular)

        getDataFromDB()

        viewModel = ViewModelProviders.of(this).get(ViewModelSearch::class.java)

        for (x in 1..5) {
            viewModel.getTrendings(1)
        }

        binding.trendingList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = trendsAdapter
        }

        binding.searchTitle.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                if (after == 0) {
                    trendsAdapter.appendTrendings(trendsList)
                    binding.trendingLabel.show()
                } else {
                    binding.trendingLabel.gone()
                }
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.let {
                    viewModel.getMultisearchResult(it.toString(), 1)
                }
            }
        })
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        getDataFromDB()
    }

    private fun observeViewModel() {

        viewModel.trendings.observe(this, Observer { data ->
            data.results?.let {
                trendsList.clear()
                trendsList.addAll(it)
                trendsAdapter.appendTrendings(it)
            }
        })

        viewModel.multisearch.observe(this, Observer { data ->
            data.results?.let {
                trendsAdapter.appendTrendings(it)
            }
        })
    }

    private fun getDataFromDB() {
        DatabaseQueries.getSavedItems(this){watchlistData->
            if (!watchlistData.isNullOrEmpty()) {
                trendsAdapter.updateWatchlist(watchlistData)
            }
        }
    }
}