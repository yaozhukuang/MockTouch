package com.zw.yzk.mock.touch

import android.app.Application


class MockTouchApplication : Application() {

    companion object {
        lateinit var instance: MockTouchApplication
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}