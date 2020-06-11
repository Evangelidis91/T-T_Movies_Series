package com.evangelidis.t_tmoviesseries.view.seasons

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.evangelidis.t_tmoviesseries.R
import com.evangelidis.t_tmoviesseries.extensions.gone
import com.evangelidis.t_tmoviesseries.extensions.show
import com.evangelidis.t_tmoviesseries.model.Episode
import com.evangelidis.t_tmoviesseries.utils.Constants.IMAGE_SMALL_BASE_URL
import com.evangelidis.t_tmoviesseries.utils.ItemsManager.changeDateFormat
import com.evangelidis.t_tmoviesseries.utils.ItemsManager.getGlideImage

class EpisodesListAdapter(private val episodes: MutableList<Episode>) :
    RecyclerView.Adapter<EpisodesListAdapter.EpisodeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodeViewHolder {
        return EpisodeViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_episode, parent, false)
        )
    }

    override fun getItemCount() = episodes.count()

    override fun onBindViewHolder(holder: EpisodeViewHolder, position: Int) {
        holder.bind(episodes[position])
    }

    inner class EpisodeViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private var episodeName: TextView = view.findViewById(R.id.episode_name)
        private var episodeDate: TextView = view.findViewById(R.id.episode_date)
        private var episodeImage: ImageView = view.findViewById(R.id.episode_poster)
        private var episodeRate: TextView = view.findViewById(R.id.episode_rate)

        fun bind(episode: Episode) {

            episodeDate.text = changeDateFormat(episode.airDate.orEmpty())

            if (episode.voteAverage == 0.0 || episode.voteCount <= 1) {
                episodeRate.gone()
            } else {
                episodeRate.text = String.format("%.1f", episode.voteAverage)
            }

            episodeName.text = itemView.resources.getString(R.string.episode_name)
                .replace("{NUMBER}", episode.episodeNumber.toString())
                .replace("{TITLE}", episode.name.orEmpty())


            episode.stillPath?.let {
                getGlideImage(itemView.context, IMAGE_SMALL_BASE_URL.plus(it), episodeImage)
                episodeImage.show()
            }
        }
    }
}