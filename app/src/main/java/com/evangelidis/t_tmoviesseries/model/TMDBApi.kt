package com.evangelidis.t_tmoviesseries.model

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface TMDBApi {

    @GET("genre/movie/list")
    fun getGenres(
        @Query("api_key") apiKey: String
    ): Single<GenresResponse>

    @GET("movie/popular")
    fun getPopularMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String,
        @Query("page") page: Int
    ): Single<MoviesListResponse>
}