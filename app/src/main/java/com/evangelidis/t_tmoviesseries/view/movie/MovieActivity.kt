package com.evangelidis.t_tmoviesseries.view.movie

import android.content.Intent
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.evangelidis.t_tmoviesseries.utils.ItemsManager.showTrailer
import com.evangelidis.t_tmoviesseries.R
import com.evangelidis.t_tmoviesseries.extensions.gone
import com.evangelidis.t_tmoviesseries.extensions.show
import com.evangelidis.t_tmoviesseries.model.*
import com.evangelidis.t_tmoviesseries.room.*
import com.evangelidis.t_tmoviesseries.room.DatabaseManager.insertDataToDatabase
import com.evangelidis.t_tmoviesseries.room.DatabaseManager.removeDataFromDatabase
import com.evangelidis.t_tmoviesseries.utils.Constants
import com.evangelidis.t_tmoviesseries.utils.Constants.CATEGORY_DIRECTOR
import com.evangelidis.t_tmoviesseries.utils.Constants.CATEGORY_MOVIE
import com.evangelidis.t_tmoviesseries.utils.Constants.DATABASE_THREAD
import com.evangelidis.t_tmoviesseries.utils.Constants.IMAGE_POSTER_BASE_URL
import com.evangelidis.t_tmoviesseries.utils.Constants.IMAGE_SMALL_BASE_URL
import com.evangelidis.t_tmoviesseries.utils.Constants.YOUTUBE_THUMBNAIL_URL
import com.evangelidis.t_tmoviesseries.utils.Constants.YOUTUBE_VIDEO_URL
import com.evangelidis.t_tmoviesseries.utils.ItemsManager.changeDateFormat
import com.evangelidis.t_tmoviesseries.utils.ItemsManager.getGlideImage
import com.evangelidis.t_tmoviesseries.view.person.PersonActivity
import com.evangelidis.t_tmoviesseries.view.search.SearchActivity
import com.evangelidis.t_tmoviesseries.view.main.MainActivity
import com.evangelidis.tantintoast.TanTinToast
import kotlinx.android.synthetic.main.activity_movie.*
import kotlinx.android.synthetic.main.main_toolbar.*
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

class MovieActivity : AppCompatActivity() {

    private var movieId = 0
    private lateinit var viewModel: ViewModelMovie
    private var watchlistList: List<WatchlistData>? = null

    private lateinit var movie: MovieDetailsResponse

    private var mDb: WatchlistDataBase? = null
    private lateinit var mDbWorkerThread: DbWorkerThread
    private val mUiHandler = Handler()

    private var typeface: Typeface? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie)

        typeface = ResourcesCompat.getFont(this, R.font.montserrat_regular)

        movieId = intent.getIntExtra(Constants.MOVIE_ID, movieId)

        mDbWorkerThread = DbWorkerThread(DATABASE_THREAD)
        mDbWorkerThread.start()
        mDb = WatchlistDataBase.getInstance(this)

        getDataFromDB()

        imageToMain.setOnClickListener {
            val intent = Intent(this@MovieActivity, MainActivity::class.java)
            startActivity(intent)
        }

        search_img.setOnClickListener {
            val intent = Intent(this@MovieActivity, SearchActivity::class.java)
            startActivity(intent)
        }

        viewModel = ViewModelProviders.of(this).get(ViewModelMovie::class.java)

        viewModel.apply {
            getMovieDetails(movieId)
            getMovieCredits(movieId)
            getMovieVideos(movieId)
            getMovieSimilar(movieId)
            getMovieRecommendation(movieId)
        }

        observeViewModel()

        item_movie_watchlist.setOnClickListener {
            val finder = watchlistList?.find { it.itemId == movieId }
            val wishList = WatchlistData()
            wishList.apply {
                itemId = movieId
                category = CATEGORY_MOVIE
                name = movie.title.orEmpty()
                posterPath = movie.posterPath.orEmpty()
                releasedDate = movie.releaseDate.orEmpty()
            }
            movie.voteAverage?.let {
                wishList.rate = it
            }

            if (finder == null) {
                item_movie_watchlist.setImageResource(R.drawable.ic_enable_watchlist)
                insertDataToDatabase(wishList, mDb, mDbWorkerThread)
            } else {
                item_movie_watchlist.setImageResource(R.drawable.ic_disable_watchlist)
                removeDataFromDatabase(wishList, mDb, mDbWorkerThread)
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    private fun observeViewModel() {
        viewModel.movieDetails.observe(this, Observer { data ->
            data?.let {
                movie = it
                setUpUI(data)
                data.genres?.let {
                    setUpGenres(data.genres)
                }
                progressBar.gone()
            }
        })

        viewModel.movieCredits.observe(this, Observer { data ->
            data?.let {
                setUpActors(data.cast)
                setUpDirectors(data.crew)
            }
        })

        viewModel.movieVideos.observe(this, Observer { data ->
            data.results?.let {
                setUpVideosUI(data.results)
            }
        })

        viewModel.movieSimilar.observe(this, Observer { data ->
            data?.let {
                setUpSimilarMoviesUI(data)
            }
        })

        viewModel.movieRecommendation.observe(this, Observer { data ->
            data?.let {
                setUpRecommendationMoviesUI(data)
            }
        })

        viewModel.loadError.observe(this, Observer { data ->
            data?.let {
                TanTinToast.Warning(this).text(getString(R.string.error_for_data)).typeface(typeface).show()
                finish()
            }
        })
    }

    private fun setUpRecommendationMoviesUI(data: MoviesListResponse) {
        movieRecommendations.removeAllViews()
        data.results?.let {
            if (it.isNotEmpty()) {
                for (result in it) {
                    val parent = layoutInflater.inflate(R.layout.thumbnail_movie, movieRecommendations, false)
                    val thumbnail: ImageView = parent.findViewById(R.id.thumbnail)
                    val movieName: TextView = parent.findViewById(R.id.movie_name)
                    val movieRate: TextView = parent.findViewById(R.id.movie_rate)
                    movieName.text = result.title
                    movieRate.text = getString(R.string.movie_rate).replace("{MOVIE_RATE}", result.voteAverage.toString())

                    getGlideImage(this, IMAGE_SMALL_BASE_URL.plus(result.posterPath), thumbnail)

                    thumbnail.setOnClickListener {
                        val intent = Intent(this, MovieActivity::class.java)
                        intent.putExtra(Constants.MOVIE_ID, result.id)
                        startActivity(intent)
                    }
                    movieRecommendations.addView(parent)
                }
                recommendationsMoviesContainer.show()
            }
        }
    }

    private fun setUpSimilarMoviesUI(data: MoviesListResponse) {
        movieSimilar.removeAllViews()
        data.results?.let {
            if (it.isNotEmpty()) {
                for (similarResult in it) {
                    val parent = layoutInflater.inflate(R.layout.thumbnail_movie, movieSimilar, false)
                    val thumbnail: ImageView = parent.findViewById(R.id.thumbnail)
                    val movieName: TextView = parent.findViewById(R.id.movie_name)
                    val movieRate: TextView = parent.findViewById(R.id.movie_rate)
                    movieName.text = similarResult.title
                    movieRate.text = getString(R.string.movie_rate).replace("{MOVIE_RATE}", similarResult.voteAverage.toString())

                    getGlideImage(this, IMAGE_SMALL_BASE_URL.plus(similarResult.posterPath), thumbnail)

                    thumbnail.setOnClickListener {
                        val intent = Intent(this, MovieActivity::class.java)
                        intent.putExtra(Constants.MOVIE_ID, similarResult.id)
                        startActivity(intent)
                    }
                    movieSimilar.addView(parent)
                }
                similarMoviesContainer.show()
            }
        }
    }

    private fun setUpVideosUI(videos: List<Video>) {
        movieVideos.removeAllViews()
        videos.let {
            if (it.isNotEmpty()) {
                for (video in it) {
                    val parent = layoutInflater.inflate(R.layout.thumbnail_trailer, movieVideos, false)
                    val thumbnail = parent.findViewById<ImageView>(R.id.thumbnail)

                    getGlideImage(this, YOUTUBE_THUMBNAIL_URL.replace("%s", video.key.orEmpty()), thumbnail)

                    thumbnail.setOnClickListener {
                        showTrailer(String.format(YOUTUBE_VIDEO_URL, video.key), applicationContext)
                    }
                    movieVideos.addView(parent)
                }
                videosContainer.show()
            }
        }
    }

    private fun setUpActors(casts: List<MovieCast>?) {
        movieActors.removeAllViews()
        casts?.let {
            if (it.isNotEmpty()) {
                for (cast in it) {
                    val parent = layoutInflater.inflate(R.layout.thumbnail_actors_list, movieActors, false)
                    val thumbnail: ImageView = parent.findViewById(R.id.thumbnail)
                    val actorName: TextView = parent.findViewById(R.id.actor_name)
                    val actorCharacter: TextView = parent.findViewById(R.id.actor_character)
                    actorName.text = cast.name
                    actorCharacter.text = cast.character

                    getGlideImage(this, IMAGE_SMALL_BASE_URL.plus(cast.profilePath), thumbnail)

                    thumbnail.setOnClickListener {
                        val intent = Intent(this@MovieActivity, PersonActivity::class.java)
                        intent.putExtra(Constants.PERSON_ID, cast.id)
                        startActivity(intent)
                    }
                    movieActors.addView(parent)
                }
                actorsContainer.show()
            }
        }
    }

    private fun setUpDirectors(crew: List<MovieCrew>?) {
        val directorsList = ArrayList<String>()
        crew?.let { it ->
            for (x in it.indices) {
                if (it[x].job == CATEGORY_DIRECTOR) {
                    it[x].name?.let {
                        directorsList.add(it)
                    }
                }
            }
        }
        if (directorsList.isNotEmpty()) {
            when (directorsList.size) {
                1 -> movieDirectors.text = directorsList[0]
                2 -> {
                    movieDirectors.text = getString(R.string.multi_directors)
                        .replace("{dir1}", directorsList[0])
                        .replace("{dir2}", directorsList[1])
                }
                else -> {
                    var directorsString: String? = null
                    for (x in 0 until directorsList.size - 1) {
                        directorsString += directorsList[x] + ", "
                    }
                    directorsString = directorsString?.substring(0, directorsString.length - 2)
                    directorsString += "and " + directorsList[directorsList.size]

                    movieDirectors.text = directorsString
                }
            }
            directorsContainer.show()
        }
    }

    private fun setUpUI(data: MovieDetailsResponse) {
        if (!data.backdropPath.isNullOrEmpty()) {
            getGlideImage(this, IMAGE_POSTER_BASE_URL.plus(data.backdropPath), movieImage)
        }

        data.title?.let {
            toolbar_title.text = it
            movieTitle.text = it
            movieTitle.show()
        }

        movieRating.text = data.voteAverage.toString()
        totalVotes.text = data.voteCount.toString()

        if (!data.releaseDate.isNullOrEmpty()) {
            movieReleaseDate.text = changeDateFormat(data.releaseDate)
            movieReleaseDate.show()
        }

        data.runtime?.let {
            if (it > 0) {
                movieDuration.text = formatHoursAndMinutes(it)
                movieDuration.show()
            }
        }

        if (!data.overview.isNullOrEmpty()) {
            movieDetailsOverview.text = data.overview
            summaryContainer.show()
        }

        data.budget?.let {
            if (it > 0.0) {
                movieGrow.show()
                budgetContainer.show()
                movieBudget.text = convertToRealNumber(it)
                data.revenue?.let {
                    if (data.revenue > 0.0) {
                        boxOfficeContainer.show()
                        movieBoxOffice.text = convertToRealNumber(data.revenue)
                        movieBoxOfficePercent.text = calculatePercentBoxOffice(data.budget, data.revenue)
                    }
                }
            }
        }

        data.productionCompanies?.let {
            if (it.isNotEmpty()) {
                productionCompanies.removeAllViews()
                for (company in it) {
                    val parent = layoutInflater.inflate(R.layout.thumbnail_company, productionCompanies, false)
                    val companyName: TextView = parent.findViewById(R.id.productionCompanyName)
                    company.name?.let {
                        companyName.text = company.name
                        productionCompanies.addView(parent)
                    }
                }
                productionCompaniesLayout.show()
            }
        }
    }

    private fun getDataFromDB() {
        Handler().postDelayed(
            {
                val task = Runnable {
                    val watchlistData = mDb?.todoDao()?.getAll()
                    mUiHandler.post {
                        if (!watchlistData.isNullOrEmpty()) {
                            watchlistList = watchlistData
                            setWishListImage()
                        }
                    }
                }
                mDbWorkerThread.postTask(task)
            }, 800
        )
    }

    private fun setWishListImage() {
        val finder = watchlistList?.find { it.itemId == movieId }
        if (finder != null) {
            item_movie_watchlist.setImageResource(R.drawable.ic_enable_watchlist)
        }
    }

    private fun formatHoursAndMinutes(totalMinutes: Int): String {
        val hours = (totalMinutes / 60).toString()
        val minutes = (totalMinutes % 60).toString()
        return resources.getString(R.string.hour_format)
            .replace("{hour}", (hours)) + getString(R.string.minutes_format)
            .replace("{min}", minutes)
    }


    private fun setUpGenres(data: List<Genre>) {
        val genres: ArrayList<String> = arrayListOf()
        for (element in data) {
            genres.add(element.name.orEmpty())
        }
        if (genres.isNotEmpty()) {
            movieGenres.text = genres.joinToString(separator = ", ")
            movieGenres.show()
        }
    }

    private fun convertToRealNumber(budget: Double?): String {
        val df = DecimalFormat(",###", DecimalFormatSymbols.getInstance(Locale.UK))
        df.maximumFractionDigits = 340
        return df.format(budget) + " $"
    }

    private fun calculatePercentBoxOffice(first: Double, second: Double): String {
        val percentValue = ((100 * (second - first)) / first).roundToInt()
        return when {
            percentValue > 0 -> {
                percentImage.setImageDrawable(resources.getDrawable(R.drawable.ic_percent_up))
                getString(R.string.percent_number_up).replace("{PERCENT_VALUE}", percentValue.toString())
            }
            percentValue < 0 -> {
                percentImage.setImageDrawable(resources.getDrawable(R.drawable.ic_percent_down))
                getString(R.string.percent_number_down).replace("{PERCENT_VALUE}", percentValue.toString())
            }
            else -> {
                "0%"
            }
        }
    }
}