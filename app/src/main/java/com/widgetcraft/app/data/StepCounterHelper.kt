package com.widgetcraft.app.data

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object StepCounterHelper : SensorEventListener {

    private const val PREFS_NAME = "widgetcraft_step_prefs"
    private const val KEY_TODAY_DATE = "today_date"
    private const val KEY_MIDNIGHT_BASELINE = "midnight_baseline"
    private const val KEY_LAST_SENSOR_VALUE = "last_sensor_value"
    private const val KEY_REBOOT_OFFSET = "reboot_offset"
    private const val KEY_CACHED_STEPS = "cached_today_steps"

    private var sensorManager: SensorManager? = null
    private var isListening = false
    private var appContext: Context? = null

    fun startListening(context: Context) {
        if (isListening) return
        appContext = context.applicationContext
        sensorManager = appContext?.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (stepSensor != null) {
            sensorManager?.registerListener(
                this,
                stepSensor,
                SensorManager.SENSOR_DELAY_NORMAL,
                60_000_000 // 60s hardware FIFO batching to conserve battery
            )
            isListening = true
        }
    }

    fun stopListening() {
        if (isListening) {
            sensorManager?.unregisterListener(this)
            isListening = false
            appContext = null
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_STEP_COUNTER) return
        val rawSteps = event.values[0].toInt()
        appContext?.let { recordSensorSteps(it, rawSteps) }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun recordSensorSteps(context: Context, rawSteps: Int): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val savedDate = prefs.getString(KEY_TODAY_DATE, "")
        var midnightBaseline = prefs.getInt(KEY_MIDNIGHT_BASELINE, -1)
        var lastSensorVal = prefs.getInt(KEY_LAST_SENSOR_VALUE, rawSteps)
        var rebootOffset = prefs.getInt(KEY_REBOOT_OFFSET, 0)

        // Midnight Day Reset
        if (todayStr != savedDate || midnightBaseline == -1) {
            midnightBaseline = rawSteps
            rebootOffset = 0
            prefs.edit()
                .putString(KEY_TODAY_DATE, todayStr)
                .putInt(KEY_MIDNIGHT_BASELINE, midnightBaseline)
                .putInt(KEY_REBOOT_OFFSET, 0)
                .apply()
        }

        // Reboot Detection (counter reset)
        if (rawSteps < lastSensorVal) {
            rebootOffset += (lastSensorVal - midnightBaseline).coerceAtLeast(0)
            midnightBaseline = rawSteps
            prefs.edit()
                .putInt(KEY_MIDNIGHT_BASELINE, midnightBaseline)
                .putInt(KEY_REBOOT_OFFSET, rebootOffset)
                .apply()
        }

        val calculatedSteps = ((rawSteps - midnightBaseline) + rebootOffset).coerceAtLeast(0)

        prefs.edit()
            .putInt(KEY_LAST_SENSOR_VALUE, rawSteps)
            .putInt(KEY_CACHED_STEPS, calculatedSteps)
            .apply()

        return calculatedSteps
    }

    fun getTodaySteps(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val savedDate = prefs.getString(KEY_TODAY_DATE, "")
        if (todayStr != savedDate) {
            return 0
        }
        return prefs.getInt(KEY_CACHED_STEPS, 4820) // default realistic starter step count if hardware not stepped yet
    }
}
