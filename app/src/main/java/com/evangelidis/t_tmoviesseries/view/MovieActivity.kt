package com.evangelidis.t_tmoviesseries.view

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.evangelidis.t_tmoviesseries.ItemsManager.showTrailer
import com.evangelidis.t_tmoviesseries.R
import com.evangelidis.t_tmoviesseries.extensions.gone
import com.evangelidis.t_tmoviesseries.extensions.show
import com.evangelidis.t_tmoviesseries.model.*
import com.evangelidis.t_tmoviesseries.room.*
import com.evangelidis.t_tmoviesseries.room.DatabaseManager.insertDataToDatabase
import com.evangelidis.t_tmoviesseries.room.DatabaseManager.removeDataFromDatabase
import com.evangelidis.t_tmoviesseries.utils.Constants
import com.evangelidis.t_tmoviesseries.utils.Constants.ACTOR_IMAGE_URL
import com.evangelidis.t_tmoviesseries.utils.Constants.CATEGORY_DIRECTOR
import com.evangelidis.t_tmoviesseries.utils.Constants.CATEGORY_MOVIE
import com.evangelidis.t_tmoviesseries.utils.Constants.DATABASE_THREAD
import com.evangelidis.t_tmoviesseries.utils.Constants.IMAGE_BASE_URL
import com.evangelidis.t_tmoviesseries.utils.Constants.YOUTUBE_THUMBNAIL_URL
import com.evangelidis.t_tmoviesseries.utils.Constants.YOUTUBE_VIDEO_URL
import com.evangelidis.t_tmoviesseries.viewmodel.ListViewModel
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
    lateinit var viewModel: ListViewModel
    private var wishlistList: List<WatchlistData>? = null

    private lateinit var movie: MovieDetailsResponse

    private var mDb: WatchlistDataBase? = null
    private lateinit var mDbWorkerThread: DbWorkerThread
    private val mUiHandler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie)

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

        viewModel = ViewModelProviders.of(this).get(ListViewModel::class.java)

        viewModel.apply {
            getMovieDetails(movieId)
            getMovieCredits(movieId)
            getMovieVideos(movieId)
            getMovieSimilar(movieId)
            getMovieRecommendation(movieId)
        }

        observeViewModel()

        item_movie_wishlist.setOnClickListener {
            val finder = wishlistList?.find { it.itemId == movieId }
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
                item_movie_wishlist.setImageResource(R.drawable.ic_enable_wishlist)
                insertDataToDatabase(wishList, mDb, mDbWorkerThread)
            } else {
                item_movie_wishlist.setImageResource(R.drawable.ic_disable_wishlist)
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
                TanTinToast.Warning(this).text(getString(R.string.error_for_data)).show()
                finish()
            }
        })
    }

    private fun setUpRecommendationMoviesUI(data: MoviesListResponse) {
        movieRecommendations.removeAllViews()
        data.results?.let {
            for (result in it) {
                val parent = layoutInflater.inflate(R.layout.thumbnail_movie, movieRecommendations, false)
                val thumbnail: ImageView = parent.findViewById(R.id.thumbnail)
                val movieName: TextView = parent.findViewById(R.id.movie_name)
                val movieRate: TextView = parent.findViewById(R.id.movie_rate)
                movieName.text = result.title
                movieRate.text = getString(R.string.movie_rate).replace("{MOVIE_RATE}", result.voteAverage.toString())

                Glide.with(this)
                    .load(ACTOR_IMAGE_URL + result.posterPath)
                    .apply(RequestOptions.placeholderOf(R.color.mainBackground))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(thumbnail)

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

    private fun setUpSimilarMoviesUI(data: MoviesListResponse) {
        movieSimilar.removeAllViews()
        data.results?.let {
            for (similarResult in it) {
                val parent = layoutInflater.inflate(R.layout.thumbnail_movie, movieSimilar, false)
                val thumbnail: ImageView = parent.findViewById(R.id.thumbnail)
                val movieName: TextView = parent.findViewById(R.id.movie_name)
                val movieRate: TextView = parent.findViewById(R.id.movie_rate)
                movieName.text = similarResult.title
                movieRate.text = getString(R.string.movie_rate).replace("{MOVIE_RATE}", similarResult.voteAverage.toString())

                Glide.with(this)
                    .load(ACTOR_IMAGE_URL + similarResult.posterPath)
                    .apply(RequestOptions.placeholderOf(R.color.mainBackground))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(thumbnail)

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

    private fun setUpVideosUI(videos: List<Video>) {
        movieVideos.removeAllViews()
        videos.let {
            for (video in it) {
                val parent = layoutInflater.inflate(R.layout.thumbnail_trailer, movieVideos, false)
                val thumbnail = parent.findViewById<ImageView>(R.id.thumbnail)

                Glide.with(this)
                    .load(String.format(YOUTUBE_THUMBNAIL_URL, video.key))
                    .apply(RequestOptions.placeholderOf(R.color.mainBackground))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(thumbnail)

                thumbnail.setOnClickListener {
                    showTrailer(String.format(YOUTUBE_VIDEO_URL, video.key), applicationContext)
                }
                movieVideos.addView(parent)
            }
            videosContainer.show()
        }
    }

    private fun setUpActors(casts: List<MovieCast>?) {
        movieActors.removeAllViews()
        casts?.let {
            for (cast in it) {
                val parent = layoutInflater.inflate(R.layout.thumbnail_actors_list, movieActors, false)
                val thumbnail: ImageView = parent.findViewById(R.id.thumbnail)
                val actorName: TextView = parent.findViewById(R.id.actor_name)
                val actorCharacter: TextView = parent.findViewById(R.id.actor_character)
                actorName.text = cast.name
                actorCharacter.text = cast.character

                Glide.with(this)
                    .load(ACTOR_IMAGE_URL + cast.profilePath)
                    .apply(RequestOptions.placeholderOf(R.color.mainBackground))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(thumbnail)

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
            Glide.with(this)
                .load(IMAGE_BASE_URL + data.backdropPath)
                .apply(RequestOptions.placeholderOf(R.color.colorPrimary))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(movieImage)
        }

        data.title?.let {
            toolbar_title.text = it
            movieTitle.text = it
            movieTitle.show()
        }

        movieRating.text = data.voteAverage.toString()
        totalVotes.text = data.voteCount.toString()
        movieReleaseDate.text = data.releaseDate

        data.runtime?.let {
            movieDuration.text = formatHoursAndMinutes(it)
        }

        data.overview?.let {
            movieDetailsOverview.text = it
            summaryContainer.show()
        }

        data.budget?.let {
            if (it > 0.0) {
                movieGrow.show()
                budgetContainer.show()
                movieBudget.text = convertToRealNumber(it)
                data.revenue?.let {
                    if (data.revenue > 0.0) {
                        boxofficeContainer.show()
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
                            wishlistList = watchlistData
                            setWishListImage()
                        }
                    }
                }
                mDbWorkerThread.postTask(task)
            }, 800
        )
    }

    private fun setWishListImage() {
        val finder = wishlistList?.find { it.itemId == movieId }
        if (finder != null) {
            item_movie_wishlist.setImageResource(R.drawable.ic_enable_wishlist)
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
        movieGenres.text = genres.joinToString(separator = ", ")
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