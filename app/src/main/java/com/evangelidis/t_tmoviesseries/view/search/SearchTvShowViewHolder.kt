package com.evangelidis.t_tmoviesseries.view.search

import androidx.recyclerview.widget.RecyclerView
import com.evangelidis.t_tmoviesseries.R
import com.evangelidis.t_tmoviesseries.databinding.ItemTvBinding
import com.evangelidis.t_tmoviesseries.extensions.show
import com.evangelidis.t_tmoviesseries.model.Genre
import com.evangelidis.t_tmoviesseries.model.Multisearch
import com.evangelidis.t_tmoviesseries.utils.Constants
import com.evangelidis.t_tmoviesseries.utils.ItemsManager

class SearchTvShowViewHolder(private val binding: ItemTvBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(trend: Multisearch, callback: SearchCallback, tvShowGenres: MutableList<Genre>, searchAdapter: SearchAdapter) {
        with(binding) {
            itemTvWatchlist.setImageResource(R.drawable.ic_disable_watchlist)
            itemTvTitle.text = trend.name
            itemTvReleaseDate.text = trend.firstAirDate
            itemTvRating.text = trend.voteAverage.toString()
            searchCategory.apply {
                text = Constants.CATEGORY_TV
                show()
            }

            trend.genreIds?.let {
                itemTvGenre.text = ItemsManager.getGenres(it, tvShowGenres as ArrayList<Genre>)
            }
            ItemsManager.getGlideImage(itemView.context, Constants.IMAGE_SMALL_BASE_URL.plus(trend.posterPath), itemTvPoster)

            root.setOnClickListener { callback.navigateToTvShow(trend.id) }

            searchAdapter.setWatchListListener(itemTvWatchlist, trend.id, Constants.CATEGORY_TV, trend.name, trend.posterPath, trend.firstAirDate, trend.voteAverage)
        }
    }
}
