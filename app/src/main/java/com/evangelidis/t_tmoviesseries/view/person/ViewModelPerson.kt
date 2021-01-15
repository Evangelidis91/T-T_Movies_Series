package com.evangelidis.t_tmoviesseries.view.person

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.evangelidis.t_tmoviesseries.di.DaggerApiComponent
import com.evangelidis.t_tmoviesseries.model.PersonCombinedResponse
import com.evangelidis.t_tmoviesseries.model.PersonDetailsResponse
import com.evangelidis.t_tmoviesseries.model.api.TMDBService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class ViewModelPerson : ViewModel() {

    @Inject
    lateinit var tmdbService: TMDBService

    private val disposable = CompositeDisposable()

    val personDetails = MutableLiveData<PersonDetailsResponse>()
    val personCombinedCredits = MutableLiveData<PersonCombinedResponse>()
    val loadError = MutableLiveData<Boolean>()

    init {
        DaggerApiComponent.create().inject(this)
    }

    fun getPersonDetails(id: Int) {
        fetchPersonDetails(id)
    }

    fun getPersonCombinedCredits(id: Int) {
        fetchPersonCombinedCredits(id)
    }

    private fun fetchPersonDetails(id: Int) {
        disposable.add(
            tmdbService.getPersonInfo(id)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<PersonDetailsResponse>() {
                    override fun onSuccess(t: PersonDetailsResponse) {
                        personDetails.value = t
                    }

                    override fun onError(e: Throwable) {
                        loadError.value = true
                    }
                })
        )
    }

    private fun fetchPersonCombinedCredits(id: Int) {
        disposable.add(
            tmdbService.getPersonCombinedCredits(id)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<PersonCombinedResponse>() {
                    override fun onSuccess(t: PersonCombinedResponse) {
                        personCombinedCredits.value = t
                    }

                    override fun onError(e: Throwable) {
                        loadError.value = true
                    }
                })
        )
    }
}
