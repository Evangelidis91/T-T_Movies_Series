package com.evangelidis.t_tmoviesseries.view.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.evangelidis.t_tmoviesseries.databinding.ItemMovieBinding
import com.evangelidis.t_tmoviesseries.model.Genre
import com.evangelidis.t_tmoviesseries.model.Movie
import com.evangelidis.t_tmoviesseries.room.*

class MoviesListAdapter(private var callback: MainCallback) : RecyclerView.Adapter<MoviesViewHolder>() {

    var genresList = arrayListOf<Genre>()
        set(value) {
            genresList.addAll(value)
            notifyDataSetChanged()
        }

    var moviesListData = mutableListOf<Movie>()
        set(value) {
            moviesListData.clear()
            moviesListData.addAll(value)
            notifyDataSetChanged()
        }

    var watchlistList = mutableListOf<WatchlistData>()
        set(value) {
            watchlistList.clear()
            watchlistList.addAll(value)
            notifyDataSetChanged()
        }

    var newData = mutableListOf<Movie>()
        set(value) {
            moviesListData.addAll(value)
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoviesViewHolder {
        return MoviesViewHolder(ItemMovieBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount() = moviesListData.count()

    override fun onBindViewHolder(holder: MoviesViewHolder, position: Int) {
        holder.bind(moviesListData[position], watchlistList, callback, genresList)
    }
}
