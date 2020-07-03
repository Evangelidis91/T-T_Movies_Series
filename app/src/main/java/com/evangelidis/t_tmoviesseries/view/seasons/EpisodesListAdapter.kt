package com.evangelidis.t_tmoviesseries.view.seasons

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.evangelidis.t_tmoviesseries.R
import com.evangelidis.t_tmoviesseries.databinding.ItemEpisodeBinding
import com.evangelidis.t_tmoviesseries.extensions.gone
import com.evangelidis.t_tmoviesseries.extensions.show
import com.evangelidis.t_tmoviesseries.model.Episode
import com.evangelidis.t_tmoviesseries.utils.Constants.IMAGE_SMALL_BASE_URL
import com.evangelidis.t_tmoviesseries.utils.ItemsManager.changeDateFormat
import com.evangelidis.t_tmoviesseries.utils.ItemsManager.getGlideImage

class EpisodesListAdapter(private val episodes: MutableList<Episode>) :
    RecyclerView.Adapter<EpisodesListAdapter.EpisodeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodeViewHolder {
        return EpisodeViewHolder(ItemEpisodeBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount() = episodes.count()

    override fun onBindViewHolder(holder: EpisodeViewHolder, position: Int) {
        holder.bind(episodes[position])
    }

    inner class EpisodeViewHolder(private val binding: ItemEpisodeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(episode: Episode) {
            binding.episodeDate.text = changeDateFormat(episode.airDate.orEmpty())

            if (episode.voteAverage == 0.0 || episode.voteCount <= 1) {
                binding.episodeRate.gone()
            } else {
                binding.episodeRate.text = String.format("%.1f", episode.voteAverage)
            }

            binding.episodeName.text = itemView.resources.getString(R.string.episode_name)
                .replace("{NUMBER}", episode.episodeNumber.toString())
                .replace("{TITLE}", episode.name.orEmpty())


            episode.stillPath?.let {
                getGlideImage(itemView.context, IMAGE_SMALL_BASE_URL.plus(it), binding.episodePoster)
                binding.episodePoster.show()
            }
        }
    }
}