package com.evangelidis.t_tmoviesseries.view.search

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.evangelidis.t_tmoviesseries.di.DaggerApiComponent
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

    val trendings = MutableLiveData<MultisearchResponse>()
    val multisearch = MutableLiveData<MultisearchResponse>()
    val loadError = MutableLiveData<Boolean>()

    fun getTrendings(pageNumber: Int) {
        fetchTrendings(pageNumber)
    }

    fun getMultisearchResult(query: String, pageNumber: Int) {
        fetchMultisearch(query, pageNumber)
    }

    private fun fetchTrendings(pageNumber: Int) {
        disposable.add(
            tmdbService.getTrendings(pageNumber)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<MultisearchResponse>() {
                    override fun onSuccess(t: MultisearchResponse) {
                        trendings.value = t
                    }

                    override fun onError(e: Throwable) {
                        loadError.value = true
                    }
                })
        )
    }

    private fun fetchMultisearch(query: String, pageNumber: Int) {
        disposable.add(
            tmdbService.getMultiSearchResult(query, pageNumber)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<MultisearchResponse>() {
                    override fun onSuccess(t: MultisearchResponse) {
                        multisearch.value = t
                    }

                    override fun onError(e: Throwable) {
                        loadError.value = true
                    }
                })
        )
    }
}
