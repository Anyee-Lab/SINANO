package com.example.huihutong

import android.content.res.ColorStateList
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.viewpager2.widget.ViewPager2
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.huihutong.databinding.ActivityMainBinding
import com.example.huihutong.widget.HuiHuTongWidgetWorker
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            updateTabUi(position)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.viewPager.adapter = MainPagerAdapter(this)
        binding.viewPager.registerOnPageChangeCallback(pageChangeCallback)

        binding.navHuihutong.setOnClickListener { binding.viewPager.currentItem = TAB_HUIHUTONG }
        binding.navProfile.setOnClickListener { binding.viewPager.currentItem = TAB_PROFILE }

        updateTabUi(TAB_HUIHUTONG)

        // Fallback background refresh (minimum 15 min), preserves original behavior.
        scheduleFallbackWorker()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.viewPager.unregisterOnPageChangeCallback(pageChangeCallback)
    }

    private fun scheduleFallbackWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val workRequest = PeriodicWorkRequestBuilder<HuiHuTongWidgetWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "huihutong_fallback_refresh",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    private fun updateTabUi(position: Int) {
        val selectedColor = ContextCompat.getColor(this, R.color.primary)
        val unselectedColor = ContextCompat.getColor(this, R.color.text_hint)

        if (position == TAB_HUIHUTONG) {
            binding.titleBar.setText(R.string.tab_huihutong)
            ImageViewCompat.setImageTintList(binding.navHuihutongIcon, ColorStateList.valueOf(selectedColor))
            binding.navHuihutongLabel.setTextColor(selectedColor)
            ImageViewCompat.setImageTintList(binding.navProfileIcon, ColorStateList.valueOf(unselectedColor))
            binding.navProfileLabel.setTextColor(unselectedColor)
        } else {
            binding.titleBar.setText(R.string.tab_profile)
            ImageViewCompat.setImageTintList(binding.navHuihutongIcon, ColorStateList.valueOf(unselectedColor))
            binding.navHuihutongLabel.setTextColor(unselectedColor)
            ImageViewCompat.setImageTintList(binding.navProfileIcon, ColorStateList.valueOf(selectedColor))
            binding.navProfileLabel.setTextColor(selectedColor)
        }
    }

    companion object {
        private const val TAB_HUIHUTONG = 0
        private const val TAB_PROFILE = 1
    }
}
