package com.widgetcraft.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import android.widget.RemoteViews
import com.widgetcraft.app.MainActivity
import com.widgetcraft.app.R
import com.widgetcraft.app.data.BentoWidgetConfig
import com.widgetcraft.app.data.WidgetStorage

class BentoWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val storage = WidgetStorage(context)
        for (id in appWidgetIds) {
            updateWidget(context, appWidgetManager, id, storage)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        val storage = WidgetStorage(context)
        for (id in appWidgetIds) {
            storage.removeBinding(id)
        }
    }

    companion object {
        fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            storage: WidgetStorage = WidgetStorage(context)
        ) {
            val config = storage.getBentoConfigForWidgetId(appWidgetId) ?: BentoWidgetConfig()
            val views = RemoteViews(context.packageName, R.layout.widget_bento)

            val bitmap = WidgetRenderer.renderBentoWidget(context, config, targetWidth = 900, targetHeight = 480)
            views.setImageViewBitmap(R.id.widget_bento_image_view, bitmap)

            // Hotspot 1: Alarms / Clock
            val alarmIntent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val resolvedAlarm = if (alarmIntent.resolveActivity(context.packageManager) != null) {
                alarmIntent
            } else {
                Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_APP_ALARM)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            }
            val alarmPending = PendingIntent.getActivity(
                context, 301, resolvedAlarm,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.bento_hotspot_clock, alarmPending)

            // Hotspot 2: Weather details / Edit screen
            val weatherIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                putExtra("edit_preset_id", config.id)
                putExtra("widget_type", "BENTO")
            }
            val weatherPending = PendingIntent.getActivity(
                context, 302, weatherIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.bento_hotspot_weather, weatherPending)

            // Hotspot 3: Calendar
            val calendarIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_APP_CALENDAR)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val calendarPending = PendingIntent.getActivity(
                context, 303, calendarIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.bento_hotspot_calendar, calendarPending)

            // Hotspot 4: Battery & Device Diagnostics
            val batteryIntent = Intent(Intent.ACTION_POWER_USAGE_SUMMARY).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val batteryPending = PendingIntent.getActivity(
                context, 304, batteryIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.bento_hotspot_stats, batteryPending)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun refreshAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = appWidgetManager.getAppWidgetIds(ComponentName(context, BentoWidgetProvider::class.java))
            val storage = WidgetStorage(context)
            for (id in ids) {
                updateWidget(context, appWidgetManager, id, storage)
            }
        }
    }
}
