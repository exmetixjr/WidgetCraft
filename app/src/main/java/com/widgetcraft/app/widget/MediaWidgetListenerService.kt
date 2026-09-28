package com.widgetcraft.app.widget

import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.service.notification.NotificationListenerService

class MediaWidgetListenerService : NotificationListenerService() {

    private var mediaSessionManager: MediaSessionManager? = null
    private var activeController: MediaController? = null

    private val sessionListener = MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
        updateActiveController(controllers?.firstOrNull())
    }

    private val mediaCallback = object : MediaController.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadata?) {
            updateFromMetadata(metadata)
            MusicWidgetProvider.refreshAllWidgets(this@MediaWidgetListenerService)
        }

        override fun onPlaybackStateChanged(state: PlaybackState?) {
            MusicWidgetProvider.latestIsPlaying = (state?.state == PlaybackState.STATE_PLAYING)
            MusicWidgetProvider.refreshAllWidgets(this@MediaWidgetListenerService)
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        isNotificationListenerEnabled = true
        mediaSessionManager = getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager
        val component = ComponentName(this, MediaWidgetListenerService::class.java)
        mediaSessionManager?.addOnActiveSessionsChangedListener(sessionListener, component)

        val controllers = try {
            mediaSessionManager?.getActiveSessions(component)
        } catch (e: SecurityException) {
            emptyList()
        }
        updateActiveController(controllers?.firstOrNull())
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        isNotificationListenerEnabled = false
        activeController?.unregisterCallback(mediaCallback)
        activeController = null
    }

    private fun updateActiveController(newController: MediaController?) {
        activeController?.unregisterCallback(mediaCallback)
        activeController = newController
        activeController?.registerCallback(mediaCallback)

        updateFromMetadata(activeController?.metadata)
        val state = activeController?.playbackState
        MusicWidgetProvider.latestIsPlaying = (state?.state == PlaybackState.STATE_PLAYING)
        MusicWidgetProvider.refreshAllWidgets(this)
    }

    private fun updateFromMetadata(metadata: MediaMetadata?) {
        if (metadata != null) {
            val title = metadata.getString(MediaMetadata.METADATA_KEY_TITLE)
                ?: metadata.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE)
                ?: "Now Playing"
            val artist = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST)
                ?: metadata.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)
                ?: ""

            val rawArt = metadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
                ?: metadata.getBitmap(MediaMetadata.METADATA_KEY_ART)

            val scaledArt = if (rawArt != null) {
                val maxDim = 256
                val scale = minOf(maxDim.toFloat() / rawArt.width, maxDim.toFloat() / rawArt.height, 1.0f)
                val w = (rawArt.width * scale).toInt().coerceAtLeast(1)
                val h = (rawArt.height * scale).toInt().coerceAtLeast(1)
                Bitmap.createScaledBitmap(rawArt, w, h, true)
            } else {
                null
            }

            MusicWidgetProvider.latestTrackTitle = title
            MusicWidgetProvider.latestArtistName = artist
            MusicWidgetProvider.latestAlbumArt = scaledArt
        }
    }

    companion object {
        var isNotificationListenerEnabled = false
    }
}
