package com.evangelidis.t_tmoviesseries.view.tvshow

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.evangelidis.t_tmoviesseries.di.DaggerApiComponent
import com.evangelidis.t_tmoviesseries.model.TvShowCreditsResponse
import com.evangelidis.t_tmoviesseries.model.TvShowDetailsResponse
import com.evangelidis.t_tmoviesseries.model.TvShowListResponse
import com.evangelidis.t_tmoviesseries.model.VideosResponse
import com.evangelidis.t_tmoviesseries.model.api.TMDBService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class ViewModelTvShow : ViewModel() {

    @Inject
    lateinit var tmdbService: TMDBService

    private val disposable = CompositeDisposable()

    val tvShowDetails = MutableLiveData<TvShowDetailsResponse>()
    val tvShowCredits = MutableLiveData<TvShowCreditsResponse>()
    val tvShowVideos = MutableLiveData<VideosResponse>()
    val tvShowSimilar = MutableLiveData<TvShowListResponse>()
    val tvShowRecommendation = MutableLiveData<TvShowListResponse>()
    val loadError = MutableLiveData<Boolean>()

    init {
        DaggerApiComponent.create().inject(this)
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

}