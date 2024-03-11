package com.zw.yzk.mock.touch.extention

import android.content.res.Resources

fun Float.dp(): Float {
    return Resources.getSystem().displayMetrics.density * this
}