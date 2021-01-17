package com.evangelidis.t_tmoviesseries.view.search

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.evangelidis.t_tmoviesseries.di.DaggerApiComponent
import com.evangelidis.t_tmoviesseries.model.GenresResponse
import com.evangelidis.t_tmoviesseries.model.MultisearchResponse
import com.evangelidis.t_tmoviesseries.model.api.TMDBService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class ViewModelSearch : ViewModel() {

    @Inject
    lateinit var tmdbService: TMDBService

    private val disposable = CompositeDisposable()

    init {
        DaggerApiComponent.create().inject(this)
    }

    val trends = MutableLiveData<MultisearchResponse>()
    val multiSearch = MutableLiveData<MultisearchResponse>()
    val genresMovieData = MutableLiveData<GenresResponse>()
    val genresTvShowData = MutableLiveData<GenresResponse> ()
    val loadError = MutableLiveData<Boolean>()

    fun getTrends(pageNumber: Int) {
        fetchTrends(pageNumber)
    }

    fun getMultiSearchResult(query: String, pageNumber: Int) {
        fetchMultiSearch(query, pageNumber)
    }

    fun getMoviesGenres() {
        fetchMovieGenres()
    }

    fun getTvShowGenres() {
        fetchTvShowGenres()
    }

    private fun fetchTrends(pageNumber: Int) {
        disposable.add(
            tmdbService.getTrendings(pageNumber)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<MultisearchResponse>() {
                    override fun onSuccess(t: MultisearchResponse) {
                        trends.value = t
                    }

                    override fun onError(e: Throwable) {
                        loadError.value = true
                    }
                })
        )
    }

    private fun fetchMultiSearch(query: String, pageNumber: Int) {
        disposable.add(
            tmdbService.getMultiSearchResult(query, pageNumber)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<MultisearchResponse>() {
                    override fun onSuccess(t: MultisearchResponse) {
                        multiSearch.value = t
                    }

                    override fun onError(e: Throwable) {
                        loadError.value = true
                    }
                })
        )
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
                        loadError.value = false
                    }

                    override fun onError(e: Throwable) {}
                })
        )
    }
}
