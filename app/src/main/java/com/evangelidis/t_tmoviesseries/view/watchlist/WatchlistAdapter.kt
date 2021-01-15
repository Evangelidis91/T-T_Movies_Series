package com.evangelidis.t_tmoviesseries.view.watchlist

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.evangelidis.t_tmoviesseries.R
import com.evangelidis.t_tmoviesseries.databinding.ItemWatchlistBinding
import com.evangelidis.t_tmoviesseries.extensions.show
import com.evangelidis.t_tmoviesseries.room.DatabaseQueries
import com.evangelidis.t_tmoviesseries.room.WatchlistData
import com.evangelidis.t_tmoviesseries.utils.Constants.IMAGE_SMALL_BASE_URL
import com.evangelidis.t_tmoviesseries.utils.InternetStatus
import com.evangelidis.t_tmoviesseries.utils.ItemsManager.getGlideImage
import com.evangelidis.tantintoast.TanTinToast

class WatchlistAdapter(var callback: WatchListItemCallback) : RecyclerView.Adapter<WatchlistAdapter.WatchlistViewHolder>() {

    companion object {
        const val TV = "TV"
        const val Movie = "Movie"
    }

    private val watchList = mutableListOf<WatchlistData>()

    fun appendWatchlistData(watchlist: List<WatchlistData>) {
        watchList.clear()
        watchList.addAll(watchlist)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WatchlistViewHolder {
        return WatchlistViewHolder(ItemWatchlistBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int = watchList.count()

    override fun onBindViewHolder(holder: WatchlistViewHolder, position: Int) {
        holder.bind(watchList[position], position)
    }

    inner class WatchlistViewHolder(private val binding: ItemWatchlistBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(watchlist: WatchlistData, position: Int) {
            with(binding) {
                itemTitle.text = watchlist.name
                itemReleaseDate.text = watchlist.releasedDate
                itemType.text = watchlist.category
                itemRating.text = watchlist.rate.toString()
                itemWatchlist.apply {
                    setImageResource(R.drawable.ic_enable_watchlist)
                    show()
                    setOnClickListener {
                        DatabaseQueries.removeItem(root.context, watchlist.itemId) {
                            remove(position)
                        }
                    }
                }
                getGlideImage(root.context, IMAGE_SMALL_BASE_URL.plus(watchlist.posterPath), itemPoster)
                root.setOnClickListener {
                    if (InternetStatus.getInstance(root.context).isOnline) {
                        when (watchlist.category) {
                            TV -> callback.navigateToTvShow(watchlist.itemId)
                            Movie -> callback.navigateToMovie(watchlist.itemId)
                        }
                    } else {
                        val typeface: Typeface? = ResourcesCompat.getFont(root.context, R.font.montserrat_regular)
                        TanTinToast.Warning(root.context).text(root.context.getString(R.string.no_internet)).typeface(typeface).show()
                    }
                }
            }
        }

        private fun remove(position: Int) {
            watchList.removeAt(position)
            notifyItemChanged(position)
            notifyItemRangeRemoved(position, 1)
            notifyDataSetChanged()

            if (watchList.isEmpty()) {
                callback.displayEmptyList()
            }
        }
    }
}
