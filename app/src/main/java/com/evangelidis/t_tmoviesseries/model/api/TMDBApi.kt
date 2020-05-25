package com.evangelidis.t_tmoviesseries.model.api

import com.evangelidis.t_tmoviesseries.model.*
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
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

    @GET("movie/{movie_id}")
    fun getMovieDetails(
        @Path("movie_id") id: Int,
        @Query("api_key") apiKEy: String,
        @Query("language") language: String
    ): Single<MovieDetailsResponse>

    @GET("movie/{movie_id}/credits")
    fun getMovieCredits(
        @Path("movie_id") id: Int,
        @Query("api_key") apiKEy: String,
        @Query("language") language: String
    ): Single<MovieCredits>

    @GET("movie/{movie_id}/videos")
    fun getMovieVideos(
        @Path("movie_id") id: Int,
        @Query("api_key") apiKEy: String,
        @Query("language") language: String
    ): Single<VideosResponse>

    @GET("movie/{movie_id}/similar")
    fun getMovieSimilar(
        @Path("movie_id") id: Int,
        @Query("api_key") apiKEy: String,
        @Query("language") language: String
    ): Single<MoviesListResponse>

    @GET("movie/{movie_id}/recommendations")
    fun getMovieRecommendations(
        @Path("movie_id") id: Int,
        @Query("api_key") apiKEy: String,
        @Query("language") language: String
    ): Single<MoviesListResponse>

    @GET("tv/{tv_show_id}")
    fun getTvShowDetails(
        @Path("tv_show_id") id: Int,
        @Query("api_key") apiKEy: String,
        @Query("language") language: String
    ): Single<TvShowDetailsResponse>

    @GET("tv/{tv_show_id}/credits")
    fun getTvShowCredits(
        @Path("tv_show_id") id: Int,
        @Query("api_key") apiKEy: String,
        @Query("language") language: String
    ): Single<TvShowCreditsResponse>

    @GET("tv/{tv_show_id}/videos")
    fun getTvShowTrailers(
        @Path("tv_show_id") id: Int,
        @Query("api_key") apiKEy: String,
        @Query("language") language: String
    ): Single<VideosResponse>

    @GET("tv/{tv_show_id}/similar")
    fun getTvShowSimilar(
        @Path("tv_show_id") id: Int,
        @Query("api_key") apiKEy: String,
        @Query("language") language: String
    ): Single<TvShowListResponse>

    @GET("tv/{tv_show_id}/recommendations")
    fun getTvShowRecommendations(
        @Path("tv_show_id") id: Int,
        @Query("api_key") apiKEy: String,
        @Query("language") language: String
    ): Single<TvShowListResponse>

    @GET("tv/{tv_show_id}/season/{season_number}")
    fun getTvShowSeasonDetails(
        @Path("tv_show_id") id: Int,
        @Path("season_number") seasonNumber: Int,
        @Query("api_key") apiKEy: String,
        @Query("language") language: String
    ): Single<TvShowSeasonResponse>

    @GET("person/{person_id}")
    fun getPersonInfo(
        @Path("person_id") id: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String
    ): Single<PersonDetailsResponse>

    @GET("person/{person_id}/combined_credits")
    fun getPersonCombinedCredits(
        @Path("person_id") id: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String
    ): Single<PersonCombinedResponse>

    @GET("trending/all/day")
    fun getTrendings(
        @Query("api_key") apiKEy: String,
        @Query("page") page: Int,
        @Query("language") language: String
    ): Single<MultisearchResponse>

    @GET("search/multi")
    fun getMultiSearchResult(
        @Query("api_key") apiKey: String,
        @Query("query") query: String,
        @Query("page") page: Int,
        @Query("language") language: String
    ): Single<MultisearchResponse>
}