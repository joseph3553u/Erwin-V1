package com.example.widgets

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class WidgetUpdateWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            WidgetUpdater.updateAllWidgets(appContext)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
