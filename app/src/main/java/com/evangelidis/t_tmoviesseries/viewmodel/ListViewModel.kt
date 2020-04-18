package com.evangelidis.t_tmoviesseries.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.evangelidis.t_tmoviesseries.di.DaggerApiComponent
import com.evangelidis.t_tmoviesseries.model.GenresResponse
import com.evangelidis.t_tmoviesseries.model.MoviesListResponse
import com.evangelidis.t_tmoviesseries.model.TMDBService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class ListViewModel : ViewModel() {

    @Inject
    lateinit var tmdbService: TMDBService

    private val disposable = CompositeDisposable()

    val genresData = MutableLiveData<GenresResponse>()
    val moviesList = MutableLiveData<MoviesListResponse>()
    val loadError = MutableLiveData<Boolean>()
    val loading = MutableLiveData<Boolean>()

    init {
        DaggerApiComponent.create().inject(this)
    }

    fun getGenres() {
        fetchGenres()
    }

    fun getMovies(i: Int) {
        fetchMovies(i)
    }

    private fun fetchGenres() {
        disposable.add(
            tmdbService.getGenres()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<GenresResponse>() {
                    override fun onSuccess(t: GenresResponse) {
                        genresData.value = t
                        loadError.value = false
                        loading.value = false
                    }

                    override fun onError(e: Throwable) { }
                })
        )
    }

    fun fetchMovies(i: Int) {
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
}