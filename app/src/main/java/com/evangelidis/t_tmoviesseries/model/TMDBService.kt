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

    fun getMoviesGenres(): Single<GenresResponse> {
        return api.getMoviesGenres(API_KEY)
    }

    fun getTvGenres(): Single<GenresResponse> {
        return api.getMoviesGenres(API_KEY)
    }

    fun getPopularMovies(i: Int): Single<MoviesListResponse> {
        return api.getPopularMovies(API_KEY, LANGUAGE, i)
    }

    fun getNowPlayingMovies(i: Int): Single<MoviesListResponse> {
        return api.getNowPlayingMovies(API_KEY, LANGUAGE, i)
    }

    fun getTopRatedMovies(i: Int): Single<MoviesListResponse> {
        return api.getTopRatedMovies(API_KEY, LANGUAGE, i)
    }

    fun getUpcomingMovies(i: Int): Single<MoviesListResponse> {
        return api.getUpcomingMovies(API_KEY, LANGUAGE, i)
    }

    fun getPopularTvSeries(i: Int): Single<TvShowListResponse> {
        return api.getPopularTvSeries(API_KEY, LANGUAGE, i)
    }

    fun getTopRatedTvSeries(i: Int): Single<TvShowListResponse> {
        return api.getTopRatedTvSeries(API_KEY, LANGUAGE, i)
    }

    fun getOnTheAirTvSeries(i: Int): Single<TvShowListResponse> {
        return api.getOnTheAirTvSeries(API_KEY, LANGUAGE, i)
    }

    fun getAiringTodayTvSeries(i: Int): Single<TvShowListResponse> {
        return api.getAiringTodayTvSeries(API_KEY, LANGUAGE, i)
    }

    fun getMovieDetails(id: Int): Single<MovieDetailsResponse> {
        return api.getMovieDetails(id, API_KEY, LANGUAGE)
    }

    fun getMovieCredits(id: Int): Single<MovieCredits> {
        return api.getMovieCredits(id, API_KEY, LANGUAGE)
    }

    fun getMovieVideos(id: Int): Single<MovieVideos> {
        return api.getMovieVideos(id, API_KEY, LANGUAGE)
    }

    fun getMovieSimilar(id: Int): Single<MoviesListResponse> {
        return api.getMovieSimilar(id, API_KEY, LANGUAGE)
    }

    fun getMovieRecommendations(id: Int): Single<MoviesListResponse> {
        return api.getMovieRecommendations(id, API_KEY, LANGUAGE)
    }

}