package com.evangelidis.t_tmoviesseries.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.evangelidis.t_tmoviesseries.R
import com.evangelidis.t_tmoviesseries.model.Episode
import com.evangelidis.t_tmoviesseries.utils.Constants
import java.text.SimpleDateFormat
import java.util.*

class EpisodesListAdapter(private val episodes: MutableList<Episode>) :
    RecyclerView.Adapter<EpisodesListAdapter.EpisodeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodeViewHolder {
        return EpisodeViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_episode, parent, false)
        )
    }

    override fun getItemCount() = episodes.size

    override fun onBindViewHolder(holder: EpisodeViewHolder, position: Int) {
        holder.bind(episodes[position])
    }

    inner class EpisodeViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private var episodeName: TextView = view.findViewById(R.id.episode_name)
        private var episodeDate: TextView = view.findViewById(R.id.episode_date)
        private var episodeImage: ImageView = view.findViewById(R.id.episode_poster)
        private var episodeRate: TextView = view.findViewById(R.id.episode_rate)
        private var episodeRateLayout: LinearLayout = view.findViewById(R.id.episode_rate_layout)

        fun bind(episode: Episode) {

            val parser = SimpleDateFormat("yyyy-MM-dd")
            val formatter = SimpleDateFormat("dd-MMM-yyyy")
            val output = formatter.format(parser.parse(episode.airDate))
            val currentDate = formatter.format(Date())
            val cd = formatter.parse(currentDate)
            val ed = formatter.parse(output)

            if (episode.voteAverage == 0.0 || (cd.before(ed)) || episode.voteCount <= 1) {
                episodeRateLayout.visibility = View.GONE
            } else {
                episodeRate.text = String.format("%.1f", episode.voteAverage)
            }

            episodeName.text = (episode.episodeNumber).toString() + ". " + episode.name
            episodeDate.text = output

            if (!episode.stillPath.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(Constants.IMAGE_BASE_URL + episode.stillPath)
                    .into(episodeImage)
            } else{
                episodeImage.visibility = View.INVISIBLE
            }
        }
    }
}