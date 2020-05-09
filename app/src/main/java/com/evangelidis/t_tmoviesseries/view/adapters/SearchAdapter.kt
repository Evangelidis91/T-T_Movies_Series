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
import com.evangelidis.t_tmoviesseries.utils.Constants.IMAGE_BASE_URL_SMALL
import com.evangelidis.t_tmoviesseries.utils.InternetStatus.Companion.context

class SearchAdapter(
    var trendingsList: MutableList<Multisearch>,
    var callback: OnTrendingClickCallback
) : RecyclerView.Adapter<SearchAdapter.SearchViewHolder>() {


    fun appendTrendings(trendings: MutableList<Multisearch>) {
        trendingsList.clear()
        trendings.sortByDescending { it.popularity }
        trendingsList.addAll(trendings)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) = SearchViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_search, parent, false)
    )

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

        fun bind(trend: Multisearch) {
            var imagePath: String? = null

            if (trend.mediaType == "tv") {
                title.text = trend.name
                itemReleaseDate.text = trend.firstAirDate
                itemType.text = context.getString(R.string.tv_show)
                imagePath = trend.posterPath
            } else if (trend.mediaType == "person") {
                itemReleaseDate.visibility = View.GONE
                itemRating.visibility = View.GONE
                itemType.text = context.getString(R.string.person)
                imagePath = trend.profilePath
                title.text = trend.name
            } else if (trend.mediaType == "movie"){
                title.text = trend.title
                itemReleaseDate.text = trend.releaseDate
                itemType.text = context.getString(R.string.movie)
                imagePath = trend.posterPath
            }
            itemRating.text = trend.voteAverage.toString()

            Glide.with(itemView)
                .load(IMAGE_BASE_URL_SMALL + imagePath)
                .apply(RequestOptions.placeholderOf(R.color.colorPrimary))
                .into(itemPoster)

            itemView.setOnClickListener { callback.onClick(trend) }
        }
    }
}