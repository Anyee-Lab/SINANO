package com.example.huihutong.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.PowerManager
import android.widget.RemoteViews
import com.example.huihutong.PrefsHelper
import com.example.huihutong.R
import com.example.huihutong.api.HuiHuTongApiService
import com.example.huihutong.qr.QrCodeGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object WidgetUpdateHelper {

    private const val WAKE_LOCK_TAG = "HuiHuTong::WidgetUpdateWakeLock"
    private const val WAKE_LOCK_TIMEOUT_MS = 10_000L

    /**
     * Refresh the QR code and update every instance of the widget.
     *
     * This performs network I/O on the IO dispatcher and returns a human readable status.
     */
    suspend fun updateAllWidgets(context: Context): String {
        val prefs = PrefsHelper.getInstance(context)
        val openId = prefs.openId

        if (openId.isNullOrBlank()) {
            updateWidgetStatus(context, context.getString(R.string.widget_status_no_openid))
            return context.getString(R.string.widget_status_no_openid)
        }

        // Keep CPU awake while we do the network request and bitmap generation
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            WAKE_LOCK_TAG
        ).apply {
            acquire(WAKE_LOCK_TIMEOUT_MS)
        }

        val result = try {
            withContext(Dispatchers.IO) {
                refreshQrCode(context, prefs, openId)
            }
        } finally {
            if (wakeLock.isHeld) {
                wakeLock.release()
            }
        }

        updateWidgetStatus(context, result)
        return result
    }

    private suspend fun refreshQrCode(context: Context, prefs: PrefsHelper, openId: String): String {
        val api = HuiHuTongApiService.api
        val satoken = prefs.satoken

        // 1. Try to get QR code with current token
        val qrString = try {
            if (!satoken.isNullOrBlank()) {
                val response = api.makeQrcode(satoken)
                response.data?.takeIf { it.isNotBlank() }
            } else null
        } catch (e: Exception) {
            null
        }

        if (qrString != null) {
            renderQrCode(context, qrString, prefs.scale)
            return context.getString(R.string.widget_status_ok)
        }

        // 2. Token might be expired -> login again
        return try {
            val loginResponse = api.certificateLogin(openId)
            val newToken = loginResponse.data?.token
            if (newToken.isNullOrBlank()) {
                context.getString(R.string.widget_status_login_failed)
            } else {
                prefs.satoken = newToken
                val retryResponse = api.makeQrcode(newToken)
                val retryQr = retryResponse.data?.takeIf { it.isNotBlank() }
                if (retryQr != null) {
                    renderQrCode(context, retryQr, prefs.scale)
                    context.getString(R.string.widget_status_ok)
                } else {
                    context.getString(R.string.widget_status_qrcode_empty)
                }
            }
        } catch (e: Exception) {
            context.getString(R.string.widget_status_network_error, e.message ?: "unknown")
        }
    }

    private fun renderQrCode(context: Context, qrString: String, scale: Float) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, HuiHuTongWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

        // Size in px for the bitmap.  The widget is roughly 180dp x 180dp.
        val density = context.resources.displayMetrics.density
        val baseSize = (180 * density).toInt()
        val size = (baseSize * scale.coerceIn(0.4f, 1.0f)).toInt()

        val bitmap: Bitmap? = QrCodeGenerator.generate(qrString, size)

        for (appWidgetId in appWidgetIds) {
            val remoteViews = RemoteViews(context.packageName, R.layout.widget_layout)
            if (bitmap != null) {
                remoteViews.setImageViewBitmap(R.id.widget_qr_image, bitmap)
                remoteViews.setTextViewText(R.id.widget_status, context.getString(R.string.widget_status_ok))
            } else {
                remoteViews.setImageViewResource(R.id.widget_qr_image, R.drawable.ic_qr_placeholder)
                remoteViews.setTextViewText(R.id.widget_status, context.getString(R.string.widget_status_render_failed))
            }
            attachClickIntents(context, remoteViews)
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
        }
    }

    private fun updateWidgetStatus(context: Context, status: String) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, HuiHuTongWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

        for (appWidgetId in appWidgetIds) {
            val remoteViews = RemoteViews(context.packageName, R.layout.widget_layout)
            remoteViews.setTextViewText(R.id.widget_status, status)
            attachClickIntents(context, remoteViews)
            appWidgetManager.partiallyUpdateAppWidget(appWidgetId, remoteViews)
        }
    }

    private fun attachClickIntents(context: Context, remoteViews: RemoteViews) {
        // Tap the QR image -> refresh
        val refreshIntent = Intent(context, HuiHuTongWidgetProvider::class.java).apply {
            action = HuiHuTongWidgetProvider.ACTION_REFRESH_WIDGET
        }
        val refreshPendingIntent = android.app.PendingIntent.getBroadcast(
            context,
            0,
            refreshIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        remoteViews.setOnClickPendingIntent(R.id.widget_qr_image, refreshPendingIntent)

        // Tap the status text -> open main app
        val openAppIntent = Intent(context, com.example.huihutong.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = android.app.PendingIntent.getActivity(
            context,
            1,
            openAppIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        remoteViews.setOnClickPendingIntent(R.id.widget_status, openAppPendingIntent)
    }
}
