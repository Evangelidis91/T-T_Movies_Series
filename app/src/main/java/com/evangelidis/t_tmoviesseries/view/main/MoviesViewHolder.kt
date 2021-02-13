package com.evangelidis.t_tmoviesseries.view.main

import androidx.recyclerview.widget.RecyclerView
import com.evangelidis.t_tmoviesseries.R
import com.evangelidis.t_tmoviesseries.databinding.ItemMovieBinding
import com.evangelidis.t_tmoviesseries.model.Genre
import com.evangelidis.t_tmoviesseries.model.Movie
import com.evangelidis.t_tmoviesseries.room.DatabaseQueries
import com.evangelidis.t_tmoviesseries.room.WatchlistData
import com.evangelidis.t_tmoviesseries.utils.Constants
import com.evangelidis.t_tmoviesseries.utils.ItemsManager

class MoviesViewHolder(private val binding: ItemMovieBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(movie: Movie, watchlistList: MutableList<WatchlistData>, callback: MainCallback, genresList: ArrayList<Genre>) {
        binding.itemMovieReleaseDate.text = movie.releaseDate?.substringBefore("-")
        binding.itemMovieTitle.text = movie.title
        binding.itemMovieRating.text = movie.voteAverage.toString()
        movie.genreIds?.let {
            binding.itemMovieGenre.text = ItemsManager.getGenres(it, genresList)
        }
        binding.itemMovieWatchlist.setImageResource(R.drawable.ic_disable_watchlist)

        ItemsManager.getGlideImage(itemView.context, Constants.IMAGE_SMALL_BASE_URL.plus(movie.posterPath), binding.itemMoviePoster)

        itemView.setOnClickListener { callback.navigateToMovie(movie.id) }

        if (!watchlistList.isNullOrEmpty()) {
            val currentItem = WatchlistData()
            currentItem.itemId = movie.id
            currentItem.category = Constants.CATEGORY_MOVIE

            if (watchlistList.find { it.itemId == movie.id && it.category == Constants.CATEGORY_MOVIE } != null) {
                binding.itemMovieWatchlist.setImageResource(R.drawable.ic_enable_watchlist)
            }
        }

        binding.itemMovieWatchlist.setOnClickListener {
            val watchItem = WatchlistData().apply {
                itemId = movie.id
                category = Constants.CATEGORY_MOVIE
                name = movie.title.orEmpty()
                posterPath = movie.posterPath.orEmpty()
                releasedDate = movie.releaseDate.orEmpty()
                movie.voteAverage?.let {
                    rate = it
                }
            }

            if (watchlistList.isNullOrEmpty()) {
                DatabaseQueries.saveItem(binding.root.context, watchItem)
                watchlistList.add(watchItem)
                binding.itemMovieWatchlist.setImageResource(R.drawable.ic_enable_watchlist)
            } else {
                if (watchlistList.find { it.itemId == movie.id && it.category == Constants.CATEGORY_MOVIE } != null) {
                    binding.itemMovieWatchlist.setImageResource(R.drawable.ic_disable_watchlist)
                    DatabaseQueries.removeItem(binding.root.context, watchItem.itemId)
                    watchlistList.remove(watchItem)
                } else {
                    DatabaseQueries.saveItem(binding.root.context, watchItem)
                    watchlistList.add(watchItem)
                    binding.itemMovieWatchlist.setImageResource(R.drawable.ic_enable_watchlist)
                }
            }
        }
    }
}
