package com.evangelidis.t_tmoviesseries.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.evangelidis.t_tmoviesseries.di.DaggerApiComponent
import com.evangelidis.t_tmoviesseries.model.GenresResponse
import com.evangelidis.t_tmoviesseries.model.MoviesListResponse
import com.evangelidis.t_tmoviesseries.model.TMDBService
import com.evangelidis.t_tmoviesseries.model.TvShowListResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class ListViewModel : ViewModel() {

    @Inject
    lateinit var tmdbService: TMDBService

    private val disposable = CompositeDisposable()

    val genresMovieData = MutableLiveData<GenresResponse>()
    val genresTvShowData = MutableLiveData<GenresResponse>()
    val moviesList = MutableLiveData<MoviesListResponse>()
    val tvShowsList = MutableLiveData<TvShowListResponse>()
    val loadError = MutableLiveData<Boolean>()
    val loading = MutableLiveData<Boolean>()

    init {
        DaggerApiComponent.create().inject(this)
    }

    fun getMoviesGenres() {
        fetchMovieGenres()
    }

    fun getPopularMovies(i: Int) {
        fetchPopularMovies(i)
    }

    fun getPlayingNowMovies(i: Int) {
        fetchPlayingNowMovies(i)
    }

    fun getTopRatedMovies(i: Int) {
        fetchTopRatedMovies(i)
    }

    fun getUpcomingMovies(i: Int) {
        fetchUpcomingMovies(i)
    }

    fun getPopularTvShows(i: Int) {
        fetchPopularTvShows(i)
    }

    fun getTopRatedTvShows(i: Int) {
        fetchTopRatedTvShows(i)
    }

    fun getOnAirTvShows(i: Int) {
        fetchOnAirTvShows(i)
    }

    fun getAiringTodayTvShows(i: Int) {
        fetchAiringTodayTvShows(i)
    }

    private fun fetchMovieGenres() {
        disposable.add(
            tmdbService.getMoviesGenres()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<GenresResponse>() {
                    override fun onSuccess(t: GenresResponse) {
                        genresMovieData.value = t
                        loadError.value = false
                        loading.value = false
                    }

                    override fun onError(e: Throwable) {}
                })
        )
    }

    private fun fetchTvShowGenres() {
        disposable.add(
            tmdbService.getTvGenres()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<GenresResponse>() {
                    override fun onSuccess(t: GenresResponse) {
                        genresTvShowData.value = t
                    }

                    override fun onError(e: Throwable) {

                    }

                })
        )
    }

    private fun fetchPopularMovies(i: Int) {
        disposable.add(
            tmdbService.getPopularMovies(i)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<MoviesListResponse>() {
                    override fun onSuccess(t: MoviesListResponse) {
                        moviesList.value = t
                        loadError.value = false
                        loading.value = false
                    }

                    override fun onError(e: Throwable) {
                        loadError.value = true
                    }
                })
        )
    }

    private fun fetchTopRatedMovies(i: Int) {
        disposable.add(
            tmdbService.getTopRatedMovies(i)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<MoviesListResponse>() {
                    override fun onSuccess(t: MoviesListResponse) {
                        moviesList.value = t
                        loadError.value = false
                        loading.value = false
                    }

                    override fun onError(e: Throwable) {
                        loadError.value = true
                    }
                })
        )
    }

    private fun fetchPlayingNowMovies(i: Int) {
        disposable.add(
            tmdbService.getNowPlayingMovies(i)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<MoviesListResponse>() {
                    override fun onSuccess(t: MoviesListResponse) {
                        moviesList.value = t
                        loadError.value = false
                        loading.value = false
                    }

                    override fun onError(e: Throwable) {
                        loadError.value = true
                    }
                })
        )
    }

    private fun fetchUpcomingMovies(i: Int) {
        disposable.add(
            tmdbService.getUpcomingMovies(i)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<MoviesListResponse>() {
                    override fun onSuccess(t: MoviesListResponse) {
                        moviesList.value = t
                        loadError.value = false
                        loading.value = false
                    }

                    override fun onError(e: Throwable) {
                        loadError.value = true
                    }
                })
        )
    }

    private fun fetchPopularTvShows(i: Int) {
        disposable.add(
            tmdbService.getPopularTvSeries(i)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<TvShowListResponse>() {
                    override fun onSuccess(t: TvShowListResponse) {
                        tvShowsList.value = t
                        loadError.value = false
                        loading.value = false
                    }

                    override fun onError(e: Throwable) {
                        loadError.value = true
                    }

                })
        )
    }

    private fun fetchTopRatedTvShows(i: Int) {
        disposable.add(
            tmdbService.getTopRatedTvSeries(i)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<TvShowListResponse>() {
                    override fun onSuccess(t: TvShowListResponse) {
                        tvShowsList.value = t
                        loadError.value = false
                        loading.value = false
                    }

                    override fun onError(e: Throwable) {
                        loadError.value = true
                    }

                })
        )
    }

    private fun fetchOnAirTvShows(i: Int) {
        disposable.add(
            tmdbService.getOnTheAirTvSeries(i)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<TvShowListResponse>() {
                    override fun onSuccess(t: TvShowListResponse) {
                        tvShowsList.value = t
                        loadError.value = false
                        loading.value = false
                    }

                    override fun onError(e: Throwable) {
                        loadError.value = true
                    }

                })
        )
    }

    private fun fetchAiringTodayTvShows(i: Int) {
        disposable.add(
            tmdbService.getAiringTodayTvSeries(i)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<TvShowListResponse>() {
                    override fun onSuccess(t: TvShowListResponse) {
                        tvShowsList.value = t
                        loadError.value = false
                        loading.value = false
                    }

                    override fun onError(e: Throwable) {
                        loadError.value = true
                    }

                })
        )
    }
}