package com.evangelidis.t_tmoviesseries.view.search

interface SearchCallback {
    fun navigateToMovie(itemId: Int)
    fun navigateToTvShow(itemId: Int)
    fun navigateToPerson(itemId: Int)
}
