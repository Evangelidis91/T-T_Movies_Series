package com.evangelidis.t_tmoviesseries.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.evangelidis.t_tmoviesseries.ItemsManager.getGenres
import com.evangelidis.t_tmoviesseries.callbacks.OnTvShowClickCallback
import com.evangelidis.t_tmoviesseries.R
import com.evangelidis.t_tmoviesseries.model.Genre
import com.evangelidis.t_tmoviesseries.model.TvShow
import com.evangelidis.t_tmoviesseries.room.DatabaseManager.insertDataToDatabase
import com.evangelidis.t_tmoviesseries.room.DatabaseManager.removeDataFromDatabase
import com.evangelidis.t_tmoviesseries.room.DbWorkerThread
import com.evangelidis.t_tmoviesseries.room.WatchlistData
import com.evangelidis.t_tmoviesseries.room.WatchlistDataBase
import com.evangelidis.t_tmoviesseries.utils.Constants.CATEGORY_TV
import com.evangelidis.t_tmoviesseries.utils.Constants.DATABASE_THREAD
import com.evangelidis.t_tmoviesseries.utils.Constants.IMAGE_BASE_URL

class TvShowAdapter(
    var tvShowListData: MutableList<TvShow>,
    var tvShowCallback: OnTvShowClickCallback,
    var watchlistList: MutableList<WatchlistData>
) : RecyclerView.Adapter<TvShowAdapter.TvShowViewHolder>() {

    private var genresList: ArrayList<Genre> = arrayListOf()
    private var mDb: WatchlistDataBase? = null
    private lateinit var mDbWorkerThread: DbWorkerThread

    fun updateData(newData: MutableList<TvShow>) {
        tvShowListData.clear()
        tvShowListData.addAll(newData)
        notifyDataSetChanged()
    }

    fun updateWatchlist(watchlist: MutableList<WatchlistData>) {
        watchlistList.clear()
        watchlistList.addAll(watchlist)
        notifyDataSetChanged()
    }

    fun appendGenres(genres: ArrayList<Genre>) {
        genresList.addAll(genres)
    }

    fun appendData(newData: MutableList<TvShow>) {
        tvShowListData.addAll(newData)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TvShowViewHolder {
        mDbWorkerThread = DbWorkerThread(DATABASE_THREAD)
        mDbWorkerThread.start()
        mDb = WatchlistDataBase.getInstance(parent.context)

        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tv, parent, false)
        return TvShowViewHolder(view)
    }

    override fun getItemCount() = tvShowListData.count()

    override fun onBindViewHolder(holder: TvShowViewHolder, position: Int) {
        holder.bind(tvShowListData[position])
    }

    inner class TvShowViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        var releaseDate: TextView = itemView.findViewById(R.id.item_movie_release_date)
        var title: TextView = itemView.findViewById(R.id.item_movie_title)
        var rating: TextView = itemView.findViewById(R.id.item_movie_rating)
        var genres: TextView = itemView.findViewById(R.id.item_movie_genre)
        var poster: ImageView = itemView.findViewById(R.id.item_movie_poster)
        var addToWishList: ImageView = itemView.findViewById(R.id.item_movie_wishlist)

        fun bind(tv: TvShow) {
            tv.firstAirDate?.let {
                releaseDate.text = it.substringBefore("-")
            }

            title.text = tv.name
            rating.text = tv.voteAverage.toString()
            tv.genreIds?.let {
                genres.text = getGenres(it, genresList)
            }

            addToWishList.setImageResource(R.drawable.ic_disable_wishlist)

            Glide.with(itemView)
                .load(IMAGE_BASE_URL + tv.posterPath)
                .apply(RequestOptions.placeholderOf(R.color.colorPrimary))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(poster)

            addToWishList.setImageResource(R.drawable.ic_disable_wishlist)

            itemView.setOnClickListener { tvShowCallback.onClick(tv) }

            if (!watchlistList.isNullOrEmpty()) {
                val currentItem = WatchlistData()
                currentItem.itemId = tv.id
                currentItem.category = CATEGORY_TV

                val finder = watchlistList.find { it.itemId == tv.id && it.category == CATEGORY_TV }
                if (finder != null) {
                    addToWishList.setImageResource(R.drawable.ic_enable_wishlist)
                }
            }

            addToWishList.setOnClickListener {
                val wishList = WatchlistData()
                wishList.itemId = tv.id
                wishList.category = CATEGORY_TV
                wishList.name = tv.name.orEmpty()
                wishList.posterPath = tv.posterPath.orEmpty()
                wishList.releasedDate = tv.firstAirDate.orEmpty()
                tv.voteAverage?.let {
                    wishList.rate = it
                }

                if (watchlistList.isNullOrEmpty()) {
                    insertDataToDatabase(wishList, mDb, mDbWorkerThread)
                    watchlistList.add(wishList)
                    addToWishList.setImageResource(R.drawable.ic_enable_wishlist)
                } else {
                    val finder = watchlistList.find { it.itemId == tv.id && it.category == CATEGORY_TV }
                    if (finder != null) {
                        addToWishList.setImageResource(R.drawable.ic_disable_wishlist)
                        removeDataFromDatabase(wishList, mDb, mDbWorkerThread)
                        watchlistList.remove(wishList)
                    } else {
                        insertDataToDatabase(wishList, mDb, mDbWorkerThread)
                        watchlistList.add(wishList)
                        addToWishList.setImageResource(R.drawable.ic_enable_wishlist)
                    }
                }
            }
        }
    }
}