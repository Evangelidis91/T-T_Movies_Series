package com.evangelidis.t_tmoviesseries.model

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface TMDBApi {

    @GET("genre/movie/list")
    fun getMoviesGenres(
        @Query("api_key") apiKey: String
    ): Single<GenresResponse>

    @GET("genre/tv/list")
    fun getTvGenres(
        @Query("api_key") apiKey: String
    ): Single<GenresResponse>

    @GET("movie/popular")
    fun getPopularMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String,
        @Query("page") page: Int
    ): Single<MoviesListResponse>

    @GET("movie/top_rated")
    fun getTopRatedMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String,
        @Query("page") page: Int
    ): Single<MoviesListResponse>

    @GET("movie/now_playing")
    fun getNowPlayingMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String,
        @Query("page") page: Int
    ): Single<MoviesListResponse>

    @GET("movie/upcoming")
    fun getUpcomingMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String,
        @Query("page") page: Int
    ): Single<MoviesListResponse>

    @GET("tv/popular")
    fun getPopularTvSeries(
        @Query("api_key") apiKey: String,
        @Query("language") language: String,
        @Query("page") page: Int
    ): Single<TvShowListResponse>

    @GET("tv/top_rated")
    fun getTopRatedTvSeries(
        @Query("api_key") apiKey: String,
        @Query("language") language: String,
        @Query("page") page: Int
    ): Single<TvShowListResponse>

    @GET("tv/airing_today")
    fun getAiringTodayTvSeries(
        @Query("api_key") apiKey: String,
        @Query("language") language: String,
        @Query("page") page: Int
    ): Single<TvShowListResponse>

    @GET("tv/on_the_air")
    fun getOnTheAirTvSeries(
        @Query("api_key") apiKey: String,
        @Query("language") language: String,
        @Query("page") page: Int
    ): Single<TvShowListResponse>
}