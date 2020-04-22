package com.evangelidis.t_tmoviesseries.view

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.evangelidis.t_tmoviesseries.OnMoviesClickCallback
import com.evangelidis.t_tmoviesseries.R
import com.evangelidis.t_tmoviesseries.model.Movie
import com.evangelidis.t_tmoviesseries.utils.Constants.AIRING_TODAY_TV
import com.evangelidis.t_tmoviesseries.utils.Constants.MOVIE_ID
import com.evangelidis.t_tmoviesseries.utils.Constants.ON_THE_AIR_TV
import com.evangelidis.t_tmoviesseries.utils.Constants.PLAYING_NOW_MOVIES
import com.evangelidis.t_tmoviesseries.utils.Constants.POPULAR_MOVIES
import com.evangelidis.t_tmoviesseries.utils.Constants.POPULAR_TV
import com.evangelidis.t_tmoviesseries.utils.Constants.TOP_RATED_MOVIES
import com.evangelidis.t_tmoviesseries.utils.Constants.TOP_RATED_TV
import com.evangelidis.t_tmoviesseries.utils.Constants.UPCOMING_MOVIES
import com.evangelidis.t_tmoviesseries.utils.InternetStatus
import com.evangelidis.t_tmoviesseries.viewmodel.ListViewModel
import com.evangelidis.tantintoast.TanTinToast
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.navigation_drawer.*

class MainActivity : AppCompatActivity() {

    private var movieCallback: OnMoviesClickCallback = object :
        OnMoviesClickCallback {
        override fun onClick(movie: Movie) {
            if (InternetStatus.getInstance(applicationContext).isOnline) {
                val intent = Intent(this@MainActivity, MovieActivity::class.java)
                intent.putExtra(MOVIE_ID, movie.id)
                startActivity(intent)
            } else {
                TanTinToast.Warning(this@MainActivity).text(getString(R.string.no_internet))
                    .time(Toast.LENGTH_LONG).show()
            }
        }
    }

    lateinit var viewModel: ListViewModel
    private val moviesListAdapter = MoviesListAdapter(arrayListOf(), movieCallback)
    private val tvShowAdapter = TvShowAdapter(arrayListOf())
    private var sortBy = POPULAR_MOVIES

    var listOfRetrievedPages = arrayListOf(1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar_title.text = getString(R.string.popular_movies)

        expandableLayoutMovies.collapse()
        expandableLayoutTv.collapse()
        expandableLayoutCommunicate.expand()

        viewModel = ViewModelProviders.of(this).get(ListViewModel::class.java)
        viewModel.getMoviesGenres()
        viewModel.getTvShowGenres()
        viewModel.getPopularMovies(1)

        moviesList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = moviesListAdapter
        }

        tvshowList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = tvShowAdapter
        }

        observeViewModel()
        setUpScrollListener()
        setupNavigationViewClickListeners()
    }

    private fun setupNavigationViewClickListeners() {

        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        val toggle = ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawer.addDrawerListener(toggle)
        toggle.syncState()
        navigationView.itemTextColor = ColorStateList.valueOf(Color.WHITE)

        nav_movies.setOnClickListener { expandMoviesLayout() }
        nav_tv.setOnClickListener { expandTvLayout() }
        nav_communicate.setOnClickListener { expandCommunicateLayout() }

        nav_popular_movies.setOnClickListener {
            Log.i("Menu Option", "POPULAR_MOVIES")
            toolbar_title.text = getString(R.string.popular_movies)
            if (sortBy != POPULAR_MOVIES) {
                sortBy = POPULAR_MOVIES
                listOfRetrievedPages = arrayListOf(1)
                viewModel.getPopularMovies(1)
            }
            drawer.closeDrawer(GravityCompat.START)
        }

        nav_playing_now_movies.setOnClickListener {
            Log.i("Menu Option", "PLAYING_NOW_MOVIES")
            toolbar_title.text = getString(R.string.playing_now_movies)
            if (sortBy != PLAYING_NOW_MOVIES) {
                sortBy = PLAYING_NOW_MOVIES
                listOfRetrievedPages = arrayListOf(1)
                viewModel.getPlayingNowMovies(1)
            }
            drawer.closeDrawer(GravityCompat.START)
        }

        nav_top_rated_movies.setOnClickListener {
            Log.i("Menu Option", "TOP_RATED_MOVIES")
            toolbar_title.text = getString(R.string.top_rated_movies)
            if (sortBy != TOP_RATED_MOVIES) {
                sortBy = TOP_RATED_MOVIES
                listOfRetrievedPages = arrayListOf(1)
                viewModel.getTopRatedMovies(1)
            }
            drawer.closeDrawer(GravityCompat.START)
        }

        nav_upcoming_movies.setOnClickListener {
            Log.i("Menu Option", "UPCOMING_MOVIES")
            toolbar_title.text = getString(R.string.upcoming_movies)
            if (sortBy != UPCOMING_MOVIES) {
                sortBy = UPCOMING_MOVIES
                listOfRetrievedPages = arrayListOf(1)
                viewModel.getUpcomingMovies(1)
            }
            drawer.closeDrawer(GravityCompat.START)
        }


        nav_popular_tv_shows.setOnClickListener {
            Log.i("Menu Option", "POPULAR_TV")
            toolbar_title.text = getString(R.string.popular_tv_shows)
            if (sortBy != POPULAR_TV) {
                sortBy = POPULAR_TV
                listOfRetrievedPages = arrayListOf(1)
                viewModel.getPopularTvShows(1)
            }
            drawer.closeDrawer(GravityCompat.START)
        }

        nav_top_rated_tv_shows.setOnClickListener {
            Log.i("Menu Option", "TOP_RATED_TV")
            toolbar_title.text = getString(R.string.top_rated_tv_shows)
            if (sortBy != TOP_RATED_TV) {
                sortBy = TOP_RATED_TV
                listOfRetrievedPages = arrayListOf(1)
                viewModel.getTopRatedTvShows(1)
            }
            drawer.closeDrawer(GravityCompat.START)
        }

        nav_on_air_tv_shows.setOnClickListener {
            Log.i("Menu Option", "nav_on_air_tv_shows")
            toolbar_title.text = getString(R.string.on_the_air_tv_shows)
            if (sortBy != ON_THE_AIR_TV) {
                sortBy = ON_THE_AIR_TV
                listOfRetrievedPages = arrayListOf(1)
                viewModel.getOnAirTvShows(1)
            }
            drawer.closeDrawer(GravityCompat.START)
        }

        nav_airing_today_tv_shows.setOnClickListener {
            Log.i("Menu Option", "nav_airing_today_tv_shows")
            toolbar_title.text = getString(R.string.airing_today_tv_shows)
            if (sortBy != AIRING_TODAY_TV) {
                sortBy = AIRING_TODAY_TV
                listOfRetrievedPages = arrayListOf(1)
                viewModel.getAiringTodayTvShows(1)
            }
            drawer.closeDrawer(GravityCompat.START)
        }

        //nav_send.setOnClickListener { submitMessage() }
    }

    private fun expandMoviesLayout() {
        if (expandableLayoutMovies.isExpanded) {
            expandableLayoutMovies.collapse()
            movies_arrow.rotation = 90F
        } else {
            expandableLayoutMovies.expand()
            movies_arrow.rotation = 270F
        }
    }

    private fun expandTvLayout() {
        if (expandableLayoutTv.isExpanded) {
            expandableLayoutTv.collapse()
            tv_arrow.rotation = 90F
        } else {
            expandableLayoutTv.expand()
            tv_arrow.rotation = 270F
        }
    }

    private fun expandCommunicateLayout() {
        if (expandableLayoutCommunicate.isExpanded) {
            expandableLayoutCommunicate.collapse()
            communication_arrow.rotation = 90F
        } else {
            expandableLayoutCommunicate.expand()
            communication_arrow.rotation = 270F
        }
    }

    private fun setUpScrollListener() {
        val manager = LinearLayoutManager(this)
        moviesList.layoutManager = manager
        moviesList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(@NonNull recyclerView: RecyclerView, dx: Int, dy: Int) {
                val totalItemCount = manager.itemCount
                val visibleItemCount = manager.childCount
                val firstVisibleItem = manager.findFirstVisibleItemPosition()
                if (firstVisibleItem + visibleItemCount >= totalItemCount / 2) {
                    listOfRetrievedPages.add(listOfRetrievedPages.last() + 1)
                    when (sortBy) {
                        POPULAR_MOVIES -> {
                            viewModel.getPopularMovies(listOfRetrievedPages.last())
                        }
                        TOP_RATED_MOVIES -> {
                            viewModel.getTopRatedMovies(listOfRetrievedPages.last())
                        }
                        PLAYING_NOW_MOVIES -> {
                            viewModel.getPlayingNowMovies(listOfRetrievedPages.last())
                        }
                        UPCOMING_MOVIES -> {
                            viewModel.getUpcomingMovies(listOfRetrievedPages.last())
                        }
                    }
                }
            }
        })

        val manager1 = LinearLayoutManager(this)
        tvshowList.layoutManager = manager1
        tvshowList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(@NonNull recyclerView: RecyclerView, dx: Int, dy: Int) {
                val totalItemCount = manager1.itemCount
                val visibleItemCount = manager1.childCount
                val firstVisibleItem = manager1.findFirstVisibleItemPosition()
                if (firstVisibleItem + visibleItemCount >= totalItemCount / 2) {
                    listOfRetrievedPages.add(listOfRetrievedPages.last() + 1)
                    when (sortBy) {
                        POPULAR_TV -> {
                            viewModel.getPopularTvShows(listOfRetrievedPages.last())
                        }
                        TOP_RATED_TV -> {
                            viewModel.getTopRatedTvShows(listOfRetrievedPages.last())
                        }
                        ON_THE_AIR_TV -> {
                            viewModel.getOnAirTvShows(listOfRetrievedPages.last())
                        }
                        AIRING_TODAY_TV -> {
                            viewModel.getAiringTodayTvShows(listOfRetrievedPages.last())
                        }
                    }
                }
            }
        })
    }

    private fun observeViewModel() {
        viewModel.genresMovieData.observe(this, Observer { data ->
            data?.let {
                moviesListAdapter.appendGenres(data.genres)
            }
        })

        viewModel.genresTvShowData.observe(this, Observer { data ->
            data?.let {
                tvShowAdapter.appendGenres(data.genres)
            }
        })

        viewModel.moviesList.observe(this, Observer { data ->
            data?.let {
                moviesList.visibility = View.VISIBLE
                tvshowList.visibility = View.GONE
                if (listOfRetrievedPages.size == 1) {
                    moviesListAdapter.updateData(it.results)
                } else {
                    moviesListAdapter.appendData(it.results)
                }
            }
        })

        viewModel.tvShowsList.observe(this, Observer { data ->
            data?.let {
                moviesList.visibility = View.GONE
                tvshowList.visibility = View.VISIBLE
                if (listOfRetrievedPages.size == 1) {
                    tvShowAdapter.updateData(it.results)
                } else {
                    tvShowAdapter.appendData(it.results)
                }
            }
        })

        viewModel.loadError.observe(this, Observer { isError ->
            isError?.let {
                list_error.visibility = if (it) View.VISIBLE else View.GONE
                if (it) {
                    list_error.visibility = View.VISIBLE
                    TanTinToast.Warning(this).text("Please check your internet connection.").show()
                }
            }
        })

        viewModel.loading.observe(this, Observer { isLoading ->
            isLoading?.let {
                loading_view.visibility = if (it) View.VISIBLE else View.GONE
                if (it) {
                    list_error.visibility = View.GONE
                    moviesList.visibility = View.GONE
                }
            }
        })
    }
}
