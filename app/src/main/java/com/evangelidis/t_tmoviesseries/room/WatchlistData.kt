package com.evangelidis.t_tmoviesseries.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.evangelidis.t_tmoviesseries.utils.Constants.ROOM_WATCHLIST_TABLE_NAME

@Entity(tableName = ROOM_WATCHLIST_TABLE_NAME)
data class WatchlistData(
    @PrimaryKey(autoGenerate = true) var id: Int?,
    @ColumnInfo(name = "itemId") var itemId: Int,
    @ColumnInfo(name = "category") var category: String,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "posterPath") var posterPath: String,
    @ColumnInfo(name = "releasedDate") var releasedDate: String,
    @ColumnInfo(name = "rate") var rate: Double
) {
    constructor() : this(null, 0, "", "", "", "", 0.0)
}