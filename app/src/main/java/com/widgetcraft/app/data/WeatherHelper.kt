package com.widgetcraft.app.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object WeatherHelper {

    private const val PREFS_NAME = "widgetcraft_weather_prefs"
    private const val KEY_CACHED_JSON = "cached_weather_json"
    private const val KEY_LAST_UPDATE = "cached_weather_timestamp"
    private const val CACHE_EXPIRY_MS = 30 * 60 * 1000L // 30 minutes

    suspend fun getWeather(context: Context, latitude: Double = 37.7749, longitude: Double = -122.4194): WeatherForecastData {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastUpdate = prefs.getLong(KEY_LAST_UPDATE, 0L)
        val cachedJson = prefs.getString(KEY_CACHED_JSON, null)

        val now = System.currentTimeMillis()
        if (cachedJson != null && (now - lastUpdate) < CACHE_EXPIRY_MS) {
            parseWeatherJson(cachedJson)?.let { return it }
        }

        return withContext(Dispatchers.IO) {
            try {
                val urlStr = "https://api.open-meteo.com/v1/forecast?latitude=$latitude&longitude=$longitude&current=temperature_2m,relative_humidity_2m,apparent_temperature,is_day,weather_code,wind_speed_10m&daily=weather_code,temperature_2m_max,temperature_2m_min&timezone=auto"
                val url = URL(urlStr)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.connectTimeout = 8000
                conn.readTimeout = 8000

                if (conn.responseCode in 200..299) {
                    val reader = BufferedReader(InputStreamReader(conn.inputStream))
                    val jsonStr = reader.readText()
                    prefs.edit()
                        .putString(KEY_CACHED_JSON, jsonStr)
                        .putLong(KEY_LAST_UPDATE, now)
                        .apply()
                    parseWeatherJson(jsonStr) ?: WeatherForecastData()
                } else {
                    if (cachedJson != null) parseWeatherJson(cachedJson) ?: WeatherForecastData()
                    else WeatherForecastData()
                }
            } catch (e: Exception) {
                if (cachedJson != null) parseWeatherJson(cachedJson) ?: WeatherForecastData()
                else WeatherForecastData()
            }
        }
    }

    fun getCachedWeather(context: Context): WeatherForecastData {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val cachedJson = prefs.getString(KEY_CACHED_JSON, null)
        return if (cachedJson != null) parseWeatherJson(cachedJson) ?: WeatherForecastData() else WeatherForecastData()
    }

    private fun parseWeatherJson(jsonStr: String): WeatherForecastData? {
        return try {
            val root = JSONObject(jsonStr)
            val current = root.getJSONObject("current")
            val tempVal = current.getDouble("temperature_2m")
            val wmoCode = current.getInt("weather_code")
            val windSpeedVal = current.optDouble("wind_speed_10m", 10.0)
            val humidityVal = current.optInt("relative_humidity_2m", 50)
            val isDay = current.optInt("is_day", 1)

            val condition = mapWmoCodeToCondition(wmoCode, isDay == 1)

            var high = "${tempVal.toInt() + 3}°C"
            var low = "${tempVal.toInt() - 4}°C"
            val daily = root.optJSONObject("daily")
            if (daily != null) {
                val maxArr = daily.optJSONArray("temperature_2m_max")
                val minArr = daily.optJSONArray("temperature_2m_min")
                if (maxArr != null && maxArr.length() > 0) {
                    high = "${maxArr.getDouble(0).toInt()}°C"
                }
                if (minArr != null && minArr.length() > 0) {
                    low = "${minArr.getDouble(0).toInt()}°C"
                }
            }

            WeatherForecastData(
                temp = "${tempVal.toInt()}°C",
                condition = condition,
                wmoCode = wmoCode,
                windSpeed = "${windSpeedVal.toInt()} km/h",
                humidity = "$humidityVal%",
                locationName = "Local Area",
                dailyHigh = high,
                dailyLow = low,
                lastUpdatedMs = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            null
        }
    }

    fun mapWmoCodeToCondition(code: Int, isDay: Boolean = true): String {
        return when (code) {
            0 -> if (isDay) "Sunny" else "Clear Night"
            1, 2, 3 -> "Partly Cloudy"
            45, 48 -> "Foggy"
            51, 53, 55 -> "Drizzle"
            56, 57 -> "Freezing Drizzle"
            61, 63, 65 -> "Rain"
            66, 67 -> "Freezing Rain"
            71, 73, 75, 77 -> "Snow"
            80, 81, 82 -> "Showers"
            85, 86 -> "Snow Showers"
            95, 96, 99 -> "Thunderstorm"
            else -> "Fair"
        }
    }

    fun mapWmoCodeToGlyph(code: Int, isDay: Boolean = true): String {
        return when (code) {
            0 -> if (isDay) "☀️" else "🌙"
            1, 2, 3 -> "⛅"
            45, 48 -> "🌫️"
            51, 53, 55, 56, 57 -> "🌦️"
            61, 63, 65, 80, 81, 82 -> "🌧️"
            66, 67, 71, 73, 75, 77, 85, 86 -> "❄️"
            95, 96, 99 -> "⛈️"
            else -> "☀️"
        }
    }
}
