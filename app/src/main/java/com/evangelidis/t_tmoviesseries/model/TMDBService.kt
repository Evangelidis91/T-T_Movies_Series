package com.evangelidis.t_tmoviesseries.model

import com.evangelidis.t_tmoviesseries.di.DaggerApiComponent
import com.evangelidis.t_tmoviesseries.utils.Constants.API_KEY
import com.evangelidis.t_tmoviesseries.utils.Constants.LANGUAGE
import io.reactivex.Single
import javax.inject.Inject

class TMDBService {

    @Inject
    lateinit var api: TMDBApi

    init {
        DaggerApiComponent.create().inject(this)
    }

    fun getGenres(): Single<GenresResponse> {
        return api.getGenres(API_KEY)
    }

    fun getPopularMovies(i: Int): Single<MoviesListResponse> {
        return api.getPopularMovies(API_KEY, LANGUAGE, i)
    }

}