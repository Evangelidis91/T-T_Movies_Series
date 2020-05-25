package com.evangelidis.t_tmoviesseries.view.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.evangelidis.t_tmoviesseries.R
import com.evangelidis.t_tmoviesseries.callbacks.OnWatchlistClickCallback
import com.evangelidis.t_tmoviesseries.extensions.show
import com.evangelidis.t_tmoviesseries.room.DatabaseManager.removeDataFromDatabase
import com.evangelidis.t_tmoviesseries.room.DbWorkerThread
import com.evangelidis.t_tmoviesseries.room.WatchlistData
import com.evangelidis.t_tmoviesseries.room.WatchlistDataBase
import com.evangelidis.t_tmoviesseries.utils.Constants.DATABASE_THREAD
import com.evangelidis.t_tmoviesseries.utils.Constants.IMAGE_BASE_URL_SMALL
import com.evangelidis.t_tmoviesseries.view.WatchlistActivity

class WatchlistAdapter(
    var watchlistList: MutableList<WatchlistData>,
    var callback: OnWatchlistClickCallback,
    var context: Context
) : RecyclerView.Adapter<WatchlistAdapter.WatchlistViewHolder>() {


    private var mDb: WatchlistDataBase? = null
    private lateinit var mDbWorkerThread: DbWorkerThread

    fun appendWatchlistData(watchlist: MutableList<WatchlistData>) {
        watchlistList.clear()
        watchlistList.addAll(watchlist)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup,viewType: Int): WatchlistViewHolder {
        mDbWorkerThread = DbWorkerThread(DATABASE_THREAD)
        mDbWorkerThread.start()
        mDb = WatchlistDataBase.getInstance(parent.context)

        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_search, parent, false)
        return WatchlistViewHolder(view)
    }

    override fun getItemCount(): Int = watchlistList.count()

    override fun onBindViewHolder(holder: WatchlistViewHolder, position: Int) {
        holder.bind(watchlistList[position], position)
    }

    inner class WatchlistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val itemPoster = itemView.findViewById<ImageView>(R.id.item_poster)
        private val title = itemView.findViewById<TextView>(R.id.item_title)
        private val itemReleaseDate = itemView.findViewById<TextView>(R.id.item_release_date)
        private val itemType = itemView.findViewById<TextView>(R.id.item_type)
        private val itemRating = itemView.findViewById<TextView>(R.id.item_rating)
        private val itemWatchlist = itemView.findViewById<ImageView>(R.id.item_movie_wishlist)

        fun bind(watchlist: WatchlistData, position: Int) {

            title.text = watchlist.name
            itemReleaseDate.text = watchlist.releasedDate
            itemType.text = watchlist.category
            itemRating.text = watchlist.rate.toString()
            itemWatchlist.apply {
                setImageResource(R.drawable.ic_enable_wishlist)
                show()
            }

            itemView.setOnClickListener { callback.onClick(watchlist) }

            Glide.with(itemView)
                .load(IMAGE_BASE_URL_SMALL + watchlist.posterPath)
                .apply(RequestOptions.placeholderOf(R.color.colorPrimary))
                .into(itemPoster)

            itemWatchlist.setOnClickListener {
                removeDataFromDatabase(watchlist, mDb, mDbWorkerThread)
                remove(position)
            }
        }

        private fun remove(position: Int) {
            watchlistList.removeAt(position)
            notifyItemChanged(position)
            notifyItemRangeRemoved(position, 1)
            notifyDataSetChanged()

            if (watchlistList.isEmpty()) {
                (context as WatchlistActivity).displayEmptyList()
            }
        }
    }
}