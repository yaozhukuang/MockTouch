package com.zw.yzk.mock.touch.data

import android.gesture.Gesture

object GestureRepository {

    private val gestures = mutableListOf<GestureItem>()

    fun addGesture(name: String, duration: Long, gesture: Gesture) {
        gestures.add(GestureItem(name, duration, gesture))
    }

    fun getGesture(): List<GestureItem> {
        return gestures
    }
}