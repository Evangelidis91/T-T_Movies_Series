package com.evangelidis.t_tmoviesseries.extensions

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment

fun View.hideKeyboard(): Boolean {
    return try {
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
    } catch (ignored: RuntimeException) {
        false
    }
}

fun View.showKeyboard() {
    this.requestFocus()
    (context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}

fun Activity.showKeyboard() {
    if (!isFinishing) {
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        //(getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY)
    }
}

fun Activity.hideKeyboard() {
    if (!isFinishing) {
        findViewById<View>(android.R.id.content).hideKeyboard()
    }
}

fun Fragment.showKeyboard() {
    if (isAdded) {
        activity?.showKeyboard()
    }
}

fun Fragment.hideKeyboard() {
    if (isAdded) {
        this.activity?.findViewById<View>(android.R.id.content)?.hideKeyboard()
    }
}

fun View.show() {
    visibility = View.VISIBLE
}

fun View.showIf(default: Int = View.GONE, predicate: (View) -> Boolean) {
    visibility = predicate(this) then View.VISIBLE ?: default
}

fun View.hide() {
    visibility = View.INVISIBLE
}

fun View.hideIf(default: Int = View.VISIBLE, predicate: (View) -> Boolean) {
    visibility = predicate(this) then View.INVISIBLE ?: default
}

fun View.gone() {
    visibility = View.GONE
}

fun View.goneIf(default: Int = View.VISIBLE, predicate: (View) -> Boolean) {
    visibility = predicate(this) then View.GONE ?: default
}

fun View.isVisible() = visibility == View.VISIBLE

/**
 * Updates the padding of the view. All values should be in pixels. Any parameters not given will not be changed from their current value.
 */
fun View.updatePadding(left: Int = paddingLeft, top: Int = paddingTop, right: Int = paddingRight, bottom: Int = paddingBottom) {
    setPadding(left, top, right, bottom)
}

/**
 * Sets either exact height of view (in pixels), or can be [ViewGroup.LayoutParams.WRAP_CONTENT]/[ViewGroup.LayoutParams.MATCH_PARENT]
 */
fun View.setHeight(value: Int) {
    layoutParams?.let {
        layoutParams = it.apply { height = value }
    }
}

/**
 * Sets either exact width of view (in pixels), or can be [ViewGroup.LayoutParams.WRAP_CONTENT]/[ViewGroup.LayoutParams.MATCH_PARENT]
 */
fun View.setWidth(value: Int) {
    layoutParams?.let {
        layoutParams = it.apply { width = value }
    }
}

/**
 * Updates margins of view. Margins should be provided in pixels. Any margins not provided will not be changed.
 */
fun View.updateMargins(left: Int? = null, top: Int? = null, right: Int? = null, bottom: Int? = null) {
    val lp = layoutParams as? ViewGroup.MarginLayoutParams ?: return // Early return for layout params that don't support margins

    lp.setMargins(
        left ?: lp.leftMargin,
        top ?: lp.topMargin,
        right ?: lp.rightMargin,
        bottom ?: lp.bottomMargin
    )

    layoutParams = lp
}