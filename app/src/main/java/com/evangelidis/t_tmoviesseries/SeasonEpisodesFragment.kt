package com.evangelidis.t_tmoviesseries

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.evangelidis.t_tmoviesseries.model.TvShowSeasonResponse

class SeasonEpisodesFragment(seasonDetailsResponse: TvShowSeasonResponse) : Fragment() {

    var seasonDetails = seasonDetailsResponse
    var episodesListAdapter: EpisodesListAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_season_episodes, container, false)

        val episodesList = view.findViewById<RecyclerView>(R.id.episodes_list)
        episodesList.layoutManager = LinearLayoutManager(context)

        episodesListAdapter = EpisodesListAdapter(seasonDetails.episodes)
        episodesList.adapter = episodesListAdapter

        return view
    }
}