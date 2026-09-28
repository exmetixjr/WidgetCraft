package com.widgetcraft.app.ui.screens

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
import androidx.compose.material.icons.filled.TouchApp
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
import com.widgetcraft.app.data.BentoStyle
import com.widgetcraft.app.data.BentoWidgetConfig
import com.widgetcraft.app.data.WidgetStorage
import com.widgetcraft.app.widget.BentoWidgetProvider
import com.widgetcraft.app.widget.WidgetPinManager
import com.widgetcraft.app.widget.WidgetRenderer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BentoWidgetEditorScreen(
    storage: WidgetStorage,
    presetId: String?,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val existingConfig = remember {
        presetId?.let { storage.getBentoConfig(it) } ?: BentoWidgetConfig()
    }

    var name by remember { mutableStateOf(existingConfig.name) }
    var selectedStyle by remember { mutableStateOf(existingConfig.style) }
    var showClock by remember { mutableStateOf(existingConfig.showClock) }
    var showWeather by remember { mutableStateOf(existingConfig.showWeather) }
    var showBattery by remember { mutableStateOf(existingConfig.showBattery) }
    var showRam by remember { mutableStateOf(existingConfig.showRam) }
    var showSteps by remember { mutableStateOf(existingConfig.showSteps) }
    var showMusicSnippet by remember { mutableStateOf(existingConfig.showMusicSnippet) }
    var accentColor by remember { mutableStateOf(existingConfig.accentColorHex) }
    var backgroundColor by remember { mutableStateOf(existingConfig.backgroundColorHex) }
    var cornerRadius by remember { mutableFloatStateOf(existingConfig.cornerRadiusDp) }

    val currentConfig = remember(name, selectedStyle, showClock, showWeather, showBattery, showRam, showSteps, showMusicSnippet, accentColor, backgroundColor, cornerRadius) {
        existingConfig.copy(
            name = name,
            style = selectedStyle,
            showClock = showClock,
            showWeather = showWeather,
            showBattery = showBattery,
            showRam = showRam,
            showSteps = showSteps,
            showMusicSnippet = showMusicSnippet,
            accentColorHex = accentColor,
            backgroundColorHex = backgroundColor,
            cornerRadiusDp = cornerRadius
        )
    }

    fun saveAndSync() {
        storage.saveBentoConfig(currentConfig)
        val appWidgetManager = android.appwidget.AppWidgetManager.getInstance(context)
        val provider = android.content.ComponentName(context, BentoWidgetProvider::class.java)
        val ids = appWidgetManager.getAppWidgetIds(provider)
        for (id in ids) {
            BentoWidgetProvider.updateWidget(context, appWidgetManager, id, storage)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (presetId == null) "New Bento Dashboard" else "Edit Bento Dashboard") },
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
                WidgetRenderer.renderBentoWidget(context, currentConfig, targetWidth = 900, targetHeight = 480)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = previewBitmap.asImageBitmap(),
                    contentDescription = "Bento Preview",
                    modifier = Modifier.fillMaxWidth().height(190.dp).padding(8.dp)
                )
            }

            // Quick Pin Action
            Button(
                onClick = {
                    saveAndSync()
                    WidgetPinManager.pinBentoWidget(
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

            // Hotspot Explainer Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.TouchApp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Bento widgets feature 4 independent tap hotspots: Clock opens Alarm, Weather opens Weather info, Steps opens Health, and Battery opens System stats.",
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Title field
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Widget Title") },
                modifier = Modifier.fillMaxWidth()
            )

            // Bento Style Picker
            Column {
                Text("Bento Layout & Theme", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(BentoStyle.values()) { style ->
                        FilterChip(
                            selected = selectedStyle == style,
                            onClick = { selectedStyle = style },
                            label = { Text(style.name.replace('_', ' ')) }
                        )
                    }
                }
            }

            // Quadrant & Module Toggles
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Show Big Clock Quadrant")
                        Switch(checked = showClock, onCheckedChange = { showClock = it })
                    }

                    Divider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Show Live Weather & Forecast")
                        Switch(checked = showWeather, onCheckedChange = { showWeather = it })
                    }

                    Divider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Show Hardware Step Counter (Pedometer)")
                        Switch(checked = showSteps, onCheckedChange = { showSteps = it })
                    }

                    Divider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Show Battery & RAM Metrics")
                        Switch(checked = showBattery, onCheckedChange = { showBattery = it; showRam = it })
                    }
                }
            }

            // Corner Radius Slider
            Column {
                Text("Corner Radius: ${cornerRadius.toInt()} dp")
                Slider(
                    value = cornerRadius,
                    onValueChange = { cornerRadius = it },
                    valueRange = 8f..48f
                )
            }

            // Accent Highlight Color
            Column {
                Text("Accent Highlight Color", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                val accents = listOf("#D71921", "#D0BCFF", "#06B6D4", "#F43F5E", "#10B981", "#EAB308", "#FFFFFF")
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
