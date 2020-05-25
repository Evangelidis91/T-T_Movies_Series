package com.evangelidis.t_tmoviesseries.room

object DatabaseManager {

    fun insertDataToDatabase(watchlist: WatchlistData, mDb: WatchlistDataBase?, mDbWorkerThread: DbWorkerThread) {
        val task = Runnable { mDb?.todoDao()?.insert(watchlist) }
        mDbWorkerThread.postTask(task)
    }

    fun removeDataFromDatabase(watchlist: WatchlistData, mDb: WatchlistDataBase?, mDbWorkerThread: DbWorkerThread) {
        val task = Runnable {
            mDb?.todoDao()?.deleteByUserId(watchlist.itemId)
        }
        mDbWorkerThread.postTask(task)
    }
}
