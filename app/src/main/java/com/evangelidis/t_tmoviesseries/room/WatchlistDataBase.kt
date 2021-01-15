package com.evangelidis.t_tmoviesseries.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.evangelidis.t_tmoviesseries.utils.Constants.ROOM_DATABASE_NAME

@Database(entities = [WatchlistData::class], version = 2)
abstract class WatchlistDataBase : RoomDatabase() {

    abstract fun itemDao(): ItemDao

    companion object {
        private var instance: WatchlistDataBase? = null

        fun getInstance(context: Context): WatchlistDataBase? {
            if (instance == null) {
                synchronized(WatchlistDataBase::class) {
                    instance = Room.databaseBuilder(context, WatchlistDataBase::class.java, ROOM_DATABASE_NAME).build()
                }
            }
            return instance
        }
    }
}
