package com.evangelidis.t_tmoviesseries.view.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.evangelidis.t_tmoviesseries.callbacks.OnTrendingClickCallback
import com.evangelidis.t_tmoviesseries.R
import com.evangelidis.t_tmoviesseries.databinding.ItemSearchBinding
import com.evangelidis.t_tmoviesseries.extensions.gone
import com.evangelidis.t_tmoviesseries.model.Multisearch
import com.evangelidis.t_tmoviesseries.room.DatabaseQueries
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
        return SearchViewHolder(ItemSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount() = trendingsList.count()

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        holder.bind(trendingsList[position])
    }

    inner class SearchViewHolder(private val binding: ItemSearchBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(trend: Multisearch) {
            var imagePath: String? = null
            var releasedDate: String? = null

            binding.itemWatchlist.setImageResource(R.drawable.ic_disable_watchlist)

            when (trend.mediaType) {
                "tv" -> {
                    binding.itemTitle.text = trend.name
                    binding.itemReleaseDate.text = trend.firstAirDate
                    releasedDate = trend.firstAirDate
                    binding.itemType.text = itemView.context.getString(R.string.tv_show)
                    imagePath = trend.posterPath
                    category = "TV"
                }
                "person" -> {
                    binding.itemReleaseDate.gone()
                    binding.itemRating.gone()
                    binding.itemWatchlist.gone()
                    binding.itemType.text = CATEGORY_PERSON
                    imagePath = trend.profilePath
                    binding.itemTitle.text = trend.name
                    category = CATEGORY_PERSON
                }
                "movie" -> {
                    binding.itemTitle.text = trend.title
                    binding.itemReleaseDate.text = trend.releaseDate
                    releasedDate = trend.releaseDate
                    binding.itemType.text = CATEGORY_MOVIE
                    imagePath = trend.posterPath
                    category = CATEGORY_MOVIE
                }
            }
            binding.itemRating.text = trend.voteAverage.toString()

            getGlideImage(itemView.context, IMAGE_SMALL_BASE_URL.plus(imagePath), binding.itemPoster)

            itemView.setOnClickListener { callback.onClick(trend) }

            if (!watchlistList.isNullOrEmpty()) {
                val finder = watchlistList.find { it.itemId == trend.id && it.category == category }
                if (finder != null) {
                    binding.itemWatchlist.setImageResource(R.drawable.ic_enable_watchlist)
                }
            }

            binding.itemWatchlist.setOnClickListener {
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
                    DatabaseQueries.saveItem(binding.root.context, wishList)
                    watchlistList.add(wishList)
                    binding.itemWatchlist.setImageResource(R.drawable.ic_enable_watchlist)
                } else {
                    val finder = watchlistList.find { it.itemId == trend.id && it.category == category }
                    if (finder != null) {
                        binding.itemWatchlist.setImageResource(R.drawable.ic_disable_watchlist)
                        DatabaseQueries.removeItem(binding.root.context, wishList.itemId)
                        watchlistList.remove(wishList)
                    } else {
                        DatabaseQueries.saveItem(binding.root.context, wishList)
                        watchlistList.add(wishList)
                        binding.itemWatchlist.setImageResource(R.drawable.ic_enable_watchlist)
                    }
                }
            }
        }
    }
}