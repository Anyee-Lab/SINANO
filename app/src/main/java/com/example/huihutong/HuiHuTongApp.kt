package com.example.huihutong

import android.app.Application
import androidx.work.Configuration

class HuiHuTongApp : Application(), Configuration.Provider {

    override fun onCreate() {
        super.onCreate()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
}
