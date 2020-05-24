package com.evangelidis.t_tmoviesseries.view

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.evangelidis.t_tmoviesseries.R
import com.evangelidis.t_tmoviesseries.model.*
import com.evangelidis.t_tmoviesseries.room.DbWorkerThread
import com.evangelidis.t_tmoviesseries.room.WishListData
import com.evangelidis.t_tmoviesseries.room.WishListDataBase
import com.evangelidis.t_tmoviesseries.utils.Constants
import com.evangelidis.t_tmoviesseries.utils.Constants.ACTOR_IMAGE_URL
import com.evangelidis.t_tmoviesseries.utils.Constants.PERSON_ID
import com.evangelidis.t_tmoviesseries.utils.Constants.TOTAL_SEASONS
import com.evangelidis.t_tmoviesseries.utils.Constants.TV_SHOW_ID
import com.evangelidis.t_tmoviesseries.utils.Constants.TV_SHOW_NAME
import com.evangelidis.t_tmoviesseries.utils.Constants.YOUTUBE_THUMBNAIL_URL
import com.evangelidis.t_tmoviesseries.utils.Constants.YOUTUBE_VIDEO_URL
import com.evangelidis.t_tmoviesseries.viewmodel.ListViewModel
import kotlinx.android.synthetic.main.activity_movie.*
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
    lateinit var viewModel: ListViewModel
    private lateinit var tvShow: TvShowDetailsResponse

    private var totalSeasonsNumber = 1

    private var wishlistList: List<WishListData>? = null

    private var mDb: WishListDataBase? = null
    private lateinit var mDbWorkerThread: DbWorkerThread
    private val mUiHandler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tv_show)

        tvShowId = intent.getIntExtra(TV_SHOW_ID, tvShowId)

        mDbWorkerThread = DbWorkerThread("dbWorkerThread")
        mDbWorkerThread.start()
        mDb = WishListDataBase.getInstance(this)

        getDataFromDB()

        item_tv_wishlist.setOnClickListener {
            val finder = wishlistList?.find { it.itemId == tvShowId }
            val wishList = WishListData()
            wishList.itemId = tvShowId
            wishList.category = "TV"
            wishList.name = tvShow.name.orEmpty()
            wishList.posterPath = tvShow.posterPath.orEmpty()
            wishList.releasedDate = tvShow.firstAirDate.orEmpty()
            tvShow.voteAverage?.let {
                wishList.rate = it
            }

            if (finder == null) {
                item_tv_wishlist.setImageResource(R.drawable.ic_enable_wishlist)
                insertDataToDatabase(wishList)
            } else {
                item_tv_wishlist.setImageResource(R.drawable.ic_disable_wishlist)
                removeDataFromDatabase(wishList)
            }
        }

        imageToMain.setOnClickListener {
            val intent = Intent(this@TvShowActivity, MainActivity::class.java)
            startActivity(intent)
        }

        viewModel = ViewModelProviders.of(this).get(ListViewModel::class.java)

        viewModel.getTvShowDetails(tvShowId)
        viewModel.getTvShowCredits(tvShowId)
        viewModel.getTvShowVideos(tvShowId)
        viewModel.getTvShowSimilar(tvShowId)
        viewModel.getTvShowRecommendation(tvShowId)

        observeViewModel()

        tvShowAllSeasons.setOnClickListener {
            val intent = Intent(this@TvShowActivity, SeasonsActivity::class.java)
            intent.putExtra(TOTAL_SEASONS, totalSeasonsNumber)
            intent.putExtra(TV_SHOW_NAME, tvShowTitle.text)
            intent.putExtra(TV_SHOW_ID, tvShowId)
            startActivity(intent)
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
                progressBar.visibility = View.GONE
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

        if (!data.backdropPath.isNullOrEmpty()) {
            Glide.with(this)
                .load(Constants.IMAGE_BASE_URL + data.backdropPath)
                .apply(RequestOptions.placeholderOf(R.color.colorPrimary))
                .into(tvShowBackdrop)
        } else if (!data.posterPath.isNullOrEmpty()) {
            Glide.with(this)
                .load(Constants.IMAGE_BASE_URL + data.posterPath)
                .apply(RequestOptions.placeholderOf(R.color.colorPrimary))
                .into(tvShowBackdrop)
        }

        data.name?.let {
            toolbar_title.text = data.name
            tvShowTitle.text = data.name
            tvShowTitle.visibility = View.VISIBLE
        }

        data.voteCount?.let {
            if (it > 0) {
                tvShowRating.text = data.voteAverage.toString()
                tvShowTotalVotes.text =
                    getString(R.string.reviews_number).replace("{REVIEWS_NUMBER}", it.toString())
            }
        }

        data.inProduction?.let { it ->
            val releasedYear = data.firstAirDate?.substring(0, 4)
            if (it) {
                tvShowReleaseDate.text = getString(R.string.tv_show_release_date_format1).replace(
                    "{year}",
                    releasedYear.toString()
                )
            } else {
                data.seasons?.last()?.airDate?.let {
                    val lastYear = it.substring(0, 4)
                    if (lastYear == releasedYear) {
                        tvShowReleaseDate.text =
                            getString(R.string.tv_show_release_date_format1)
                                .replace("{year}", releasedYear.toString())
                    } else {
                        tvShowReleaseDate.text = getString(R.string.tv_show_release_date_format2)
                            .replace("{year1}", releasedYear.toString())
                            .replace("{year2}", it.substring(0, 4))
                    }
                }
            }
        }

        data.episodeRunTime?.get(0)?.let {
            tvShowEpisodeDuration.text =
                getString(R.string.minutes_format).replace("{min}", it.toString())
        }

        data.numberOfSeasons?.let {
            if (it == 1) {
                tvShowAllSeasons.text = getString(R.string.one_season_text)
            } else {
                tvShowAllSeasons.text =
                    getString(R.string.season_number).replace("{x}", it.toString())
            }
        }

        data.overview?.let {
            tvShowOverview.text = it
            summaryContainer.visibility = View.VISIBLE
        }

        data.productionCompanies?.let {
            productionCompanies.removeAllViews()
            for (company in it) {
                val parent = layoutInflater.inflate(R.layout.thumbnail_company, productionCompanies, false)
                val companyName: TextView = parent.findViewById(R.id.productionCompanyName)
                company.name?.let {
                    companyName.text = company.name
                    productionCompanies.addView(parent)
                }
            }
            productionCompaniesLayout.visibility = View.VISIBLE
        }
    }

    private fun setUpGenres(data: List<Genre>) {
        var currentGenres = ""
        for (element in data) {
            currentGenres += element.name
            currentGenres += ", "
        }
        tvShowGenres.text = currentGenres.substring(0, currentGenres.length - 2)
    }

    private fun setUpActors(casts: List<TvShowCast>?) {
        casts?.let {
            tvShowActorsContainer.visibility = View.VISIBLE
            for (cast in it) {

                val parent =
                    layoutInflater.inflate(R.layout.thumbnail_actors_list, tvShowActors, false)
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

                Glide.with(this@TvShowActivity)
                    .load(ACTOR_IMAGE_URL + cast.profilePath)
                    .apply(RequestOptions.placeholderOf(R.color.mainBackground))
                    .into(thumbnail)

                tvShowActors.addView(parent)
            }
        }
    }

    private fun setUpVideosUI(results: List<Video>?) {

        results?.let {
            tvShowTrailersContainer.visibility = View.VISIBLE

            tvShowTrailers.removeAllViews()
            for (trailer in it) {
                val parent =
                    layoutInflater.inflate(R.layout.thumbnail_trailer, tvShowTrailers, false)

                val thumbnail = parent.findViewById<ImageView>(R.id.thumbnail)
                thumbnail.requestLayout()
                thumbnail.setOnClickListener {
                    showTrailer(String.format(YOUTUBE_VIDEO_URL, trailer.key))
                }
                Glide.with(this@TvShowActivity)
                    .load(String.format(YOUTUBE_THUMBNAIL_URL, trailer.key))
                    .apply(RequestOptions.placeholderOf(R.color.colorPrimary).centerCrop())
                    .into(thumbnail)
                tvShowTrailers.addView(parent)
            }
        }
    }

    private fun setUpDirectors(crew: List<TvShowCrew>?) {

        val directorsList = ArrayList<String>()

        crew?.let { it ->
            for (x in it.indices) {
                if (it[x].job == "Director") {
                    it[x].name?.let {
                        directorsList.add(it)
                    }
                }
            }
        }


        if (!directorsList.isNullOrEmpty()) {
            directorsContainer.visibility = View.VISIBLE
            when (directorsList.size) {
                1 -> {
                    tvShowDirectors.text = directorsList[0]
                }
                2 -> {
                    tvShowDirectors.text = resources.getString(R.string.multi_directors)
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
        }
    }


    private fun setUpSimilarTvShowUI(data: TvShowListResponse) {

        data.results?.let {
            tvShowSimilarContainer.visibility = View.VISIBLE
            tvShowSimilar.removeAllViews()
            for (x in it) {
                val parent =
                    layoutInflater.inflate(R.layout.thumbnail_movie, tvShowSimilar, false)
                val thumbnail = parent.findViewById<ImageView>(R.id.thumbnail)
                val textView = parent.findViewById<TextView>(R.id.movie_name)
                textView.text = x.name + " (" + x.voteAverage + ")"
                thumbnail.requestLayout()

                thumbnail.setOnClickListener {
                    val intent = Intent(this@TvShowActivity, TvShowActivity::class.java)
                    intent.putExtra(TV_SHOW_ID, x.id)
                    startActivity(intent)
                }

                Glide.with(this@TvShowActivity)
                    .load(ACTOR_IMAGE_URL + x.posterPath)
                    .apply(RequestOptions.placeholderOf(R.color.mainBackground))
                    .into(thumbnail)
                tvShowSimilar.addView(parent)
            }
        }
    }

    private fun setUpRecommendationTvShowsUI(data: TvShowListResponse) {

        data.results?.let {
            tvShowRecommendationsContainer.visibility = View.VISIBLE
            tvShowRecommendations.removeAllViews()

            for (x in it) {
                val parent = layoutInflater.inflate(
                    R.layout.thumbnail_movie,
                    tvShowRecommendations,
                    false
                )
                val thumbnail = parent.findViewById<ImageView>(R.id.thumbnail)
                val textView = parent.findViewById<TextView>(R.id.movie_name)
                textView.text = x.name + " (" + x.voteAverage + ")"
                thumbnail.requestLayout()

                thumbnail.setOnClickListener {
                    val intent = Intent(this@TvShowActivity, TvShowActivity::class.java)
                    intent.putExtra(TV_SHOW_ID, x.id)
                    startActivity(intent)
                }

                Glide.with(this@TvShowActivity)
                    .load(ACTOR_IMAGE_URL + x.posterPath)
                    .apply(RequestOptions.placeholderOf(R.color.mainBackground))
                    .into(thumbnail)
                tvShowRecommendations.addView(parent)
            }
        }
    }

    private fun getDataFromDB() {
        Handler().postDelayed(
            {
                val task = Runnable {
                    val wishlistData = mDb?.todoDao()?.getAll()
                    mUiHandler.post {
                        if (!wishlistData.isNullOrEmpty()) {
                            wishlistList = wishlistData
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
        val finder = wishlistList?.find { it.itemId == tvShowId }
        if (finder != null) {
            item_tv_wishlist.setImageResource(R.drawable.ic_enable_wishlist)
        }
    }

    private fun insertDataToDatabase(wishList: WishListData) {
        val task = Runnable { mDb?.todoDao()?.insert(wishList) }
        mDbWorkerThread.postTask(task)
    }

    private fun removeDataFromDatabase(wishList: WishListData) {
        val task = Runnable {
            mDb?.todoDao()?.deleteByUserId(wishList.itemId)
        }
        mDbWorkerThread.postTask(task)
    }

    private fun showTrailer(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

}
