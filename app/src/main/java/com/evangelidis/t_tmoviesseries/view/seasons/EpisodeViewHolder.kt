package com.evangelidis.t_tmoviesseries.view.seasons

import androidx.recyclerview.widget.RecyclerView
import com.evangelidis.t_tmoviesseries.R
import com.evangelidis.t_tmoviesseries.databinding.ItemEpisodeBinding
import com.evangelidis.t_tmoviesseries.extensions.gone
import com.evangelidis.t_tmoviesseries.extensions.show
import com.evangelidis.t_tmoviesseries.model.Episode
import com.evangelidis.t_tmoviesseries.utils.Constants
import com.evangelidis.t_tmoviesseries.utils.ItemsManager

class EpisodeViewHolder(private val binding: ItemEpisodeBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(episode: Episode) {
        binding.episodeDate.text = ItemsManager.changeDateFormat(episode.airDate.orEmpty())

        if (episode.voteAverage == 0.0 || episode.voteCount <= 1) {
            binding.episodeRate.gone()
        } else {
            binding.episodeRate.text = String.format("%.1f", episode.voteAverage)
        }

        binding.episodeName.text = itemView.resources.getString(R.string.episode_name)
            .replace("{NUMBER}", episode.episodeNumber.toString())
            .replace("{TITLE}", episode.name.orEmpty())

        episode.stillPath?.let {
            ItemsManager.getGlideImage(itemView.context, Constants.IMAGE_MEDIUM_BASE_URL.plus(it), binding.episodePoster)
            binding.episodePoster.show()
        }
    }
}
