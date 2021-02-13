package com.evangelidis.t_tmoviesseries.view.tvshow

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.evangelidis.t_tmoviesseries.R
import com.evangelidis.t_tmoviesseries.databinding.*
import com.evangelidis.t_tmoviesseries.extensions.gone
import com.evangelidis.t_tmoviesseries.extensions.show
import com.evangelidis.t_tmoviesseries.extensions.updatePadding
import com.evangelidis.t_tmoviesseries.model.*
import com.evangelidis.t_tmoviesseries.room.*
import com.evangelidis.t_tmoviesseries.utils.Constants.CATEGORY_DIRECTOR
import com.evangelidis.t_tmoviesseries.utils.Constants.CATEGORY_TV
import com.evangelidis.t_tmoviesseries.utils.Constants.IMAGE_POSTER_BASE_URL
import com.evangelidis.t_tmoviesseries.utils.Constants.IMAGE_SMALL_BASE_URL
import com.evangelidis.t_tmoviesseries.utils.Constants.YOUTUBE_THUMBNAIL_URL
import com.evangelidis.t_tmoviesseries.utils.Constants.YOUTUBE_VIDEO_URL
import com.evangelidis.t_tmoviesseries.utils.ItemsManager.getGlideImage
import com.evangelidis.t_tmoviesseries.utils.ItemsManager.getImageTopRadius
import com.evangelidis.t_tmoviesseries.utils.ItemsManager.showTrailer
import com.evangelidis.t_tmoviesseries.view.main.MainActivity
import com.evangelidis.t_tmoviesseries.view.person.PersonActivity
import com.evangelidis.t_tmoviesseries.view.search.SearchActivity
import com.evangelidis.t_tmoviesseries.view.seasons.SeasonsActivity
import java.util.ArrayList

class TvShowActivity : AppCompatActivity() {

    companion object {
        const val TV_SHOW_ID = "TV_SHOW_ID"

        fun createIntent(context: Context, tvShowId: Int): Intent =
            Intent(context, TvShowActivity::class.java)
                .putExtra(TV_SHOW_ID, tvShowId)
    }

    private lateinit var viewModel: ViewModelTvShow
    private lateinit var tvShow: TvShowDetailsResponse

    private var totalSeasonsNumber = 1

    private var watchlistList: List<WatchlistData>? = null

    private val binding: ActivityTvShowBinding by lazy { ActivityTvShowBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val tvShowId = intent.getIntExtra(TV_SHOW_ID, 0)

        getDataFromDB(tvShowId)
        setToolbar()

        binding.itemTvWatchlist.setOnClickListener {
            val finder = watchlistList?.find { it.itemId == tvShowId }
            val wishList = WatchlistData()
            wishList.apply {
                itemId = tvShowId
                category = CATEGORY_TV
                name = tvShow.name.orEmpty()
                posterPath = tvShow.posterPath.orEmpty()
                releasedDate = tvShow.firstAirDate.orEmpty()
            }
            tvShow.voteAverage?.let {
                wishList.rate = it
            }

            if (finder == null) {
                binding.itemTvWatchlist.setImageResource(R.drawable.ic_enable_watchlist)
                DatabaseQueries.saveItem(this, wishList)
            } else {
                binding.itemTvWatchlist.setImageResource(R.drawable.ic_disable_watchlist)
                DatabaseQueries.removeItem(this, wishList.itemId)
            }
            getDataFromDB(tvShowId)
        }

        binding.tvShowAllSeasons.setOnClickListener {
            startActivity(SeasonsActivity.createIntent(this, totalSeasonsNumber, tvShowId, binding.tvShowTitle.text.toString()))
        }

        viewModel = ViewModelProviders.of(this).get(ViewModelTvShow::class.java)
        viewModel.apply {
            getTvShowDetails(tvShowId)
            getTvShowCredits(tvShowId)
            getTvShowVideos(tvShowId)
            getTvShowSimilar(tvShowId)
            getTvShowRecommendation(tvShowId)
        }

        observeViewModel()
    }

    private fun setToolbar() {
        binding.toolbar.apply {
            imageToMain.setOnClickListener {
                startActivity(MainActivity.createIntent(this@TvShowActivity))
            }
            searchIcn.setOnClickListener {
                startActivity(SearchActivity.createIntent(this@TvShowActivity))
            }
        }
    }

    private fun observeViewModel() {
        viewModel.tvShowDetails.observe(this, Observer { data ->
            data?.let {
                tvShow = data
                setUpUI(data)
                data.numberOfSeasons?.let {
                    totalSeasonsNumber = data.numberOfSeasons
                }
                data.genres?.let {
                    setUpGenres(it)
                }
                binding.progressBar.gone()
            }
        })

        viewModel.tvShowCredits.observe(this, Observer { data ->
            data?.let {
                setUpActors(data.cast)
                setUpDirectors(data.crew)
            }
        })

        viewModel.tvShowVideos.observe(this, Observer { data ->
            data?.let {
                setUpVideosUI(data.results)
            }
        })

        viewModel.tvShowSimilar.observe(this, Observer { data ->
            data?.let {
                setUpSimilarTvShowUI(data)
            }
        })

        viewModel.tvShowRecommendation.observe(this, Observer { data ->
            data?.let {
                setUpRecommendationTvShowsUI(data)
            }
        })
    }

    private fun setUpUI(data: TvShowDetailsResponse) {

        val imageUrl = data.backdropPath ?: data.posterPath
        if (!imageUrl.isNullOrEmpty()) {
            getGlideImage(this, IMAGE_POSTER_BASE_URL.plus(imageUrl), binding.tvShowBackdrop)
        }

        data.name?.let {
            binding.toolbar.toolbarTitle.text = data.name
            binding.tvShowTitle.text = data.name
            binding.tvShowTitle.show()
        }

        data.voteCount?.let {
            if (it > 0) {
                binding.tvShowRating.text = data.voteAverage.toString()
                binding.tvShowTotalVotes.text = it.toString()
            }
        }

        if (!data.firstAirDate.isNullOrEmpty()) {
            data.inProduction?.let { it ->
                val releasedYear = data.firstAirDate.substring(0, 4)
                if (it) {
                    binding.tvShowReleaseDate.text = getString(R.string.tv_show_release_date_format1).replace("{year}", releasedYear)
                } else {
                    data.seasons?.last()?.airDate?.let {
                        val lastYear = it.substring(0, 4)
                        if (lastYear == releasedYear) {
                            binding.tvShowReleaseDate.text = getString(R.string.tv_show_release_date_format1).replace("{year}", releasedYear)
                        } else {
                            binding.tvShowReleaseDate.text = getString(R.string.tv_show_release_date_format2)
                                .replace("{year1}", releasedYear)
                                .replace("{year2}", it.substring(0, 4))
                        }
                    }
                }
                binding.tvShowReleaseDate.show()
            }
        }

        if (!data.episodeRunTime.isNullOrEmpty()) {
            data.episodeRunTime.first().let {
                binding.tvShowEpisodeDuration.text = getString(R.string.minutes_format).replace("{min}", it.toString())
                binding.tvShowEpisodeDuration.show()
            }
        }

        data.numberOfSeasons?.let {
            if (it == 1) {
                binding.tvShowAllSeasons.text = getString(R.string.one_season_text)
            } else {
                binding.tvShowAllSeasons.text = getString(R.string.season_number).replace("{x}", it.toString())
            }
            binding.tvShowAllSeasons.show()
        }

        if (!data.overview.isNullOrEmpty()) {
            binding.tvShowOverview.text = data.overview
            binding.tvShowSummaryContainer.show()
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
                binding.tvShowProductionCompaniesLayout.show()
            }
        }
    }

    private fun setUpGenres(data: List<Genre>) {
        val genres: ArrayList<String> = arrayListOf()
        for (element in data) {
            genres.add(element.name.orEmpty())
        }
        if (genres.isNotEmpty()) {
            binding.tvShowGenres.text = genres.joinToString(separator = ", ")
            binding.tvShowGenres.show()
        }
    }

    private fun setUpActors(casts: List<TvShowCast>?) {
        casts?.let {
            if (it.isNotEmpty()) {
                for (cast in it) {
                    val item = ThumbnailActorsListBinding.inflate(layoutInflater)
                    getImageTopRadius(this, IMAGE_SMALL_BASE_URL.plus(cast.profilePath), item.thumbnail)
                    item.actorName.text = cast.name
                    item.actorCharacter.text = cast.character
                    item.thumbnail.requestLayout()
                    item.thumbnail.setOnClickListener {
                        cast.id?.let {
                            startActivity(PersonActivity.createIntent(this, it))
                        }
                    }
                    item.root.updatePadding(left = 20, right = 20, bottom = 20)
                    binding.tvShowActors.addView(item.root)
                }
                binding.tvShowActorsContainer.show()
            }
        }
    }

    private fun setUpVideosUI(results: List<Video>?) {
        results?.let {
            if (it.isNotEmpty()) {
                binding.tvShowTrailers.removeAllViews()
                for (trailer in it) {
                    val item = ThumbnailTrailerBinding.inflate(layoutInflater)
                    item.thumbnail.requestLayout()
                    item.thumbnail.setOnClickListener {
                        showTrailer(String.format(YOUTUBE_VIDEO_URL, trailer.key), applicationContext)
                    }

                    getGlideImage(this, YOUTUBE_THUMBNAIL_URL.replace("%s", trailer.key.orEmpty()), item.thumbnail)
                    binding.tvShowTrailers.addView(item.root)
                }
                binding.tvShowTrailersContainer.show()
            }
        }
    }

    private fun setUpDirectors(crew: List<TvShowCrew>?) {
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

        if (!directorsList.isNullOrEmpty()) {
            when (directorsList.size) {
                1 -> {
                    binding.tvShowDirectors.text = directorsList[0]
                }
                2 -> {
                    binding.tvShowDirectors.text = getString(R.string.multi_directors)
                        .replace("{dir1}", directorsList[0]).replace("{dir2}", directorsList[1])
                }
                else -> {
                    var directorsString = ""
                    for (x in 0 until directorsList.size - 1) {
                        directorsString += directorsList[x] + ", "
                    }
                    directorsString = directorsString.substring(0, directorsString.length - 2)
                    directorsString += "and " + directorsList[directorsList.size]

                    binding.tvShowDirectors.text = directorsString
                }
            }
            binding.tvShowDirectorsContainer.show()
        }
    }

    private fun setUpSimilarTvShowUI(data: TvShowListResponse) {
        data.results?.let {
            if (it.isNotEmpty()) {
                binding.tvShowSimilar.removeAllViews()
                for (x in it) {
                    val item = ThumbnailMovieBinding.inflate(layoutInflater)
                    getImageTopRadius(this, IMAGE_SMALL_BASE_URL.plus(x.posterPath), item.thumbnail)
                    item.movieName.text = x.name
                    item.movieRate.text = getString(R.string.movie_rate).replace("{MOVIE_RATE}", x.voteAverage.toString())
                    item.thumbnail.requestLayout()
                    item.thumbnail.setOnClickListener {
                        startActivity(createIntent(this, x.id))
                    }

                    item.root.updatePadding(left = 20, right = 20, bottom = 20)
                    binding.tvShowSimilar.addView(item.root)
                }
                binding.tvShowSimilarContainer.show()
            }
        }
    }

    private fun setUpRecommendationTvShowsUI(data: TvShowListResponse) {
        data.results?.let {
            if (it.isNotEmpty()) {
                binding.tvShowRecommendations.removeAllViews()
                for (x in it) {
                    val item = ThumbnailMovieBinding.inflate(layoutInflater)
                    getImageTopRadius(this, IMAGE_SMALL_BASE_URL.plus(x.posterPath), item.thumbnail)
                    item.movieName.text = x.name
                    item.movieRate.text = getString(R.string.movie_rate).replace("{MOVIE_RATE}", x.voteAverage.toString())
                    item.thumbnail.requestLayout()
                    item.thumbnail.setOnClickListener {
                        startActivity(createIntent(this, x.id))
                    }

                    item.root.updatePadding(left = 20, right = 20, bottom = 20)
                    binding.tvShowRecommendations.addView(item.root)
                }
                binding.tvShowRecommendationsContainer.show()
            }
        }
    }

    private fun getDataFromDB(tvShowId: Int) {
        DatabaseQueries.getSavedItems(this) { watchlistData ->
            if (!watchlistData.isNullOrEmpty()) {
                watchlistList = watchlistData
                setWishListImage(tvShowId)
            }
        }
    }

    private fun setWishListImage(tvShowId: Int) {
        val finder = watchlistList?.find { it.itemId == tvShowId }
        if (finder != null) {
            binding.itemTvWatchlist.setImageResource(R.drawable.ic_enable_watchlist)
        }
    }
}
