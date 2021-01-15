package com.evangelidis.t_tmoviesseries.callbacks

import com.evangelidis.t_tmoviesseries.model.TvShow

interface OnTvShowClickCallback {
    fun onClick(tvShow: TvShow)
}
