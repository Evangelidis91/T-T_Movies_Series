package com.evangelidis.t_tmoviesseries.view.movie

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

class ViewModelMovie : ViewModel() {

    @Inject
    lateinit var tmdbService: TMDBService

    private val disposable = CompositeDisposable()

    val genresMovieData = MutableLiveData<GenresResponse>()
    val movieDetails = MutableLiveData<MovieDetailsResponse>()
    val movieCredits = MutableLiveData<MovieCredits>()
    val movieVideos = MutableLiveData<VideosResponse>()
    val movieSimilar = MutableLiveData<MoviesListResponse>()
    val movieRecommendation = MutableLiveData<MoviesListResponse>()
    val loadError = MutableLiveData<Boolean>()
    val loading = MutableLiveData<Boolean>()

    init {
        DaggerApiComponent.create().inject(this)
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
}
