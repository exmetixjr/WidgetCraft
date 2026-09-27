package com.widgetcraft.app.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.widgetcraft.app.widget.IconWidgetProvider
import com.widgetcraft.app.widget.WidgetPinManager
import com.widgetcraft.app.widget.WidgetRenderer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconChangerScreen(
    storage: WidgetStorage,
    presetId: String?,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val appHelper = remember { InstalledAppHelper(context) }
    val installedApps = remember { appHelper.getInstalledLaunchableApps() }

    val existingConfig = remember {
        presetId?.let { storage.getIconConfig(it) } ?: IconWidgetConfig()
    }

    var selectedApp by remember {
        mutableStateOf(installedApps.find { it.packageName == existingConfig.targetPackageName } ?: installedApps.firstOrNull())
    }
    var customLabel by remember { mutableStateOf(existingConfig.label.ifBlank { selectedApp?.appName ?: "" }) }
    var customIconUri by remember { mutableStateOf(existingConfig.iconImageUri) }
    var selectedShape by remember { mutableStateOf(existingConfig.iconShape) }
    var selectedPresetStyle by remember { mutableStateOf(existingConfig.presetStyle) }
    var cornerRadius by remember { mutableFloatStateOf(existingConfig.cornerRadiusDp) }
    var showAppPickerSheet by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val iconPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedPath = storage.copyUriToInternalStorage(uri)
            if (savedPath != null) {
                customIconUri = savedPath
            }
        }
    }

    val currentConfig = remember(selectedApp, customLabel, customIconUri, selectedShape, selectedPresetStyle, cornerRadius) {
        existingConfig.copy(
            targetPackageName = selectedApp?.packageName ?: "",
            targetActivityName = selectedApp?.activityName ?: "",
            label = customLabel,
            iconImageUri = customIconUri,
            iconShape = selectedShape,
            presetStyle = selectedPresetStyle,
            cornerRadiusDp = cornerRadius
        )
    }

    fun saveAndSync() {
        storage.saveIconConfig(currentConfig)
        IconWidgetProvider.refreshAllWidgets(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (presetId == null) "Custom App Icon" else "Edit App Icon") },
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
            // Live Icon Preview
            val previewBitmap = remember(currentConfig) {
                WidgetRenderer.renderIcon(context, currentConfig, targetSize = 200)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        bitmap = previewBitmap.asImageBitmap(),
                        contentDescription = "Icon Preview",
                        modifier = Modifier.size(80.dp)
                    )
                    if (customLabel.isNotBlank()) {
                        Spacer(Modifier.height(6.dp))
                        Text(customLabel, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                    }
                }
            }

            // Quick Pin Micro-Widget Action
            Button(
                onClick = {
                    saveAndSync()
                    WidgetPinManager.requestPinWidget(
                        context = context,
                        providerClass = IconWidgetProvider::class.java,
                        presetId = currentConfig.id,
                        widgetType = "ICON",
                        previewBitmap = previewBitmap
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.AddToHomeScreen, null)
                Spacer(Modifier.width(8.dp))
                Text("Pin Micro-Widget (No Launcher Badge)")
            }

            // Native Shortcut Fallback Action
            OutlinedButton(
                onClick = {
                    saveAndSync()
                    val iconBmp = WidgetRenderer.renderIcon(context, currentConfig, targetSize = 192)
                    val success = appHelper.createPinnedShortcut(
                        targetPackageName = currentConfig.targetPackageName,
                        targetActivityName = currentConfig.targetActivityName,
                        label = currentConfig.label,
                        iconBitmap = iconBmp
                    )
                    if (success) {
                        Toast.makeText(context, "Shortcut requested on your launcher!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Pinned shortcuts not supported on this launcher. Use the micro-widget instead.", Toast.LENGTH_LONG).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Shortcut, null)
                Spacer(Modifier.width(8.dp))
                Text("Pin as Native Shortcut")
            }

            // Target App Selector
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showAppPickerSheet = true },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Apps, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Target Application", style = MaterialTheme.typography.labelSmall)
                        Text(
                            text = selectedApp?.appName ?: "Tap to choose app",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        if (selectedApp != null) {
                            Text(selectedApp!!.packageName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                }
            }

            // Icon Aesthetic Preset Styles
            Column {
                Text("Aesthetic Preset Style", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(IconPresetStyle.values()) { style ->
                        FilterChip(
                            selected = selectedPresetStyle == style,
                            onClick = { selectedPresetStyle = style },
                            label = { Text(style.name.replace('_', ' ')) }
                        )
                    }
                }
            }

            // Custom Icon Image Selector
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Custom Icon Graphic", fontWeight = FontWeight.Bold)
                        Text(
                            if (customIconUri != null) "Custom Image Selected" else "Using Default App Icon",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row {
                        if (customIconUri != null) {
                            TextButton(onClick = { customIconUri = null }) {
                                Text("Reset")
                            }
                        }
                        Button(
                            onClick = { iconPickerLauncher.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(Icons.Default.Image, null)
                            Spacer(Modifier.width(4.dp))
                            Text("Pick Image")
                        }
                    }
                }
            }

            // Label Customization
            OutlinedTextField(
                value = customLabel,
                onValueChange = { customLabel = it },
                label = { Text("Home Screen Label (leave empty for no text)") },
                modifier = Modifier.fillMaxWidth()
            )

            // Shape Selector
            Column {
                Text("Icon Mask Shape", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf(ShapeType.SQUIRCLE, ShapeType.ROUNDED, ShapeType.CIRCLE, ShapeType.RECTANGLE)) { shape ->
                        FilterChip(
                            selected = selectedShape == shape,
                            onClick = { selectedShape = shape },
                            label = { Text(shape.name.lowercase().replaceFirstChar { it.uppercase() }) }
                        )
                    }
                }
            }
        }
    }

    // App Picker Bottom Sheet
    if (showAppPickerSheet) {
        ModalBottomSheet(onDismissRequest = { showAppPickerSheet = false }) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text("Select Application", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search apps...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                val filtered = installedApps.filter {
                    it.appName.contains(searchQuery, ignoreCase = true) || it.packageName.contains(searchQuery, ignoreCase = true)
                }
                LazyColumn(modifier = Modifier.height(400.dp)) {
                    items(filtered) { app ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedApp = app
                                    if (customLabel.isBlank() || customLabel == selectedApp?.appName) {
                                        customLabel = app.appName
                                    }
                                    showAppPickerSheet = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(app.appName, fontWeight = FontWeight.Medium)
                                Text(app.packageName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Divider()
                    }
                }
            }
        }
    }
}
