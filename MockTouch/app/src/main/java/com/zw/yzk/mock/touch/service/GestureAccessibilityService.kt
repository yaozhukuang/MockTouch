package com.zw.yzk.mock.touch.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.accessibilityservice.GestureDescription.StrokeDescription
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Path
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.zw.yzk.mock.touch.data.AccessibilityItem
import com.zw.yzk.mock.touch.data.GestureDelay
import com.zw.yzk.mock.touch.data.GestureItem
import com.zw.yzk.mock.touch.data.GestureLiveData
import com.zw.yzk.mock.touch.data.GestureRecorder
import com.zw.yzk.mock.touch.data.RecorderRepository
import com.zw.yzk.mock.touch.data.TaskItem
import com.zw.yzk.mock.touch.widget.FloatingImageView


class GestureAccessibilityService : AccessibilityService() {

    companion object {
        const val EXECUTE_BACK = "execute_back"
        const val EXECUTE_TASK = "execute_task"
        const val QUIT = "quit"
        private const val MSG_EXECUTE = 100001
    }

    private val path = Path()
    private val bounds = Rect()
    private var currentIndex = 0
    private var isPausing = false
    private var gestureRecorder = GestureRecorder()

    private val mainHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_EXECUTE -> execute()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        GestureLiveData.instance.executingState.observeForever {
            when (it) {
                GestureLiveData.EXECUTE_STATE_PAUSE -> {
                    isPausing = true
                    mainHandler.removeMessages(MSG_EXECUTE)
                }

                GestureLiveData.EXECUTE_STATE_START -> {
                    currentIndex = 0
                    path.reset()
                    isPausing = false
                    mainHandler.sendEmptyMessage(MSG_EXECUTE)
                }
            }
        }
        GestureLiveData.instance.recording.observeForever {
            when (it) {
                GestureLiveData.RECORDING_STATE_START -> {
                    gestureRecorder.clear()
                }

                GestureLiveData.RECORDING_STATE_COMPLETE -> {
                    RecorderRepository.addRecord(gestureRecorder.clone())
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val quit = intent.getBooleanExtra(QUIT, false)
        if (quit) {
            stopSelf()
        }
        val executeBack = intent.getBooleanExtra(EXECUTE_BACK, false)
        if (executeBack) {
            performGlobalAction(GLOBAL_ACTION_BACK)
        }
        val executeTask = intent.getBooleanExtra(EXECUTE_TASK, false)
        if (executeTask) {
            GestureLiveData.instance.setState(GestureLiveData.EXECUTE_STATE_START)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        mainHandler.removeMessages(MSG_EXECUTE)
        GestureLiveData.instance.setState(GestureLiveData.EXECUTE_STATE_READY)
    }

    @SuppressLint("SwitchIntDef")
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (!GestureLiveData.instance.isRecording()) {
            return
        }
        when (event.eventType) {
            AccessibilityEvent.TYPE_VIEW_LONG_CLICKED -> {
                gestureRecorder.add(event)
            }

            AccessibilityEvent.TYPE_VIEW_CLICKED -> {
                if (ignore(event)) {
                    return
                }
                gestureRecorder.add(event)
            }
        }
    }

    override fun onInterrupt() {
    }

    // 录制操作的时候，如果点击悬浮窗那么停止录制且本次点击事件不做记录
    private fun ignore(event: AccessibilityEvent): Boolean {
        val sourceNode: AccessibilityNodeInfo = event.source ?: return true
        return sourceNode.className.toString() == FloatingImageView::class.java.name
    }

    private fun execute() {
        val repeat = GestureLiveData.instance.currentTask.value?.repeat ?: -1
        val count = GestureLiveData.instance.currentTaskExecuteCount.value ?: 0
        if (repeat != -1 && count > repeat) {
            // 达到执行总数，停止
            GestureLiveData.instance.setState(GestureLiveData.EXECUTE_STATE_PAUSE)
            return
        }
        val list = GestureLiveData.instance.currentTask.value?.task ?: emptyList()
        if (isPausing || list.isEmpty()) {
            return
        }
        currentIndex = if (currentIndex > list.lastIndex) 0 else currentIndex
        if (currentIndex == 0) {
            GestureLiveData.instance.currentTaskExecuteCount.postValue(count + 1)
        }
        val item = list[currentIndex]
        if (item is GestureDelay) {
            // 执行延时
            currentIndex++
            mainHandler.sendEmptyMessageDelayed(MSG_EXECUTE, item.duration)
            return
        }
        // 执行手势
        val description = getStroke(item as GestureItem)
        if (description == null) {
            currentIndex++
            mainHandler.sendEmptyMessage(MSG_EXECUTE)
            return
        }
        GestureLiveData.instance.currentGestureName.postValue(item.name)
        dispatchGesture(description, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription) {
                super.onCompleted(gestureDescription)
                // 执行成功，继续下一个手势
                currentIndex++
                mainHandler.sendEmptyMessage(MSG_EXECUTE)
            }

            override fun onCancelled(gestureDescription: GestureDescription) {
                super.onCancelled(gestureDescription)
                // 执行失败，重新执行当前手势
                mainHandler.sendEmptyMessageDelayed(MSG_EXECUTE, 1000)
            }
        }, mainHandler)
    }

    private fun getStroke(item: TaskItem): GestureDescription? {
        return when (item) {
            is GestureItem -> getGestureStroke(item)
            is AccessibilityItem -> getAccessibilityStroke(item)
            else -> null
        }
    }

    private fun getGestureStroke(item: GestureItem): GestureDescription {
        val stroke = item.gesture.strokes[0]
        path.reset()
        path.set(stroke.path)
        return createGestureDescription(path, item.duration)
    }

    private fun getAccessibilityStroke(item: AccessibilityItem): GestureDescription? {
        val source = item.event.source ?: return null
        path.reset()
        if (source.childCount == 0) {
            path.moveTo(bounds.centerX().toFloat(), bounds.centerY().toFloat())
            source.getBoundsInScreen(bounds)
            return createGestureDescription(path, 100)
        }
        val list = mutableListOf<AccessibilityNodeInfo>()
        findClickedAccessibilityNodeInfo(source, list)
        for (i in list.lastIndex..0) {
            if (list[i].viewIdResourceName == source.viewIdResourceName) {
                path.moveTo(bounds.centerX().toFloat(), bounds.centerY().toFloat())
                source.getBoundsInScreen(bounds)
                return createGestureDescription(path, 100)
            }
        }
        return null
    }

    private fun findClickedAccessibilityNodeInfo(
        info: AccessibilityNodeInfo,
        list: MutableList<AccessibilityNodeInfo>
    ){
        if (info.isClickable) {
            list.add(info)
        }
        if (info.childCount == 0) {
            return
        }
        for (i in 0 until info.childCount) {
            if (info.getChild(i) != null && info.getChild(i).isClickable) {
                findClickedAccessibilityNodeInfo(info.getChild(i), list)
            }
        }
    }

    private fun createGestureDescription(path: Path, duration: Long): GestureDescription {
        val strokeDescription = StrokeDescription(path, 0, duration)
        return GestureDescription.Builder().addStroke(strokeDescription).build()
    }


}