package com.widgetcraft.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.widget.RemoteViews
import com.widgetcraft.app.MainActivity
import com.widgetcraft.app.R
import com.widgetcraft.app.data.MusicWidgetConfig
import com.widgetcraft.app.data.WidgetStorage

class MusicWidgetProvider : AppWidgetProvider() {

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
        var latestTrackTitle: String? = null
        var latestArtistName: String? = null
        var latestIsPlaying: Boolean = false
        var latestAlbumArt: Bitmap? = null

        fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            storage: WidgetStorage = WidgetStorage(context)
        ) {
            val baseConfig = storage.getMusicConfigForWidgetId(appWidgetId) ?: MusicWidgetConfig()
            val effectiveConfig = baseConfig.copy(
                trackTitle = latestTrackTitle ?: baseConfig.trackTitle,
                artistName = latestArtistName ?: baseConfig.artistName,
                isPlaying = latestIsPlaying
            )

            val views = RemoteViews(context.packageName, R.layout.widget_music)
            val bitmap = WidgetRenderer.renderMusicWidget(context, effectiveConfig, latestAlbumArt)
            views.setImageViewBitmap(R.id.widget_music_image_view, bitmap)

            // Transport Control Pending Intents
            val prevIntent = Intent(context, MusicControlReceiver::class.java).apply {
                action = MusicControlReceiver.ACTION_PREV
            }
            val prevPending = PendingIntent.getBroadcast(
                context, 201, prevIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_music_btn_prev, prevPending)

            val playIntent = Intent(context, MusicControlReceiver::class.java).apply {
                action = MusicControlReceiver.ACTION_PLAY_PAUSE
            }
            val playPending = PendingIntent.getBroadcast(
                context, 202, playIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_music_btn_play_pause, playPending)

            val nextIntent = Intent(context, MusicControlReceiver::class.java).apply {
                action = MusicControlReceiver.ACTION_NEXT
            }
            val nextPending = PendingIntent.getBroadcast(
                context, 203, nextIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_music_btn_next, nextPending)

            // Click container to open app editor
            val editIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                putExtra("edit_preset_id", baseConfig.id)
                putExtra("widget_type", "MUSIC")
            }
            val editPending = PendingIntent.getActivity(
                context, appWidgetId, editIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_music_container, editPending)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun refreshAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = appWidgetManager.getAppWidgetIds(ComponentName(context, MusicWidgetProvider::class.java))
            val storage = WidgetStorage(context)
            for (id in ids) {
                updateWidget(context, appWidgetManager, id, storage)
            }
        }
    }
}
