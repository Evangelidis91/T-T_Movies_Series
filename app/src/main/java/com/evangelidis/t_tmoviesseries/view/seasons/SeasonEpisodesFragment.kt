package com.evangelidis.t_tmoviesseries.view.seasons

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.evangelidis.t_tmoviesseries.databinding.FragmentSeasonEpisodesBinding
import com.evangelidis.t_tmoviesseries.model.TvShowSeasonResponse

class SeasonEpisodesFragment(seasonDetailsResponse: TvShowSeasonResponse) : Fragment() {

    private var seasonDetails = seasonDetailsResponse
    private var episodesListAdapter: EpisodesListAdapter? = null
    private val binding: FragmentSeasonEpisodesBinding by lazy { FragmentSeasonEpisodesBinding.inflate(layoutInflater) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        episodesListAdapter = EpisodesListAdapter(seasonDetails.episodes)
        binding.episodesList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = episodesListAdapter
        }
        return binding.root
    }
}