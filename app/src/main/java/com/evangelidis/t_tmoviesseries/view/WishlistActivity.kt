package com.evangelidis.t_tmoviesseries.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.evangelidis.t_tmoviesseries.R
import com.evangelidis.t_tmoviesseries.view.adapters.WishlistAdapter
import com.evangelidis.t_tmoviesseries.callbacks.OnWishlistClickCallback
import com.evangelidis.t_tmoviesseries.room.DbWorkerThread
import com.evangelidis.t_tmoviesseries.room.WishListData
import com.evangelidis.t_tmoviesseries.room.WishListDataBase
import com.evangelidis.t_tmoviesseries.utils.Constants
import com.evangelidis.t_tmoviesseries.utils.InternetStatus
import com.evangelidis.tantintoast.TanTinToast
import kotlinx.android.synthetic.main.activity_wishlist.*

class WishlistActivity : AppCompatActivity() {

    var wishlistCallback: OnWishlistClickCallback = object :
        OnWishlistClickCallback {
        override fun onClick(wishlist: WishListData) {
            if (InternetStatus.getInstance(applicationContext).isOnline) {
                when (wishlist.category) {
                    "TV" -> {
                        val intent = Intent(this@WishlistActivity, TvShowActivity::class.java)
                        intent.putExtra(Constants.TV_SHOW_ID, wishlist.itemId)
                        startActivity(intent)
                    }
                    "Movie" -> {
                        val intent = Intent(this@WishlistActivity, MovieActivity::class.java)
                        intent.putExtra(Constants.MOVIE_ID, wishlist.itemId)
                        startActivity(intent)
                    }
                }
            } else {
                TanTinToast.Warning(this@WishlistActivity).text(getString(R.string.no_internet))
                    .time(Toast.LENGTH_LONG).show()
            }
        }
    }

    private val wishlistAdapter =
        WishlistAdapter(
            arrayListOf(),
            wishlistCallback
        )
    private var mDb: WishListDataBase? = null
    private lateinit var mDbWorkerThread: DbWorkerThread
    private val mUiHandler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wishlist)

        mDbWorkerThread = DbWorkerThread("dbWorkerThread")
        mDbWorkerThread.start()
        mDb = WishListDataBase.getInstance(this)

        getDataFromDB()
    }

    override fun onResume() {
        super.onResume()
        loading_view.visibility = View.VISIBLE
        getDataFromDB()
    }

    private fun getDataFromDB() {
        Handler().postDelayed(
            {
                val task = Runnable {
                    val wishlistData = mDb?.todoDao()?.getAll()
                    mUiHandler.post {
                        if (!wishlistData.isNullOrEmpty()) {
                            wishlist_list.apply {
                                layoutManager = LinearLayoutManager(context)
                                adapter = wishlistAdapter
                            }
                            wishlistAdapter.appendWishlistData(wishlistData)
                            loading_view.visibility = View.GONE
                        }
                    }
                }
                mDbWorkerThread.postTask(task)
            },
            400
        )
    }
}