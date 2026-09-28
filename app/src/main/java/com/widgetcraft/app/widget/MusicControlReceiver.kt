package com.widgetcraft.app.widget

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState

class MusicControlReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val mediaSessionManager = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager
        val component = ComponentName(context, MediaWidgetListenerService::class.java)

        val controllers = try {
            mediaSessionManager?.getActiveSessions(component)
        } catch (e: SecurityException) {
            emptyList()
        }

        val controller = controllers?.firstOrNull()

        if (controller != null) {
            val controls = controller.transportControls
            val state = controller.playbackState?.state

            when (action) {
                ACTION_PLAY_PAUSE -> {
                    if (state == PlaybackState.STATE_PLAYING) {
                        controls.pause()
                    } else {
                        controls.play()
                    }
                }
                ACTION_NEXT -> controls.skipToNext()
                ACTION_PREV -> controls.skipToPrevious()
            }
        } else {
            // No active session: fallback to launching music intent or toggling demo state
            if (action == ACTION_PLAY_PAUSE) {
                val launchIntent = context.packageManager.getLaunchIntentForPackage("com.spotify.music")
                    ?: context.packageManager.getLaunchIntentForPackage("com.google.android.apps.youtube.music")
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                }
            }
        }

        // Trigger widget update
        MusicWidgetProvider.refreshAllWidgets(context)
    }

    companion object {
        const val ACTION_PLAY_PAUSE = "com.widgetcraft.app.ACTION_MUSIC_PLAY_PAUSE"
        const val ACTION_NEXT = "com.widgetcraft.app.ACTION_MUSIC_NEXT"
        const val ACTION_PREV = "com.widgetcraft.app.ACTION_MUSIC_PREV"
    }
}
