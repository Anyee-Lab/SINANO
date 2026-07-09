package com.example.huihutong.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Receives the 8-second alarm and triggers a widget refresh.
 * Also reschedules the next exact alarm for API 31+.
 */
class WidgetAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        CoroutineScope(Dispatchers.IO).launch {
            WidgetUpdateHelper.updateAllWidgets(context)
        }
        // Reschedule the next exact alarm if we are using exact alarms
        HuiHuTongWidgetProvider.scheduleRefreshAlarm(context)
    }
}
