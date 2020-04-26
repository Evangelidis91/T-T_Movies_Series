package com.evangelidis.t_tmoviesseries

import com.evangelidis.t_tmoviesseries.model.Movie

interface OnMoviesClickCallback {
    fun onClick(movie: Movie)
}
