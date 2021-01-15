package com.evangelidis.t_tmoviesseries.view.watchlist

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.evangelidis.t_tmoviesseries.R
import com.evangelidis.t_tmoviesseries.callbacks.OnWatchlistClickCallback
import com.evangelidis.t_tmoviesseries.databinding.ItemSearchBinding
import com.evangelidis.t_tmoviesseries.extensions.show
import com.evangelidis.t_tmoviesseries.room.DatabaseQueries
import com.evangelidis.t_tmoviesseries.room.WatchlistData
import com.evangelidis.t_tmoviesseries.utils.Constants.IMAGE_SMALL_BASE_URL
import com.evangelidis.t_tmoviesseries.utils.ItemsManager.getGlideImage

class WatchlistAdapter(
    var watchlistList: MutableList<WatchlistData>,
    var callback: OnWatchlistClickCallback,
    var context: Context
) : RecyclerView.Adapter<WatchlistAdapter.WatchlistViewHolder>() {

    fun appendWatchlistData(watchlist: List<WatchlistData>) {
        watchlistList.clear()
        watchlistList.addAll(watchlist)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WatchlistViewHolder {
        return WatchlistViewHolder(ItemSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int = watchlistList.count()

    override fun onBindViewHolder(holder: WatchlistViewHolder, position: Int) {
        holder.bind(watchlistList[position], position)
    }

    inner class WatchlistViewHolder(private val binding: ItemSearchBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(watchlist: WatchlistData, position: Int) {
            binding.itemTitle.text = watchlist.name
            binding.itemReleaseDate.text = watchlist.releasedDate
            binding.itemType.text = watchlist.category
            binding.itemRating.text = watchlist.rate.toString()
            binding.itemWatchlist.apply {
                setImageResource(R.drawable.ic_enable_watchlist)
                show()
            }

            itemView.setOnClickListener { callback.onClick(watchlist) }

            getGlideImage(itemView.context, IMAGE_SMALL_BASE_URL.plus(watchlist.posterPath), binding.itemPoster)

            binding.itemWatchlist.setOnClickListener {
                DatabaseQueries.removeItem(binding.root.context, watchlist.itemId)
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
