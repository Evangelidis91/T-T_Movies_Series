package com.evangelidis.t_tmoviesseries.room

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

typealias DatabaseResponse<T> = (T) -> Unit

class DatabaseQueries {

    companion object {

        fun getSavedItems(context: Context, response: DatabaseResponse<MutableList<WatchlistData>?>) {
            CoroutineScope(Dispatchers.Main).launch {
                response(getSavedItems(context))
            }
        }

        private suspend fun getSavedItems(context: Context) = withContext(Dispatchers.IO) {
            WatchlistDataBase.getInstance(context)?.itemDao()?.getAll()
        }

        fun saveItem(context: Context, watchlistItem: WatchlistData, response: () -> Unit = {}) {
            CoroutineScope(Dispatchers.Main).launch {
                saveItem(context, watchlistItem)
                response()
            }
        }

        private suspend fun saveItem(context: Context, watchlistItem: WatchlistData) = withContext(Dispatchers.IO) {
            WatchlistDataBase.getInstance(context)?.itemDao()?.insert(watchlistItem)
        }

        fun removeItem(context: Context, itemId: Int, response: () -> Unit = {}) {
            CoroutineScope(Dispatchers.Main).launch {
                removeItem(context, itemId)
                response()
            }
        }

        private suspend fun removeItem(context: Context, itemId: Int) = withContext(Dispatchers.IO) {
            WatchlistDataBase.getInstance(context)?.itemDao()?.deleteByUserId(itemId)
        }
    }
}
