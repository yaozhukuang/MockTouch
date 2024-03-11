package com.zw.yzk.mock.touch.data

import android.accessibilityservice.GestureDescription
import android.gesture.Gesture
import android.graphics.Path
import android.os.Parcelable
import android.view.accessibility.AccessibilityEvent
import kotlinx.parcelize.Parcelize


interface TaskItem : Parcelable {
    fun getShowContent(): String
}

@Parcelize
data class GestureItem(var name: String, val duration: Long, val gesture: Gesture) : TaskItem{
    override fun getShowContent() = name
}

@Parcelize
data class GestureDelay(val duration: Long, val display: String) : TaskItem {
    override fun getShowContent() = display
}

@Parcelize
data class AccessibilityItem(val event: AccessibilityEvent) : TaskItem {
    override fun getShowContent(): String {
        return AccessibilityEvent.eventTypeToString(event.eventType)
    }
}

@Parcelize
data class ExecuteTask(val name: String, val repeat: Int, val task: List<TaskItem>) : Parcelable

@Parcelize
data class TaskSave(val list: List<ExecuteTask>) : Parcelable
