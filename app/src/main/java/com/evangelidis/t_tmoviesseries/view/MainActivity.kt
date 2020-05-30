package com.evangelidis.t_tmoviesseries.view

import android.content.DialogInterface.OnClickListener
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.evangelidis.t_tmoviesseries.R
import com.evangelidis.t_tmoviesseries.callbacks.OnMoviesClickCallback
import com.evangelidis.t_tmoviesseries.callbacks.OnTvShowClickCallback
import com.evangelidis.t_tmoviesseries.extensions.*
import com.evangelidis.t_tmoviesseries.login.LoginActivity
import com.evangelidis.t_tmoviesseries.login.LoginRegisterMethods
import com.evangelidis.t_tmoviesseries.model.MessagePost
import com.evangelidis.t_tmoviesseries.model.Movie
import com.evangelidis.t_tmoviesseries.model.TvShow
import com.evangelidis.t_tmoviesseries.room.DbWorkerThread
import com.evangelidis.t_tmoviesseries.room.WatchlistDataBase
import com.evangelidis.t_tmoviesseries.utils.Constants.AIRING_TODAY_TV
import com.evangelidis.t_tmoviesseries.utils.Constants.DATABASE_THREAD
import com.evangelidis.t_tmoviesseries.utils.Constants.FIREBASE_DATABASE_DATE_FORMAT
import com.evangelidis.t_tmoviesseries.utils.Constants.FIREBASE_MESSAGES_DATABASE_PATH
import com.evangelidis.t_tmoviesseries.utils.Constants.FIREBASE_MESSAGES_DATABASE_PATH_CHILD
import com.evangelidis.t_tmoviesseries.utils.Constants.IS_LOGIN_SKIPPED
import com.evangelidis.t_tmoviesseries.utils.Constants.IS_NOTIFICATION_ON
import com.evangelidis.t_tmoviesseries.utils.Constants.IS_SYNC_WATCHLIST_ON
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
import es.dmoral.prefs.Prefs
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.navigation_drawer.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private var movieCallback: OnMoviesClickCallback = object :
        OnMoviesClickCallback {
        override fun onClick(movie: Movie) {
            if (InternetStatus.getInstance(applicationContext).isOnline) {
                val intent = Intent(this@MainActivity, MovieActivity::class.java)
                intent.putExtra(MOVIE_ID, movie.id)
                startActivity(intent)
            } else {
                TanTinToast.Warning(this@MainActivity).text(getString(R.string.no_internet)).typeface(typeface).show()
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
                TanTinToast.Warning(this@MainActivity).text(getString(R.string.no_internet)).typeface(typeface).show()
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

    private var mDb: WatchlistDataBase? = null
    private lateinit var mDbWorkerThread: DbWorkerThread
    private val mUiHandler = Handler()

    private var typeface: Typeface? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        typeface  = ResourcesCompat.getFont(this, R.font.montserrat_regular)

        database = FirebaseDatabase.getInstance()
        myRef = database.getReference(FIREBASE_MESSAGES_DATABASE_PATH)
        user = FirebaseAuth.getInstance().currentUser

        toolbar_title.text = getString(R.string.popular_movies)

        expandableLayoutMovies.collapse()
        expandableLayoutTv.collapse()
        expandableLayoutCommunicate.expand()
        expandableLayoutSettings.collapse()

        mDbWorkerThread = DbWorkerThread(DATABASE_THREAD)
        mDbWorkerThread.start()
        mDb = WatchlistDataBase.getInstance(this)

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

        setUpNavigationDrawerWatchlistSwitch()
        setUpNavigationDrawerNotificationSwitch()
        setUpNavigationLoginUI()
    }

    override fun onResume() {
        super.onResume()
        getDataFromDB()
    }

    override fun onBackPressed() {
        val drawer: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            val builder = AlertDialog.Builder(this@MainActivity, R.style.AlertDialogTheme)
            builder.apply {
                setIcon(R.drawable.video_camera)
                setTitle(R.string.app_name)
                setMessage(R.string.close_popup_window_title)
                setCancelable(false)
                setPositiveButton(R.string.close_the_app_text, OnClickListener { dialog, id -> finish() })
                setNegativeButton(R.string.cancel, OnClickListener { dialog, id -> dialog.cancel() })
                create().show()
            }
        }
    }

    private fun getDataFromDB() {
        val task = Runnable {
            val watchlistData = mDb?.todoDao()?.getAll()
            mUiHandler.post {
                watchlistData?.let {
                    moviesListAdapter.updateWatchlist(it)
                    tvShowAdapter.updateWatchlist(it)
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
                moviesList.show()
                tvshowList.gone()
                if (listOfRetrievedPages.size == 1) {
                    moviesListAdapter.updateData(it)
                } else {
                    moviesListAdapter.appendData(it)
                }
            }
        })

        viewModel.tvShowsList.observe(this, Observer { data ->
            data.results?.let {
                moviesList.gone()
                tvshowList.show()
                if (listOfRetrievedPages.size == 1) {
                    tvShowAdapter.updateData(it)
                } else {
                    tvShowAdapter.appendData(it)
                }
            }
        })

        viewModel.loadError.observe(this, Observer { isError ->
            isError?.let {
                list_error.showIf { isError }
                if (it) {
                    list_error.show()
                    TanTinToast.Warning(this).text(getString(R.string.no_internet)).typeface(typeface).show()
                }
            }
        })

        viewModel.loading.observe(this, Observer { isLoading ->
            isLoading?.let {
                loading_view.showIf { isLoading }
                if (it) {
                    list_error.gone()
                    moviesList.gone()
                }
            }
        })
    }

    private fun setUpScrollListener() {
        val movieManager = LinearLayoutManager(this)
        moviesList.layoutManager = movieManager
        moviesList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val totalItemCount = movieManager.itemCount
                val visibleItemCount = movieManager.childCount
                val firstVisibleItem = movieManager.findFirstVisibleItemPosition()
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

        val tvManager = LinearLayoutManager(this)
        tvshowList.layoutManager = tvManager
        tvshowList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val totalItemCount = tvManager.itemCount
                val visibleItemCount = tvManager.childCount
                val firstVisibleItem = tvManager.findFirstVisibleItemPosition()
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
        val toggle = ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()
        navigationView.itemTextColor = ColorStateList.valueOf(Color.WHITE)

        nav_movies.setOnClickListener { expandMoviesLayout() }
        nav_tv.setOnClickListener { expandTvLayout() }
        nav_communicate.setOnClickListener { expandCommunicateLayout() }
        nav_settings.setOnClickListener { expandSettings() }

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
            val intent = Intent(this@MainActivity, WatchlistActivity::class.java)
            startActivity(intent)
        }

        nav_send.setOnClickListener { submitMessage() }

        nav_activate_account.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null){
                if (user.isEmailVerified){
                    TanTinToast.Success(this).text("Your email is activated").typeface(typeface).show()
                } else{
                    LoginRegisterMethods.sendVerificationEmail(user)
                }
                nav_activate_account.gone()
            }
        }
    }

    private fun setUpNavigationDrawerNotificationSwitch() {
        if (Prefs.with(applicationContext).readBoolean(IS_NOTIFICATION_ON, false)) {
            nav_notifications.apply {
                isChecked = true
                text = getString(R.string.notifications_are_on)
            }
        } else {
            nav_notifications.apply {
                isChecked = false
                text = getString(R.string.notifications_are_off)
            }
        }

        nav_notifications.setOnClickListener {
            if (Prefs.with(applicationContext).readBoolean(IS_NOTIFICATION_ON, false)) {
                Prefs.with(applicationContext).writeBoolean(IS_NOTIFICATION_ON, false)
                nav_notifications.apply {
                    isChecked = false
                    text = getString(R.string.notifications_are_off)
                }
            } else {
                Prefs.with(applicationContext).writeBoolean(IS_NOTIFICATION_ON, true)
                nav_notifications.apply {
                    isChecked = true
                    text = getString(R.string.notifications_are_on)
                }
            }
        }
    }

    private fun setUpNavigationDrawerWatchlistSwitch() {
        if (Prefs.with(applicationContext).readBoolean(IS_SYNC_WATCHLIST_ON, false)) {
            nav_sync_watchlist.apply {
                isChecked = true
                text = getString(R.string.sync_watchlist_is_on)
            }
        } else {
            nav_sync_watchlist.apply {
                isChecked = false
                text = getString(R.string.sync_watchlist_is_off)
            }
        }

        nav_sync_watchlist.setOnClickListener {
            if (Prefs.with(applicationContext).readBoolean(IS_SYNC_WATCHLIST_ON, false)) {
                Prefs.with(applicationContext).writeBoolean(IS_SYNC_WATCHLIST_ON, false)
                nav_sync_watchlist.apply {
                    isChecked = false
                    text = getString(R.string.sync_watchlist_is_off)
                }
            } else {
                Prefs.with(applicationContext).writeBoolean(IS_SYNC_WATCHLIST_ON, true)
                nav_sync_watchlist.apply {
                    isChecked = true
                    text = getString(R.string.sync_watchlist_is_on)
                }
            }
        }
    }

    private fun setUpNavigationLoginUI() {
        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {
            nav_logout.gone()
        } else {
            //nav_activate_account.showIf { !user.isEmailVerified }
            user.email?.let {
                loginText.text = resources.getString(R.string.welcome_user).replace("{USERNAME}", it.substringBefore("@"))
                nav_logout.show()
            }
        }

        nav_login_layout.setOnClickListener {
            if (user == null) {
                val intent = Intent(this@MainActivity, LoginActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                Prefs.with(applicationContext).writeBoolean(IS_LOGIN_SKIPPED, false)
                startActivity(intent)
            }
        }

        nav_logout.setOnClickListener {
            displayPopWindowsForLogout()
        }
    }

    private fun displayPopWindowsForLogout() {
        val builder = AlertDialog.Builder(this@MainActivity, R.style.AlertDialogTheme)
        builder.apply {
            setIcon(R.drawable.video_camera)
            setTitle(R.string.app_name)
            setMessage(R.string.logout_popup_window_title)
            setCancelable(false)
            setPositiveButton(getString(R.string.logout_from_the_app_text),
                OnClickListener { dialog, id ->
                    FirebaseAuth.getInstance().signOut()
                    loginText.text = getString(R.string.login)
                    setUpNavigationLoginUI()
                    nav_activate_account.gone()
                })
            setNegativeButton(getString(R.string.cancel), OnClickListener { dialog, id -> dialog.cancel() })
            create().show()
        }
    }

    private fun submitMessage() {
        val sliderView = LayoutInflater.from(this).inflate(R.layout.submit_question_layout, null)
        val messageDialog: AlertDialog = AlertDialog.Builder(this).create()
        messageDialog.apply {
            setView(sliderView)
            show()
        }
        val emailInput: TextInputLayout = sliderView.findViewById(R.id.profile_input_email)
        val emailET: EditText = sliderView.findViewById(R.id.profile_et_email)
        val messageInput: TextInputLayout = sliderView.findViewById(R.id.profile_input_message)
        val messageET: EditText = sliderView.findViewById(R.id.profile_et_message)
        val nameET = sliderView.findViewById<EditText>(R.id.profile_et_name)
        val submitMessageToCloud: Button = sliderView.findViewById(R.id.submit_message)
        val declineMessage: Button = sliderView.findViewById(R.id.decline_message)

        showKeyboard()

        user?.email?.let {
            emailET.setText(it)
            nameET.requestFocus()
        }

        declineMessage.setOnClickListener { messageDialog.dismiss() }

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
                    val df = SimpleDateFormat(FIREBASE_DATABASE_DATE_FORMAT, Locale.UK)
                    val date = df.format(Calendar.getInstance().time)
                    val post = MessagePost(emailET.text.toString(), messageET.text.toString(), date, userName)
                    myRef.child(FIREBASE_MESSAGES_DATABASE_PATH_CHILD).push()
                        .setValue(post)
                        .addOnSuccessListener(OnSuccessListener<Void> {
                            TanTinToast.Success(this).text(getString(R.string.message_succ)).typeface(typeface).show()
                            messageDialog.dismiss()
                        })
                        .addOnFailureListener(OnFailureListener {
                            TanTinToast.Error(this).text(getString(R.string.message_fail)).typeface(typeface).show()
                        })

                    sliderView.hideKeyboard()

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
                val df = SimpleDateFormat(FIREBASE_DATABASE_DATE_FORMAT, Locale.UK)
                val date = df.format(Calendar.getInstance().time)
                val post = MessagePost(emailET.text.toString(), messageET.text.toString(), date, userName)
                myRef.child(FIREBASE_MESSAGES_DATABASE_PATH_CHILD).push()
                    .setValue(post)
                    .addOnSuccessListener(OnSuccessListener<Void> {
                        TanTinToast.Success(this).text(getString(R.string.message_succ)).typeface(typeface).show()
                        messageDialog.dismiss()
                    })
                    .addOnFailureListener(OnFailureListener {
                        TanTinToast.Error(this).text(getString(R.string.message_fail)).typeface(typeface).show()
                    })

                sliderView.hideKeyboard()
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

    private fun expandSettings() {
        if (expandableLayoutSettings.isExpanded) {
            expandableLayoutSettings.collapse()
            settings_arrow.rotation = 90F
        } else {
            expandableLayoutSettings.expand()
            settings_arrow.rotation = 270F
        }
    }

    private fun isValidEmailAddress(email: String): Boolean {
        var result = true
        if (!LoginRegisterMethods.isEmailValid(email)) {
            result = false
        }
        return result
    }
}