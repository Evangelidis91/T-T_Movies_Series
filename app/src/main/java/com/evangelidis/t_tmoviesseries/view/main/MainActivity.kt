package com.evangelidis.t_tmoviesseries.view.main

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.GravityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.evangelidis.t_tmoviesseries.R
import com.evangelidis.t_tmoviesseries.callbacks.OnMoviesClickCallback
import com.evangelidis.t_tmoviesseries.callbacks.OnTvShowClickCallback
import com.evangelidis.t_tmoviesseries.databinding.ActivityMainBinding
import com.evangelidis.t_tmoviesseries.databinding.SubmitQuestionLayoutBinding
import com.evangelidis.t_tmoviesseries.extensions.*
import com.evangelidis.t_tmoviesseries.model.MessagePost
import com.evangelidis.t_tmoviesseries.model.Movie
import com.evangelidis.t_tmoviesseries.model.TvShow
import com.evangelidis.t_tmoviesseries.room.DatabaseQueries
import com.evangelidis.t_tmoviesseries.utils.Constants.AIRING_TODAY_TV
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
import com.evangelidis.t_tmoviesseries.view.login.LoginActivity
import com.evangelidis.t_tmoviesseries.view.login.LoginRegisterMethods
import com.evangelidis.t_tmoviesseries.view.movie.MovieActivity
import com.evangelidis.t_tmoviesseries.view.search.SearchActivity
import com.evangelidis.t_tmoviesseries.view.tvshow.TvShowActivity
import com.evangelidis.t_tmoviesseries.view.watchlist.WatchlistActivity
import com.evangelidis.tantintoast.TanTinToast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import es.dmoral.prefs.Prefs
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

    lateinit var viewModel: ViewModelMain
    private val moviesListAdapter = MoviesListAdapter(arrayListOf(), movieCallback, mutableListOf())
    private val tvShowAdapter = TvShowAdapter(arrayListOf(), tvShowCallback, mutableListOf())
    private var sortBy = POPULAR_MOVIES

    private lateinit var database: FirebaseDatabase
    private lateinit var myRef: DatabaseReference
    private var user: FirebaseUser? = null

    var listOfRetrievedPages = arrayListOf(1)

    private var typeface: Typeface? = null

    private val binding: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        typeface = ResourcesCompat.getFont(this, R.font.montserrat_regular)

        database = FirebaseDatabase.getInstance()
        myRef = database.getReference(FIREBASE_MESSAGES_DATABASE_PATH)
        user = FirebaseAuth.getInstance().currentUser

        binding.appBar.toolbarTitle.text = getString(R.string.popular_movies)

        binding.navigationDrawer.expandableLayoutMovies.collapse()
        binding.navigationDrawer.expandableLayoutTv.collapse()
        binding.navigationDrawer.expandableLayoutCommunicate.expand()
        binding.navigationDrawer.expandableLayoutSettings.collapse()

        getDataFromDB()

        viewModel = ViewModelProviders.of(this).get(ViewModelMain::class.java)
        viewModel.getMoviesGenres()
        viewModel.getTvShowGenres()
        viewModel.getPopularMovies(1)

        binding.appBar.mainView.moviesList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = moviesListAdapter
        }

        binding.appBar.mainView.tvShowList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = tvShowAdapter
        }

        binding.appBar.searchIcn.setOnClickListener {
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
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            val builder = AlertDialog.Builder(this@MainActivity, R.style.AlertDialogTheme)
            builder.apply {
                setIcon(R.drawable.video_camera)
                setTitle(R.string.app_name)
                setMessage(R.string.close_popup_window_title)
                setCancelable(false)
                setPositiveButton(R.string.close_the_app_text) { _, _ -> finish() }
                setNegativeButton(R.string.cancel) { dialog, _ -> dialog.cancel() }
                create().show()
            }
        }
    }

    private fun getDataFromDB() {
        DatabaseQueries.getSavedItems(this) { watchlistData ->
            watchlistData?.let {
                moviesListAdapter.updateWatchlist(it)
                tvShowAdapter.updateWatchlist(it)
            }
        }
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
                binding.appBar.mainView.moviesList.show()
                binding.appBar.mainView.tvShowList.gone()
                if (listOfRetrievedPages.size == 1) {
                    moviesListAdapter.updateData(it)
                } else {
                    moviesListAdapter.appendData(it)
                }
            }
        })

        viewModel.tvShowsList.observe(this, Observer { data ->
            data.results?.let {
                binding.appBar.mainView.moviesList.gone()
                binding.appBar.mainView.tvShowList.show()
                if (listOfRetrievedPages.size == 1) {
                    tvShowAdapter.updateData(it)
                } else {
                    tvShowAdapter.appendData(it)
                }
            }
        })

        viewModel.loadError.observe(this, Observer { isError ->
            isError?.let {
                binding.appBar.mainView.listError.showIf { isError }
                if (it) {
                    binding.appBar.mainView.listError.show()
                    TanTinToast.Warning(this).text(getString(R.string.no_internet)).typeface(typeface).show()
                }
            }
        })

        viewModel.loading.observe(this, Observer { isLoading ->
            isLoading?.let {
                binding.appBar.mainView.loadingView.showIf { isLoading }
                if (it) {
                    binding.appBar.mainView.listError.gone()
                    binding.appBar.mainView.moviesList.gone()
                }
            }
        })
    }

    private fun setUpScrollListener() {
        val movieManager = LinearLayoutManager(this)
        binding.appBar.mainView.moviesList.layoutManager = movieManager
        binding.appBar.mainView.moviesList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
        binding.appBar.mainView.tvShowList.layoutManager = tvManager
        binding.appBar.mainView.tvShowList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
        val toggle = ActionBarDrawerToggle(this, binding.drawerLayout, binding.appBar.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        binding.navView.itemTextColor = ColorStateList.valueOf(Color.WHITE)

        binding.navigationDrawer.navMovies.setOnClickListener { expandMoviesLayout() }
        binding.navigationDrawer.navTv.setOnClickListener { expandTvLayout() }
        binding.navigationDrawer.navCommunicate.setOnClickListener { expandCommunicateLayout() }
        binding.navigationDrawer.navSettings.setOnClickListener { expandSettings() }

        binding.navigationDrawer.navPopularMovies.setOnClickListener {
            Tracking.trackListCategory(this, getString(R.string.popular_movies))
            binding.appBar.toolbarTitle.text = getString(R.string.popular_movies)
            if (sortBy != POPULAR_MOVIES) {
                sortBy = POPULAR_MOVIES
                listOfRetrievedPages = arrayListOf(1)
                viewModel.getPopularMovies(1)
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        binding.navigationDrawer.navPlayingNowMovies.setOnClickListener {
            Tracking.trackListCategory(this, getString(R.string.playing_now_movies))
            binding.appBar.toolbarTitle.text = getString(R.string.playing_now_movies)
            if (sortBy != PLAYING_NOW_MOVIES) {
                sortBy = PLAYING_NOW_MOVIES
                listOfRetrievedPages = arrayListOf(1)
                viewModel.getPlayingNowMovies(1)
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        binding.navigationDrawer.navTopRatedMovies.setOnClickListener {
            Tracking.trackListCategory(this, getString(R.string.top_rated_movies))
            binding.appBar.toolbarTitle.text = getString(R.string.top_rated_movies)
            if (sortBy != TOP_RATED_MOVIES) {
                sortBy = TOP_RATED_MOVIES
                listOfRetrievedPages = arrayListOf(1)
                viewModel.getTopRatedMovies(1)
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        binding.navigationDrawer.navUpcomingMovies.setOnClickListener {
            Tracking.trackListCategory(this, getString(R.string.upcoming_movies))
            binding.appBar.toolbarTitle.text = getString(R.string.upcoming_movies)
            if (sortBy != UPCOMING_MOVIES) {
                sortBy = UPCOMING_MOVIES
                listOfRetrievedPages = arrayListOf(1)
                viewModel.getUpcomingMovies(1)
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        binding.navigationDrawer.navPopularTvShows.setOnClickListener {
            Tracking.trackListCategory(this, getString(R.string.popular_tv_shows))
            binding.appBar.toolbarTitle.text = getString(R.string.popular_tv_shows)
            if (sortBy != POPULAR_TV) {
                sortBy = POPULAR_TV
                listOfRetrievedPages = arrayListOf(1)
                viewModel.getPopularTvShows(1)
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        binding.navigationDrawer.navTopRatedTvShows.setOnClickListener {
            Tracking.trackListCategory(this, getString(R.string.top_rated_tv_shows))
            binding.appBar.toolbarTitle.text = getString(R.string.top_rated_tv_shows)
            if (sortBy != TOP_RATED_TV) {
                sortBy = TOP_RATED_TV
                listOfRetrievedPages = arrayListOf(1)
                viewModel.getTopRatedTvShows(1)
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        binding.navigationDrawer.navOnAirTvShows.setOnClickListener {
            Tracking.trackListCategory(this, getString(R.string.on_the_air_tv_shows))
            binding.appBar.toolbarTitle.text = getString(R.string.on_the_air_tv_shows)
            if (sortBy != ON_THE_AIR_TV) {
                sortBy = ON_THE_AIR_TV
                listOfRetrievedPages = arrayListOf(1)
                viewModel.getOnAirTvShows(1)
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        binding.navigationDrawer.navAiringTodayTvShows.setOnClickListener {
            Tracking.trackListCategory(this, getString(R.string.airing_today_tv_shows))
            binding.appBar.toolbarTitle.text = getString(R.string.airing_today_tv_shows)
            if (sortBy != AIRING_TODAY_TV) {
                sortBy = AIRING_TODAY_TV
                listOfRetrievedPages = arrayListOf(1)
                viewModel.getAiringTodayTvShows(1)
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        binding.navigationDrawer.navWatchlist.setOnClickListener {
            val intent = Intent(this@MainActivity, WatchlistActivity::class.java)
            startActivity(intent)
        }

        binding.navigationDrawer.navSend.setOnClickListener { submitMessage() }

        binding.navigationDrawer.navActivateAccount.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                if (user.isEmailVerified) {
                    TanTinToast.Success(this).text("Your email is activated").typeface(typeface).show()
                } else {
                    LoginRegisterMethods.sendVerificationEmail(user)
                }
                binding.navigationDrawer.navActivateAccount.gone()
            }
        }
    }

    private fun setUpNavigationDrawerNotificationSwitch() {
        if (Prefs.with(applicationContext).readBoolean(IS_NOTIFICATION_ON, false)) {
            binding.navigationDrawer.navNotifications.apply {
                isChecked = true
                text = getString(R.string.notifications_are_on)
            }
        } else {
            binding.navigationDrawer.navNotifications.apply {
                isChecked = false
                text = getString(R.string.notifications_are_off)
            }
        }

        binding.navigationDrawer.navNotifications.setOnClickListener {
            if (Prefs.with(applicationContext).readBoolean(IS_NOTIFICATION_ON, false)) {
                Prefs.with(applicationContext).writeBoolean(IS_NOTIFICATION_ON, false)
                binding.navigationDrawer.navNotifications.apply {
                    isChecked = false
                    text = getString(R.string.notifications_are_off)
                }
            } else {
                Prefs.with(applicationContext).writeBoolean(IS_NOTIFICATION_ON, true)
                binding.navigationDrawer.navNotifications.apply {
                    isChecked = true
                    text = getString(R.string.notifications_are_on)
                }
            }
        }
    }

    private fun setUpNavigationDrawerWatchlistSwitch() {
        if (Prefs.with(applicationContext).readBoolean(IS_SYNC_WATCHLIST_ON, false)) {
            binding.navigationDrawer.navSyncWatchlist.apply {
                isChecked = true
                text = getString(R.string.sync_watchlist_is_on)
            }
        } else {
            binding.navigationDrawer.navSyncWatchlist.apply {
                isChecked = false
                text = getString(R.string.sync_watchlist_is_off)
            }
        }

        binding.navigationDrawer.navSyncWatchlist.setOnClickListener {
            if (Prefs.with(applicationContext).readBoolean(IS_SYNC_WATCHLIST_ON, false)) {
                Prefs.with(applicationContext).writeBoolean(IS_SYNC_WATCHLIST_ON, false)
                binding.navigationDrawer.navSyncWatchlist.apply {
                    isChecked = false
                    text = getString(R.string.sync_watchlist_is_off)
                }
            } else {
                Prefs.with(applicationContext).writeBoolean(IS_SYNC_WATCHLIST_ON, true)
                binding.navigationDrawer.navSyncWatchlist.apply {
                    isChecked = true
                    text = getString(R.string.sync_watchlist_is_on)
                }
            }
        }
    }

    private fun setUpNavigationLoginUI() {
        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {
            binding.navigationDrawer.navLogout.gone()
        } else {
            // nav_activate_account.showIf { !user.isEmailVerified }
            user.email?.let {
                binding.navigationDrawer.loginText.text = resources.getString(R.string.welcome_user).replace("{USERNAME}", it.substringBefore("@"))
                binding.navigationDrawer.navLogout.show()
            }
        }

        binding.navigationDrawer.loginText.setOnClickListener {
            if (user == null) {
                val intent = Intent(this@MainActivity, LoginActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                Prefs.with(applicationContext).writeBoolean(IS_LOGIN_SKIPPED, false)
                startActivity(intent)
            }
        }

        binding.navigationDrawer.navLogout.setOnClickListener {
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
            setPositiveButton(getString(R.string.logout_from_the_app_text)) { _, _ ->
                FirebaseAuth.getInstance().signOut()
                binding.navigationDrawer.loginText.text = getString(R.string.login)
                setUpNavigationLoginUI()
                binding.navigationDrawer.navActivateAccount.gone()
            }
            setNegativeButton(getString(R.string.cancel)) { dialog, _ -> dialog.cancel() }
            create().show()
        }
    }

    @SuppressLint("InflateParams")
    private fun submitMessage() {
        val item = SubmitQuestionLayoutBinding.inflate(layoutInflater)
        val messageDialog: AlertDialog = AlertDialog.Builder(this).create()
        messageDialog.apply {
            setView(item.root)
            show()
        }

        showKeyboard()

        user?.email?.let {
            item.profileEtEmail.setText(it)
            item.profileEtName.requestFocus()
        }

        item.declineMessage.setOnClickListener { messageDialog.dismiss() }

        // When user press done in keyboard
        item.profileEtMessage.setOnEditorActionListener { v, actionId, event ->
            var handled = false
            if (actionId == EditorInfo.IME_ACTION_GO) {
                if (isValidEmailAddress(item.profileEtEmail.text.toString()) && !item.profileEtMessage.text.isNullOrEmpty()) {
                    val userName: String = if (item.profileEtName.text != null) {
                        item.profileEtName.text.toString()
                    } else {
                        ""
                    }
                    val df = SimpleDateFormat(FIREBASE_DATABASE_DATE_FORMAT, Locale.UK)
                    val date = df.format(Calendar.getInstance().time)
                    val post = MessagePost(item.profileEtEmail.text.toString(), item.profileEtMessage.text.toString(), date, userName)
                    myRef.child(FIREBASE_MESSAGES_DATABASE_PATH_CHILD).push()
                        .setValue(post)
                        .addOnSuccessListener {
                            TanTinToast.Success(this).text(getString(R.string.message_success)).typeface(typeface).show()
                            messageDialog.dismiss()
                        }
                        .addOnFailureListener {
                            TanTinToast.Error(this).text(getString(R.string.message_fail)).typeface(typeface).show()
                        }

                    item.root.hideKeyboard()
                } else {
                    if (!isValidEmailAddress(item.profileEtEmail.text.toString())) {
                        item.profileInputEmail.error = getString(R.string.mail_error)
                    } else if (!item.profileEtMessage.text.isNullOrEmpty()) {
                        item.profileInputMessage.error = getString(R.string.message_short)
                    }
                }
                handled = true
            }
            handled
        }

        item.submitMessage.setOnClickListener {
            if (isValidEmailAddress(item.profileEtEmail.text.toString()) && !item.profileEtMessage.text.isNullOrEmpty()) {
                val userName: String = if (item.profileEtName.text.isNullOrEmpty()) {
                    ""
                } else {
                    item.profileEtName.text.toString()
                }
                val df = SimpleDateFormat(FIREBASE_DATABASE_DATE_FORMAT, Locale.UK)
                val date = df.format(Calendar.getInstance().time)
                val post = MessagePost(item.profileEtEmail.text.toString(), item.profileEtMessage.text.toString(), date, userName)
                myRef.child(FIREBASE_MESSAGES_DATABASE_PATH_CHILD).push()
                    .setValue(post)
                    .addOnSuccessListener {
                        TanTinToast.Success(this).text(getString(R.string.message_success)).typeface(typeface).show()
                        messageDialog.dismiss()
                    }
                    .addOnFailureListener {
                        TanTinToast.Error(this).text(getString(R.string.message_fail)).typeface(typeface).show()
                    }

                item.root.hideKeyboard()
            } else {
                if (!isValidEmailAddress(item.profileEtEmail.text.toString())) {
                    item.profileInputEmail.error = getString(R.string.mail_error)
                } else if (!item.profileEtMessage.text.isNullOrEmpty()) {
                    item.profileInputMessage.error = getString(R.string.message_short)
                }
            }
        }
    }

    private fun expandMoviesLayout() {
        if (binding.navigationDrawer.expandableLayoutMovies.isExpanded) {
            binding.navigationDrawer.expandableLayoutMovies.collapse()
            binding.navigationDrawer.moviesArrow.rotation = 90F
        } else {
            binding.navigationDrawer.expandableLayoutMovies.expand()
            binding.navigationDrawer.moviesArrow.rotation = 270F
        }
    }

    private fun expandTvLayout() {
        if (binding.navigationDrawer.expandableLayoutTv.isExpanded) {
            binding.navigationDrawer.expandableLayoutTv.collapse()
            binding.navigationDrawer.tvArrow.rotation = 90F
        } else {
            binding.navigationDrawer.expandableLayoutTv.expand()
            binding.navigationDrawer.tvArrow.rotation = 270F
        }
    }

    private fun expandCommunicateLayout() {
        if (binding.navigationDrawer.expandableLayoutCommunicate.isExpanded) {
            binding.navigationDrawer.expandableLayoutCommunicate.collapse()
            binding.navigationDrawer.communicationArrow.rotation = 90F
        } else {
            binding.navigationDrawer.expandableLayoutCommunicate.expand()
            binding.navigationDrawer.communicationArrow.rotation = 270F
        }
    }

    private fun expandSettings() {
        if (binding.navigationDrawer.expandableLayoutSettings.isExpanded) {
            binding.navigationDrawer.expandableLayoutSettings.collapse()
            binding.navigationDrawer.settingsArrow.rotation = 90F
        } else {
            binding.navigationDrawer.expandableLayoutSettings.expand()
            binding.navigationDrawer.settingsArrow.rotation = 270F
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
