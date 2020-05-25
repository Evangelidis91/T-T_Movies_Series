package com.evangelidis.t_tmoviesseries

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.evangelidis.t_tmoviesseries.model.Genre

object ItemsManager {

     fun getGenres(genreIds: List<Int>, genresList: ArrayList<Genre>): String {
        val movieGenres: ArrayList<String> = arrayListOf()
        for (genreId in genreIds) {
            for ((id, name) in genresList) {
                if (id == genreId) {
                    name?.let {
                        movieGenres.add(it)
                    }
                    break
                }
            }
        }
        return movieGenres.joinToString(separator = ", ", limit = 3)
    }

    fun showTrailer(url: String,context: Context) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }
}