package com.evangelidis.t_tmoviesseries.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.evangelidis.t_tmoviesseries.R
import com.evangelidis.t_tmoviesseries.callbacks.OnWishlistClickCallback
import com.evangelidis.t_tmoviesseries.room.DbWorkerThread
import com.evangelidis.t_tmoviesseries.room.WishListData
import com.evangelidis.t_tmoviesseries.room.WishListDataBase
import com.evangelidis.t_tmoviesseries.utils.Constants.IMAGE_BASE_URL_SMALL

class WishlistAdapter(
    var wishlistList: MutableList<WishListData>,
    var callback: OnWishlistClickCallback
) : RecyclerView.Adapter<WishlistAdapter.WishlistViewHolder>() {

    private var mDb: WishListDataBase? = null
    private lateinit var mDbWorkerThread: DbWorkerThread

    fun appendWishlistData(wishlistData: MutableList<WishListData>) {
        wishlistList.clear()
        wishlistList.addAll(wishlistData)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): WishlistViewHolder {
        mDbWorkerThread = DbWorkerThread("dbWorkerThread")
        mDbWorkerThread.start()
        mDb = WishListDataBase.getInstance(parent.context)

        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_search, parent, false)
        return WishlistViewHolder(view)
    }

    override fun getItemCount(): Int = wishlistList.size

    override fun onBindViewHolder(holder: WishlistViewHolder, position: Int) {
        holder.bind(wishlistList[position], position)
    }

    inner class WishlistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val itemPoster = itemView.findViewById<ImageView>(R.id.item_poster)
        private val title = itemView.findViewById<TextView>(R.id.item_title)
        private val itemReleaseDate = itemView.findViewById<TextView>(R.id.item_release_date)
        private val itemType = itemView.findViewById<TextView>(R.id.item_type)
        private val itemRating = itemView.findViewById<TextView>(R.id.item_rating)
        private val itemWishlist = itemView.findViewById<ImageView>(R.id.item_movie_wishlist)

        fun bind(wishlist: WishListData, position: Int) {

            title.text = wishlist.name
            itemReleaseDate.text = wishlist.releasedDate
            itemType.text = wishlist.category
            itemRating.text = wishlist.rate.toString()
            itemWishlist.visibility = View.VISIBLE
            itemWishlist.setImageResource(R.drawable.ic_enable_wishlist)

            itemView.setOnClickListener { callback.onClick(wishlist) }

            Glide.with(itemView)
                .load(IMAGE_BASE_URL_SMALL + wishlist.posterPath)
                .apply(RequestOptions.placeholderOf(R.color.colorPrimary))
                .into(itemPoster)

            itemWishlist.setOnClickListener {
                removeDataFromDatabase(wishlist, position)
            }
        }

        private fun removeDataFromDatabase(wishList: WishListData, position: Int) {
            val task = Runnable {
                mDb?.todoDao()?.deleteByUserId(wishList.itemId)
            }
            mDbWorkerThread.postTask(task)
            remove(position)
        }

        private fun remove(position: Int) {
            wishlistList.removeAt(position)
            notifyItemChanged(position)
            notifyItemRangeRemoved(position, 1)
        }
    }
}