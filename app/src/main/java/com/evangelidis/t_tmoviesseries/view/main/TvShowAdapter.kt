package com.evangelidis.t_tmoviesseries.view.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.evangelidis.t_tmoviesseries.databinding.ItemTvBinding
import com.evangelidis.t_tmoviesseries.model.Genre
import com.evangelidis.t_tmoviesseries.model.TvShow
import com.evangelidis.t_tmoviesseries.room.WatchlistData

class TvShowAdapter(private var callback: MainCallback) : RecyclerView.Adapter<TvShowViewHolder>() {

    var genresList = arrayListOf<Genre>()
        set(value) {
            genresList.addAll(value)
            notifyDataSetChanged()
        }

    var tvShowListData = mutableListOf<TvShow>()
        set(value) {
            tvShowListData.clear()
            tvShowListData.addAll(value)
            notifyDataSetChanged()
        }

    var watchlistList = mutableListOf<WatchlistData>()
        set(value) {
            watchlistList.clear()
            watchlistList.addAll(value)
            notifyDataSetChanged()
        }

    var newData = mutableListOf<TvShow>()
        set(value) {
            tvShowListData.addAll(value)
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TvShowViewHolder {
        return TvShowViewHolder(ItemTvBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount() = tvShowListData.count()

    override fun onBindViewHolder(holder: TvShowViewHolder, position: Int) {
        holder.bind(tvShowListData[position], watchlistList, callback, genresList)
    }
}
