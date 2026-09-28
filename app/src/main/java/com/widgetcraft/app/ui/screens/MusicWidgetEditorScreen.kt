package com.widgetcraft.app.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddToHomeScreen
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.widgetcraft.app.data.MusicStyle
import com.widgetcraft.app.data.MusicWidgetConfig
import com.widgetcraft.app.data.WidgetStorage
import com.widgetcraft.app.widget.MusicWidgetProvider
import com.widgetcraft.app.widget.WidgetPinManager
import com.widgetcraft.app.widget.WidgetRenderer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicWidgetEditorScreen(
    storage: WidgetStorage,
    presetId: String?,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val existingConfig = remember {
        presetId?.let { storage.getMusicConfig(it) } ?: MusicWidgetConfig()
    }

    var name by remember { mutableStateOf(existingConfig.name) }
    var selectedStyle by remember { mutableStateOf(existingConfig.style) }
    var trackTitle by remember { mutableStateOf(existingConfig.trackTitle) }
    var artistName by remember { mutableStateOf(existingConfig.artistName) }
    var textColor by remember { mutableStateOf(existingConfig.textColorHex) }
    var accentColor by remember { mutableStateOf(existingConfig.accentColorHex) }
    var backgroundColor by remember { mutableStateOf(existingConfig.backgroundColorHex) }
    var cornerRadius by remember { mutableFloatStateOf(existingConfig.cornerRadiusDp) }

    val currentConfig = remember(name, selectedStyle, trackTitle, artistName, textColor, accentColor, backgroundColor, cornerRadius) {
        existingConfig.copy(
            name = name,
            style = selectedStyle,
            trackTitle = trackTitle,
            artistName = artistName,
            textColorHex = textColor,
            accentColorHex = accentColor,
            backgroundColorHex = backgroundColor,
            cornerRadiusDp = cornerRadius
        )
    }

    fun saveAndSync() {
        storage.saveMusicConfig(currentConfig)
        val appWidgetManager = android.appwidget.AppWidgetManager.getInstance(context)
        val provider = android.content.ComponentName(context, MusicWidgetProvider::class.java)
        val ids = appWidgetManager.getAppWidgetIds(provider)
        for (id in ids) {
            MusicWidgetProvider.updateWidget(context, appWidgetManager, id, storage)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (presetId == null) "New Music Widget" else "Edit Music Widget") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            saveAndSync()
                            onNavigateBack()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Check, null)
                        Spacer(Modifier.width(4.dp))
                        Text("Save")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Live Interactive Preview
            val previewBitmap = remember(currentConfig) {
                WidgetRenderer.renderMusicWidget(context, currentConfig, albumArt = null, targetWidth = 720, targetHeight = 360)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = previewBitmap.asImageBitmap(),
                    contentDescription = "Music Preview",
                    modifier = Modifier.fillMaxWidth().height(180.dp).padding(12.dp)
                )
            }

            // Quick Pin Action
            Button(
                onClick = {
                    saveAndSync()
                    WidgetPinManager.pinMusicWidget(
                        context = context,
                        presetId = currentConfig.id,
                        previewBitmap = previewBitmap
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Icon(Icons.Default.AddToHomeScreen, null)
                Spacer(Modifier.width(8.dp))
                Text("Save & Add to Home Screen")
            }

            // Notification Listener Permission Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Live Now Playing Sync", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            "Required to sync Spotify, YouTube Music, and track progress live.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    FilledTonalButton(
                        onClick = {
                            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                        }
                    ) {
                        Text("Grant", fontSize = 12.sp)
                    }
                }
            }

            // Widget Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Widget Title") },
                modifier = Modifier.fillMaxWidth()
            )

            // Music Style Picker
            Column {
                Text("Player Aesthetic Style", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(MusicStyle.values()) { style ->
                        FilterChip(
                            selected = selectedStyle == style,
                            onClick = { selectedStyle = style },
                            label = { Text(style.name.replace('_', ' ')) }
                        )
                    }
                }
            }

            // Fallback Track Metadata
            OutlinedTextField(
                value = trackTitle,
                onValueChange = { trackTitle = it },
                label = { Text("Default Track Name (Fallback)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = artistName,
                onValueChange = { artistName = it },
                label = { Text("Default Artist Name (Fallback)") },
                modifier = Modifier.fillMaxWidth()
            )

            // Corner Radius Slider
            Column {
                Text("Corner Radius: ${cornerRadius.toInt()} dp")
                Slider(
                    value = cornerRadius,
                    onValueChange = { cornerRadius = it },
                    valueRange = 0f..48f
                )
            }

            // Accent Color Palette
            Column {
                Text("Accent Highlight Color", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                val accents = listOf("#D71921", "#1DB954", "#D0BCFF", "#06B6D4", "#F43F5E", "#EAB308", "#FFFFFF")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    accents.forEach { hex ->
                        val parsed = try { Color(android.graphics.Color.parseColor(hex)) } catch (e: Exception) { Color.Red }
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(parsed)
                                .border(
                                    width = if (accentColor == hex) 3.dp else 1.dp,
                                    color = if (accentColor == hex) MaterialTheme.colorScheme.primary else Color.Gray,
                                    shape = CircleShape
                                )
                                .clickable { accentColor = hex }
                        )
                    }
                }
            }
        }
    }
}
