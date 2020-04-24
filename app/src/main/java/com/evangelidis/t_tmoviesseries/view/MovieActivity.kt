package com.evangelidis.t_tmoviesseries.view

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.evangelidis.t_tmoviesseries.R
import com.evangelidis.t_tmoviesseries.model.*
import com.evangelidis.t_tmoviesseries.utils.Constants
import com.evangelidis.t_tmoviesseries.utils.Constants.ACTOR_IMAGE_URL
import com.evangelidis.t_tmoviesseries.utils.Constants.IMAGE_BASE_URL
import com.evangelidis.t_tmoviesseries.utils.Constants.YOUTUBE_THUMBNAIL_URL
import com.evangelidis.t_tmoviesseries.utils.Constants.YOUTUBE_VIDEO_URL
import com.evangelidis.t_tmoviesseries.viewmodel.ListViewModel
import kotlinx.android.synthetic.main.activity_movie.*
import kotlinx.android.synthetic.main.main_toolbar.*
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import kotlin.math.roundToInt

class MovieActivity : AppCompatActivity() {

    private var movieId = 0
    lateinit var viewModel: ListViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie)

        movieId = intent.getIntExtra(Constants.MOVIE_ID, movieId)

        imageToMain.setOnClickListener {
            val intent = Intent(this@MovieActivity, MainActivity::class.java)
            startActivity(intent)
        }

        viewModel = ViewModelProviders.of(this).get(ListViewModel::class.java)

        viewModel.getMovieDetails(movieId)
        viewModel.getMovieCredits(movieId)
        viewModel.getMovieVideos(movieId)
        viewModel.getMovieSimilar(movieId)
        viewModel.getMovieRecommendation(movieId)

        observeViewModel()
    }

    private fun observeViewModel() {

        viewModel.movieDetails.observe(this, Observer { data ->
            data?.let {
                setUpUI(data)
                setUpGenres(data.genres)
                progressBar.visibility = View.GONE
            }
        })

        viewModel.movieCredits.observe(this, Observer { data ->
            data?.let {
                setUpActors(data.cast)
                setUpDirectors(data.crew)
            }
        })

        viewModel.movieVideos.observe(this, Observer { data ->
            data?.let {
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
    }

    private fun setUpRecommendationMoviesUI(data: MoviesListResponse) {

        movieRecommendations.removeAllViews()
        if (!data.results.isNullOrEmpty()){
            recommendationsMoviesContainer.visibility = View.VISIBLE
            for (result in data.results) {
                val parent = layoutInflater.inflate(
                    R.layout.thumbnail_movie,
                    movieRecommendations,
                    false
                )
                val thumbnail = parent.findViewById<ImageView>(R.id.thumbnail)
                val movieName = parent.findViewById<TextView>(R.id.movie_name)
                val movieRate = parent.findViewById<TextView>(R.id.movie_rate)
                movieName.text = result.title
                movieRate.text = resources.getString(R.string.movie_rate)
                    .replace("{MOVIE_RATE}", result.voteAverage.toString())

                thumbnail.setOnClickListener {
                    val intent = Intent(this, MovieActivity::class.java)
                    intent.putExtra(Constants.MOVIE_ID, result.id)
                    startActivity(intent)
                }
                Glide.with(this)
                    .load(ACTOR_IMAGE_URL + result.posterPath)
                    .apply(RequestOptions.placeholderOf(R.color.mainBackground))
                    .into(thumbnail)
                movieRecommendations.addView(parent)
            }
        }

    }

    private fun setUpSimilarMoviesUI(data: MoviesListResponse) {

        movieSimilar.removeAllViews()
        if (!data.results.isNullOrEmpty()){
            similarMoviesContainer.visibility = View.VISIBLE
            for (similarResult in data.results) {
                val parent =
                    layoutInflater.inflate(R.layout.thumbnail_movie, movieSimilar, false)
                val thumbnail = parent.findViewById<ImageView>(R.id.thumbnail)
                val movieName = parent.findViewById<TextView>(R.id.movie_name)
                val movieRate = parent.findViewById<TextView>(R.id.movie_rate)
                movieName.text = similarResult.title
                movieRate.text = resources.getString(R.string.movie_rate)
                    .replace("{MOVIE_RATE}", similarResult.voteAverage.toString())

                thumbnail.setOnClickListener {
                    val intent = Intent(this, MovieActivity::class.java)
                    intent.putExtra(Constants.MOVIE_ID, similarResult.id)
                    startActivity(intent)
                }

                Glide.with(this)
                    .load(ACTOR_IMAGE_URL + similarResult.posterPath)
                    .apply(RequestOptions.placeholderOf(R.color.mainBackground))
                    .into(thumbnail)
                movieSimilar.addView(parent)
            }
        }

    }

    private fun setUpVideosUI(videos: List<Video>) {
        movieVideos.removeAllViews()
        if (!videos.isNullOrEmpty()){
            videosContainer.visibility = View.VISIBLE
            for (video in videos) {
                val parent =
                    layoutInflater.inflate(R.layout.thumbnail_trailer, movieVideos, false)
                val thumbnail = parent.findViewById<ImageView>(R.id.thumbnail)
                thumbnail.requestLayout()
                thumbnail.setOnClickListener {
                    showTrailer(
                        String.format(YOUTUBE_VIDEO_URL, video.key)
                    )
                }
                Glide.with(this)
                    .load(String.format(YOUTUBE_THUMBNAIL_URL, video.key))
                    .apply(
                        RequestOptions.placeholderOf(R.color.mainBackground).centerCrop()
                    )
                    .into(thumbnail)
                movieVideos.addView(parent)
            }
        }
    }

    private fun showTrailer(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    private fun setUpActors(casts: List<Cast>) {
        movieActors.removeAllViews()
        if (!casts.isNullOrEmpty()) {
            movieActors.visibility = View.VISIBLE
            for (cast in casts) {
                val parent =
                    layoutInflater.inflate(R.layout.thumbnail_actors_list, movieActors, false)
                val thumbnail = parent.findViewById<ImageView>(R.id.thumbnail)
                val textView = parent.findViewById<TextView>(R.id.actor_name)
                val textView1 = parent.findViewById<TextView>(R.id.actor_character)
                textView.text = cast.name
                textView1.text = cast.character
                thumbnail.requestLayout()
                thumbnail.setOnClickListener {
//                    val intent = Intent(this@MovieActivity, PersonActivity::class.java)
//                    intent.putExtra(Constants.PERSON_ID, cast.id)
//                    startActivity(intent)
                }

                Glide.with(this)
                    .load(ACTOR_IMAGE_URL + cast.profilePath)
                    .apply(RequestOptions.placeholderOf(R.color.mainBackground))
                    .into(thumbnail)

                movieActors.addView(parent)
            }
        }
    }

    private fun setUpDirectors(crew: List<Crew>) {

        val directorsList = ArrayList<String>()
        for (x in crew.indices) {
            if (crew[x].job == "Director" && crew[x].name.isNotEmpty()) {
                directorsList.add(crew[x].name)
            }
        }
        if (!directorsList.isNullOrEmpty()) {
            directorsContainer.visibility = View.VISIBLE
            when (directorsList.size) {
                1 -> {
                    movieDirectors.text = directorsList[0]
                }
                2 -> {
                    movieDirectors.text = resources.getString(R.string.multi_directors)
                        .replace("{dir1}", directorsList[0]).replace("{dir2}", directorsList[1])
                }
                else -> {
                    var directorsString = ""
                    for (x in 0 until directorsList.size - 1) {
                        directorsString += directorsList[x] + ", "
                    }
                    directorsString = directorsString.substring(0, directorsString.length - 2)
                    directorsString += "and " + directorsList[directorsList.size]

                    movieDirectors.text = directorsString
                }
            }
        }


    }

    private fun setUpUI(data: MovieDetailsResponse) {

        if (data.backdropPath.isNotEmpty()) {
            Glide.with(this)
                .load(IMAGE_BASE_URL + data.backdropPath)
                .apply(RequestOptions.placeholderOf(R.color.colorPrimary))
                .into(movieImage)
        }

        if (data.title.isNotEmpty()) {
            movieTitle.visibility = View.VISIBLE
            movieTitle.text = data.title
            toolbar_title.text = data.title
        }

        movieRating.text = data.voteAverage.toString()
        totalVotes.text = data.voteCount.toString()
        movieReleaseDate.text = data.releaseDate
        movieDuration.text = formatHoursAndMinutes(data.runtime)

        if (data.overview.isNotEmpty()) {
            summaryContainer.visibility = View.VISIBLE
            movieDetailsOverview.text = data.overview
        }

        if (data.budget.toString().isNotEmpty() || data.budget.toString().isNotEmpty()) {
            movieGrow.visibility = View.VISIBLE
            if (data.budget > 0.0) {
                budgetContainer.visibility = View.VISIBLE
                movieBudget.text = convertToRealNumber(data.budget)
                if (data.revenue > 0.0) {
                    boxofficeContainer.visibility = View.VISIBLE
                    movieBoxOffice.text = convertToRealNumber(data.revenue)
                    movieBoxOfficePercent.text =
                        calculatePercentBoxOffice(data.budget, data.revenue)
                }
            }
        }
    }

    private fun formatHoursAndMinutes(totalMinutes: Int): String {
        val hours = (totalMinutes / 60).toString()
        val minutes = (totalMinutes % 60).toString()
        return resources.getString(R.string.hour_format).replace(
            "{hour}",
            (hours)
        ) + resources.getString(R.string.minutes_format).replace("{min}", minutes)
    }

    private fun setUpGenres(data: List<Genre>) {
        var currentGenres = ""
        for (element in data) {
            currentGenres += element.name
            currentGenres += ", "
        }
        movieGenres.text = currentGenres.substring(0, currentGenres.length - 2)
    }

    private fun convertToRealNumber(budget: Double): String {
        val df = DecimalFormat(",###", DecimalFormatSymbols.getInstance(Locale.US))
        df.maximumFractionDigits = 340
        return df.format(budget) + " $"
    }

    private fun calculatePercentBoxOffice(first: Double, second: Double): String {

        val percentValue = ((100 * (second - first)) / first).roundToInt()
        return when {
            percentValue > 0 -> {
                percentImage.setImageDrawable(resources.getDrawable(R.drawable.ic_percent_up))
                resources.getString(R.string.percent_number_up)
                    .replace("{PERCENT_VALUE}", percentValue.toString())
            }
            percentValue < 0 -> {
                percentImage.setImageDrawable(resources.getDrawable(R.drawable.ic_percent_down))
                resources.getString(R.string.percent_number_down)
                    .replace("{PERCENT_VALUE}", percentValue.toString())
            }
            else -> {
                "0%"
            }
        }
    }
}
