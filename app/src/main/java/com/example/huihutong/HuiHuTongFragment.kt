package com.example.huihutong

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.huihutong.api.HuiHuTongApiService
import com.example.huihutong.databinding.FragmentHuihutongBinding
import com.example.huihutong.qr.QrCodeGenerator
import com.example.huihutong.widget.HuiHuTongWidgetProvider
import com.example.huihutong.widget.WidgetUpdateHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HuiHuTongFragment : Fragment() {

    private var _binding: FragmentHuihutongBinding? = null
    private val binding get() = _binding!!

    private var autoRefreshJob: Job? = null
    private var firstLoad = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHuihutongBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.qrPreview.setOnClickListener { refreshQrCode() }
        binding.refreshButton.setOnClickListener { refreshQrCode() }

        refreshQrCode()
        HuiHuTongWidgetProvider.scheduleRefreshAlarm(requireContext())
    }

    override fun onResume() {
        super.onResume()
        startAutoRefresh()
    }

    override fun onPause() {
        super.onPause()
        autoRefreshJob?.cancel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        autoRefreshJob?.cancel()
        _binding = null
    }

    private fun startAutoRefresh() {
        autoRefreshJob?.cancel()
        autoRefreshJob = viewLifecycleOwner.lifecycleScope.launch {
            while (isActive) {
                doRefreshQrCode()
                delay(AUTO_REFRESH_INTERVAL_MS)
            }
        }
    }

    private fun refreshQrCode() {
        viewLifecycleOwner.lifecycleScope.launch {
            doRefreshQrCode()
        }
    }

    private suspend fun doRefreshQrCode() {
        binding.statusText.setText(R.string.status_refreshing)

        val prefs = PrefsHelper.getInstance(requireContext())

        // On first launch, try to show cached QR immediately before network refresh
        if (firstLoad) {
            val cachedOpenId = prefs.openId
            val cachedSatoken = prefs.satoken
            if (!cachedOpenId.isNullOrBlank() && !cachedSatoken.isNullOrBlank()) {
                val cachedBitmap = generateQrBitmap(cachedSatoken)
                if (cachedBitmap != null) {
                    binding.qrPreview.setImageBitmap(cachedBitmap)
                    showQrPreview()
                }
            }
            if (binding.qrPreview.visibility != View.VISIBLE) {
                binding.qrPreview.visibility = View.INVISIBLE
                binding.qrLoading.visibility = View.VISIBLE
            }
        }

        val status = withContext(Dispatchers.IO) {
            WidgetUpdateHelper.updateAllWidgets(requireContext())
        }
        binding.statusText.text = if (status == getString(R.string.widget_status_ok)) {
            getString(R.string.status_qr_updated)
        } else {
            status
        }

        val openId = prefs.openId
        val satoken = prefs.satoken
        if (!openId.isNullOrBlank() && !satoken.isNullOrBlank()) {
            val bitmap = generateQrBitmap(satoken)
            if (bitmap != null) {
                binding.qrPreview.setImageBitmap(bitmap)
                showQrPreview()
            }
        }

        if (firstLoad) {
            firstLoad = false
            if (binding.qrPreview.visibility != View.VISIBLE) {
                binding.qrLoading.visibility = View.GONE
            }
        }
    }

    private suspend fun generateQrBitmap(satoken: String): android.graphics.Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                val response = HuiHuTongApiService.api.makeQrcode(satoken)
                val qrString = response.data?.takeIf { it.isNotBlank() }
                if (qrString != null) {
                    val size = (QR_BITMAP_SIZE_DP * resources.displayMetrics.density).toInt()
                    QrCodeGenerator.generate(qrString, size)
                } else {
                    null
                }
            } catch (_: Exception) {
                null
            }
        }
    }

    private fun showQrPreview() {
        if (firstLoad) {
            firstLoad = false
        }
        binding.qrLoading.visibility = View.GONE
        binding.qrPreview.visibility = View.VISIBLE
    }

    companion object {
        private const val AUTO_REFRESH_INTERVAL_MS = 8_000L
        private const val QR_BITMAP_SIZE_DP = 600
    }
}
