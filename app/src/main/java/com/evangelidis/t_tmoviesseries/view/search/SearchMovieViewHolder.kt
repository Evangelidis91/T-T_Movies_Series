package com.evangelidis.t_tmoviesseries.view.search

import androidx.recyclerview.widget.RecyclerView
import com.evangelidis.t_tmoviesseries.R
import com.evangelidis.t_tmoviesseries.databinding.ItemMovieBinding
import com.evangelidis.t_tmoviesseries.model.Genre
import com.evangelidis.t_tmoviesseries.model.Multisearch
import com.evangelidis.t_tmoviesseries.utils.Constants
import com.evangelidis.t_tmoviesseries.utils.ItemsManager

class SearchMovieViewHolder(private val binding: ItemMovieBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(trend: Multisearch, callback: SearchCallback, moviesGenres: MutableList<Genre>, searchAdapter: SearchAdapter) {
        with(binding) {
            itemMovieWatchlist.setImageResource(R.drawable.ic_disable_watchlist)
            itemMovieTitle.text = trend.title
            itemMovieReleaseDate.text = trend.releaseDate?.substringBefore("-")
            itemMovieRating.text = trend.voteAverage.toString()

            trend.genreIds?.let {
                itemMovieGenre.text = ItemsManager.getGenres(it, moviesGenres as ArrayList<Genre>)
            }
            ItemsManager.getGlideImage(itemView.context, Constants.IMAGE_SMALL_BASE_URL.plus(trend.posterPath), itemMoviePoster)

            root.setOnClickListener { callback.navigateToMovie(trend.id) }

            searchAdapter.setWatchListListener(itemMovieWatchlist, trend.id, Constants.CATEGORY_MOVIE, trend.title, trend.posterPath, trend.releaseDate, trend.voteAverage)
        }
    }
}
