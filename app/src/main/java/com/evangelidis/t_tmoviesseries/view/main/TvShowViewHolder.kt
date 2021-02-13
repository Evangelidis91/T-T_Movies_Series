package com.evangelidis.t_tmoviesseries.view.main

import androidx.recyclerview.widget.RecyclerView
import com.evangelidis.t_tmoviesseries.R
import com.evangelidis.t_tmoviesseries.databinding.ItemTvBinding
import com.evangelidis.t_tmoviesseries.model.Genre
import com.evangelidis.t_tmoviesseries.model.TvShow
import com.evangelidis.t_tmoviesseries.room.DatabaseQueries
import com.evangelidis.t_tmoviesseries.room.WatchlistData
import com.evangelidis.t_tmoviesseries.utils.Constants
import com.evangelidis.t_tmoviesseries.utils.ItemsManager

class TvShowViewHolder(private val binding: ItemTvBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(tv: TvShow, watchlistList: MutableList<WatchlistData>, callback: MainCallback, genresList: ArrayList<Genre>) {
        tv.firstAirDate?.let {
            binding.itemTvReleaseDate.text = it.substringBefore("-")
        }

        binding.itemTvTitle.text = tv.name
        binding.itemTvRating.text = tv.voteAverage.toString()
        tv.genreIds?.let {
            binding.itemTvGenre.text = ItemsManager.getGenres(it, genresList)
        }

        binding.itemTvWatchlist.setImageResource(R.drawable.ic_disable_watchlist)

        ItemsManager.getGlideImage(binding.root.context, Constants.IMAGE_SMALL_BASE_URL.plus(tv.posterPath), binding.itemTvPoster)

        binding.root.setOnClickListener { callback.navigateToTvShow(tv.id) }

        if (!watchlistList.isNullOrEmpty()) {
            val currentItem = WatchlistData()
            currentItem.itemId = tv.id
            currentItem.category = Constants.CATEGORY_TV

            if (watchlistList.find { it.itemId == tv.id && it.category == Constants.CATEGORY_TV } != null) {
                binding.itemTvWatchlist.setImageResource(R.drawable.ic_enable_watchlist)
            }
        }

        binding.itemTvWatchlist.setOnClickListener {
            val watchItem = WatchlistData().apply {
                itemId = tv.id
                category = Constants.CATEGORY_TV
                name = tv.name.orEmpty()
                posterPath = tv.posterPath.orEmpty()
                releasedDate = tv.firstAirDate.orEmpty()
                tv.voteAverage?.let {
                    rate = it
                }
            }

            if (watchlistList.isNullOrEmpty()) {
                DatabaseQueries.saveItem(binding.root.context, watchItem)
                watchlistList.add(watchItem)
                binding.itemTvWatchlist.setImageResource(R.drawable.ic_enable_watchlist)
            } else {
                if (watchlistList.find { it.itemId == tv.id && it.category == Constants.CATEGORY_TV } != null) {
                    binding.itemTvWatchlist.setImageResource(R.drawable.ic_disable_watchlist)
                    DatabaseQueries.removeItem(binding.root.context, watchItem.itemId)
                    watchlistList.remove(watchItem)
                } else {
                    DatabaseQueries.saveItem(binding.root.context, watchItem)
                    watchlistList.add(watchItem)
                    binding.itemTvWatchlist.setImageResource(R.drawable.ic_enable_watchlist)
                }
            }
        }
    }
}
