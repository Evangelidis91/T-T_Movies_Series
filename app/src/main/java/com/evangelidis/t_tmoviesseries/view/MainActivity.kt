package com.evangelidis.t_tmoviesseries.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.evangelidis.t_tmoviesseries.callbacks.OnMoviesClickCallback
import com.evangelidis.t_tmoviesseries.callbacks.OnTvShowClickCallback
import com.evangelidis.t_tmoviesseries.R
import com.evangelidis.t_tmoviesseries.model.MessagePost
import com.evangelidis.t_tmoviesseries.model.Movie
import com.evangelidis.t_tmoviesseries.model.TvShow
import com.evangelidis.t_tmoviesseries.room.DbWorkerThread
import com.evangelidis.t_tmoviesseries.room.WishListDataBase
import com.evangelidis.t_tmoviesseries.utils.Constants.AIRING_TODAY_TV
import com.evangelidis.t_tmoviesseries.utils.Constants.FIREBASE_DATABASE_DATE_FORMAT
import com.evangelidis.t_tmoviesseries.utils.Constants.MOVIE_ID
import com.evangelidis.t_tmoviesseries.utils.Constants.ON_THE_AIR_TV
import com.evangelidis.t_tmoviesseries.utils.Constants.PLAYING_NOW_MOVIES
import com.evangelidis.t_tmoviesseries.utils.Constants.POPULAR_MOVIES
import com.evangelidis.t_tmoviesseries.utils.Constants.POPULAR_TV
import com.evangelidis.t_tmoviesseries.utils.Constants.TOP_RATED_MOVIES
import com.evangelidis.t_tmoviesseries.utils.Constants.TOP_RATED_TV
import com.evangelidis.t_tmoviesseries.utils.Constants.TV_SHOW_ID
import com.evangelidis.t_tmoviesseries.utils.Constants.UPCOMING_MOVIES
import com.evangelidis.t_tmoviesseries.utils.InternetStatus
import com.evangelidis.t_tmoviesseries.utils.Tracking
import com.evangelidis.t_tmoviesseries.view.adapters.MoviesListAdapter
import com.evangelidis.t_tmoviesseries.view.adapters.TvShowAdapter
import com.evangelidis.t_tmoviesseries.viewmodel.ListViewModel
import com.evangelidis.tantintoast.TanTinToast
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.navigation.NavigationView
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.app_bar_main.search_img
import kotlinx.android.synthetic.main.app_bar_main.toolbar_title
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.navigation_drawer.*
import java.text.SimpleDateFormat
import java.util.*
import javax.mail.internet.AddressException
import javax.mail.internet.InternetAddress

class MainActivity : AppCompatActivity() {

    private var movieCallback: OnMoviesClickCallback = object :
        OnMoviesClickCallback {
        override fun onClick(movie: Movie) {
            if (InternetStatus.getInstance(applicationContext).isOnline) {
                val intent = Intent(this@MainActivity, MovieActivity::class.java)
                intent.putExtra(MOVIE_ID, movie.id)
                startActivity(intent)
            } else {
                TanTinToast.Warning(this@MainActivity).text(getString(R.string.no_internet)).show()
            }
        }
    }

    private var tvShowCallback: OnTvShowClickCallback = object :
        OnTvShowClickCallback {
        override fun onClick(tvShow: TvShow) {
            if (InternetStatus.getInstance(applicationContext).isOnline) {
                val intent = Intent(this@MainActivity, TvShowActivity::class.java)
                intent.putExtra(TV_SHOW_ID, tvShow.id)
                startActivity(intent)
            } else {
                TanTinToast.Warning(this@MainActivity).text(getString(R.string.no_internet)).show()
            }
        }
    }

    lateinit var viewModel: ListViewModel
    private val moviesListAdapter = MoviesListAdapter(arrayListOf(), movieCallback, mutableListOf())
    private val tvShowAdapter = TvShowAdapter(arrayListOf(), tvShowCallback, mutableListOf())
    private var sortBy = POPULAR_MOVIES

    private lateinit var database: FirebaseDatabase
    private lateinit var myRef: DatabaseReference
    private var user: FirebaseUser? = null

    var listOfRetrievedPages = arrayListOf(1)

    private var mDb: WishListDataBase? = null
    private lateinit var mDbWorkerThread: DbWorkerThread
    private val mUiHandler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        database = FirebaseDatabase.getInstance()
        myRef = database.getReference("message")
        user = FirebaseAuth.getInstance().currentUser

        toolbar_title.text = getString(R.string.popular_movies)

        expandableLayoutMovies.collapse()
        expandableLayoutTv.collapse()
        expandableLayoutCommunicate.expand()

        mDbWorkerThread = DbWorkerThread("dbWorkerThread")
        mDbWorkerThread.start()
        mDb = WishListDataBase.getInstance(this)

        getDataFromDB()

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

        search_img.setOnClickListener {
            val intent = Intent(this@MainActivity, SearchActivity::class.java)
            startActivity(intent)
        }

        observeViewModel()
        setUpScrollListener()
        setupNavigationViewClickListeners()
    }

    override fun onResume() {
        super.onResume()
        hideProgressBar()
        getDataFromDB()
    }

    private fun hideProgressBar() {
        //progressbar.visibility = View.GONE
    }

    private fun getDataFromDB() {
        val task = Runnable {
            val wishlistData = mDb?.todoDao()?.getAll()
            mUiHandler.post {
                if (!wishlistData.isNullOrEmpty()) {
                    moviesListAdapter.updateWishlist(wishlistData)
                    tvShowAdapter.updateWishlist(wishlistData)
                }
            }
        }
        mDbWorkerThread.postTask(task)
    }

    private fun observeViewModel() {
        viewModel.genresMovieData.observe(this, Observer { data ->
            data.genres?.let {
                moviesListAdapter.appendGenres(it)
            }
        })

        viewModel.genresTvShowData.observe(this, Observer { data ->
            data.genres?.let {
                tvShowAdapter.appendGenres(it)
            }
        })

        viewModel.moviesList.observe(this, Observer { data ->
            data.results?.let {
                moviesList.visibility = View.VISIBLE
                tvshowList.visibility = View.GONE
                if (listOfRetrievedPages.size == 1) {
                    moviesListAdapter.updateData(it)
                } else {
                    moviesListAdapter.appendData(it)
                }
            }
        })

        viewModel.tvShowsList.observe(this, Observer { data ->
            data.results?.let {
                moviesList.visibility = View.GONE
                tvshowList.visibility = View.VISIBLE
                if (listOfRetrievedPages.size == 1) {
                    tvShowAdapter.updateData(it)
                } else {
                    tvShowAdapter.appendData(it)
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

    private fun setUpScrollListener() {
        val manager = LinearLayoutManager(this)
        moviesList.layoutManager = manager
        moviesList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
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
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
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
            Tracking.trackListCategory(this, getString(R.string.popular_movies))
            toolbar_title.text = getString(R.string.popular_movies)
            if (sortBy != POPULAR_MOVIES) {
                sortBy = POPULAR_MOVIES
                listOfRetrievedPages = arrayListOf(1)
                viewModel.getPopularMovies(1)
            }
            drawer.closeDrawer(GravityCompat.START)
        }

        nav_playing_now_movies.setOnClickListener {
            Tracking.trackListCategory(this, getString(R.string.playing_now_movies))
            toolbar_title.text = getString(R.string.playing_now_movies)
            if (sortBy != PLAYING_NOW_MOVIES) {
                sortBy = PLAYING_NOW_MOVIES
                listOfRetrievedPages = arrayListOf(1)
                viewModel.getPlayingNowMovies(1)
            }
            drawer.closeDrawer(GravityCompat.START)
        }

        nav_top_rated_movies.setOnClickListener {
            Tracking.trackListCategory(this, getString(R.string.top_rated_movies))
            toolbar_title.text = getString(R.string.top_rated_movies)
            if (sortBy != TOP_RATED_MOVIES) {
                sortBy = TOP_RATED_MOVIES
                listOfRetrievedPages = arrayListOf(1)
                viewModel.getTopRatedMovies(1)
            }
            drawer.closeDrawer(GravityCompat.START)
        }

        nav_upcoming_movies.setOnClickListener {
            Tracking.trackListCategory(this, getString(R.string.upcoming_movies))
            toolbar_title.text = getString(R.string.upcoming_movies)
            if (sortBy != UPCOMING_MOVIES) {
                sortBy = UPCOMING_MOVIES
                listOfRetrievedPages = arrayListOf(1)
                viewModel.getUpcomingMovies(1)
            }
            drawer.closeDrawer(GravityCompat.START)
        }


        nav_popular_tv_shows.setOnClickListener {
            Tracking.trackListCategory(this, getString(R.string.popular_tv_shows))
            toolbar_title.text = getString(R.string.popular_tv_shows)
            if (sortBy != POPULAR_TV) {
                sortBy = POPULAR_TV
                listOfRetrievedPages = arrayListOf(1)
                viewModel.getPopularTvShows(1)
            }
            drawer.closeDrawer(GravityCompat.START)
        }

        nav_top_rated_tv_shows.setOnClickListener {
            Tracking.trackListCategory(this, getString(R.string.top_rated_tv_shows))
            toolbar_title.text = getString(R.string.top_rated_tv_shows)
            if (sortBy != TOP_RATED_TV) {
                sortBy = TOP_RATED_TV
                listOfRetrievedPages = arrayListOf(1)
                viewModel.getTopRatedTvShows(1)
            }
            drawer.closeDrawer(GravityCompat.START)
        }

        nav_on_air_tv_shows.setOnClickListener {
            Tracking.trackListCategory(this, getString(R.string.on_the_air_tv_shows))
            toolbar_title.text = getString(R.string.on_the_air_tv_shows)
            if (sortBy != ON_THE_AIR_TV) {
                sortBy = ON_THE_AIR_TV
                listOfRetrievedPages = arrayListOf(1)
                viewModel.getOnAirTvShows(1)
            }
            drawer.closeDrawer(GravityCompat.START)
        }

        nav_airing_today_tv_shows.setOnClickListener {
            Tracking.trackListCategory(this, getString(R.string.airing_today_tv_shows))
            toolbar_title.text = getString(R.string.airing_today_tv_shows)
            if (sortBy != AIRING_TODAY_TV) {
                sortBy = AIRING_TODAY_TV
                listOfRetrievedPages = arrayListOf(1)
                viewModel.getAiringTodayTvShows(1)
            }
            drawer.closeDrawer(GravityCompat.START)
        }

        nav_wishlist.setOnClickListener {
            val intent = Intent(this@MainActivity, WishlistActivity::class.java)
            startActivity(intent)
        }

        nav_send.setOnClickListener { submitMessage() }
    }

    private fun submitMessage() {
        val sliderView = LayoutInflater.from(this).inflate(R.layout.submit_question_layout, null)
        val messageDialog: AlertDialog = AlertDialog.Builder(this).create()
        messageDialog.setView(sliderView)
        messageDialog.show()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        val emailInput = sliderView.findViewById<TextInputLayout>(R.id.profile_input_email)
        val emailET = sliderView.findViewById<EditText>(R.id.profile_et_email)
        val messageInput = sliderView.findViewById<TextInputLayout>(R.id.profile_input_message)
        val messageET = sliderView.findViewById<EditText>(R.id.profile_et_message)
        val nameET = sliderView.findViewById<EditText>(R.id.profile_et_name)
        val submitMessageToCloud = sliderView.findViewById<Button>(R.id.submit_message)

        user?.email?.let {
            emailET.setText(it)
            nameET.requestFocus()
        }

        // When user press done in keyboard
        messageET.setOnEditorActionListener { v, actionId, event ->
            var handled = false
            if (actionId == EditorInfo.IME_ACTION_GO) {
                if (isValidEmailAddress(emailET.text.toString()) && messageET.text.isNotEmpty()) {
                    val userName: String = if (nameET.text != null) {
                        nameET.text.toString()
                    } else {
                        ""
                    }
                    val df = SimpleDateFormat(FIREBASE_DATABASE_DATE_FORMAT)
                    val date = df.format(Calendar.getInstance().time)
                    val post = MessagePost(
                        emailET.text.toString(),
                        messageET.text.toString(),
                        date,
                        userName
                    )
                    myRef.child(getString(R.string.firebase_Users_Posts_Path)).push()
                        .setValue(post)
                        .addOnSuccessListener(OnSuccessListener<Void> {
                            TanTinToast.Success(this).text(getString(R.string.message_succ)).show()
                            messageDialog.dismiss()
                        })
                        .addOnFailureListener(OnFailureListener {
                            TanTinToast.Error(this).text(getString(R.string.message_fail)).show()
                        })

                    val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)

                } else {
                    if (!isValidEmailAddress(emailET.text.toString())) {
                        emailInput.error = getString(R.string.mail_error)
                    } else if (messageET.text.isEmpty()) {
                        messageInput.error = getString(R.string.message_short)
                    }
                }
                handled = true
            }
            handled
        }

        submitMessageToCloud.setOnClickListener {
            if (isValidEmailAddress(emailET.text.toString()) && messageET.text.isNotEmpty()) {
                val userName: String = if (nameET.text.isNullOrEmpty()) {
                    ""
                } else {
                    nameET.text.toString()
                }
                val df = SimpleDateFormat(FIREBASE_DATABASE_DATE_FORMAT)
                val date = df.format(Calendar.getInstance().time)
                val post = MessagePost(
                    emailET.text.toString(),
                    messageET.text.toString(),
                    date,
                    userName
                )
                myRef.child(resources.getString(R.string.firebase_Users_Posts_Path)).push()
                    .setValue(post)
                    .addOnSuccessListener(OnSuccessListener<Void> {
                        TanTinToast.Success(this).text(getString(R.string.message_succ)).show()
                        messageDialog.dismiss()
                    })
                    .addOnFailureListener(OnFailureListener {
                        TanTinToast.Error(this).text(getString(R.string.message_fail)).show()
                    })
                val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
            } else {
                if (!isValidEmailAddress(emailET.text.toString())) {
                    emailInput.error = getString(R.string.mail_error)
                } else if (messageET.text.isEmpty()) {
                    messageInput.error = getString(R.string.message_short)
                }
            }
        }
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

    private fun isValidEmailAddress(email: String): Boolean {
        var result = true
        try {
            val emailAddress = InternetAddress(email)
            emailAddress.validate()
        } catch (ex: AddressException) {
            result = false
        }
        return result
    }
}
