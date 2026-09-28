package com.widgetcraft.app

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.widgetcraft.app.data.WidgetStorage
import com.widgetcraft.app.ui.screens.*
import com.widgetcraft.app.ui.theme.WidgetCraftTheme
import com.widgetcraft.app.widget.WidgetPinManager

sealed class Screen {
    object Home : Screen()
    data class EditImageWidget(val presetId: String?) : Screen()
    data class EditClockWidget(val presetId: String?) : Screen()
    data class EditNoteWidget(val presetId: String?) : Screen()
    data class EditIconWidget(val presetId: String?) : Screen()
    object AiStudio : Screen()
    data class ConfigurePicker(val appWidgetId: Int) : Screen()
}

class MainActivity : ComponentActivity() {

    private var configureAppWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val storage = WidgetStorage(this)

        // Check if launched for widget configuration from system launcher
        val intentExtras = intent.extras
        if (intentExtras != null) {
            configureAppWidgetId = intentExtras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        // Set default CANCELED result per Android documentation
        if (configureAppWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            val resultValue = Intent().apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, configureAppWidgetId)
            }
            setResult(Activity.RESULT_CANCELED, resultValue)
        }

        val directEditPresetId = intent.getStringExtra("edit_preset_id")
        val directWidgetType = intent.getStringExtra("widget_type")

        setContent {
            WidgetCraftTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var currentScreen by remember {
                        mutableStateOf<Screen>(
                            when {
                                directEditPresetId != null -> {
                                    when (directWidgetType) {
                                        "CLOCK" -> Screen.EditClockWidget(directEditPresetId)
                                        "NOTE" -> Screen.EditNoteWidget(directEditPresetId)
                                        "ICON" -> Screen.EditIconWidget(directEditPresetId)
                                        else -> Screen.EditImageWidget(directEditPresetId)
                                    }
                                }
                                configureAppWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID -> {
                                    Screen.ConfigurePicker(configureAppWidgetId)
                                }
                                else -> Screen.Home
                            }
                        )
                    }

                    when (val screen = currentScreen) {
                        is Screen.Home -> {
                            HomeScreen(
                                storage = storage,
                                onNavigateToImageEditor = { id -> currentScreen = Screen.EditImageWidget(id) },
                                onNavigateToClockEditor = { id -> currentScreen = Screen.EditClockWidget(id) },
                                onNavigateToNoteEditor = { id -> currentScreen = Screen.EditNoteWidget(id) },
                                onNavigateToIconChanger = { id -> currentScreen = Screen.EditIconWidget(id) },
                                onNavigateToAiStudio = { currentScreen = Screen.AiStudio }
                            )
                        }
                        is Screen.AiStudio -> {
                            AiDesignStudioScreen(
                                storage = storage,
                                onNavigateBack = { currentScreen = Screen.Home },
                                onEditClock = { id -> currentScreen = Screen.EditClockWidget(id) },
                                onEditNote = { id -> currentScreen = Screen.EditNoteWidget(id) }
                            )
                        }
                        is Screen.ConfigurePicker -> {
                            ConfigurePickerScreen(
                                storage = storage,
                                appWidgetId = screen.appWidgetId,
                                onSelectPreset = { presetId, type ->
                                    WidgetPinManager.completeConfiguration(
                                        this@MainActivity,
                                        screen.appWidgetId,
                                        presetId,
                                        type
                                    )
                                },
                                onCreateNewImage = { currentScreen = Screen.EditImageWidget(null) },
                                onCreateNewClock = { currentScreen = Screen.EditClockWidget(null) },
                                onCreateNewNote = { currentScreen = Screen.EditNoteWidget(null) },
                                onCreateNewIcon = { currentScreen = Screen.EditIconWidget(null) },
                                onCancel = { finish() }
                            )
                        }
                        is Screen.EditImageWidget -> {
                            ImageWidgetEditorScreen(
                                storage = storage,
                                presetId = screen.presetId,
                                onNavigateBack = {
                                    if (configureAppWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                                        currentScreen = Screen.ConfigurePicker(configureAppWidgetId)
                                    } else {
                                        currentScreen = Screen.Home
                                    }
                                }
                            )
                        }
                        is Screen.EditClockWidget -> {
                            ClockWidgetEditorScreen(
                                storage = storage,
                                presetId = screen.presetId,
                                onNavigateBack = {
                                    if (configureAppWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                                        currentScreen = Screen.ConfigurePicker(configureAppWidgetId)
                                    } else {
                                        currentScreen = Screen.Home
                                    }
                                }
                            )
                        }
                        is Screen.EditNoteWidget -> {
                            NoteWidgetEditorScreen(
                                storage = storage,
                                presetId = screen.presetId,
                                onNavigateBack = {
                                    if (configureAppWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                                        currentScreen = Screen.ConfigurePicker(configureAppWidgetId)
                                    } else {
                                        currentScreen = Screen.Home
                                    }
                                }
                            )
                        }
                        is Screen.EditIconWidget -> {
                            IconChangerScreen(
                                storage = storage,
                                presetId = screen.presetId,
                                onNavigateBack = {
                                    if (configureAppWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                                        currentScreen = Screen.ConfigurePicker(configureAppWidgetId)
                                    } else {
                                        currentScreen = Screen.Home
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
