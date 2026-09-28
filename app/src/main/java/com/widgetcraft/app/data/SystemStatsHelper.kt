package com.widgetcraft.app.data

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Environment
import android.os.StatFs
import android.os.SystemClock

object SystemStatsHelper {

    fun getStorageStats(): Pair<Int, Int> {
        val stat = StatFs(Environment.getDataDirectory().path)
        val freeBytes = stat.availableBlocksLong * stat.blockSizeLong
        val totalBytes = stat.blockCountLong * stat.blockSizeLong
        val usedPct = if (totalBytes > 0) (((totalBytes - freeBytes).toFloat() / totalBytes.toFloat()) * 100).toInt() else 0
        val freeGb = (freeBytes / (1024L * 1024L * 1024L)).toInt()
        return Pair(freeGb, usedPct)
    }

    fun getRamStats(context: Context): Triple<Float, Float, Int> {
        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager?.getMemoryInfo(memInfo)
        val totalGb = memInfo.totalMem / (1024f * 1024f * 1024f)
        val availGb = memInfo.availMem / (1024f * 1024f * 1024f)
        val usedGb = totalGb - availGb
        val pct = if (totalGb > 0) ((usedGb / totalGb) * 100).toInt() else 0
        return Triple(usedGb, totalGb, pct)
    }

    fun getBatteryStats(context: Context): Pair<Int, Float> {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val pct = if (level >= 0 && scale > 0) ((level.toFloat() / scale.toFloat()) * 100).toInt() else 100
        val tempRaw = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
        val tempC = tempRaw / 10.0f
        return Pair(pct, tempC)
    }

    fun getUptimeFormatted(): String {
        val uptimeMs = SystemClock.elapsedRealtime()
        val hours = (uptimeMs / (1000 * 60 * 60)).toInt()
        val minutes = ((uptimeMs / (1000 * 60)) % 60).toInt()
        return "${hours}h ${minutes}m"
    }
}
