package com.example.widgets

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

object WidgetUpdater {
    private const val PERIODIC_WORK_TAG = "civora_widget_periodic_update"

    suspend fun updateAllWidgets(context: Context) {
        try {
            NextClassWidget().updateAll(context)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            TaskWidget().updateAll(context)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun triggerImmediateUpdate(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            updateAllWidgets(context)
        }
    }

    fun schedulePeriodicUpdates(context: Context) {
        try {
            val workRequest = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(
                15, TimeUnit.MINUTES,
                5, TimeUnit.MINUTES
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                PERIODIC_WORK_TAG,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
}
