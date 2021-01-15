package com.evangelidis.t_tmoviesseries.view.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.evangelidis.t_tmoviesseries.R
import com.evangelidis.t_tmoviesseries.callbacks.OnMoviesClickCallback
import com.evangelidis.t_tmoviesseries.databinding.ItemMovieBinding
import com.evangelidis.t_tmoviesseries.model.Genre
import com.evangelidis.t_tmoviesseries.model.Movie
import com.evangelidis.t_tmoviesseries.room.*
import com.evangelidis.t_tmoviesseries.utils.Constants.CATEGORY_MOVIE
import com.evangelidis.t_tmoviesseries.utils.Constants.IMAGE_SMALL_BASE_URL
import com.evangelidis.t_tmoviesseries.utils.ItemsManager.getGenres
import com.evangelidis.t_tmoviesseries.utils.ItemsManager.getGlideImage

class MoviesListAdapter(
    private var moviesListData: MutableList<Movie>,
    private var movieCallback: OnMoviesClickCallback,
    private var watchlistList: MutableList<WatchlistData>
) : RecyclerView.Adapter<MoviesListAdapter.MoviesViewHolder>() {

    private var genresList: ArrayList<Genre> = arrayListOf()

    fun updateData(newData: MutableList<Movie>) {
        moviesListData.clear()
        moviesListData.addAll(newData)
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

    fun appendData(newData: MutableList<Movie>) {
        moviesListData.addAll(newData)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoviesViewHolder {
        return MoviesViewHolder(ItemMovieBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount() = moviesListData.count()

    override fun onBindViewHolder(holder: MoviesViewHolder, position: Int) {
        holder.bind(moviesListData[position])
    }

    inner class MoviesViewHolder(private val binding: ItemMovieBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(movie: Movie) {
            binding.itemMovieReleaseDate.text = movie.releaseDate?.substringBefore("-")
            binding.itemMovieTitle.text = movie.title
            binding.itemMovieRating.text = movie.voteAverage.toString()
            movie.genreIds?.let {
                binding.itemMovieGenre.text = getGenres(it, genresList)
            }
            binding.itemMovieWatchlist.setImageResource(R.drawable.ic_disable_watchlist)

            getGlideImage(itemView.context, IMAGE_SMALL_BASE_URL.plus(movie.posterPath), binding.itemMoviePoster)

            itemView.setOnClickListener { movieCallback.onClick(movie) }

            if (!watchlistList.isNullOrEmpty()) {
                val currentItem = WatchlistData()
                currentItem.itemId = movie.id
                currentItem.category = CATEGORY_MOVIE

                val finder = watchlistList.find { it.itemId == movie.id && it.category == CATEGORY_MOVIE }
                if (finder != null) {
                    binding.itemMovieWatchlist.setImageResource(R.drawable.ic_enable_watchlist)
                }
            }

            binding.itemMovieWatchlist.setOnClickListener {
                val watchItem = WatchlistData().apply {
                    itemId = movie.id
                    category = CATEGORY_MOVIE
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
                    val finder = watchlistList.find { it.itemId == movie.id && it.category == CATEGORY_MOVIE }
                    if (finder != null) {
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
}
