package com.widgetcraft.app.widget

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.widgetcraft.app.data.WidgetStorage

class NoteChecklistReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_TOGGLE_CHECKLIST) {
            val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            val itemIndex = intent.getIntExtra(EXTRA_ITEM_INDEX, -1)

            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID && itemIndex >= 0) {
                val storage = WidgetStorage(context)
                val config = storage.getNoteConfigForWidgetId(appWidgetId)
                if (config != null) {
                    val lines = config.content.lines().toMutableList()
                    var checklistItemCounter = 0
                    for (i in lines.indices) {
                        val line = lines[i].trim()
                        val isItem = line.startsWith("[x]", ignoreCase = true) || line.startsWith("[ ]") || line.startsWith("•") || line.startsWith("-")
                        if (isItem) {
                            if (checklistItemCounter == itemIndex) {
                                lines[i] = if (line.startsWith("[x]", ignoreCase = true)) {
                                    "[ ] " + line.substring(3).trim()
                                } else if (line.startsWith("[ ]")) {
                                    "[x] " + line.substring(3).trim()
                                } else if (line.startsWith("•") || line.startsWith("-")) {
                                    "[x] " + line.substring(1).trim()
                                } else {
                                    line
                                }
                                break
                            }
                            checklistItemCounter++
                        }
                    }
                    config.content = lines.joinToString("\n")
                    storage.saveNoteConfig(config)

                    val appWidgetManager = AppWidgetManager.getInstance(context)
                    NoteWidgetProvider.updateWidget(context, appWidgetManager, appWidgetId, storage)
                }
            }
        }
    }

    companion object {
        const val ACTION_TOGGLE_CHECKLIST = "com.widgetcraft.app.ACTION_TOGGLE_CHECKLIST"
        const val EXTRA_ITEM_INDEX = "com.widgetcraft.app.EXTRA_ITEM_INDEX"
    }
}
