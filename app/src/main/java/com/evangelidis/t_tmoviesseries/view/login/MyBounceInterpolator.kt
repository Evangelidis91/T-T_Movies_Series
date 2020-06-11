package com.evangelidis.t_tmoviesseries.view.login

import android.view.animation.Interpolator
import kotlin.math.cos
import kotlin.math.pow

internal class MyBounceInterpolator(amp: Double, freq: Double) : Interpolator {
    private var amplitude = 1.0
    private var frequency = 10.0
    override fun getInterpolation(time: Float) = ((-1 * Math.E.pow(-time / amplitude) * cos(frequency * time) + 1).toFloat())

    init {
        amplitude = amp
        frequency = freq
    }
}