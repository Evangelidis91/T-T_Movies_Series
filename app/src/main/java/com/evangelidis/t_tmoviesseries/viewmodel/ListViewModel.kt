package com.evangelidis.t_tmoviesseries.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.evangelidis.t_tmoviesseries.di.DaggerApiComponent
import com.evangelidis.t_tmoviesseries.model.*
import com.evangelidis.t_tmoviesseries.model.api.TMDBService
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
    val movieDetails = MutableLiveData<MovieDetailsResponse>()
    val movieCredits = MutableLiveData<MovieCredits>()
    val movieVideos = MutableLiveData<VideosResponse>()
    val movieSimilar = MutableLiveData<MoviesListResponse>()
    val movieRecommendation = MutableLiveData<MoviesListResponse>()
    val tvShowDetails = MutableLiveData<TvShowDetailsResponse>()
    val tvShowCredits = MutableLiveData<TvShowCreditsResponse>()
    val tvShowVideos = MutableLiveData<VideosResponse>()
    val tvShowSimilar = MutableLiveData<TvShowListResponse>()
    val tvShowRecommendation = MutableLiveData<TvShowListResponse>()
    val tvShowSeasonDetails = MutableLiveData<TvShowSeasonResponse>()
    val loadError = MutableLiveData<Boolean>()
    val loading = MutableLiveData<Boolean>()

    init {
        DaggerApiComponent.create().inject(this)
    }

    fun getMoviesGenres() {
        fetchMovieGenres()
    }

    fun getTvShowGenres() {
        fetchTvShowGenres()
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

    fun getMovieDetails(id: Int) {
        fetchMovieDetails(id)
    }

    fun getMovieCredits(id: Int) {
        fetchMovieCredits(id)
    }

    fun getMovieVideos(id: Int) {
        fetchMovieVideos(id)
    }

    fun getMovieSimilar(id: Int) {
        fetchMovieSimilar(id)
    }

    fun getMovieRecommendation(id: Int) {
        fetchMovieRecommendation(id)
    }

    fun getTvShowDetails(id: Int) {
        fetchTvShowDetails(id)
    }

    fun getTvShowCredits(id: Int) {
        fetchTvShowCredits(id)
    }

    fun getTvShowVideos(id: Int) {
        fetchTvShowVideos(id)
    }

    fun getTvShowSimilar(id: Int) {
        fetchTvShowSimilar(id)
    }

    fun getTvShowRecommendation(id: Int) {
        fetchTvShowRecommendation(id)
    }

    fun getTvShowSeasonDetails(id: Int, season: Int) {
        fetchTvShowSeasonDetails(id, season)
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

    private fun fetchMovieDetails(id: Int) {
        disposable.add(
            tmdbService.getMovieDetails(id)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<MovieDetailsResponse>() {
                    override fun onSuccess(t: MovieDetailsResponse) {
                        movieDetails.value = t
                    }

                    override fun onError(e: Throwable) {
                        loadError.value = true
                    }

                })
        )
    }

    private fun fetchMovieCredits(id: Int) {
        disposable.add(
            tmdbService.getMovieCredits(id)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<MovieCredits>() {
                    override fun onSuccess(t: MovieCredits) {
                        movieCredits.value = t
                    }

                    override fun onError(e: Throwable) {
                        loadError.value = true
                    }

                })
        )
    }

    private fun fetchMovieVideos(id: Int) {
        disposable.add(
            tmdbService.getMovieVideos(id)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<VideosResponse>() {
                    override fun onSuccess(t: VideosResponse) {
                        movieVideos.value = t
                    }

                    override fun onError(e: Throwable) {
                        loadError.value = true
                    }

                })
        )
    }

    private fun fetchMovieSimilar(id: Int) {
        disposable.add(
            tmdbService.getMovieSimilar(id)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<MoviesListResponse>() {
                    override fun onSuccess(t: MoviesListResponse) {
                        movieSimilar.value = t
                    }

                    override fun onError(e: Throwable) {
                        loadError.value = true
                    }

                })
        )
    }

    private fun fetchMovieRecommendation(id: Int) {
        disposable.add(
            tmdbService.getMovieRecommendations(id)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<MoviesListResponse>() {
                    override fun onSuccess(t: MoviesListResponse) {
                        movieRecommendation.value = t
                    }

                    override fun onError(e: Throwable) {
                        loadError.value = true
                    }

                })
        )
    }

    private fun fetchTvShowDetails(id: Int) {
        disposable.add(
            tmdbService.getTvShowDetails(id)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<TvShowDetailsResponse>() {
                    override fun onSuccess(t: TvShowDetailsResponse) {
                        tvShowDetails.value = t
                    }

                    override fun onError(e: Throwable) {
                        loadError.value = true
                    }

                })
        )
    }

    private fun fetchTvShowCredits(id: Int) {
        disposable.add(
            tmdbService.getTvShowCredits(id)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<TvShowCreditsResponse>() {
                    override fun onSuccess(t: TvShowCreditsResponse) {
                        tvShowCredits.value = t
                    }

                    override fun onError(e: Throwable) {
                        loadError.value = true
                    }

                })
        )
    }

    private fun fetchTvShowVideos(id: Int) {
        disposable.add(
            tmdbService.getTvShowVideos(id)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<VideosResponse>() {
                    override fun onSuccess(t: VideosResponse) {
                        tvShowVideos.value = t
                    }

                    override fun onError(e: Throwable) {
                        loadError.value = true
                    }

                })
        )
    }

    private fun fetchTvShowSimilar(id: Int) {
        disposable.add(
            tmdbService.getTvShowSimilar(id)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<TvShowListResponse>() {
                    override fun onSuccess(t: TvShowListResponse) {
                        tvShowSimilar.value = t
                    }

                    override fun onError(e: Throwable) {
                        loadError.value = true
                    }

                })
        )
    }

    private fun fetchTvShowRecommendation(id: Int) {
        disposable.add(
            tmdbService.getTvShowRecommendations(id)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<TvShowListResponse>() {
                    override fun onSuccess(t: TvShowListResponse) {
                        tvShowRecommendation.value = t
                    }

                    override fun onError(e: Throwable) {
                        loadError.value = true
                    }

                })
        )
    }

    private fun fetchTvShowSeasonDetails(id: Int, season: Int) {
        disposable.add(
            tmdbService.getTvShowSeasonDetails(id, season)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<TvShowSeasonResponse>() {
                    override fun onSuccess(t: TvShowSeasonResponse) {
                        tvShowSeasonDetails.value = t
                    }

                    override fun onError(e: Throwable) {
                        loadError.value = true
                    }

                })
        )
    }
}