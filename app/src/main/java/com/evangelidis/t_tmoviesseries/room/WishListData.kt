package com.evangelidis.t_tmoviesseries.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wishlistData")
data class WishListData(
    @PrimaryKey(autoGenerate = true) var id: Int?,
    @ColumnInfo(name = "itemId") var itemId: Int,
    @ColumnInfo(name = "category") var category: String
) {
    constructor() : this(null, 0, "")
}