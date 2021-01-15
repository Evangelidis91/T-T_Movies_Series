package com.evangelidis.t_tmoviesseries.view.watchlist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.evangelidis.t_tmoviesseries.R
import com.evangelidis.t_tmoviesseries.databinding.ActivityWatchlistBinding
import com.evangelidis.t_tmoviesseries.extensions.gone
import com.evangelidis.t_tmoviesseries.extensions.show
import com.evangelidis.t_tmoviesseries.room.*
import com.evangelidis.t_tmoviesseries.utils.Constants
import com.evangelidis.t_tmoviesseries.utils.ItemsManager.underline
import com.evangelidis.t_tmoviesseries.view.main.MainActivity
import com.evangelidis.t_tmoviesseries.view.movie.MovieActivity
import com.evangelidis.t_tmoviesseries.view.search.SearchActivity
import com.evangelidis.t_tmoviesseries.view.tvshow.TvShowActivity

class WatchlistActivity : AppCompatActivity(), WatchListItemCallback {

    companion object {
        const val TV = "TV"
        const val Movie = "Movie"

        fun createIntent(context: Context): Intent =
            Intent(context, WatchlistActivity::class.java)
    }

    private val watchlistAdapter by lazy { WatchlistAdapter(this) }
    private val binding: ActivityWatchlistBinding by lazy { ActivityWatchlistBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.watchlistList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = watchlistAdapter
        }

        setToolbar()
    }

    override fun onResume() {
        super.onResume()
        getDataFromDB()
    }

    private fun setToolbar() {
        with(binding.toolbar) {
            toolbarTitle.text = getString(R.string.my_watchlist).underline()
            imageToMain.setOnClickListener {
                startActivity(Intent(this@WatchlistActivity, MainActivity::class.java))
            }
            searchIcn.setOnClickListener {
                startActivity(Intent(this@WatchlistActivity, SearchActivity::class.java))
            }
        }
    }

    private fun getDataFromDB() {
        binding.watchlistList.gone()
        DatabaseQueries.getSavedItems(this) { watchlistData ->
            if (!watchlistData.isNullOrEmpty()) {
                binding.emptyWatchlistText.gone()
                watchlistAdapter.appendWatchlistData(watchlistData.reversed())
                binding.watchlistList.show()
            } else {
                displayEmptyList()
            }
            binding.loadingView.gone()
        }
    }

    override fun displayEmptyList() {
        binding.watchlistList.gone()
        binding.emptyWatchlistText.show()
    }

    override fun navigateToMovie(itemId: Int) {
        val intent = Intent(this, MovieActivity::class.java)
        intent.putExtra(Constants.MOVIE_ID, itemId)
        startActivity(intent)
    }

    override fun navigateToTvShow(itemId: Int) {
        val intent = Intent(this, TvShowActivity::class.java)
        intent.putExtra(Constants.TV_SHOW_ID, itemId)
        startActivity(intent)
    }
}
