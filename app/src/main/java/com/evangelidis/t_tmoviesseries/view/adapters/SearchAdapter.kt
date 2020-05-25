package com.evangelidis.t_tmoviesseries.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.evangelidis.t_tmoviesseries.callbacks.OnTrendingClickCallback
import com.evangelidis.t_tmoviesseries.R
import com.evangelidis.t_tmoviesseries.model.Multisearch
import com.evangelidis.t_tmoviesseries.room.DatabaseManager.insertDataToDatabase
import com.evangelidis.t_tmoviesseries.room.DatabaseManager.removeDataFromDatabase
import com.evangelidis.t_tmoviesseries.room.DbWorkerThread
import com.evangelidis.t_tmoviesseries.room.WatchlistData
import com.evangelidis.t_tmoviesseries.room.WatchlistDataBase
import com.evangelidis.t_tmoviesseries.utils.Constants.IMAGE_BASE_URL_SMALL

class SearchAdapter(
    var trendingsList: MutableList<Multisearch>,
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
        mDbWorkerThread = DbWorkerThread("dbWorkerThread")
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

        private val itemPoster = itemView.findViewById<ImageView>(R.id.item_poster)
        private val title = itemView.findViewById<TextView>(R.id.item_title)
        private val itemReleaseDate = itemView.findViewById<TextView>(R.id.item_release_date)
        private val itemType = itemView.findViewById<TextView>(R.id.item_type)
        private val itemRating = itemView.findViewById<TextView>(R.id.item_rating)
        private val itemWishlist = itemView.findViewById<ImageView>(R.id.item_movie_wishlist)

        fun bind(trend: Multisearch) {
            var imagePath: String? = null
            var releasedDate: String? = null

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
                    itemReleaseDate.visibility = View.GONE
                    itemRating.visibility = View.GONE
                    itemWishlist.visibility = View.GONE
                    itemType.text = itemView.context.getString(R.string.person)
                    imagePath = trend.profilePath
                    title.text = trend.name
                    category = "Person"
                }
                "movie" -> {
                    title.text = trend.title
                    itemReleaseDate.text = trend.releaseDate
                    releasedDate = trend.releaseDate
                    itemType.text = itemView.context.getString(R.string.movie)
                    imagePath = trend.posterPath
                    category = "Movie"
                }
            }
            itemRating.text = trend.voteAverage.toString()

            Glide.with(itemView)
                .load(IMAGE_BASE_URL_SMALL + imagePath)
                .apply(RequestOptions.placeholderOf(R.color.colorPrimary))
                .into(itemPoster)

            itemView.setOnClickListener { callback.onClick(trend) }

            if (!watchlistList.isNullOrEmpty()) {
                val finder = watchlistList.find { it.itemId == trend.id && it.category == category }
                if (finder != null) {
                    itemWishlist.setImageResource(R.drawable.ic_enable_wishlist)
                }
            }

            itemWishlist.setOnClickListener {
                val wishList = WatchlistData()
                wishList.itemId = trend.id
                wishList.category = category.orEmpty()
                wishList.name = trend.name.orEmpty()
                wishList.posterPath = imagePath.orEmpty()
                wishList.releasedDate = releasedDate.orEmpty()
                trend.voteAverage?.let {
                    wishList.rate = it
                }

                if (watchlistList.isNullOrEmpty()) {
                    insertDataToDatabase(wishList, mDb, mDbWorkerThread)
                    watchlistList.add(wishList)
                    itemWishlist.setImageResource(R.drawable.ic_enable_wishlist)
                } else {
                    val finder =
                        watchlistList.find { it.itemId == trend.id && it.category == category }
                    if (finder != null) {
                        itemWishlist.setImageResource(R.drawable.ic_disable_wishlist)
                        removeDataFromDatabase(wishList, mDb, mDbWorkerThread)
                        watchlistList.remove(wishList)
                    } else {
                        insertDataToDatabase(wishList, mDb, mDbWorkerThread)
                        watchlistList.add(wishList)
                        itemWishlist.setImageResource(R.drawable.ic_enable_wishlist)
                    }
                }
            }
        }
    }
}