package com.evangelidis.t_tmoviesseries.di

import com.evangelidis.t_tmoviesseries.model.api.TMDBService
import com.evangelidis.t_tmoviesseries.view.main.ViewModelMain
import com.evangelidis.t_tmoviesseries.view.movie.ViewModelMovie
import com.evangelidis.t_tmoviesseries.view.person.ViewModelPerson
import com.evangelidis.t_tmoviesseries.view.search.ViewModelSearch
import com.evangelidis.t_tmoviesseries.view.seasons.ViewModelSeasons
import com.evangelidis.t_tmoviesseries.view.tvshow.ViewModelTvShow
import dagger.Component

@Component(modules = [ApiModule::class])
interface ApiComponent {

    fun inject(service: TMDBService)


    fun inject(viewModel: ViewModelMain)

    fun inject(viewModel: ViewModelMovie)

    fun inject(viewModel: ViewModelTvShow)

    fun inject(viewModel: ViewModelPerson)

    fun inject(viewModel: ViewModelSearch)

    fun inject(ViewModel : ViewModelSeasons)
}