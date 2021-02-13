package com.evangelidis.t_tmoviesseries.view.movie

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.evangelidis.t_tmoviesseries.R
import com.evangelidis.t_tmoviesseries.databinding.*
import com.evangelidis.t_tmoviesseries.extensions.*
import com.evangelidis.t_tmoviesseries.model.*
import com.evangelidis.t_tmoviesseries.room.*
import com.evangelidis.t_tmoviesseries.utils.Constants.CATEGORY_DIRECTOR
import com.evangelidis.t_tmoviesseries.utils.Constants.CATEGORY_MOVIE
import com.evangelidis.t_tmoviesseries.utils.Constants.IMAGE_POSTER_BASE_URL
import com.evangelidis.t_tmoviesseries.utils.Constants.IMAGE_SMALL_BASE_URL
import com.evangelidis.t_tmoviesseries.utils.Constants.YOUTUBE_THUMBNAIL_URL
import com.evangelidis.t_tmoviesseries.utils.Constants.YOUTUBE_VIDEO_URL
import com.evangelidis.t_tmoviesseries.utils.ItemsManager.changeDateFormat
import com.evangelidis.t_tmoviesseries.utils.ItemsManager.getGlideImage
import com.evangelidis.t_tmoviesseries.utils.ItemsManager.getImageTopRadius
import com.evangelidis.t_tmoviesseries.utils.ItemsManager.showTrailer
import com.evangelidis.t_tmoviesseries.view.main.MainActivity
import com.evangelidis.t_tmoviesseries.view.person.PersonActivity
import com.evangelidis.t_tmoviesseries.view.search.SearchActivity
import com.evangelidis.tantintoast.TanTinToast
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

class MovieActivity : AppCompatActivity() {

    companion object {
        const val MOVIE_ID = "MOVIE_ID"

        fun createIntent(context: Context, movieId: Int): Intent =
            Intent(context, MovieActivity::class.java)
                .putExtra(MOVIE_ID, movieId)
    }

    private lateinit var viewModel: ViewModelMovie
    private var watchlistList: List<WatchlistData>? = null

    private lateinit var movie: MovieDetailsResponse

    private var typeface: Int = R.font.montserrat_regular

    private val binding: ActivityMovieBinding by lazy { ActivityMovieBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val movieId = intent.getIntExtra(MOVIE_ID, 0)

        viewModel = ViewModelProviders.of(this).get(ViewModelMovie::class.java)

        viewModel.apply {
            getMovieDetails(movieId)
            getMovieCredits(movieId)
            getMovieVideos(movieId)
            getMovieSimilar(movieId)
            getMovieRecommendation(movieId)
        }

        getDataFromDB(movieId)
        setToolbar()
        observeViewModel()
    }

    private fun setToolbar() {
        with(binding.toolbar) {
            imageToMain.setOnClickListener {
                startActivity(MainActivity.createIntent(this@MovieActivity))
            }
            searchIcn.setOnClickListener {
                startActivity(SearchActivity.createIntent(this@MovieActivity))
            }
        }
    }

    private fun observeViewModel() {
        viewModel.movieDetails.observe(this, Observer { data ->
            data?.let {
                movie = it
                setUpUI(data)
                data.genres?.let {
                    setUpGenres(it)
                }
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
                setUpVideosUI(it)
            }
        })

        viewModel.movieSimilar.observe(this, Observer { data ->
            data?.let {
                setUpSimilarMoviesUI(it)
            }
        })

        viewModel.movieRecommendation.observe(this, Observer { data ->
            data?.let {
                setUpRecommendationMoviesUI(it)
            }
        })

        viewModel.loadError.observe(this, Observer {
            if (it) {
                TanTinToast.Warning(this).text(getString(R.string.error_for_data)).typeface(typeface).show()
                finish()
            }
        })

        viewModel.loading.observe(this, Observer {
            if (it) {
                binding.progressBar.show()
            } else {
                binding.progressBar.gone()
            }
        })
    }

    private fun setUpRecommendationMoviesUI(data: MoviesListResponse) {
        binding.movieRecommendations.removeAllViews()
        data.results?.let {
            if (it.isNotEmpty()) {
                for (result in it) {
                    val item = ThumbnailMovieBinding.inflate(layoutInflater)
                    getImageTopRadius(this, IMAGE_SMALL_BASE_URL.plus(result.posterPath), item.thumbnail)
                    item.movieName.text = result.title
                    item.movieRate.text = getString(R.string.movie_rate).replace("{MOVIE_RATE}", result.voteAverage.toString())
                    item.thumbnail.setOnClickListener {
                        startActivity(createIntent(this, result.id))
                    }
                    item.root.updatePadding(left = 20, right = 20, bottom = 20)
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
                    getImageTopRadius(this, IMAGE_SMALL_BASE_URL.plus(similarResult.posterPath), item.thumbnail)
                    item.movieName.text = similarResult.title
                    item.movieRate.text = similarResult.voteAverage.toString()
                    item.thumbnail.setOnClickListener {
                        startActivity(createIntent(this, similarResult.id))
                    }
                    item.root.updatePadding(left = 20, right = 20, bottom = 20)
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

                    item.root.setOnClickListener {
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
                    getImageTopRadius(this, IMAGE_SMALL_BASE_URL.plus(cast.profilePath), item.thumbnail)
                    item.actorName.text = cast.name
                    item.actorCharacter.text = cast.character
                    item.thumbnail.setOnClickListener {
                        cast.id?.let {
                            startActivity(PersonActivity.createIntent(this, it))
                        }
                    }
                    item.root.updatePadding(left = 20, right = 20, bottom = 20)
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

        data.releaseDate?.let {
            binding.movieReleaseDate.text = changeDateFormat(it)
            binding.movieReleaseDate.show()
        }

        data.runtime?.let {
            if (it > 0) {
                binding.movieDuration.text = formatHoursAndMinutes(it)
                binding.movieDuration.show()
            }
        }

        data.overview?.let {
            binding.movieDetailsOverview.text = it
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

        if (data.adult.orFalse()) {
            binding.adultImage.show()
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

    private fun getDataFromDB(movieId: Int) {
        DatabaseQueries.getSavedItems(this) { watchlistData ->
            if (!watchlistData.isNullOrEmpty()) {
                watchlistList = watchlistData
                setWishListImage(movieId)
            }
        }
    }

    private fun setWishListImage(movieId: Int) {
        if (watchlistList?.find { it.itemId == movieId } != null) {
            binding.itemMovieWatchlist.setImageResource(R.drawable.ic_enable_watchlist)
        }

        binding.itemMovieWatchlist.setOnClickListener {
            val wishList = WatchlistData().apply {
                itemId = movieId
                category = CATEGORY_MOVIE
                name = movie.title.orEmpty()
                posterPath = movie.posterPath.orEmpty()
                releasedDate = movie.releaseDate.orEmpty()
            }
            movie.voteAverage?.let {
                wishList.rate = it
            }

            if (watchlistList?.find { it.itemId == movieId } == null) {
                DatabaseQueries.saveItem(this, wishList) {
                    binding.itemMovieWatchlist.setImageResource(R.drawable.ic_enable_watchlist)
                }
            } else {
                DatabaseQueries.removeItem(this, wishList.itemId) {
                    binding.itemMovieWatchlist.setImageResource(R.drawable.ic_disable_watchlist)
                }
            }
            getDataFromDB(movieId)
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
