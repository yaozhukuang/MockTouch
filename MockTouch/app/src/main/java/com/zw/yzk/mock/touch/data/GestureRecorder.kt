package com.zw.yzk.mock.touch.data

import android.view.accessibility.AccessibilityEvent

class GestureRecorder {

    private val eventList = mutableListOf<AccessibilityItem>()

    fun clear() {
        eventList.clear()
    }

    fun add(event: AccessibilityEvent) {
        eventList.add(AccessibilityItem(event))
    }

    fun addAll(list: List<AccessibilityItem>) {
        eventList.addAll(list)
    }

    fun getEventList(): List<AccessibilityItem> {
        return eventList
    }

    fun clone(): GestureRecorder {
        return GestureRecorder().apply {
            addAll(getEventList())
        }
    }
}