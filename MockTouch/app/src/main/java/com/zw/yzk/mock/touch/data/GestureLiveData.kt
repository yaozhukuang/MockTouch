package com.zw.yzk.mock.touch.data

import androidx.lifecycle.MutableLiveData

class GestureLiveData private constructor() {

    companion object {
        // 录制操作状态
        const val RECORDING_STATE_INIT = 0
        const val RECORDING_STATE_START = 1
        const val RECORDING_STATE_COMPLETE = 2
        // 任务执行状态
        const val EXECUTE_STATE_READY = 0
        const val EXECUTE_STATE_START = 1
        const val EXECUTE_STATE_PAUSE = 2

        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            GestureLiveData()
        }
    }

    //当前任务的执行状态
    val executingState: MutableLiveData<Int> = MutableLiveData(EXECUTE_STATE_READY)

    //当前正在执行的手势事件名称
    val currentGestureName: MutableLiveData<String> = MutableLiveData("")

    // 当前正在任务
    val currentTask: MutableLiveData<ExecuteTask?> = object : MutableLiveData<ExecuteTask?>(null) {
        override fun setValue(value: ExecuteTask?) {
            super.setValue(value)
            currentTaskExecuteCount.postValue(0)
        }
    }

    // 当前任务的执行次数
    val currentTaskExecuteCount: MutableLiveData<Int> = MutableLiveData(0)

    // 是否正在录屏
    var recording = MutableLiveData(RECORDING_STATE_INIT)

    fun isRecording(): Boolean {
        return recording.value == RECORDING_STATE_START
    }

    fun startRecording() {
        recording.postValue(RECORDING_STATE_START)
    }

    fun stopRecording() {
        recording.postValue(RECORDING_STATE_COMPLETE)
    }

    fun setState(state: Int) {
        executingState.postValue(state)
    }
}