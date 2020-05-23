package com.evangelidis.t_tmoviesseries.callbacks

import com.evangelidis.t_tmoviesseries.room.WishListData

interface OnWishlistClickCallback {

    fun onClick(wishList: WishListData)
}