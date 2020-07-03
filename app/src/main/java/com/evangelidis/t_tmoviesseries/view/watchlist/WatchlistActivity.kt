package com.evangelidis.t_tmoviesseries.view.watchlist

import android.content.Intent
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.evangelidis.t_tmoviesseries.R
import com.evangelidis.t_tmoviesseries.callbacks.OnWatchlistClickCallback
import com.evangelidis.t_tmoviesseries.databinding.ActivityWatchlistBinding
import com.evangelidis.t_tmoviesseries.extensions.gone
import com.evangelidis.t_tmoviesseries.extensions.show
import com.evangelidis.t_tmoviesseries.room.DbWorkerThread
import com.evangelidis.t_tmoviesseries.room.WatchlistData
import com.evangelidis.t_tmoviesseries.room.WatchlistDataBase
import com.evangelidis.t_tmoviesseries.utils.Constants
import com.evangelidis.t_tmoviesseries.utils.Constants.DATABASE_THREAD
import com.evangelidis.t_tmoviesseries.utils.InternetStatus
import com.evangelidis.t_tmoviesseries.utils.ItemsManager.underline
import com.evangelidis.t_tmoviesseries.view.main.MainActivity
import com.evangelidis.t_tmoviesseries.view.movie.MovieActivity
import com.evangelidis.t_tmoviesseries.view.search.SearchActivity
import com.evangelidis.t_tmoviesseries.view.tvshow.TvShowActivity
import com.evangelidis.tantintoast.TanTinToast

class WatchlistActivity : AppCompatActivity() {

    private var watchlistCallback: OnWatchlistClickCallback = object :
        OnWatchlistClickCallback {
        override fun onClick(watchlist: WatchlistData) {
            if (InternetStatus.getInstance(applicationContext).isOnline) {
                when (watchlist.category) {
                    "TV" -> {
                        val intent = Intent(this@WatchlistActivity, TvShowActivity::class.java)
                        intent.putExtra(Constants.TV_SHOW_ID, watchlist.itemId)
                        startActivity(intent)
                    }
                    "Movie" -> {
                        val intent = Intent(this@WatchlistActivity, MovieActivity::class.java)
                        intent.putExtra(Constants.MOVIE_ID, watchlist.itemId)
                        startActivity(intent)
                    }
                }
            } else {
                TanTinToast.Warning(this@WatchlistActivity).text(getString(R.string.no_internet)).typeface(typeface).show()
            }
        }
    }

    private val watchlistAdapter = WatchlistAdapter(arrayListOf(), watchlistCallback, this)
    private var mDb: WatchlistDataBase? = null
    private lateinit var mDbWorkerThread: DbWorkerThread
    private val mUiHandler = Handler()

    private var typeface: Typeface? = null

    private val binding: ActivityWatchlistBinding by lazy { ActivityWatchlistBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_watchlist)
        typeface = ResourcesCompat.getFont(this, R.font.montserrat_regular)

        mDbWorkerThread = DbWorkerThread(DATABASE_THREAD)
        mDbWorkerThread.start()
        mDb = WatchlistDataBase.getInstance(this)

        binding.toolbar.toolbarTitle.text = getString(R.string.my_watchlist).underline()

        binding.toolbar.imageToMain.setOnClickListener {
            val intent = Intent(this@WatchlistActivity, MainActivity::class.java)
            startActivity(intent)
        }

        binding.toolbar.searchIcn.setOnClickListener {
            val intent = Intent(this@WatchlistActivity, SearchActivity::class.java)
            startActivity(intent)
        }

        getDataFromDB()
    }

    override fun onResume() {
        super.onResume()
        binding.loadingView.show()
        getDataFromDB()
    }

    private fun getDataFromDB() {
        Handler().postDelayed(
            {
                val task = Runnable {
                    val watchlistData = mDb?.todoDao()?.getAll()
                    mUiHandler.post {
                        if (!watchlistData.isNullOrEmpty()) {
                            binding.emptyWatchlistText.gone()
                            binding.watchlistList.apply {
                                layoutManager = LinearLayoutManager(context)
                                adapter = watchlistAdapter
                            }
                            watchlistAdapter.appendWatchlistData(watchlistData.reversed())

                        } else {
                            binding.emptyWatchlistText.show()
                        }
                        binding.loadingView.gone()
                    }
                }
                mDbWorkerThread.postTask(task)
            },
            400
        )
    }

    fun displayEmptyList() {
        binding.emptyWatchlistText.show()
    }
}