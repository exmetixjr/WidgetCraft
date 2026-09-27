package com.widgetcraft.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.*
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
import com.widgetcraft.app.data.*
import com.widgetcraft.app.widget.ImageWidgetProvider
import com.widgetcraft.app.widget.WidgetRenderer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageWidgetEditorScreen(
    storage: WidgetStorage,
    widgetId: String?,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val existingConfig = remember {
        widgetId?.let { storage.getImageConfig(it) } ?: ImageWidgetConfig()
    }

    var name by remember { mutableStateOf(existingConfig.name) }
    var imageUris by remember { mutableStateOf(existingConfig.imageUris.toList()) }
    var selectedLayout by remember { mutableStateOf(existingConfig.layout) }
    var selectedShape by remember { mutableStateOf(existingConfig.shape) }
    var cornerRadius by remember { mutableFloatStateOf(existingConfig.cornerRadiusDp) }
    var borderWidth by remember { mutableFloatStateOf(existingConfig.borderWidthDp) }
    var borderColor by remember { mutableStateOf(existingConfig.borderColorHex) }
    var backgroundColor by remember { mutableStateOf(existingConfig.backgroundColorHex) }
    var opacity by remember { mutableFloatStateOf(existingConfig.opacity) }
    var tapAction by remember { mutableStateOf(existingConfig.tapAction) }
    var tapActionTarget by remember { mutableStateOf(existingConfig.tapActionTarget) }

    // Multi-photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        val savedPaths = uris.mapNotNull { storage.copyUriToInternalStorage(it) }
        if (savedPaths.isNotEmpty()) {
            imageUris = imageUris + savedPaths
        }
    }

    val currentConfig = remember(name, imageUris, selectedLayout, selectedShape, cornerRadius, borderWidth, borderColor, backgroundColor, opacity, tapAction, tapActionTarget) {
        existingConfig.copy(
            name = name,
            imageUris = imageUris.toMutableList(),
            layout = selectedLayout,
            shape = selectedShape,
            cornerRadiusDp = cornerRadius,
            borderWidthDp = borderWidth,
            borderColorHex = borderColor,
            backgroundColorHex = backgroundColor,
            opacity = opacity,
            tapAction = tapAction,
            tapActionTarget = tapActionTarget
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (widgetId == null) "New Photo Widget" else "Edit Photo Widget") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            storage.saveImageConfig(currentConfig)
                            ImageWidgetProvider.refreshAllWidgets(context)
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                val previewBitmap = remember(currentConfig) {
                    WidgetRenderer.renderImageWidget(context, currentConfig, targetWidth = 500, targetHeight = 500)
                }
                Image(
                    bitmap = previewBitmap.asImageBitmap(),
                    contentDescription = "Widget Preview",
                    modifier = Modifier.size(200.dp)
                )
            }

            // Widget Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Widget Title") },
                modifier = Modifier.fillMaxWidth()
            )

            // Image Selection
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Selected Photos (${imageUris.size})", fontWeight = FontWeight.Bold)
                        Button(
                            onClick = { photoPickerLauncher.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, null)
                            Spacer(Modifier.width(4.dp))
                            Text("Add Photos")
                        }
                    }

                    if (imageUris.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(imageUris) { path ->
                                Box(modifier = Modifier.size(70.dp)) {
                                    val bmp = remember(path) { storage.loadBitmap(path) }
                                    if (bmp != null) {
                                        Image(
                                            bitmap = bmp.asImageBitmap(),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(RoundedCornerShape(8.dp))
                                        )
                                    }
                                    IconButton(
                                        onClick = { imageUris = imageUris.filter { it != path } },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .size(24.dp)
                                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                    ) {
                                        Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Layout Picker
            Column {
                Text("Collage / Layout Style", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(CollageLayout.values()) { layout ->
                        FilterChip(
                            selected = selectedLayout == layout,
                            onClick = { selectedLayout = layout },
                            label = { Text(layout.name.replace('_', ' ')) }
                        )
                    }
                }
            }

            // Shape Picker
            Column {
                Text("Widget Shape & Mask", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(ShapeType.values()) { shape ->
                        FilterChip(
                            selected = selectedShape == shape,
                            onClick = { selectedShape = shape },
                            label = { Text(shape.name.lowercase().replaceFirstChar { it.uppercase() }) }
                        )
                    }
                }
            }

            // Sliders: Corner Radius, Border Width, Opacity
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (selectedShape == ShapeType.ROUNDED) {
                    Text("Corner Radius: ${cornerRadius.toInt()} dp")
                    Slider(
                        value = cornerRadius,
                        onValueChange = { cornerRadius = it },
                        valueRange = 0f..60f
                    )
                }

                Text("Border Width: ${borderWidth.toInt()} dp")
                Slider(
                    value = borderWidth,
                    onValueChange = { borderWidth = it },
                    valueRange = 0f..16f
                )

                Text("Opacity: ${(opacity * 100).toInt()}%")
                Slider(
                    value = opacity,
                    onValueChange = { opacity = it },
                    valueRange = 0.2f..1.0f
                )
            }

            // Border Color Palette
            Column {
                Text("Border Color", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                val borderColors = listOf("#FFFFFF", "#000000", "#D0BCFF", "#06B6D4", "#F43F5E", "#EAB308", "#10B981")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    borderColors.forEach { hex ->
                        val parsed = try { Color(android.graphics.Color.parseColor(hex)) } catch (e: Exception) { Color.White }
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(parsed)
                                .border(
                                    width = if (borderColor == hex) 3.dp else 1.dp,
                                    color = if (borderColor == hex) MaterialTheme.colorScheme.primary else Color.Gray,
                                    shape = CircleShape
                                )
                                .clickable { borderColor = hex }
                        )
                    }
                }
            }

            // Tap Action Picker
            Column {
                Text("Widget Tap Action", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(TapActionType.values()) { action ->
                        FilterChip(
                            selected = tapAction == action,
                            onClick = { tapAction = action },
                            label = { Text(action.name.replace('_', ' ')) }
                        )
                    }
                }

                if (tapAction == TapActionType.OPEN_URL) {
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tapActionTarget,
                        onValueChange = { tapActionTarget = it },
                        label = { Text("Web URL (e.g. google.com)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else if (tapAction == TapActionType.OPEN_APP) {
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tapActionTarget,
                        onValueChange = { tapActionTarget = it },
                        label = { Text("Package Name (e.g. com.spotify.music)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
