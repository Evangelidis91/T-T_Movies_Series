package com.evangelidis.t_tmoviesseries.view.adapters

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.evangelidis.t_tmoviesseries.OnMoviesClickCallback
import com.evangelidis.t_tmoviesseries.R
import com.evangelidis.t_tmoviesseries.model.Genre
import com.evangelidis.t_tmoviesseries.model.Movie
import com.evangelidis.t_tmoviesseries.room.DbWorkerThread
import com.evangelidis.t_tmoviesseries.room.WishListData
import com.evangelidis.t_tmoviesseries.room.WishListDataBase
import com.evangelidis.t_tmoviesseries.utils.Constants.IMAGE_BASE_URL

class MoviesListAdapter(
    var moviesListData: MutableList<Movie>,
    var movieCallback: OnMoviesClickCallback,
    var wishlistList: MutableList<WishListData>
) : RecyclerView.Adapter<MoviesListAdapter.MoviesViewHolder>() {

    private var mDb: WishListDataBase? = null
    private lateinit var mDbWorkerThread: DbWorkerThread

    private var genresList: ArrayList<Genre> = arrayListOf()

    fun updateData(newData: MutableList<Movie>) {
        moviesListData.clear()
        moviesListData.addAll(newData)
        notifyDataSetChanged()
    }

    fun updateWishlist(wishlist: MutableList<WishListData>){
        wishlistList.clear()
        wishlistList.addAll(wishlist)
        notifyDataSetChanged()
    }

    fun appendGenres(genres: ArrayList<Genre>) {
        genresList.addAll(genres)
    }

    fun appendData(newData: MutableList<Movie>) {
        moviesListData.addAll(newData)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) : MoviesViewHolder{
        mDbWorkerThread = DbWorkerThread("dbWorkerThread")
        mDbWorkerThread.start()
        mDb = WishListDataBase.getInstance(parent.context)

        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_movie, parent, false)
        return MoviesViewHolder(view)
    }
    override fun getItemCount() = moviesListData.size

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
            releaseDate.text =
                movie.releaseDate?.let {
                    it.split("-".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()[0]
                }
            title.text = movie.title
            rating.text = movie.voteAverage.toString()
            movie.genreIds?.let {
                genres.text = getGenres(it)
            }
            addToWishList.setImageResource(R.drawable.ic_disable_wishlist)

            Glide.with(itemView.context)
                .load(IMAGE_BASE_URL + movie.posterPath)
                .dontAnimate()
                .apply(RequestOptions.placeholderOf(R.color.colorPrimary))
                .into(poster)

            addToWishList.setImageResource(R.drawable.ic_disable_wishlist)

            itemView.setOnClickListener { movieCallback.onClick(movie) }

            if (!wishlistList.isNullOrEmpty()) {
                val currentItem = WishListData()
                currentItem.itemId = movie.id
                currentItem.category = "Movie"

                val finder = wishlistList.find { it.itemId == movie.id && it.category == "Movie" }
                if (finder != null) {
                    addToWishList.setImageResource(R.drawable.ic_enable_wishlist)
                }
            }

            addToWishList.setOnClickListener {
                val wishList = WishListData()
                wishList.itemId = movie.id
                wishList.category = "Movie"

                if (wishlistList.isNullOrEmpty()) {
                    insertDataToDatabase(wishList)
                    wishlistList.add(wishList)
                    addToWishList.setImageResource(R.drawable.ic_enable_wishlist)
                } else {
                    val finder =
                        wishlistList.find { it.itemId == movie.id && it.category == "Movie" }
                    if (finder != null) {
                        addToWishList.setImageResource(R.drawable.ic_disable_wishlist)
                        removeDataFromDatabase(wishList)
                        wishlistList.remove(wishList)
                    } else {
                        insertDataToDatabase(wishList)
                        wishlistList.add(wishList)
                        addToWishList.setImageResource(R.drawable.ic_enable_wishlist)
                    }
                }
            }
        }

        private fun insertDataToDatabase(wishList: WishListData) {
            val task = Runnable { mDb?.todoDao()?.insert(wishList) }
            mDbWorkerThread.postTask(task)
        }

        private fun removeDataFromDatabase(wishList: WishListData) {
            val task = Runnable {
                mDb?.todoDao()?.deleteByUserId(wishList.itemId)
            }
            mDbWorkerThread.postTask(task)
        }

        private fun getGenres(genreIds: List<Int>): String {
            val movieGenres: MutableList<String> = arrayListOf()
            for (genreId in genreIds) {
                for ((id, name) in genresList) {
                    if (id == genreId) {
                        name?.let {
                            movieGenres.add(it)
                        }
                        break
                    }
                }
            }
            return TextUtils.join(", ", movieGenres)
        }
    }
}