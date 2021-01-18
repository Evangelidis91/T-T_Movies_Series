package com.evangelidis.t_tmoviesseries.view.seasons

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.evangelidis.t_tmoviesseries.databinding.ItemEpisodeBinding
import com.evangelidis.t_tmoviesseries.model.Episode

class EpisodesListAdapter : RecyclerView.Adapter<EpisodeViewHolder>() {

    var episodes = mutableListOf<Episode>()
        set(value) {
            episodes.clear()
            episodes.addAll(value)
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodeViewHolder =
        EpisodeViewHolder(ItemEpisodeBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = episodes.count()

    override fun onBindViewHolder(holder: EpisodeViewHolder, position: Int) = holder.bind(episodes[position])
}
