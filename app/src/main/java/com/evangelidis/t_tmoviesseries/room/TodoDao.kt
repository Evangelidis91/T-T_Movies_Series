package com.evangelidis.t_tmoviesseries.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TodoDao {
    @Query("SELECT * FROM watchlistData")
    fun getAll(): MutableList<WatchlistData>

    @Insert
    fun insert(watchlistData: WatchlistData)

    @Query("DELETE FROM watchlistData WHERE itemId = :itemId")
    fun deleteByUserId(itemId: Int)

    //@Query("DELETE from watchlistData")
    //fun deleteAll()
}