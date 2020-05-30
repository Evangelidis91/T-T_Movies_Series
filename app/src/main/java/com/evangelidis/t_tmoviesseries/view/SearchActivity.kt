package com.evangelidis.t_tmoviesseries.view

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
import com.evangelidis.t_tmoviesseries.model.Multisearch
import com.evangelidis.t_tmoviesseries.room.DbWorkerThread
import com.evangelidis.t_tmoviesseries.room.WatchlistDataBase
import com.evangelidis.t_tmoviesseries.utils.Constants.DATABASE_THREAD
import com.evangelidis.t_tmoviesseries.utils.Constants.MOVIE_ID
import com.evangelidis.t_tmoviesseries.utils.Constants.PERSON_ID
import com.evangelidis.t_tmoviesseries.utils.Constants.TV_SHOW_ID
import com.evangelidis.t_tmoviesseries.utils.InternetStatus
import com.evangelidis.t_tmoviesseries.view.adapters.SearchAdapter
import com.evangelidis.t_tmoviesseries.viewmodel.ListViewModel
import com.evangelidis.tantintoast.TanTinToast
import kotlinx.android.synthetic.main.activity_search.*

class SearchActivity : AppCompatActivity() {

    var trendCallback: OnTrendingClickCallback = object :
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

    lateinit var viewModel: ListViewModel
    private val trendsAdapter = SearchAdapter(arrayListOf(), trendCallback, mutableListOf())

    private val trendsList = mutableListOf<Multisearch>()

    private var mDb: WatchlistDataBase? = null
    private lateinit var mDbWorkerThread: DbWorkerThread
    private val mUiHandler = Handler()

    private var typeface: Typeface? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        typeface  = ResourcesCompat.getFont(this, R.font.montserrat_regular)

        mDbWorkerThread = DbWorkerThread(DATABASE_THREAD)
        mDbWorkerThread.start()
        mDb = WatchlistDataBase.getInstance(this)

        getDataFromDB()

        viewModel = ViewModelProviders.of(this).get(ListViewModel::class.java)

        for (x in 1..5) {
            viewModel.getTrendings(1)
        }

        trendingList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = trendsAdapter
        }

        searchText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                if (after == 0){
                    trendsAdapter.appendTrendings(trendsList)
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
        val task = Runnable {
            val watchlistData = mDb?.todoDao()?.getAll()
            mUiHandler.post {
                if (!watchlistData.isNullOrEmpty()) {
                    trendsAdapter.updateWatchlist(watchlistData)
                }
            }
        }
        mDbWorkerThread.postTask(task)
    }
}