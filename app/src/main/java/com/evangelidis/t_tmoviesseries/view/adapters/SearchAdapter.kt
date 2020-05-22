package com.evangelidis.t_tmoviesseries.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.evangelidis.t_tmoviesseries.OnTrendingClickCallback
import com.evangelidis.t_tmoviesseries.R
import com.evangelidis.t_tmoviesseries.model.Multisearch
import com.evangelidis.t_tmoviesseries.room.DbWorkerThread
import com.evangelidis.t_tmoviesseries.room.WishListData
import com.evangelidis.t_tmoviesseries.room.WishListDataBase
import com.evangelidis.t_tmoviesseries.utils.Constants.IMAGE_BASE_URL_SMALL

class SearchAdapter(
    var trendingsList: MutableList<Multisearch>,
    var callback: OnTrendingClickCallback,
    var wishlistList: MutableList<WishListData>
) : RecyclerView.Adapter<SearchAdapter.SearchViewHolder>() {

    private var mDb: WishListDataBase? = null
    private lateinit var mDbWorkerThread: DbWorkerThread
    private var category: String? = null

    fun appendTrendings(trendings: MutableList<Multisearch>) {
        trendingsList.clear()
        trendings.sortByDescending { it.popularity }
        trendingsList.addAll(trendings)
        notifyDataSetChanged()
    }

    fun updateWishlist(wishlist: MutableList<WishListData>){
        wishlistList.clear()
        wishlistList.addAll(wishlist)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SearchViewHolder {
        mDbWorkerThread = DbWorkerThread("dbWorkerThread")
        mDbWorkerThread.start()
        mDb = WishListDataBase.getInstance(parent.context)

        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_search, parent, false)
        return SearchViewHolder(view)
    }

    override fun getItemCount() = trendingsList.size

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        holder.bind(trendingsList[position])
    }

    inner class SearchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val itemPoster = itemView.findViewById<ImageView>(R.id.item_poster)
        private val title = itemView.findViewById<TextView>(R.id.item_title)
        private val itemReleaseDate = itemView.findViewById<TextView>(R.id.item_release_date)
        private val itemType = itemView.findViewById<TextView>(R.id.item_type)
        private val itemRating = itemView.findViewById<TextView>(R.id.item_rating)
        private val itemWishlist = itemView.findViewById<ImageView>(R.id.item_movie_wishlist)

        fun bind(trend: Multisearch) {
            var imagePath: String? = null

            when (trend.mediaType) {
                "tv" -> {
                    title.text = trend.name
                    itemReleaseDate.text = trend.firstAirDate
                    itemType.text = itemView.context.getString(R.string.tv_show)
                    imagePath = trend.posterPath
                    category = "Movie"
                }
                "person" -> {
                    itemReleaseDate.visibility = View.GONE
                    itemRating.visibility = View.GONE
                    itemWishlist.visibility = View.GONE
                    itemType.text = itemView.context.getString(R.string.person)
                    imagePath = trend.profilePath
                    title.text = trend.name
                    category = "Person"
                }
                "movie" -> {
                    title.text = trend.title
                    itemReleaseDate.text = trend.releaseDate
                    itemType.text = itemView.context.getString(R.string.movie)
                    imagePath = trend.posterPath
                    category = "TV"
                }
            }
            itemRating.text = trend.voteAverage.toString()

            Glide.with(itemView)
                .load(IMAGE_BASE_URL_SMALL + imagePath)
                .apply(RequestOptions.placeholderOf(R.color.colorPrimary))
                .into(itemPoster)

            itemView.setOnClickListener { callback.onClick(trend) }

            if (!wishlistList.isNullOrEmpty()) {
                val finder = wishlistList.find { it.itemId == trend.id && it.category == category }
                if (finder != null) {
                    itemWishlist.setImageResource(R.drawable.ic_enable_wishlist)
                }
            }

            itemWishlist.setOnClickListener {
                val wishList = WishListData()
                wishList.itemId = trend.id
                wishList.category = category.orEmpty()

                if (wishlistList.isNullOrEmpty()) {
                    insertDataToDatabase(wishList)
                    wishlistList.add(wishList)
                    itemWishlist.setImageResource(R.drawable.ic_enable_wishlist)
                } else {
                    val finder =
                        wishlistList.find { it.itemId == trend.id && it.category == category }
                    if (finder != null) {
                        itemWishlist.setImageResource(R.drawable.ic_disable_wishlist)
                        removeDataFromDatabase(wishList)
                        wishlistList.remove(wishList)
                    } else {
                        insertDataToDatabase(wishList)
                        wishlistList.add(wishList)
                        itemWishlist.setImageResource(R.drawable.ic_enable_wishlist)
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
    }
}