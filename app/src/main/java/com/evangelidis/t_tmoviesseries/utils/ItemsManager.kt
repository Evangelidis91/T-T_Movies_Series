package com.evangelidis.t_tmoviesseries.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.evangelidis.t_tmoviesseries.R
import com.evangelidis.t_tmoviesseries.model.Genre
import com.evangelidis.t_tmoviesseries.utils.Constants.INPUT_DATE_FORMAT
import com.evangelidis.t_tmoviesseries.utils.Constants.OUTPUT_DATE_FORMAT
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

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

    fun showTrailer(url: String, context: Context) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }

    fun getGlideImage(context: Context, url: String, target: ImageView) {
        Glide.with(context)
            .load(url)
            .apply(RequestOptions.placeholderOf(R.color.colorPrimary))
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(target)
    }

    fun String.underline(): SpannableString = SpannableString(this).also { it.setSpan(UnderlineSpan(), 0, this.length, 0) }

    fun changeDateFormat(date: String): String {
        val parser = SimpleDateFormat(INPUT_DATE_FORMAT, Locale.UK)
        val formatter = SimpleDateFormat(OUTPUT_DATE_FORMAT, Locale.UK)
        return formatter.format(parser.parse(date))
    }
}
