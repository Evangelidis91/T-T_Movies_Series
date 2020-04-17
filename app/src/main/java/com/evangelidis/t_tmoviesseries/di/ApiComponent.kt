package com.evangelidis.t_tmoviesseries.di

import com.evangelidis.t_tmoviesseries.model.TMDBService
import com.evangelidis.t_tmoviesseries.viewmodel.ListViewModel
import dagger.Component

@Component(modules = [ApiModule::class])
interface ApiComponent {

    fun inject(service: TMDBService)

    fun inject(viewModel : ListViewModel)
}