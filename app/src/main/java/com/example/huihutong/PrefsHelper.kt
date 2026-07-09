package com.example.huihutong

import android.content.Context
import android.content.SharedPreferences

/**
 * Simple helper to persist openId, satoken and widget scale.
 */
class PrefsHelper(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var openId: String?
        get() = prefs.getString(KEY_OPEN_ID, null)
        set(value) = prefs.edit().putString(KEY_OPEN_ID, value).apply()

    var satoken: String?
        get() = prefs.getString(KEY_SATOKEN, null)
        set(value) = prefs.edit().putString(KEY_SATOKEN, value).apply()

    var scale: Float
        get() = prefs.getFloat(KEY_SCALE, DEFAULT_SCALE)
        set(value) = prefs.edit().putFloat(KEY_SCALE, value).apply()

    companion object {
        private const val PREFS_NAME = "huihutong_prefs"
        private const val KEY_OPEN_ID = "open_id"
        private const val KEY_SATOKEN = "satoken"
        private const val KEY_SCALE = "widget_scale"
        private const val DEFAULT_SCALE = 0.8f

        @Volatile
        private var instance: PrefsHelper? = null

        fun getInstance(context: Context): PrefsHelper {
            return instance ?: synchronized(this) {
                instance ?: PrefsHelper(context.applicationContext).also { instance = it }
            }
        }
    }
}
