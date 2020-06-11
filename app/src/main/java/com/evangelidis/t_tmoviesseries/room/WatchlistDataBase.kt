package com.evangelidis.t_tmoviesseries.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.evangelidis.t_tmoviesseries.utils.Constants.ROOM_DATABASE_NAME

@Database(entities = [WatchlistData::class], version = 2)
abstract class WatchlistDataBase : RoomDatabase() {

    abstract fun todoDao(): TodoDao

    companion object {
        private var INSTANCE: WatchlistDataBase? = null

        fun getInstance(context: Context): WatchlistDataBase? {
            if (INSTANCE == null){
                synchronized(WatchlistDataBase::class){
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                        WatchlistDataBase::class.java, ROOM_DATABASE_NAME)
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