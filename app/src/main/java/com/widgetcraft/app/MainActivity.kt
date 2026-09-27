package com.widgetcraft.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.widgetcraft.app.data.WidgetStorage
import com.widgetcraft.app.ui.screens.*
import com.widgetcraft.app.ui.theme.WidgetCraftTheme

sealed class Screen {
    object Home : Screen()
    data class EditImageWidget(val widgetId: String?) : Screen()
    data class EditClockWidget(val widgetId: String?) : Screen()
    data class EditIconWidget(val widgetId: String?) : Screen()
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val storage = WidgetStorage(this)

        setContent {
            WidgetCraftTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

                    // Check if launched from a widget tap to configure
                    LaunchedEffect(intent) {
                        val editImgWidgetId = intent.getIntExtra("edit_image_widget_id", -1)
                        if (editImgWidgetId != -1) {
                            val config = storage.getImageConfigForWidgetId(editImgWidgetId)
                            currentScreen = Screen.EditImageWidget(config?.id)
                        }
                    }

                    when (val screen = currentScreen) {
                        is Screen.Home -> {
                            HomeScreen(
                                storage = storage,
                                onNavigateToImageEditor = { id -> currentScreen = Screen.EditImageWidget(id) },
                                onNavigateToClockEditor = { id -> currentScreen = Screen.EditClockWidget(id) },
                                onNavigateToIconChanger = { id -> currentScreen = Screen.EditIconWidget(id) }
                            )
                        }
                        is Screen.EditImageWidget -> {
                            ImageWidgetEditorScreen(
                                storage = storage,
                                widgetId = screen.widgetId,
                                onNavigateBack = { currentScreen = Screen.Home }
                            )
                        }
                        is Screen.EditClockWidget -> {
                            ClockWidgetEditorScreen(
                                storage = storage,
                                widgetId = screen.widgetId,
                                onNavigateBack = { currentScreen = Screen.Home }
                            )
                        }
                        is Screen.EditIconWidget -> {
                            IconChangerScreen(
                                storage = storage,
                                widgetId = screen.widgetId,
                                onNavigateBack = { currentScreen = Screen.Home }
                            )
                        }
                    }
                }
            }
        }
    }
}
