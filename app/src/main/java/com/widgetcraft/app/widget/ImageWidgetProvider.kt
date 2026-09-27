package com.widgetcraft.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import com.widgetcraft.app.MainActivity
import com.widgetcraft.app.R
import com.widgetcraft.app.data.ImageWidgetConfig
import com.widgetcraft.app.data.TapActionType
import com.widgetcraft.app.data.WidgetStorage

class ImageWidgetProvider : AppWidgetProvider() {

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
        if (intent.action == ACTION_CYCLE_IMAGE) {
            val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                val storage = WidgetStorage(context)
                val config = storage.getImageConfigForWidgetId(appWidgetId)
                if (config != null && config.imageUris.isNotEmpty()) {
                    config.currentImageIndex = (config.currentImageIndex + 1) % config.imageUris.size
                    storage.saveImageConfig(config)
                    val appWidgetManager = AppWidgetManager.getInstance(context)
                    updateWidget(context, appWidgetManager, appWidgetId, storage)
                }
            }
        }
    }

    companion object {
        const val ACTION_CYCLE_IMAGE = "com.widgetcraft.app.ACTION_CYCLE_IMAGE"

        fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            storage: WidgetStorage = WidgetStorage(context)
        ) {
            val config = storage.getImageConfigForWidgetId(appWidgetId) ?: ImageWidgetConfig()
            val views = RemoteViews(context.packageName, R.layout.widget_image)

            val bitmap = WidgetRenderer.renderImageWidget(context, config)
            views.setImageViewBitmap(R.id.widget_image_view, bitmap)

            // Setup Tap action
            val pendingIntent = when (config.tapAction) {
                TapActionType.CYCLE_IMAGES -> {
                    val intent = Intent(context, ImageWidgetProvider::class.java).apply {
                        action = ACTION_CYCLE_IMAGE
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                    }
                    PendingIntent.getBroadcast(
                        context,
                        appWidgetId,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                    )
                }
                TapActionType.OPEN_URL -> {
                    val url = if (config.tapActionTarget.startsWith("http")) config.tapActionTarget else "https://${config.tapActionTarget}"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    PendingIntent.getActivity(
                        context,
                        appWidgetId,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                }
                TapActionType.OPEN_APP -> {
                    val launchIntent = context.packageManager.getLaunchIntentForPackage(config.tapActionTarget)
                    if (launchIntent != null) {
                        launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        PendingIntent.getActivity(
                            context,
                            appWidgetId,
                            launchIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                    } else {
                        getDefaultAppIntent(context, appWidgetId, config.id)
                    }
                }
                TapActionType.OPEN_GALLERY -> {
                    val galleryIntent = Intent(Intent.ACTION_VIEW).apply {
                        type = "image/*"
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    PendingIntent.getActivity(
                        context,
                        appWidgetId,
                        galleryIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                }
                TapActionType.NONE -> {
                    getDefaultAppIntent(context, appWidgetId, config.id)
                }
            }

            views.setOnClickPendingIntent(R.id.widget_image_container, pendingIntent)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun getDefaultAppIntent(context: Context, appWidgetId: Int, presetId: String): PendingIntent {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                putExtra("edit_preset_id", presetId)
                putExtra("widget_type", "IMAGE")
            }
            return PendingIntent.getActivity(
                context,
                appWidgetId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
        }

        fun refreshAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = appWidgetManager.getAppWidgetIds(ComponentName(context, ImageWidgetProvider::class.java))
            val storage = WidgetStorage(context)
            for (id in ids) {
                updateWidget(context, appWidgetManager, id, storage)
            }
        }
    }
}
