package com.zw.yzk.mock.touch.data

object RecorderRepository {
    private val records = mutableListOf<GestureRecorder>()

    fun addRecord(record: GestureRecorder) {
        records.add(record)
    }

    fun getRecords(): List<GestureRecorder> {
        return records
    }
}