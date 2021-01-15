package com.evangelidis.t_tmoviesseries.callbacks

import com.evangelidis.t_tmoviesseries.model.Movie

interface OnMoviesClickCallback {
    fun onClick(movie: Movie)
}
