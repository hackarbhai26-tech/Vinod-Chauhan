package com.example.service

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Environment
import android.os.StatFs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SystemStats(
    val batteryPercent: Int = 100,
    val isCharging: Boolean = false,
    val ramUsedMb: Long = 0,
    val ramTotalMb: Long = 0,
    val ramPercent: Int = 0,
    val storageUsedGb: Double = 0.0,
    val storageTotalGb: Double = 0.0,
    val storagePercent: Int = 0
)

class SystemStatsMonitor(private val context: Context) {

    private val _stats = MutableStateFlow(SystemStats())
    val stats: StateFlow<SystemStats> = _stats.asStateFlow()

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context?, intent: Intent?) {
            updateStats()
        }
    }

    init {
        try {
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_BATTERY_CHANGED)
                addAction(Intent.ACTION_POWER_CONNECTED)
                addAction(Intent.ACTION_POWER_DISCONNECTED)
            }
            context.registerReceiver(batteryReceiver, filter)
        } catch (_: Exception) {}
        updateStats()
    }

    fun updateStats() {
        // Battery
        val bmIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = bmIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: 100
        val scale = bmIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: 100
        val batteryPct = if (level >= 0 && scale > 0) ((level.toFloat() / scale) * 100).toInt() else 100
        val status = bmIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

        // RAM Memory
        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager?.getMemoryInfo(memInfo)
        val totalRamMb = memInfo.totalMem / (1024 * 1024)
        val availRamMb = memInfo.availMem / (1024 * 1024)
        val usedRamMb = (totalRamMb - availRamMb).coerceAtLeast(0)
        val ramPct = if (totalRamMb > 0) ((usedRamMb.toFloat() / totalRamMb) * 100).toInt() else 0

        // Storage
        var totalGb = 64.0
        var usedGb = 24.0
        var storagePct = 37
        try {
            val path = Environment.getDataDirectory()
            val stat = StatFs(path.path)
            val blockSize = stat.blockSizeLong
            val totalBlocks = stat.blockCountLong
            val availableBlocks = stat.availableBlocksLong

            val totalBytes = totalBlocks * blockSize
            val freeBytes = availableBlocks * blockSize
            val usedBytes = totalBytes - freeBytes

            totalGb = (totalBytes.toDouble() / (1024 * 1024 * 1024 * 1.0))
            usedGb = (usedBytes.toDouble() / (1024 * 1024 * 1024 * 1.0))
            if (totalBytes > 0) {
                storagePct = ((usedBytes.toDouble() / totalBytes) * 100).toInt()
            }
        } catch (_: Exception) {}

        _stats.value = SystemStats(
            batteryPercent = batteryPct,
            isCharging = isCharging,
            ramUsedMb = usedRamMb,
            ramTotalMb = totalRamMb,
            ramPercent = ramPct,
            storageUsedGb = Math.round(usedGb * 10.0) / 10.0,
            storageTotalGb = Math.round(totalGb * 10.0) / 10.0,
            storagePercent = storagePct
        )
    }

    fun release() {
        try {
            context.unregisterReceiver(batteryReceiver)
        } catch (_: Exception) {}
    }
}
