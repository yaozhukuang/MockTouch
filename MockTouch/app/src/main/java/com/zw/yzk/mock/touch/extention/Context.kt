package com.zw.yzk.mock.touch.extention

import android.content.Context

fun Context.screenWidth(): Int {
    return this.resources.displayMetrics.widthPixels
}

fun Context.screenHeight(): Int {
    return this.resources.displayMetrics.heightPixels
}

