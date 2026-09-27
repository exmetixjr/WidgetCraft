package com.widgetcraft.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.widgetcraft.app.MainActivity
import com.widgetcraft.app.R
import com.widgetcraft.app.data.IconWidgetConfig
import com.widgetcraft.app.data.WidgetStorage

class IconWidgetProvider : AppWidgetProvider() {

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

    companion object {
        fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            storage: WidgetStorage = WidgetStorage(context)
        ) {
            val config = storage.getIconConfigForWidgetId(appWidgetId) ?: IconWidgetConfig()
            val views = RemoteViews(context.packageName, R.layout.widget_icon)

            val bitmap = WidgetRenderer.renderIcon(context, config)
            views.setImageViewBitmap(R.id.widget_icon_image_view, bitmap)

            if (config.label.isNotBlank()) {
                views.setTextViewText(R.id.widget_icon_label, config.label)
                views.setViewVisibility(R.id.widget_icon_label, View.VISIBLE)
            } else {
                views.setViewVisibility(R.id.widget_icon_label, View.GONE)
            }

            val launchIntent = if (config.targetPackageName.isNotBlank()) {
                context.packageManager.getLaunchIntentForPackage(config.targetPackageName)?.apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
            } else null

            val pendingIntent = if (launchIntent != null) {
                PendingIntent.getActivity(
                    context,
                    appWidgetId,
                    launchIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            } else {
                val configIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                    putExtra("edit_preset_id", config.id)
                    putExtra("widget_type", "ICON")
                }
                PendingIntent.getActivity(
                    context,
                    appWidgetId,
                    configIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                )
            }

            views.setOnClickPendingIntent(R.id.widget_icon_container, pendingIntent)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun refreshAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = appWidgetManager.getAppWidgetIds(ComponentName(context, IconWidgetProvider::class.java))
            val storage = WidgetStorage(context)
            for (id in ids) {
                updateWidget(context, appWidgetManager, id, storage)
            }
        }
    }
}
