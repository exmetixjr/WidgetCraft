package com.widgetcraft.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.widgetcraft.app.data.*
import com.widgetcraft.app.widget.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    storage: WidgetStorage,
    onNavigateToImageEditor: (String?) -> Unit,
    onNavigateToClockEditor: (String?) -> Unit,
    onNavigateToNoteEditor: (String?) -> Unit,
    onNavigateToIconChanger: (String?) -> Unit,
    onNavigateToMusicEditor: (String?) -> Unit,
    onNavigateToBentoEditor: (String?) -> Unit,
    onNavigateToAiStudio: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Photo", "Clock", "Notes", "Icons", "Music", "Bento")
    val context = LocalContext.current

    var imageWidgets by remember { mutableStateOf(storage.getAllImageConfigs()) }
    var clockWidgets by remember { mutableStateOf(storage.getAllClockConfigs()) }
    var noteWidgets by remember { mutableStateOf(storage.getAllNoteConfigs()) }
    var iconWidgets by remember { mutableStateOf(storage.getAllIconConfigs()) }
    var musicWidgets by remember { mutableStateOf(storage.getAllMusicConfigs()) }
    var bentoWidgets by remember { mutableStateOf(storage.getAllBentoConfigs()) }

    var showBackupDialog by remember { mutableStateOf(false) }

    fun refreshData() {
        imageWidgets = storage.getAllImageConfigs()
        clockWidgets = storage.getAllClockConfigs()
        noteWidgets = storage.getAllNoteConfigs()
        iconWidgets = storage.getAllIconConfigs()
        musicWidgets = storage.getAllMusicConfigs()
        bentoWidgets = storage.getAllBentoConfigs()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Widgets,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text("WidgetCraft", fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToAiStudio) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = "AI Design Studio",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { showBackupDialog = true }) {
                        Icon(
                            Icons.Default.SettingsBackupRestore,
                            contentDescription = "Backup & Restore"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    when (selectedTab) {
                        0 -> onNavigateToImageEditor(null)
                        1 -> onNavigateToClockEditor(null)
                        2 -> onNavigateToNoteEditor(null)
                        3 -> onNavigateToIconChanger(null)
                        4 -> onNavigateToMusicEditor(null)
                        5 -> onNavigateToBentoEditor(null)
                    }
                },
                icon = { Icon(Icons.Default.Add, null) },
                text = {
                    Text(
                        when (selectedTab) {
                            0 -> "New Photo Widget"
                            1 -> "New Clock Widget"
                            2 -> "New Note Widget"
                            3 -> "New Custom Icon"
                            4 -> "New Music Widget"
                            5 -> "New Bento Widget"
                            else -> "New Widget"
                        }
                    )
                },
                containerColor = MaterialTheme.colorScheme.primary
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                edgePadding = 12.dp
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            when (selectedTab) {
                0 -> PhotoWidgetsList(
                    context = context,
                    storage = storage,
                    widgets = imageWidgets,
                    onEdit = onNavigateToImageEditor,
                    onDelete = { id ->
                        storage.deleteImageConfig(id)
                        ImageWidgetProvider.refreshAllWidgets(context)
                        refreshData()
                    }
                )
                1 -> ClockWidgetsList(
                    context = context,
                    storage = storage,
                    widgets = clockWidgets,
                    onEdit = onNavigateToClockEditor,
                    onDelete = { id ->
                        storage.deleteClockConfig(id)
                        ClockWidgetProvider.refreshAllWidgets(context)
                        refreshData()
                    }
                )
                2 -> NoteWidgetsList(
                    context = context,
                    storage = storage,
                    widgets = noteWidgets,
                    onEdit = onNavigateToNoteEditor,
                    onDelete = { id ->
                        storage.deleteNoteConfig(id)
                        NoteWidgetProvider.refreshAllWidgets(context)
                        refreshData()
                    }
                )
                3 -> IconWidgetsList(
                    context = context,
                    storage = storage,
                    widgets = iconWidgets,
                    onEdit = onNavigateToIconChanger,
                    onDelete = { id ->
                        storage.deleteIconConfig(id)
                        IconWidgetProvider.refreshAllWidgets(context)
                        refreshData()
                    }
                )
                4 -> MusicWidgetsList(
                    context = context,
                    storage = storage,
                    widgets = musicWidgets,
                    onEdit = onNavigateToMusicEditor,
                    onDelete = { id ->
                        storage.deleteMusicConfig(id)
                        refreshData()
                    }
                )
                5 -> BentoWidgetsList(
                    context = context,
                    storage = storage,
                    widgets = bentoWidgets,
                    onEdit = onNavigateToBentoEditor,
                    onDelete = { id ->
                        storage.deleteBentoConfig(id)
                        refreshData()
                    }
                )
            }
        }
    }

    if (showBackupDialog) {
        BackupRestoreDialog(
            storage = storage,
            onDismiss = { showBackupDialog = false },
            onRestored = {
                refreshData()
                showBackupDialog = false
            }
        )
    }
}

@Composable
fun PhotoWidgetsList(
    context: Context,
    storage: WidgetStorage,
    widgets: List<ImageWidgetConfig>,
    onEdit: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    if (widgets.isEmpty()) {
        EmptyStateView(
            title = "No Photo Widgets Created",
            subtitle = "Tap '+ New Photo Widget' to build custom shaped photo collages, polaroids, and borders."
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(widgets, key = { it.id }) { widget ->
                val boundCount = remember(widget) { storage.getWidgetIdsForPreset(widget.id).size }
                val bitmap = remember(widget) {
                    WidgetRenderer.renderImageWidget(context, widget, targetWidth = 400, targetHeight = 400)
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(widget.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                if (boundCount > 0) {
                                    Text(
                                        "Active on Home Screen ($boundCount)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Row {
                                IconButton(onClick = { onEdit(widget.id) }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                                }
                                IconButton(onClick = { onDelete(widget.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier.size(140.dp)
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        Button(
                            onClick = {
                                WidgetPinManager.requestPinWidget(
                                    context = context,
                                    providerClass = ImageWidgetProvider::class.java,
                                    presetId = widget.id,
                                    widgetType = "IMAGE",
                                    previewBitmap = bitmap
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.AddToHomeScreen, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Add to Home Screen")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ClockWidgetsList(
    context: Context,
    storage: WidgetStorage,
    widgets: List<ClockWidgetConfig>,
    onEdit: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    if (widgets.isEmpty()) {
        EmptyStateView(
            title = "No Clock Widgets Created",
            subtitle = "Tap '+ New Clock Widget' to customize digital, editorial, terminal, or analog clocks."
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(widgets, key = { it.id }) { widget ->
                val boundCount = remember(widget) { storage.getWidgetIdsForPreset(widget.id).size }
                val bitmap = remember(widget) {
                    WidgetRenderer.renderClockWidget(context, widget, targetWidth = 600, targetHeight = 300)
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(widget.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                if (boundCount > 0) {
                                    Text(
                                        "Active on Home Screen ($boundCount)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Row {
                                IconButton(onClick = { onEdit(widget.id) }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                                }
                                IconButton(onClick = { onDelete(widget.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxWidth().height(120.dp).padding(6.dp)
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        Button(
                            onClick = {
                                WidgetPinManager.requestPinWidget(
                                    context = context,
                                    providerClass = ClockWidgetProvider::class.java,
                                    presetId = widget.id,
                                    widgetType = "CLOCK",
                                    previewBitmap = bitmap
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.AddToHomeScreen, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Add to Home Screen")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NoteWidgetsList(
    context: Context,
    storage: WidgetStorage,
    widgets: List<NoteWidgetConfig>,
    onEdit: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    if (widgets.isEmpty()) {
        EmptyStateView(
            title = "No Sticky Notes or Checklists Created",
            subtitle = "Tap '+ New Note Widget' to create customizable sticky notes, to-dos, and daily quotes."
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(widgets, key = { it.id }) { widget ->
                val boundCount = remember(widget) { storage.getWidgetIdsForPreset(widget.id).size }
                val bitmap = remember(widget) {
                    WidgetRenderer.renderNoteWidget(context, widget, targetWidth = 500, targetHeight = 500)
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(widget.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                if (boundCount > 0) {
                                    Text(
                                        "Active on Home Screen ($boundCount)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Row {
                                IconButton(onClick = { onEdit(widget.id) }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                                }
                                IconButton(onClick = { onDelete(widget.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier.size(150.dp).padding(6.dp)
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        Button(
                            onClick = {
                                WidgetPinManager.pinNoteWidget(
                                    context = context,
                                    presetId = widget.id,
                                    previewBitmap = bitmap
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.AddToHomeScreen, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Add to Home Screen")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IconWidgetsList(
    context: Context,
    storage: WidgetStorage,
    widgets: List<IconWidgetConfig>,
    onEdit: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    if (widgets.isEmpty()) {
        EmptyStateView(
            title = "No Custom Icons Created",
            subtitle = "Tap '+ New Custom Icon' to replace any app's home screen icon with zero ads and zero shortcut badges."
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(widgets, key = { it.id }) { widget ->
                val boundCount = remember(widget) { storage.getWidgetIdsForPreset(widget.id).size }
                val bitmap = remember(widget) {
                    WidgetRenderer.renderIcon(context, widget, targetSize = 160)
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )

                            Spacer(Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = widget.label.ifBlank { "Untitled Icon" },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = widget.targetPackageName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (boundCount > 0) {
                                    Text(
                                        "Placed on Home Screen ($boundCount)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            IconButton(onClick = { onEdit(widget.id) }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit")
                            }
                            IconButton(onClick = { onDelete(widget.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        Button(
                            onClick = {
                                WidgetPinManager.requestPinWidget(
                                    context = context,
                                    providerClass = IconWidgetProvider::class.java,
                                    presetId = widget.id,
                                    widgetType = "ICON",
                                    previewBitmap = bitmap
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.AddToHomeScreen, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Add Micro-Widget (No Badge)")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BackupRestoreDialog(
    storage: WidgetStorage,
    onDismiss: () -> Unit,
    onRestored: () -> Unit
) {
    val context = LocalContext.current
    var importText by remember { mutableStateOf("") }
    var exportSuccess by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Backup & Restore Presets") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Export all your custom widgets and app icons to a JSON backup, or restore from a previously exported backup.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Button(
                    onClick = {
                        val json = storage.exportBackupJson()
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("WidgetCraft Backup", json))
                        exportSuccess = true
                        Toast.makeText(context, "Backup copied to clipboard!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.ContentCopy, null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (exportSuccess) "Copied to Clipboard!" else "Export & Copy JSON")
                }

                Divider()

                OutlinedTextField(
                    value = importText,
                    onValueChange = { importText = it },
                    label = { Text("Paste JSON to Restore") },
                    placeholder = { Text("Paste exported JSON configuration here") },
                    minLines = 3,
                    maxLines = 6,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (importText.isNotBlank()) {
                        val success = storage.importBackupJson(importText.trim())
                        if (success) {
                            Toast.makeText(context, "Presets successfully restored!", Toast.LENGTH_SHORT).show()
                            onRestored()
                        } else {
                            Toast.makeText(context, "Invalid backup JSON format", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                enabled = importText.isNotBlank()
            ) {
                Text("Restore Presets")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun EmptyStateView(title: String, subtitle: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.DashboardCustomize,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )
            Spacer(Modifier.height(16.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun MusicWidgetsList(
    context: Context,
    storage: WidgetStorage,
    widgets: List<MusicWidgetConfig>,
    onEdit: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    if (widgets.isEmpty()) {
        EmptyStateView(
            title = "No Music Widgets Created",
            subtitle = "Tap '+ New Music Widget' to add a live Spotify / YouTube Music player with transport controls."
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(widgets, key = { it.id }) { widget ->
                val boundCount = remember(widget) { storage.getWidgetIdsForPreset(widget.id).size }
                val bitmap = remember(widget) {
                    WidgetRenderer.renderMusicWidget(context, widget, albumArt = null, targetWidth = 720, targetHeight = 360)
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(widget.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text(
                                    "${widget.style.name.replace('_', ' ')} • ${widget.artistName} - ${widget.trackTitle}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (boundCount > 0) {
                                    Text(
                                        "Active on Home Screen ($boundCount)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Row {
                                IconButton(onClick = { onEdit(widget.id) }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                                }
                                IconButton(onClick = { onDelete(widget.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxWidth().height(150.dp).padding(6.dp)
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        Button(
                            onClick = {
                                WidgetPinManager.pinMusicWidget(
                                    context = context,
                                    presetId = widget.id,
                                    previewBitmap = bitmap
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.AddToHomeScreen, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Pin Music Player to Home Screen")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BentoWidgetsList(
    context: Context,
    storage: WidgetStorage,
    widgets: List<BentoWidgetConfig>,
    onEdit: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    if (widgets.isEmpty()) {
        EmptyStateView(
            title = "No Bento Dashboards Created",
            subtitle = "Tap '+ New Bento Widget' to create a unified command center with Weather, Clock, Pedometer, and Battery."
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(widgets, key = { it.id }) { widget ->
                val boundCount = remember(widget) { storage.getWidgetIdsForPreset(widget.id).size }
                val bitmap = remember(widget) {
                    WidgetRenderer.renderBentoWidget(context, widget, targetWidth = 900, targetHeight = 480)
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(widget.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text(
                                    "${widget.style.name.replace('_', ' ')} • 4 Tap Hotspots Active",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (boundCount > 0) {
                                    Text(
                                        "Active on Home Screen ($boundCount)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Row {
                                IconButton(onClick = { onEdit(widget.id) }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                                }
                                IconButton(onClick = { onDelete(widget.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(170.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxWidth().height(160.dp).padding(6.dp)
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        Button(
                            onClick = {
                                WidgetPinManager.pinBentoWidget(
                                    context = context,
                                    presetId = widget.id,
                                    previewBitmap = bitmap
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.AddToHomeScreen, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Pin Bento Dashboard to Home Screen")
                        }
                    }
                }
            }
        }
    }
}

