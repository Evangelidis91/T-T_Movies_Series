package com.evangelidis.t_tmoviesseries.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import androidx.recyclerview.widget.LinearLayoutManager
import com.evangelidis.t_tmoviesseries.R
import com.evangelidis.t_tmoviesseries.callbacks.OnWatchlistClickCallback
import com.evangelidis.t_tmoviesseries.extensions.gone
import com.evangelidis.t_tmoviesseries.extensions.show
import com.evangelidis.t_tmoviesseries.room.DbWorkerThread
import com.evangelidis.t_tmoviesseries.room.WatchlistData
import com.evangelidis.t_tmoviesseries.room.WatchlistDataBase
import com.evangelidis.t_tmoviesseries.utils.Constants
import com.evangelidis.t_tmoviesseries.utils.Constants.DATABASE_THREAD
import com.evangelidis.t_tmoviesseries.utils.InternetStatus
import com.evangelidis.t_tmoviesseries.view.adapters.WatchlistAdapter
import com.evangelidis.tantintoast.TanTinToast
import kotlinx.android.synthetic.main.activity_wishlist.*

class WatchlistActivity : AppCompatActivity() {

    var watchlistCallback: OnWatchlistClickCallback = object :
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
                TanTinToast.Warning(this@WatchlistActivity).text(getString(R.string.no_internet)).show()
            }
        }
    }

    private val watchlistAdapter = WatchlistAdapter(arrayListOf(), watchlistCallback)
    private var mDb: WatchlistDataBase? = null
    private lateinit var mDbWorkerThread: DbWorkerThread
    private val mUiHandler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wishlist)

        mDbWorkerThread = DbWorkerThread(DATABASE_THREAD)
        mDbWorkerThread.start()
        mDb = WatchlistDataBase.getInstance(this)

        getDataFromDB()
    }

    override fun onResume() {
        super.onResume()
        loading_view.show()
        getDataFromDB()
    }

    private fun getDataFromDB() {
        Handler().postDelayed(
            {
                val task = Runnable {
                    val watchlistData = mDb?.todoDao()?.getAll()
                    mUiHandler.post {
                        if (!watchlistData.isNullOrEmpty()) {
                            wishlist_list.apply {
                                layoutManager = LinearLayoutManager(context)
                                adapter = watchlistAdapter
                            }
                            watchlistAdapter.appendWishlistData(watchlistData)
                            loading_view.gone()
                        }
                    }
                }
                mDbWorkerThread.postTask(task)
            },
            400
        )
    }
}