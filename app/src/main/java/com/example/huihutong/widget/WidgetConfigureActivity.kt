package com.example.huihutong.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.huihutong.PrefsHelper
import com.example.huihutong.databinding.ActivityWidgetConfigureBinding
import kotlinx.coroutines.launch

/**
 * Configuration screen shown when the user adds the widget to the home screen.
 */
class WidgetConfigureActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWidgetConfigureBinding
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWidgetConfigureBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set the result to CANCELED in case the user backs out
        setResult(RESULT_CANCELED)

        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        val prefs = PrefsHelper.getInstance(this)
        binding.openIdEdit.setText(prefs.openId ?: "")

        binding.scaleSlider.value = prefs.scale.coerceIn(0.4f, 1.0f)
        updateScaleLabel(prefs.scale)

        binding.scaleSlider.addOnChangeListener { _, value, _ ->
            updateScaleLabel(value)
        }

        binding.saveButton.setOnClickListener {
            val openId = binding.openIdEdit.text.toString().trim()
            if (openId.isBlank()) {
                binding.openIdEdit.error = "请输入 openId"
                return@setOnClickListener
            }

            val scale = binding.scaleSlider.value
            prefs.openId = openId
            prefs.scale = scale

            lifecycleScope.launch {
                WidgetUpdateHelper.updateAllWidgets(this@WidgetConfigureActivity)

                val resultValue = Intent().putExtra(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    appWidgetId
                )
                setResult(RESULT_OK, resultValue)
                finish()
            }
        }
    }

    private fun updateScaleLabel(scale: Float) {
        binding.scaleLabel.text = String.format("缩放比例: %.2f", scale)
    }
}
