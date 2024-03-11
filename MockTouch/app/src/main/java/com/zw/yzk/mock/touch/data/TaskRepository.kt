package com.zw.yzk.mock.touch.data

import android.annotation.SuppressLint
import android.content.Context
import android.os.Parcel
import com.zw.yzk.mock.touch.MockTouchApplication
import java.io.BufferedOutputStream
import java.io.FileInputStream
import java.io.FileOutputStream


object TaskRepository {

    private const val LOCAL_TASK_CACHE_TITLE = "local_task_cache"

    // 获取所有保存在本地的task
    fun getAllTask(): List<ExecuteTask> {
        return load()
    }

    // 保存已添加的任务
    fun saveTask(task: ExecuteTask) {
        val list = mutableListOf<ExecuteTask>()
        list.addAll(getAllTask())
        list.add(task)
        save(list)
    }

    // 刷新保存的任务
    fun refreshTask(list: List<ExecuteTask>) {
        save(list)
    }

    @SuppressLint("ApplySharedPref")
    private fun save(taskList: List<ExecuteTask>) {
        val fos: FileOutputStream
        try {
            fos = MockTouchApplication.instance.openFileOutput(
                LOCAL_TASK_CACHE_TITLE, Context.MODE_PRIVATE
            )
            val bos = BufferedOutputStream(fos)
            val parcel = Parcel.obtain()
            parcel.writeParcelable(TaskSave(taskList), 0)
            bos.write(parcel.marshall())
            bos.flush()
            bos.close()
            fos.flush()
            fos.close()
            parcel.recycle()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun load(): List<ExecuteTask> {
        val fis: FileInputStream
        return try {
            fis = MockTouchApplication.instance.openFileInput(LOCAL_TASK_CACHE_TITLE)
            val bytes = ByteArray(fis.available())
            fis.read(bytes)
            val parcel = Parcel.obtain()
            parcel.unmarshall(bytes, 0, bytes.size)
            parcel.setDataPosition(0)
            val data: TaskSave? = parcel.readParcelable(TaskSave::class.java.classLoader)
            parcel.recycle()
            fis.close()
            return data?.list ?: emptyList()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}

