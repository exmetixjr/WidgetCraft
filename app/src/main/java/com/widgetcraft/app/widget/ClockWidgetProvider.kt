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
import com.widgetcraft.app.data.ClockWidgetConfig
import com.widgetcraft.app.data.WidgetStorage

class ClockWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val storage = WidgetStorage(context)
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId, storage)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        val storage = WidgetStorage(context)
        for (id in appWidgetIds) {
            storage.removeBinding(id)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val action = intent.action
        if (action == Intent.ACTION_TIME_TICK ||
            action == Intent.ACTION_TIME_CHANGED ||
            action == Intent.ACTION_TIMEZONE_CHANGED ||
            action == Intent.ACTION_BATTERY_CHANGED
        ) {
            refreshAllWidgets(context)
        }
    }

    companion object {
        fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            storage: WidgetStorage = WidgetStorage(context)
        ) {
            val config = storage.getClockConfigForWidgetId(appWidgetId) ?: ClockWidgetConfig()
            val views = RemoteViews(context.packageName, R.layout.widget_clock)

            val bitmap = WidgetRenderer.renderClockWidget(context, config)
            views.setImageViewBitmap(R.id.widget_clock_image_view, bitmap)

            val clockIntent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val pendingIntent = try {
                PendingIntent.getActivity(
                    context,
                    appWidgetId,
                    clockIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            } catch (e: Exception) {
                val fallbackIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                    putExtra("edit_preset_id", config.id)
                    putExtra("widget_type", "CLOCK")
                }
                PendingIntent.getActivity(
                    context,
                    appWidgetId,
                    fallbackIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                )
            }

            views.setOnClickPendingIntent(R.id.widget_clock_container, pendingIntent)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun refreshAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = appWidgetManager.getAppWidgetIds(ComponentName(context, ClockWidgetProvider::class.java))
            val storage = WidgetStorage(context)
            for (id in ids) {
                updateWidget(context, appWidgetManager, id, storage)
            }
        }
    }
}
