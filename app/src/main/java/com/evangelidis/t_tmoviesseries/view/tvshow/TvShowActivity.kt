package com.evangelidis.t_tmoviesseries.view.tvshow

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.ImageView
import android.widget.TextView
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
import com.evangelidis.t_tmoviesseries.utils.Constants.CATEGORY_DIRECTOR
import com.evangelidis.t_tmoviesseries.utils.Constants.CATEGORY_TV
import com.evangelidis.t_tmoviesseries.utils.Constants.DATABASE_THREAD
import com.evangelidis.t_tmoviesseries.utils.Constants.IMAGE_POSTER_BASE_URL
import com.evangelidis.t_tmoviesseries.utils.Constants.IMAGE_SMALL_BASE_URL
import com.evangelidis.t_tmoviesseries.utils.Constants.PERSON_ID
import com.evangelidis.t_tmoviesseries.utils.Constants.TOTAL_SEASONS
import com.evangelidis.t_tmoviesseries.utils.Constants.TV_SHOW_ID
import com.evangelidis.t_tmoviesseries.utils.Constants.TV_SHOW_NAME
import com.evangelidis.t_tmoviesseries.utils.Constants.YOUTUBE_THUMBNAIL_URL
import com.evangelidis.t_tmoviesseries.utils.Constants.YOUTUBE_VIDEO_URL
import com.evangelidis.t_tmoviesseries.utils.ItemsManager.getGlideImage
import com.evangelidis.t_tmoviesseries.view.person.PersonActivity
import com.evangelidis.t_tmoviesseries.view.search.SearchActivity
import com.evangelidis.t_tmoviesseries.view.seasons.SeasonsActivity
import com.evangelidis.t_tmoviesseries.view.main.MainActivity
import kotlinx.android.synthetic.main.activity_tv_show.*
import kotlinx.android.synthetic.main.activity_tv_show.directorsContainer
import kotlinx.android.synthetic.main.activity_tv_show.productionCompanies
import kotlinx.android.synthetic.main.activity_tv_show.productionCompaniesLayout
import kotlinx.android.synthetic.main.activity_tv_show.tvShowDirectors
import kotlinx.android.synthetic.main.activity_tv_show.progressBar
import kotlinx.android.synthetic.main.activity_tv_show.summaryContainer
import kotlinx.android.synthetic.main.main_toolbar.*
import java.util.ArrayList

class TvShowActivity : AppCompatActivity() {

    private var tvShowId = 0
    private lateinit var viewModel: ViewModelTvShow
    private lateinit var tvShow: TvShowDetailsResponse

    private var totalSeasonsNumber = 1

    private var watchlistList: List<WatchlistData>? = null

    private var mDb: WatchlistDataBase? = null
    private lateinit var mDbWorkerThread: DbWorkerThread
    private val mUiHandler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tv_show)

        tvShowId = intent.getIntExtra(TV_SHOW_ID, tvShowId)

        mDbWorkerThread = DbWorkerThread(DATABASE_THREAD)
        mDbWorkerThread.start()
        mDb = WatchlistDataBase.getInstance(this)

        getDataFromDB()

        item_tv_watchlist.setOnClickListener {
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
                item_tv_watchlist.setImageResource(R.drawable.ic_enable_watchlist)
                insertDataToDatabase(wishList, mDb, mDbWorkerThread)
            } else {
                item_tv_watchlist.setImageResource(R.drawable.ic_disable_watchlist)
                removeDataFromDatabase(wishList, mDb, mDbWorkerThread)
            }
        }

        imageToMain.setOnClickListener {
            val intent = Intent(this@TvShowActivity, MainActivity::class.java)
            startActivity(intent)
        }

        search_img.setOnClickListener {
            val intent = Intent(this@TvShowActivity, SearchActivity::class.java)
            startActivity(intent)
        }

        tvShowAllSeasons.setOnClickListener {
            val intent = Intent(this@TvShowActivity, SeasonsActivity::class.java)
            intent.apply {
                putExtra(TOTAL_SEASONS, totalSeasonsNumber)
                putExtra(TV_SHOW_NAME, tvShowTitle.text)
                putExtra(TV_SHOW_ID, tvShowId)
            }
            startActivity(intent)
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
                progressBar.gone()
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
            getGlideImage(this, IMAGE_POSTER_BASE_URL.plus(imageUrl), tvShowBackdrop)
        }

        data.name?.let {
            toolbar_title.text = data.name
            tvShowTitle.text = data.name
            tvShowTitle.show()
        }

        data.voteCount?.let {
            if (it > 0) {
                tvShowRating.text = data.voteAverage.toString()
                tvShowTotalVotes.text = getString(R.string.reviews_number).replace("{REVIEWS_NUMBER}", it.toString())
            }
        }

        if (!data.firstAirDate.isNullOrEmpty()) {
            data.inProduction?.let { it ->
                val releasedYear = data.firstAirDate.substring(0, 4)
                if (it) {
                    tvShowReleaseDate.text = getString(R.string.tv_show_release_date_format1).replace("{year}", releasedYear)
                } else {
                    data.seasons?.last()?.airDate?.let {
                        val lastYear = it.substring(0, 4)
                        if (lastYear == releasedYear) {
                            tvShowReleaseDate.text = getString(R.string.tv_show_release_date_format1).replace("{year}", releasedYear)
                        } else {
                            tvShowReleaseDate.text = getString(R.string.tv_show_release_date_format2)
                                .replace("{year1}", releasedYear)
                                .replace("{year2}", it.substring(0, 4))
                        }
                    }
                }
                tvShowReleaseDate.show()
            }
        }

        if(!data.episodeRunTime.isNullOrEmpty()) {
            data.episodeRunTime.first().let {
                tvShowEpisodeDuration.text = getString(R.string.minutes_format).replace("{min}", it.toString())
                tvShowEpisodeDuration.show()
            }
        }

        data.numberOfSeasons?.let {
            if (it == 1) {
                tvShowAllSeasons.text = getString(R.string.one_season_text)
            } else {
                tvShowAllSeasons.text = getString(R.string.season_number).replace("{x}", it.toString())
            }
            tvShowAllSeasons.show()
        }

        if (!data.overview.isNullOrEmpty()) {
            tvShowOverview.text = data.overview
            summaryContainer.show()
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

    private fun setUpGenres(data: List<Genre>) {
        val genres: ArrayList<String> = arrayListOf()
        for (element in data) {
            genres.add(element.name.orEmpty())
        }
        if (genres.isNotEmpty()) {
            tvShowGenres.text = genres.joinToString(separator = ", ")
            tvShowGenres.show()
        }
    }

    private fun setUpActors(casts: List<TvShowCast>?) {
        casts?.let {
            if (it.isNotEmpty()) {
                for (cast in it) {
                    val parent = layoutInflater.inflate(R.layout.thumbnail_actors_list, tvShowActors, false)
                    val thumbnail = parent.findViewById<ImageView>(R.id.thumbnail)
                    val textView = parent.findViewById<TextView>(R.id.actor_name)
                    val textView1 = parent.findViewById<TextView>(R.id.actor_character)
                    textView.text = cast.name
                    textView1.text = cast.character
                    thumbnail.requestLayout()
                    thumbnail.setOnClickListener {
                        val intent = Intent(this@TvShowActivity, PersonActivity::class.java)
                        intent.putExtra(PERSON_ID, cast.id)
                        startActivity(intent)
                    }

                    getGlideImage(this, IMAGE_SMALL_BASE_URL.plus(cast.profilePath), thumbnail)

                    tvShowActors.addView(parent)
                }
                tvShowActorsContainer.show()
            }
        }
    }

    private fun setUpVideosUI(results: List<Video>?) {
        results?.let {
            if (it.isNotEmpty()) {
                tvShowTrailers.removeAllViews()
                for (trailer in it) {
                    val parent = layoutInflater.inflate(R.layout.thumbnail_trailer, tvShowTrailers, false)
                    val thumbnail = parent.findViewById<ImageView>(R.id.thumbnail)
                    thumbnail.requestLayout()
                    thumbnail.setOnClickListener {
                        showTrailer(String.format(YOUTUBE_VIDEO_URL, trailer.key), applicationContext)
                    }

                    getGlideImage(this, YOUTUBE_THUMBNAIL_URL.replace("%s",trailer.key.orEmpty()), thumbnail)
                    tvShowTrailers.addView(parent)
                }
                tvShowTrailersContainer.show()
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
                    tvShowDirectors.text = directorsList[0]
                }
                2 -> {
                    tvShowDirectors.text = getString(R.string.multi_directors)
                        .replace("{dir1}", directorsList[0]).replace("{dir2}", directorsList[1])
                }
                else -> {
                    var directorsString = ""
                    for (x in 0 until directorsList.size - 1) {
                        directorsString += directorsList[x] + ", "
                    }
                    directorsString = directorsString.substring(0, directorsString.length - 2)
                    directorsString += "and " + directorsList[directorsList.size]

                    tvShowDirectors.text = directorsString
                }
            }
            directorsContainer.show()
        }
    }

    private fun setUpSimilarTvShowUI(data: TvShowListResponse) {
        data.results?.let {
            if (it.isNotEmpty()) {
                tvShowSimilar.removeAllViews()
                for (x in it) {
                    val parent = layoutInflater.inflate(R.layout.thumbnail_movie, tvShowSimilar, false)
                    val thumbnail: ImageView = parent.findViewById(R.id.thumbnail)
                    val tvName: TextView = parent.findViewById(R.id.movie_name)
                    val tvRate: TextView = parent.findViewById(R.id.movie_rate)

                    tvName.text = x.name
                    tvRate.text = getString(R.string.movie_rate).replace("{MOVIE_RATE}", x.voteAverage.toString())
                    thumbnail.requestLayout()

                    thumbnail.setOnClickListener {
                        val intent = Intent(this@TvShowActivity, TvShowActivity::class.java)
                        intent.putExtra(TV_SHOW_ID, x.id)
                        startActivity(intent)
                    }

                    getGlideImage(this, IMAGE_SMALL_BASE_URL.plus(x.posterPath), thumbnail)
                    tvShowSimilar.addView(parent)
                }
                tvShowSimilarContainer.show()
            }
        }
    }

    private fun setUpRecommendationTvShowsUI(data: TvShowListResponse) {
        data.results?.let {
            if (it.isNotEmpty()) {
                tvShowRecommendations.removeAllViews()
                for (x in it) {
                    val parent = layoutInflater.inflate(R.layout.thumbnail_movie, tvShowRecommendations, false)
                    val thumbnail: ImageView = parent.findViewById(R.id.thumbnail)
                    val tvName: TextView = parent.findViewById(R.id.movie_name)
                    val tvRate: TextView = parent.findViewById(R.id.movie_rate)

                    tvName.text = x.name
                    tvRate.text = getString(R.string.movie_rate).replace("{MOVIE_RATE}", x.voteAverage.toString())
                    thumbnail.requestLayout()

                    thumbnail.setOnClickListener {
                        val intent = Intent(this@TvShowActivity, TvShowActivity::class.java)
                        intent.putExtra(TV_SHOW_ID, x.id)
                        startActivity(intent)
                    }

                    getGlideImage(this, IMAGE_SMALL_BASE_URL.plus(x.posterPath), thumbnail)
                    tvShowRecommendations.addView(parent)
                }
                tvShowRecommendationsContainer.show()
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
            },
            800
        )
    }

    private fun setWishListImage() {
        val finder = watchlistList?.find { it.itemId == tvShowId }
        if (finder != null) {
            item_tv_watchlist.setImageResource(R.drawable.ic_enable_watchlist)
        }
    }
}