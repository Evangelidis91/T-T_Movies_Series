package com.evangelidis.t_tmoviesseries.view.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.evangelidis.t_tmoviesseries.callbacks.OnTrendingClickCallback
import com.evangelidis.t_tmoviesseries.R
import com.evangelidis.t_tmoviesseries.extensions.gone
import com.evangelidis.t_tmoviesseries.model.Multisearch
import com.evangelidis.t_tmoviesseries.room.DatabaseManager.insertDataToDatabase
import com.evangelidis.t_tmoviesseries.room.DatabaseManager.removeDataFromDatabase
import com.evangelidis.t_tmoviesseries.room.DbWorkerThread
import com.evangelidis.t_tmoviesseries.room.WatchlistData
import com.evangelidis.t_tmoviesseries.room.WatchlistDataBase
import com.evangelidis.t_tmoviesseries.utils.Constants
import com.evangelidis.t_tmoviesseries.utils.Constants.CATEGORY_MOVIE
import com.evangelidis.t_tmoviesseries.utils.Constants.CATEGORY_PERSON
import com.evangelidis.t_tmoviesseries.utils.Constants.IMAGE_SMALL_BASE_URL
import com.evangelidis.t_tmoviesseries.utils.ItemsManager.getGlideImage

class SearchAdapter(
    private var trendingsList: MutableList<Multisearch>,
    var callback: OnTrendingClickCallback,
    var watchlistList: MutableList<WatchlistData>
) : RecyclerView.Adapter<SearchAdapter.SearchViewHolder>() {

    private var mDb: WatchlistDataBase? = null
    private lateinit var mDbWorkerThread: DbWorkerThread
    private var category: String? = null

    fun appendTrendings(trendings: MutableList<Multisearch>) {
        trendingsList.clear()
        trendings.sortByDescending { it.popularity }
        trendingsList.addAll(trendings)
        notifyDataSetChanged()
    }

    fun updateWatchlist(watchlist: MutableList<WatchlistData>) {
        watchlistList.clear()
        watchlistList.addAll(watchlist)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        mDbWorkerThread = DbWorkerThread(Constants.DATABASE_THREAD)
        mDbWorkerThread.start()
        mDb = WatchlistDataBase.getInstance(parent.context)

        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_search, parent, false)
        return SearchViewHolder(view)
    }

    override fun getItemCount() = trendingsList.count()

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        holder.bind(trendingsList[position])
    }

    inner class SearchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val itemPoster: ImageView = itemView.findViewById(R.id.item_poster)
        private val title: TextView = itemView.findViewById(R.id.item_title)
        private val itemReleaseDate: TextView = itemView.findViewById(R.id.item_release_date)
        private val itemType: TextView = itemView.findViewById(R.id.item_type)
        private val itemRating: TextView = itemView.findViewById(R.id.item_rating)
        private val itemWatchlist: ImageView = itemView.findViewById(R.id.item_movie_watchlist)

        fun bind(trend: Multisearch) {
            var imagePath: String? = null
            var releasedDate: String? = null

            itemWatchlist.setImageResource(R.drawable.ic_disable_watchlist)

            when (trend.mediaType) {
                "tv" -> {
                    title.text = trend.name
                    itemReleaseDate.text = trend.firstAirDate
                    releasedDate = trend.firstAirDate
                    itemType.text = itemView.context.getString(R.string.tv_show)
                    imagePath = trend.posterPath
                    category = "TV"
                }
                "person" -> {
                    itemReleaseDate.gone()
                    itemRating.gone()
                    itemWatchlist.gone()
                    itemType.text = CATEGORY_PERSON
                    imagePath = trend.profilePath
                    title.text = trend.name
                    category = CATEGORY_PERSON
                }
                "movie" -> {
                    title.text = trend.title
                    itemReleaseDate.text = trend.releaseDate
                    releasedDate = trend.releaseDate
                    itemType.text = CATEGORY_MOVIE
                    imagePath = trend.posterPath
                    category = CATEGORY_MOVIE
                }
            }
            itemRating.text = trend.voteAverage.toString()

            getGlideImage(itemView.context, IMAGE_SMALL_BASE_URL.plus(imagePath), itemPoster)

            itemView.setOnClickListener { callback.onClick(trend) }

            if (!watchlistList.isNullOrEmpty()) {
                val finder = watchlistList.find { it.itemId == trend.id && it.category == category }
                if (finder != null) {
                    itemWatchlist.setImageResource(R.drawable.ic_enable_watchlist)
                }
            }

            itemWatchlist.setOnClickListener {
                val wishList = WatchlistData()
                wishList.itemId = trend.id
                wishList.category = category.orEmpty()
                wishList.name = (trend.name ?: trend.title).toString()
                wishList.posterPath = imagePath.orEmpty()
                wishList.releasedDate = releasedDate.orEmpty()
                trend.voteAverage?.let {
                    wishList.rate = it
                }

                if (watchlistList.isNullOrEmpty()) {
                    insertDataToDatabase(wishList, mDb, mDbWorkerThread)
                    watchlistList.add(wishList)
                    itemWatchlist.setImageResource(R.drawable.ic_enable_watchlist)
                } else {
                    val finder = watchlistList.find { it.itemId == trend.id && it.category == category }
                    if (finder != null) {
                        itemWatchlist.setImageResource(R.drawable.ic_disable_watchlist)
                        removeDataFromDatabase(wishList, mDb, mDbWorkerThread)
                        watchlistList.remove(wishList)
                    } else {
                        insertDataToDatabase(wishList, mDb, mDbWorkerThread)
                        watchlistList.add(wishList)
                        itemWatchlist.setImageResource(R.drawable.ic_enable_watchlist)
                    }
                }
            }
        }
    }
}