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
import com.evangelidis.t_tmoviesseries.OnTvShowClickCallback
import com.evangelidis.t_tmoviesseries.R
import com.evangelidis.t_tmoviesseries.model.Genre
import com.evangelidis.t_tmoviesseries.model.TvShow
import com.evangelidis.t_tmoviesseries.room.DbWorkerThread
import com.evangelidis.t_tmoviesseries.room.WishListData
import com.evangelidis.t_tmoviesseries.room.WishListDataBase
import com.evangelidis.t_tmoviesseries.utils.Constants.IMAGE_BASE_URL

class TvShowAdapter(
    var tvShowListData: MutableList<TvShow>,
    var tvShowCallback: OnTvShowClickCallback,
    var wishlistList: MutableList<WishListData>
) : RecyclerView.Adapter<TvShowAdapter.TvShowViewHolder>() {

    private var genresList: ArrayList<Genre> = arrayListOf()
    private var mDb: WishListDataBase? = null
    private lateinit var mDbWorkerThread: DbWorkerThread

    fun updateData(newData: MutableList<TvShow>) {
        tvShowListData.clear()
        tvShowListData.addAll(newData)
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

    fun appendData(newData: MutableList<TvShow>) {
        tvShowListData.addAll(newData)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) : TvShowViewHolder{
        mDbWorkerThread = DbWorkerThread("dbWorkerThread")
        mDbWorkerThread.start()
        mDb = WishListDataBase.getInstance(parent.context)

        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tv, parent, false)
        return TvShowViewHolder(view)
    }

    override fun getItemCount() = tvShowListData.size

    override fun onBindViewHolder(holder: TvShowViewHolder, position: Int) {
        holder.bind(tvShowListData[position])
    }

    inner class TvShowViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        var releaseDate: TextView = itemView.findViewById(R.id.item_movie_release_date)
        var title: TextView = itemView.findViewById(R.id.item_movie_title)
        var rating: TextView = itemView.findViewById(R.id.item_movie_rating)
        var genres: TextView = itemView.findViewById(R.id.item_movie_genre)
        var poster: ImageView = itemView.findViewById(R.id.item_movie_poster)
        var addToWishList: ImageView = itemView.findViewById(R.id.item_movie_wishlist)

        fun bind(tv: TvShow) {
            tv.firstAirDate?.let {
                releaseDate.text = it.split("-")[0]
            }

            title.text = tv.name
            rating.text = tv.voteAverage.toString()
            genres.text = ""
            tv.genreIds?.let {
                genres.text = getGenres(it)
            }

            addToWishList.setImageResource(R.drawable.ic_disable_wishlist)

            Glide.with(itemView)
                .load(IMAGE_BASE_URL + tv.posterPath)
                .apply(RequestOptions.placeholderOf(R.color.colorPrimary))
                .into(poster)

            addToWishList.setImageResource(R.drawable.ic_disable_wishlist)

            itemView.setOnClickListener { tvShowCallback.onClick(tv) }

            if (!wishlistList.isNullOrEmpty()) {
                val currentItem = WishListData()
                currentItem.itemId = tv.id
                currentItem.category = "TV"

                val finder = wishlistList.find { it.itemId == tv.id && it.category == "TV" }
                if (finder != null) {
                    addToWishList.setImageResource(R.drawable.ic_enable_wishlist)
                }
            }

            addToWishList.setOnClickListener {
                val wishList = WishListData()
                wishList.itemId = tv.id
                wishList.category = "TV"

                if (wishlistList.isNullOrEmpty()) {
                    insertDataToDatabase(wishList)
                    wishlistList.add(wishList)
                    addToWishList.setImageResource(R.drawable.ic_enable_wishlist)
                } else {
                    val finder = wishlistList.find { it.itemId == tv.id && it.category == "TV" }
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
            val tvGenres = java.util.ArrayList<String>()
            for (genreId in genreIds) {
                for (genre in genresList) {
                    if (genre.id == genreId) {
                        genre.name?.let {
                            tvGenres.add(it)
                        }
                        break
                    }
                }
            }
            return TextUtils.join(", ", tvGenres)
        }
    }
}