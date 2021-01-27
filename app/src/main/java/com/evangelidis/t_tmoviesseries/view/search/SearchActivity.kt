package com.evangelidis.t_tmoviesseries.view.search

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.evangelidis.t_tmoviesseries.R
import com.evangelidis.t_tmoviesseries.databinding.ActivitySearchBinding
import com.evangelidis.t_tmoviesseries.extensions.gone
import com.evangelidis.t_tmoviesseries.extensions.show
import com.evangelidis.t_tmoviesseries.model.Multisearch
import com.evangelidis.t_tmoviesseries.room.DatabaseQueries
import com.evangelidis.t_tmoviesseries.view.movie.MovieActivity
import com.evangelidis.t_tmoviesseries.view.person.PersonActivity
import com.evangelidis.t_tmoviesseries.view.tvshow.TvShowActivity

class SearchActivity : AppCompatActivity(), SearchCallback {

    private lateinit var viewModel: ViewModelSearch
    private val trendsAdapter = SearchAdapter(this)

    private val trendsList = mutableListOf<Multisearch>()

    private var typeface: Typeface? = null

    val binding: ActivitySearchBinding by lazy { ActivitySearchBinding.inflate(layoutInflater) }

    companion object {
        fun createIntent(context: Context): Intent =
            Intent(context, SearchActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        typeface = ResourcesCompat.getFont(this, R.font.montserrat_regular)

        viewModel = ViewModelProviders.of(this).get(ViewModelSearch::class.java)
        viewModel.getMoviesGenres()
        viewModel.getTvShowGenres()
        viewModel.getTrends(1)

        binding.trendingList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = trendsAdapter
        }

        binding.searchTitle.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                if (after == 0) {
                    trendsAdapter.trendsList = trendsList
                    binding.trendingLabel.show()
                } else {
                    binding.trendingLabel.gone()
                }
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.let {
                    viewModel.getMultiSearchResult(it.toString(), 1)
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
        viewModel.genresMovieData.observe(this, Observer { data ->
            data.genres?.let {
                trendsAdapter.moviesGenres = it
            }
        })

        viewModel.genresTvShowData.observe(this, Observer { data ->
            data.genres?.let {
                trendsAdapter.tvShowGenres = it
            }
        })

        viewModel.trends.observe(this, Observer { data ->
            data.results?.let {
                trendsList.clear()
                trendsList.addAll(it)
                trendsAdapter.trendsList = it
            }
        })

        viewModel.multiSearch.observe(this, Observer { data ->
            data.results?.let {
                trendsAdapter.trendsList = it
            }
        })
    }

    private fun getDataFromDB() {
        DatabaseQueries.getSavedItems(this) { watchlistData ->
            watchlistData?.let {
                trendsAdapter.watchlist = it
            }
        }
    }

    override fun navigateToMovie(itemId: Int) {
        startActivity(MovieActivity.createIntent(this, itemId))
    }

    override fun navigateToTvShow(itemId: Int) {
        startActivity(TvShowActivity.createIntent(this, itemId))
    }

    override fun navigateToPerson(itemId: Int) {
        startActivity(PersonActivity.createIntent(this, itemId))
    }
}
