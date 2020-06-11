package com.evangelidis.t_tmoviesseries.view.seasons

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.evangelidis.t_tmoviesseries.di.DaggerApiComponent
import com.evangelidis.t_tmoviesseries.model.TvShowSeasonResponse
import com.evangelidis.t_tmoviesseries.model.api.TMDBService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class ViewModelSeasons : ViewModel() {

    @Inject
    lateinit var tmdbService: TMDBService

    private val disposable = CompositeDisposable()

    val tvShowSeasonDetails = MutableLiveData<TvShowSeasonResponse>()

    val loadError = MutableLiveData<Boolean>()

    init {
        DaggerApiComponent.create().inject(this)
    }

    fun getTvShowSeasonDetails(id: Int, season: Int) {
        fetchTvShowSeasonDetails(id, season)
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