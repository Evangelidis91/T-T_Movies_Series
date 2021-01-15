package com.evangelidis.t_tmoviesseries.callbacks

import com.evangelidis.t_tmoviesseries.room.WatchlistData

interface OnWatchlistClickCallback {
    fun onClick(watchlist: WatchlistData)
}
