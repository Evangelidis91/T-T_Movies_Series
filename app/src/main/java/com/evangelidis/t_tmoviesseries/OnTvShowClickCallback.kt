package com.evangelidis.t_tmoviesseries

import com.evangelidis.t_tmoviesseries.model.TvShow

interface OnTvShowClickCallback {

    fun onClick(tvShow: TvShow)

}