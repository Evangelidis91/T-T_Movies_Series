package com.evangelidis.t_tmoviesseries.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.evangelidis.t_tmoviesseries.ItemsManager.getGenres
import com.evangelidis.t_tmoviesseries.callbacks.OnMoviesClickCallback
import com.evangelidis.t_tmoviesseries.R
import com.evangelidis.t_tmoviesseries.model.Genre
import com.evangelidis.t_tmoviesseries.model.Movie
import com.evangelidis.t_tmoviesseries.room.*
import com.evangelidis.t_tmoviesseries.room.DatabaseManager.insertDataToDatabase
import com.evangelidis.t_tmoviesseries.room.DatabaseManager.removeDataFromDatabase
import com.evangelidis.t_tmoviesseries.utils.Constants.CATEGORY_MOVIE
import com.evangelidis.t_tmoviesseries.utils.Constants.DATABASE_THREAD
import com.evangelidis.t_tmoviesseries.utils.Constants.IMAGE_BASE_URL

class MoviesListAdapter(
    var moviesListData: MutableList<Movie>,
    var movieCallback: OnMoviesClickCallback,
    var watchlistList: MutableList<WatchlistData>
) : RecyclerView.Adapter<MoviesListAdapter.MoviesViewHolder>() {

    private var mDb: WatchlistDataBase? = null
    private lateinit var mDbWorkerThread: DbWorkerThread

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
        mDbWorkerThread = DbWorkerThread(DATABASE_THREAD)
        mDbWorkerThread.start()
        mDb = WatchlistDataBase.getInstance(parent.context)

        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_movie, parent, false)
        return MoviesViewHolder(view)
    }

    override fun getItemCount() = moviesListData.count()

    override fun onBindViewHolder(holder: MoviesViewHolder, position: Int) {
        holder.bind(moviesListData[position])
    }

    inner class MoviesViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        var releaseDate: TextView = itemView.findViewById(R.id.item_movie_release_date)
        var title: TextView = itemView.findViewById(R.id.item_movie_title)
        var rating: TextView = itemView.findViewById(R.id.item_movie_rating)
        var genres: TextView = itemView.findViewById(R.id.item_movie_genre)
        var poster: ImageView = itemView.findViewById(R.id.item_movie_poster)
        var addToWishList: ImageView = itemView.findViewById(R.id.item_movie_wishlist)

        fun bind(movie: Movie) {
            releaseDate.text = movie.releaseDate?.substringBefore("-")
            title.text = movie.title
            rating.text = movie.voteAverage.toString()
            movie.genreIds?.let {
                genres.text = getGenres(it, genresList)
            }
            addToWishList.setImageResource(R.drawable.ic_disable_wishlist)

            Glide.with(itemView.context)
                .load(IMAGE_BASE_URL + movie.posterPath)
                .dontAnimate()
                .apply(RequestOptions.placeholderOf(R.color.colorPrimary))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(poster)

            itemView.setOnClickListener { movieCallback.onClick(movie) }

            if (!watchlistList.isNullOrEmpty()) {
                val currentItem = WatchlistData()
                currentItem.itemId = movie.id
                currentItem.category = CATEGORY_MOVIE

                val finder = watchlistList.find { it.itemId == movie.id && it.category == CATEGORY_MOVIE }
                if (finder != null) {
                    addToWishList.setImageResource(R.drawable.ic_enable_wishlist)
                }
            }

            addToWishList.setOnClickListener {
                val wishList = WatchlistData()
                wishList.itemId = movie.id
                wishList.category = CATEGORY_MOVIE
                wishList.name = movie.title.orEmpty()
                wishList.posterPath = movie.posterPath.orEmpty()
                wishList.releasedDate = movie.releaseDate.orEmpty()
                movie.voteAverage?.let {
                    wishList.rate = it
                }

                if (watchlistList.isNullOrEmpty()) {
                    insertDataToDatabase(wishList, mDb, mDbWorkerThread)
                    watchlistList.add(wishList)
                    addToWishList.setImageResource(R.drawable.ic_enable_wishlist)
                } else {
                    val finder = watchlistList.find { it.itemId == movie.id && it.category == CATEGORY_MOVIE }
                    if (finder != null) {
                        addToWishList.setImageResource(R.drawable.ic_disable_wishlist)
                        removeDataFromDatabase(wishList, mDb, mDbWorkerThread)
                        watchlistList.remove(wishList)
                    } else {
                        insertDataToDatabase(wishList, mDb, mDbWorkerThread)
                        watchlistList.add(wishList)
                        addToWishList.setImageResource(R.drawable.ic_enable_wishlist)
                    }
                }
            }
        }
    }
}