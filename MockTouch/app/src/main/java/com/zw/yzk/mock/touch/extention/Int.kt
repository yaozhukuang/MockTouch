package com.zw.yzk.mock.touch.extention

import android.content.res.Resources

fun Int.dp(): Float {
    return Resources.getSystem().displayMetrics.density * this
}

fun Int.dpInt(): Int {
    return (Resources.getSystem().displayMetrics.density * this).toInt()
}