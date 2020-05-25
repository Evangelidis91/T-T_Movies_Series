package com.evangelidis.t_tmoviesseries.login

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.annotation.RequiresApi

class FlexibleFrameLayout : FrameLayout {
    private var currentOrder = 0

    constructor(context: Context) : super(context) {
        isChildrenDrawingOrderEnabled = true
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        isChildrenDrawingOrderEnabled = true
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        isChildrenDrawingOrderEnabled = true
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        isChildrenDrawingOrderEnabled = true
    }

    fun setDrawOrder(order: Int) {
        currentOrder = order
        invalidate()
    }

    override fun getChildDrawingOrder(childCount: Int, i: Int): Int {
        return DRAW_ORDERS[currentOrder][i]
    }

    companion object {

        @JvmField
        var ORDER_SIGN_UP_STATE = 0

        @JvmField
        var ORDER_LOGIN_STATE = 1
        private val DRAW_ORDERS = arrayOf(intArrayOf(0, 1, 2), intArrayOf(2, 1, 0))
    }
}