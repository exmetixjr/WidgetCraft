package com.widgetcraft.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.widgetcraft.app.data.WidgetStorage
import com.widgetcraft.app.widget.WidgetRenderer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigurePickerScreen(
    storage: WidgetStorage,
    appWidgetId: Int,
    onSelectPreset: (presetId: String, widgetType: String) -> Unit,
    onCreateNewImage: () -> Unit,
    onCreateNewClock: () -> Unit,
    onCreateNewNote: () -> Unit,
    onCreateNewIcon: () -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Photos", "Clocks", "Notes", "Icons")

    val imagePresets = remember { storage.getAllImageConfigs() }
    val clockPresets = remember { storage.getAllClockConfigs() }
    val notePresets = remember { storage.getAllNoteConfigs() }
    val iconPresets = remember { storage.getAllIconConfigs() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Choose Widget Design", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            when (selectedTab) {
                0 -> {
                    if (imagePresets.isEmpty()) {
                        EmptyPresetView(
                            message = "No photo presets found. Create one now!",
                            actionText = "Create Photo Widget",
                            onAction = onCreateNewImage
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(imagePresets) { preset ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onSelectPreset(preset.id, "IMAGE") },
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val bmp = remember(preset) {
                                            WidgetRenderer.renderImageWidget(context, preset, 160, 160)
                                        }
                                        Image(
                                            bitmap = bmp.asImageBitmap(),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(54.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                        )
                                        Spacer(Modifier.width(16.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(preset.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                            Text(
                                                "Tap to apply this design",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    if (clockPresets.isEmpty()) {
                        EmptyPresetView(
                            message = "No clock presets found. Create one now!",
                            actionText = "Create Clock Widget",
                            onAction = onCreateNewClock
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(clockPresets) { preset ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onSelectPreset(preset.id, "CLOCK") },
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val bmp = remember(preset) {
                                            WidgetRenderer.renderClockWidget(context, preset, 240, 120)
                                        }
                                        Image(
                                            bitmap = bmp.asImageBitmap(),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(80.dp, 44.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                        )
                                        Spacer(Modifier.width(16.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(preset.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                            Text(
                                                "Tap to apply this design",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    if (notePresets.isEmpty()) {
                        EmptyPresetView(
                            message = "No note presets found. Create one now!",
                            actionText = "Create Note Widget",
                            onAction = onCreateNewNote
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(notePresets) { preset ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onSelectPreset(preset.id, "NOTE") },
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val bmp = remember(preset) {
                                            WidgetRenderer.renderNoteWidget(context, preset, 160, 160)
                                        }
                                        Image(
                                            bitmap = bmp.asImageBitmap(),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(54.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                        )
                                        Spacer(Modifier.width(16.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(preset.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                            Text(
                                                "Tap to apply this design",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                3 -> {
                    if (iconPresets.isEmpty()) {
                        EmptyPresetView(
                            message = "No custom icon presets found. Create one now!",
                            actionText = "Create App Icon",
                            onAction = onCreateNewIcon
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(iconPresets) { preset ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onSelectPreset(preset.id, "ICON") },
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val bmp = remember(preset) {
                                            WidgetRenderer.renderIcon(context, preset, 120)
                                        }
                                        Image(
                                            bitmap = bmp.asImageBitmap(),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(50.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                        )
                                        Spacer(Modifier.width(16.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(preset.label.ifBlank { preset.targetPackageName }, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                            Text(
                                                "Tap to apply this icon",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyPresetView(message: String, actionText: String, onAction: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(message, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Spacer(Modifier.height(16.dp))
            Button(onClick = onAction) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(6.dp))
                Text(actionText)
            }
        }
    }
}
