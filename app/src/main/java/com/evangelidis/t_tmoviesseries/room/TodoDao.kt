package com.evangelidis.t_tmoviesseries.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TodoDao {
    @Query("SELECT * FROM wishlistData")
    fun getAll(): MutableList<WishListData>

    @Insert
    fun insert(wishListData: WishListData)

    @Query("DELETE FROM wishlistData WHERE itemId = :itemId")
    fun deleteByUserId(itemId: Int)

    //@Query("DELETE from wishlistData")
    //fun deleteAll()
}