package com.widgetcraft.app.widget

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.widgetcraft.app.data.WidgetStorage

class WidgetPinReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val appWidgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )
        val presetId = intent.getStringExtra(WidgetPinManager.EXTRA_PRESET_ID)
        val widgetType = intent.getStringExtra(WidgetPinManager.EXTRA_WIDGET_TYPE) ?: "IMAGE"

        if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID && presetId != null) {
            val storage = WidgetStorage(context)
            storage.bindWidgetIdToPreset(appWidgetId, presetId, widgetType)

            val appWidgetManager = AppWidgetManager.getInstance(context)
            when (widgetType) {
                "CLOCK" -> ClockWidgetProvider.updateWidget(context, appWidgetManager, appWidgetId, storage)
                "NOTE" -> NoteWidgetProvider.updateWidget(context, appWidgetManager, appWidgetId, storage)
                "ICON" -> IconWidgetProvider.updateWidget(context, appWidgetManager, appWidgetId, storage)
                else -> ImageWidgetProvider.updateWidget(context, appWidgetManager, appWidgetId, storage)
            }
            Log.d("WidgetCraft", "Successfully pinned widget $appWidgetId to preset $presetId ($widgetType)")
        }
    }

    companion object {
        const val ACTION_WIDGET_PINNED = "com.widgetcraft.app.ACTION_WIDGET_PINNED"
    }
}
