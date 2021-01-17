package com.evangelidis.t_tmoviesseries.view.search

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.evangelidis.t_tmoviesseries.R
import com.evangelidis.t_tmoviesseries.databinding.ItemMovieBinding
import com.evangelidis.t_tmoviesseries.databinding.ItemPersonBinding
import com.evangelidis.t_tmoviesseries.databinding.ItemTvBinding
import com.evangelidis.t_tmoviesseries.model.Genre
import com.evangelidis.t_tmoviesseries.model.Multisearch
import com.evangelidis.t_tmoviesseries.room.DatabaseQueries
import com.evangelidis.t_tmoviesseries.room.WatchlistData
import java.lang.IllegalStateException

class SearchAdapter(var callback: SearchCallback) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class SectionType(val sectionType: String, val sectionId: Int) {
        PERSON("person", 0),
        MOVIE("movie", 1),
        TV_SHOW("tv", 2)
    }

    var trendsList = mutableListOf<Multisearch>()
        set(value) {
            value.sortByDescending { it.popularity }
            trendsList.clear()
            trendsList.addAll(value)
            notifyDataSetChanged()
        }

    var watchlist = mutableListOf<WatchlistData>()
        set(value) {
            watchlist.clear()
            watchlist.addAll(value)
            notifyDataSetChanged()
        }

    var moviesGenres = mutableListOf<Genre>()
        set(value) {
            moviesGenres.addAll(value)
        }

    var tvShowGenres = mutableListOf<Genre>()
        set(value) {
            tvShowGenres.addAll(value)
        }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): RecyclerView.ViewHolder {
        return when (position) {
            SectionType.MOVIE.sectionId -> SearchMovieViewHolder(ItemMovieBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            SectionType.TV_SHOW.sectionId -> SearchTvShowViewHolder(ItemTvBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            SectionType.PERSON.sectionId -> SearchPersonViewHolder(ItemPersonBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else -> throw IllegalStateException("Unknown type")
        }
    }

    override fun getItemCount() = trendsList.count()

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            SectionType.MOVIE.sectionId -> (holder as? SearchMovieViewHolder)?.bind(trendsList[position], callback, moviesGenres, this)
            SectionType.TV_SHOW.sectionId -> (holder as? SearchTvShowViewHolder)?.bind(trendsList[position], callback, tvShowGenres, this)
            SectionType.PERSON.sectionId -> (holder as? SearchPersonViewHolder)?.bind(trendsList[position], callback)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (trendsList[position].mediaType) {
            SectionType.PERSON.sectionType -> SectionType.PERSON.sectionId
            SectionType.TV_SHOW.sectionType -> SectionType.TV_SHOW.sectionId
            SectionType.MOVIE.sectionType -> SectionType.MOVIE.sectionId
            else -> throw IllegalStateException("Unknown type")
        }
    }

    fun setWatchListListener(imageView: ImageView, id: Int, category: String, title: String?, posterPath: String?, releaseDate: String?, voteAverage: Double?) {
        if (!watchlist.isNullOrEmpty()) {
            val finder = watchlist.find { it.itemId == id }
            if (finder != null) {
                imageView.setImageResource(R.drawable.ic_enable_watchlist)
            }
        }

        imageView.setOnClickListener {
            val watchItem = WatchlistData().apply {
                itemId = id
                this.category = category
                name = title.orEmpty()
                this.posterPath = posterPath.orEmpty()
                releasedDate = releaseDate.orEmpty()
                voteAverage?.let {
                    rate = it
                }
            }

            if (watchlist.isNullOrEmpty()) {
                DatabaseQueries.saveItem(imageView.rootView.context, watchItem)
                watchlist.add(watchItem)
                imageView.setImageResource(R.drawable.ic_enable_watchlist)
            } else {
                val finder = watchlist.find { it.itemId == id && it.category == category }
                if (finder != null) {
                    imageView.setImageResource(R.drawable.ic_disable_watchlist)
                    DatabaseQueries.removeItem(imageView.rootView.context, watchItem.itemId)
                    watchlist.remove(watchItem)
                } else {
                    DatabaseQueries.saveItem(imageView.rootView.context, watchItem)
                    watchlist.add(watchItem)
                    imageView.setImageResource(R.drawable.ic_enable_watchlist)
                }
            }
        }
    }
}
