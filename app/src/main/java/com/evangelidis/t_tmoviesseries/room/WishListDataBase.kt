package com.evangelidis.t_tmoviesseries.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = arrayOf(WishListData::class), version = 1)
abstract class WishListDataBase : RoomDatabase() {

    abstract fun todoDao(): TodoDao

    companion object {
        private var INSTANCE: WishListDataBase? = null

        fun getInstance(context: Context): WishListDataBase? {
            if (INSTANCE == null){
                synchronized(WishListDataBase::class){
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                        WishListDataBase::class.java, "wishlist.db")
                        .build()
                }
            }
            return INSTANCE
        }

        fun destroyInstance(){
            INSTANCE = null
        }
    }
}