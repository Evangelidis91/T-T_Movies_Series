package com.evangelidis.t_tmoviesseries.view.watchlist

interface WatchListItemCallback {
    fun navigateToMovie(itemId: Int)
    fun navigateToTvShow(itemId: Int)
    fun displayEmptyList()
}
