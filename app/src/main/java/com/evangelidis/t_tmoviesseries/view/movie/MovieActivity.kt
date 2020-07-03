package com.evangelidis.t_tmoviesseries.view.movie

import android.content.Intent
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.evangelidis.t_tmoviesseries.utils.ItemsManager.showTrailer
import com.evangelidis.t_tmoviesseries.R
import com.evangelidis.t_tmoviesseries.databinding.*
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

    private val binding: ActivityMovieBinding by lazy { ActivityMovieBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        typeface = ResourcesCompat.getFont(this, R.font.montserrat_regular)

        movieId = intent.getIntExtra(Constants.MOVIE_ID, movieId)

        mDbWorkerThread = DbWorkerThread(DATABASE_THREAD)
        mDbWorkerThread.start()
        mDb = WatchlistDataBase.getInstance(this)

        getDataFromDB()

        binding.toolbar.imageToMain.setOnClickListener {
            val intent = Intent(this@MovieActivity, MainActivity::class.java)
            startActivity(intent)
        }

        binding.toolbar.searchIcn.setOnClickListener {
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

        binding.itemMovieWatchlist.setOnClickListener {
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
                binding.itemMovieWatchlist.setImageResource(R.drawable.ic_enable_watchlist)
                insertDataToDatabase(wishList, mDb, mDbWorkerThread)
            } else {
                binding.itemMovieWatchlist.setImageResource(R.drawable.ic_disable_watchlist)
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
                binding.progressBar.gone()
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
        binding.movieRecommendations.removeAllViews()
        data.results?.let {
            if (it.isNotEmpty()) {
                for (result in it) {
                    val item = ThumbnailMovieBinding.inflate(layoutInflater)
                    item.movieName.text = result.title
                    item.movieRate.text = getString(R.string.movie_rate).replace("{MOVIE_RATE}", result.voteAverage.toString())

                    getGlideImage(this, IMAGE_SMALL_BASE_URL.plus(result.posterPath), item.thumbnail)

                    item.thumbnail.setOnClickListener {
                        val intent = Intent(this, MovieActivity::class.java)
                        intent.putExtra(Constants.MOVIE_ID, result.id)
                        startActivity(intent)
                    }
                    binding.movieRecommendations.addView(item.root)
                }
                binding.recommendationsMoviesContainer.show()
            }
        }
    }

    private fun setUpSimilarMoviesUI(data: MoviesListResponse) {
        binding.movieSimilar.removeAllViews()
        data.results?.let {
            if (it.isNotEmpty()) {
                for (similarResult in it) {
                    val item = ThumbnailMovieBinding.inflate(layoutInflater)
                    item.movieName.text = similarResult.title
                    item.movieRate.text = getString(R.string.movie_rate).replace("{MOVIE_RATE}", similarResult.voteAverage.toString())

                    getGlideImage(this, IMAGE_SMALL_BASE_URL.plus(similarResult.posterPath), item.thumbnail)

                    item.thumbnail.setOnClickListener {
                        val intent = Intent(this, MovieActivity::class.java)
                        intent.putExtra(Constants.MOVIE_ID, similarResult.id)
                        startActivity(intent)
                    }
                    binding.movieSimilar.addView(item.root)
                }
                binding.similarMoviesContainer.show()
            }
        }
    }

    private fun setUpVideosUI(videos: List<Video>) {
        binding.movieVideos.removeAllViews()
        videos.let {
            if (it.isNotEmpty()) {
                for (video in it) {
                    val item = ThumbnailTrailerBinding.inflate(layoutInflater)
                    getGlideImage(this, YOUTUBE_THUMBNAIL_URL.replace("%s", video.key.orEmpty()), item.thumbnail)

                    item.thumbnail.setOnClickListener {
                        showTrailer(String.format(YOUTUBE_VIDEO_URL, video.key), applicationContext)
                    }
                    binding.movieVideos.addView(item.root)
                }
                binding.videosContainer.show()
            }
        }
    }

    private fun setUpActors(casts: List<MovieCast>?) {
        binding.movieActors.removeAllViews()
        casts?.let {
            if (it.isNotEmpty()) {
                for (cast in it) {
                    val item = ThumbnailActorsListBinding.inflate(layoutInflater)
                    item.actorName.text = cast.name
                    item.actorCharacter.text = cast.character

                    getGlideImage(this, IMAGE_SMALL_BASE_URL.plus(cast.profilePath), item.thumbnail)

                    item.thumbnail.setOnClickListener {
                        val intent = Intent(this@MovieActivity, PersonActivity::class.java)
                        intent.putExtra(Constants.PERSON_ID, cast.id)
                        startActivity(intent)
                    }
                    binding.movieActors.addView(item.root)
                }
                binding.actorsContainer.show()
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
                1 -> binding.movieDirectors.text = directorsList[0]
                2 -> {
                    binding.movieDirectors.text = getString(R.string.multi_directors)
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

                    binding.movieDirectors.text = directorsString
                }
            }
            binding.directorsContainer.show()
        }
    }

    private fun setUpUI(data: MovieDetailsResponse) {
        if (!data.backdropPath.isNullOrEmpty()) {
            getGlideImage(this, IMAGE_POSTER_BASE_URL.plus(data.backdropPath), binding.movieImage)
        }

        data.title?.let {
            binding.toolbar.toolbarTitle.text = it
            binding.movieTitle.text = it
            binding.movieTitle.show()
        }

        binding.movieRating.text = data.voteAverage.toString()
        binding.totalVotes.text = data.voteCount.toString()

        if (!data.releaseDate.isNullOrEmpty()) {
            binding.movieReleaseDate.text = changeDateFormat(data.releaseDate)
            binding.movieReleaseDate.show()
        }

        data.runtime?.let {
            if (it > 0) {
                binding.movieDuration.text = formatHoursAndMinutes(it)
                binding.movieDuration.show()
            }
        }

        if (!data.overview.isNullOrEmpty()) {
            binding.movieDetailsOverview.text = data.overview
            binding.summaryContainer.show()
        }

        data.budget?.let {
            if (it > 0.0) {
                binding.movieGrow.show()
                binding.budgetContainer.show()
                binding.movieBudget.text = convertToRealNumber(it)
                data.revenue?.let {
                    if (data.revenue > 0.0) {
                        binding.boxOfficeContainer.show()
                        binding.movieBoxOffice.text = convertToRealNumber(data.revenue)
                        binding.movieBoxOfficePercent.text = calculatePercentBoxOffice(data.budget, data.revenue)
                    }
                }
            }
        }

        data.productionCompanies?.let {
            if (it.isNotEmpty()) {
                binding.productionCompanies.removeAllViews()
                for (company in it) {
                    val item = ThumbnailCompanyBinding.inflate(layoutInflater)
                    company.name?.let {
                        item.productionCompanyName.text = company.name
                        binding.productionCompanies.addView(item.root)
                    }
                }
                binding.productionCompaniesLayout.show()
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
            binding.itemMovieWatchlist.setImageResource(R.drawable.ic_enable_watchlist)
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
            binding.movieGenres.text = genres.joinToString(separator = ", ")
            binding.movieGenres.show()
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
                binding.percentImage.setImageDrawable(resources.getDrawable(R.drawable.ic_percent_up))
                getString(R.string.percent_number_up).replace("{PERCENT_VALUE}", percentValue.toString())
            }
            percentValue < 0 -> {
                binding.percentImage.setImageDrawable(resources.getDrawable(R.drawable.ic_percent_down))
                getString(R.string.percent_number_down).replace("{PERCENT_VALUE}", percentValue.toString())
            }
            else -> {
                "0%"
            }
        }
    }
}