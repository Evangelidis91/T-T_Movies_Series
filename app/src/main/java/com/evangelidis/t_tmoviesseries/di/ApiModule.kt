package com.evangelidis.t_tmoviesseries.di

import com.evangelidis.t_tmoviesseries.model.TMDBApi
import com.evangelidis.t_tmoviesseries.model.TMDBService
import com.evangelidis.t_tmoviesseries.utils.Constants.BASE_URL
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

@Module
class ApiModule {

    @Provides
    fun provideTMDBDataApi(): TMDBApi {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create(TMDBApi::class.java)
    }

    @Provides
    fun provideTMDBService(): TMDBService {
        return TMDBService()
    }
}