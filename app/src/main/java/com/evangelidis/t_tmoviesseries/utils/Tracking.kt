package com.evangelidis.t_tmoviesseries.utils

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

object Tracking {

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    fun trackListCategory(context: Context, category: String) {
        firebaseAnalytics = FirebaseAnalytics.getInstance(context)
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, category)
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.VIEW_ITEM_LIST, bundle)
    }
}
