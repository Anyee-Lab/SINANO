package com.example.huihutong.widget

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.huihutong.R

/**
 * Fallback worker used when the user opens the app and wants to ensure
 * background refresh is registered through WorkManager.
 *
 * Note: WorkManager periodic tasks cannot run more often than 15 minutes,
 * so this is only a fallback; the real 8-second loop is handled by AlarmManager.
 */
class HuiHuTongWidgetWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val status = WidgetUpdateHelper.updateAllWidgets(applicationContext)
        return if (status.contains(applicationContext.getString(R.string.widget_status_ok))) {
            Result.success()
        } else {
            Result.retry()
        }
    }
}
