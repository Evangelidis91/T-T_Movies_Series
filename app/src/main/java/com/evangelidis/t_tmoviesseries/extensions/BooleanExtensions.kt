package com.evangelidis.t_tmoviesseries.extensions

infix fun <T> Boolean.then(param: T): T? = if (this) param else null

fun Boolean?.orFalse(): Boolean = this ?: false

fun Boolean?.orTrue(): Boolean = this ?: true