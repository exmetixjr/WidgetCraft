package com.widgetcraft.app.widget

import android.app.Activity
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.widget.RemoteViews
import android.widget.Toast
import com.widgetcraft.app.R
import com.widgetcraft.app.data.WidgetStorage

object WidgetPinManager {

    const val EXTRA_PRESET_ID = "com.widgetcraft.app.EXTRA_PRESET_ID"
    const val EXTRA_WIDGET_TYPE = "com.widgetcraft.app.EXTRA_WIDGET_TYPE"

    fun requestPinWidget(
        context: Context,
        providerClass: Class<*>,
        presetId: String,
        widgetType: String,
        previewBitmap: Bitmap? = null
    ): Boolean {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val provider = ComponentName(context, providerClass)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && appWidgetManager.isRequestPinAppWidgetSupported) {
            val callbackIntent = Intent(context, WidgetPinReceiver::class.java).apply {
                action = WidgetPinReceiver.ACTION_WIDGET_PINNED
                putExtra(EXTRA_PRESET_ID, presetId)
                putExtra(EXTRA_WIDGET_TYPE, widgetType)
            }

            val successCallback = PendingIntent.getBroadcast(
                context,
                presetId.hashCode(),
                callbackIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )

            val bundle = Bundle()
            if (previewBitmap != null) {
                val layoutRes = when (widgetType) {
                    "CLOCK" -> R.layout.widget_clock
                    "ICON" -> R.layout.widget_icon
                    else -> R.layout.widget_image
                }
                val previewViews = RemoteViews(context.packageName, layoutRes)
                val imgViewId = when (widgetType) {
                    "CLOCK" -> R.id.widget_clock_image_view
                    "ICON" -> R.id.widget_icon_image_view
                    else -> R.id.widget_image_view
                }
                previewViews.setImageViewBitmap(imgViewId, previewBitmap)
                bundle.putParcelable(AppWidgetManager.EXTRA_APPWIDGET_PREVIEW, previewViews)
            }

            appWidgetManager.requestPinAppWidget(provider, bundle, successCallback)
            return true
        } else {
            Toast.makeText(context, "Your launcher does not support direct widget pinning. Please add from home screen widget menu.", Toast.LENGTH_LONG).show()
            return false
        }
    }

    fun completeConfiguration(
        activity: Activity,
        appWidgetId: Int,
        presetId: String,
        widgetType: String
    ) {
        val storage = WidgetStorage(activity)
        storage.bindWidgetIdToPreset(appWidgetId, presetId, widgetType)

        val appWidgetManager = AppWidgetManager.getInstance(activity)
        when (widgetType) {
            "CLOCK" -> ClockWidgetProvider.updateWidget(activity, appWidgetManager, appWidgetId, storage)
            "ICON" -> IconWidgetProvider.updateWidget(activity, appWidgetManager, appWidgetId, storage)
            else -> ImageWidgetProvider.updateWidget(activity, appWidgetManager, appWidgetId, storage)
        }

        val resultValue = Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        activity.setResult(Activity.RESULT_OK, resultValue)
        activity.finish()
    }
}
