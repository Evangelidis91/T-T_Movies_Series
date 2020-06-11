package com.evangelidis.t_tmoviesseries.view.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.evangelidis.t_tmoviesseries.utils.ItemsManager.getGenres
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
import com.evangelidis.t_tmoviesseries.utils.Constants.IMAGE_SMALL_BASE_URL
import com.evangelidis.t_tmoviesseries.utils.ItemsManager.getGlideImage

class TvShowAdapter(
    private var tvShowListData: MutableList<TvShow>,
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

        private var releaseDate: TextView = itemView.findViewById(R.id.item_movie_release_date)
        private var title: TextView = itemView.findViewById(R.id.item_movie_title)
        private var rating: TextView = itemView.findViewById(R.id.item_movie_rating)
        private var genres: TextView = itemView.findViewById(R.id.item_movie_genre)
        private var poster: ImageView = itemView.findViewById(R.id.item_movie_poster)
        private var addToWishList: ImageView = itemView.findViewById(R.id.item_movie_watchlist)

        fun bind(tv: TvShow) {
            tv.firstAirDate?.let {
                releaseDate.text = it.substringBefore("-")
            }

            title.text = tv.name
            rating.text = tv.voteAverage.toString()
            tv.genreIds?.let {
                genres.text = getGenres(it, genresList)
            }

            addToWishList.setImageResource(R.drawable.ic_disable_watchlist)

            getGlideImage(itemView.context, IMAGE_SMALL_BASE_URL.plus(tv.posterPath), poster)

            addToWishList.setImageResource(R.drawable.ic_disable_watchlist)

            itemView.setOnClickListener { tvShowCallback.onClick(tv) }

            if (!watchlistList.isNullOrEmpty()) {
                val currentItem = WatchlistData()
                currentItem.itemId = tv.id
                currentItem.category = CATEGORY_TV

                val finder = watchlistList.find { it.itemId == tv.id && it.category == CATEGORY_TV }
                if (finder != null) {
                    addToWishList.setImageResource(R.drawable.ic_enable_watchlist)
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
                    addToWishList.setImageResource(R.drawable.ic_enable_watchlist)
                } else {
                    val finder = watchlistList.find { it.itemId == tv.id && it.category == CATEGORY_TV }
                    if (finder != null) {
                        addToWishList.setImageResource(R.drawable.ic_disable_watchlist)
                        removeDataFromDatabase(wishList, mDb, mDbWorkerThread)
                        watchlistList.remove(wishList)
                    } else {
                        insertDataToDatabase(wishList, mDb, mDbWorkerThread)
                        watchlistList.add(wishList)
                        addToWishList.setImageResource(R.drawable.ic_enable_watchlist)
                    }
                }
            }
        }
    }
}