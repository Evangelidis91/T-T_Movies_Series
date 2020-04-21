package com.evangelidis.t_tmoviesseries.view

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.evangelidis.t_tmoviesseries.R
import com.evangelidis.t_tmoviesseries.model.Genre
import com.evangelidis.t_tmoviesseries.model.TvShow
import com.evangelidis.t_tmoviesseries.utils.Constants.IMAGE_BASE_URL

class TvShowAdapter( var tvShowListData: MutableList<TvShow>) : RecyclerView.Adapter<TvShowAdapter.TvShowViewHolder>() {

    private var genresList: ArrayList<Genre> = arrayListOf()

    fun updateData(newData: MutableList<TvShow>) {
        tvShowListData.clear()
        tvShowListData.addAll(newData)
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
    ) = TvShowViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_tv, parent, false)
    )

    override fun getItemCount() = tvShowListData.size

    override fun onBindViewHolder(holder: TvShowAdapter.TvShowViewHolder, position: Int) {
        holder.bind(tvShowListData[position])
    }


    inner class TvShowViewHolder(view : View) : RecyclerView.ViewHolder(view){

        var releaseDate: TextView = itemView.findViewById(R.id.item_movie_release_date)
        var title: TextView = itemView.findViewById(R.id.item_movie_title)
        var rating: TextView = itemView.findViewById(R.id.item_movie_rating)
        var genres: TextView = itemView.findViewById(R.id.item_movie_genre)
        var poster: ImageView = itemView.findViewById(R.id.item_movie_poster)
        var addToWishList: ImageView = itemView.findViewById(R.id.item_movie_wishlist)

        fun bind(tv: TvShow) {
            releaseDate.text = tv.firstAirDate.split("-")[0]
            title.text = tv.name
            rating.text = tv.voteAverage.toString()
            genres.text = ""
            genres.text = getGenres(tv.genreIds)
            addToWishList.setImageResource(R.drawable.ic_disable_wishlist)

            Glide.with(itemView)
                .load(IMAGE_BASE_URL + tv.posterPath)
                .apply(RequestOptions.placeholderOf(R.color.colorPrimary))
                .into(poster)

        }

        private fun getGenres(genreIds: List<Int>): String {
            val tvGenres = java.util.ArrayList<String>()
            for (genreId in genreIds) {
                for (genre in genresList) {
                    if (genre.id == genreId) {
                        tvGenres.add(genre.name)
                        break
                    }
                }
            }
            return TextUtils.join(", ", tvGenres)
        }
    }
}