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
import com.widgetcraft.app.widget.WidgetPinManager
import com.widgetcraft.app.widget.WidgetRenderer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageWidgetEditorScreen(
    storage: WidgetStorage,
    presetId: String?,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val existingConfig = remember {
        presetId?.let { storage.getImageConfig(it) } ?: ImageWidgetConfig()
    }

    var name by remember { mutableStateOf(existingConfig.name) }
    var imageUris by remember { mutableStateOf(existingConfig.imageUris.toList()) }
    var selectedLayout by remember { mutableStateOf(existingConfig.layout) }
    var selectedShape by remember { mutableStateOf(existingConfig.shape) }
    var selectedFilter by remember { mutableStateOf(existingConfig.filter) }
    var selectedScaleType by remember { mutableStateOf(existingConfig.scaleType) }
    var cornerRadius by remember { mutableFloatStateOf(existingConfig.cornerRadiusDp) }
    var borderWidth by remember { mutableFloatStateOf(existingConfig.borderWidthDp) }
    var borderColor by remember { mutableStateOf(existingConfig.borderColorHex) }
    var backgroundColor by remember { mutableStateOf(existingConfig.backgroundColorHex) }
    var opacity by remember { mutableFloatStateOf(existingConfig.opacity) }
    var captionText by remember { mutableStateOf(existingConfig.captionText) }
    var showDateTag by remember { mutableStateOf(existingConfig.showDateTag) }
    var tapAction by remember { mutableStateOf(existingConfig.tapAction) }
    var tapActionTarget by remember { mutableStateOf(existingConfig.tapActionTarget) }
    var autoSlideMinutes by remember { mutableIntStateOf(existingConfig.autoSlideMinutes) }
    var enableTouchSlide by remember { mutableStateOf(existingConfig.enableTouchSlide) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        val savedPaths = uris.mapNotNull { storage.copyUriToInternalStorage(it) }
        if (savedPaths.isNotEmpty()) {
            imageUris = imageUris + savedPaths
        }
    }

    val currentConfig = remember(name, imageUris, selectedLayout, selectedShape, selectedFilter, selectedScaleType, cornerRadius, borderWidth, borderColor, backgroundColor, opacity, captionText, showDateTag, tapAction, tapActionTarget, autoSlideMinutes, enableTouchSlide) {
        existingConfig.copy(
            name = name,
            imageUris = imageUris.toMutableList(),
            layout = selectedLayout,
            shape = selectedShape,
            filter = selectedFilter,
            scaleType = selectedScaleType,
            cornerRadiusDp = cornerRadius,
            borderWidthDp = borderWidth,
            borderColorHex = borderColor,
            backgroundColorHex = backgroundColor,
            opacity = opacity,
            captionText = captionText,
            showDateTag = showDateTag,
            tapAction = tapAction,
            tapActionTarget = tapActionTarget,
            autoSlideMinutes = autoSlideMinutes,
            enableTouchSlide = enableTouchSlide
        )
    }

    fun saveAndSync() {
        storage.saveImageConfig(currentConfig)
        ImageWidgetProvider.refreshAllWidgets(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (presetId == null) "New Photo Widget" else "Edit Photo Widget") },
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
                WidgetRenderer.renderImageWidget(context, currentConfig, targetWidth = 500, targetHeight = 500)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = previewBitmap.asImageBitmap(),
                    contentDescription = "Widget Preview",
                    modifier = Modifier.size(200.dp)
                )
            }

            // Quick Pin Action
            Button(
                onClick = {
                    saveAndSync()
                    WidgetPinManager.requestPinWidget(
                        context = context,
                        providerClass = ImageWidgetProvider::class.java,
                        presetId = currentConfig.id,
                        widgetType = "IMAGE",
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

            // Widget Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Widget Title") },
                modifier = Modifier.fillMaxWidth()
            )

            // Photos Selector
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
                        Text("Photos (${imageUris.size})", fontWeight = FontWeight.Bold)
                        Button(
                            onClick = { photoPickerLauncher.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, null)
                            Spacer(Modifier.width(4.dp))
                            Text("Pick Photos")
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
                Text("Collage / Layout", fontWeight = FontWeight.Bold, fontSize = 16.sp)
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

            // Filters
            Column {
                Text("Photo Filter & Aesthetic", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(ImageFilterType.values()) { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            label = { Text(filter.name.lowercase().replaceFirstChar { it.uppercase() }) }
                        )
                    }
                }
            }

            // Shape Picker
            Column {
                Text("Shape & Mask", fontWeight = FontWeight.Bold, fontSize = 16.sp)
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

            // Caption Overlay
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Text Overlay & Captions", fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = captionText,
                        onValueChange = { captionText = it },
                        label = { Text("Caption Text (e.g. Memories, Quote)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Show Date Badge Overlay")
                        Switch(checked = showDateTag, onCheckedChange = { showDateTag = it })
                    }
                }
            }

            // Sliders: Radius, Border, Opacity
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
            }

            // Photo Slideshow & Cycle Controls
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Slideshow & Touch Navigation", fontWeight = FontWeight.Bold)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Tap Widget to Slide Next Photo")
                        Switch(checked = enableTouchSlide, onCheckedChange = { enableTouchSlide = it })
                    }

                    Spacer(Modifier.height(4.dp))
                    Text("Auto-Slide Timer Interval", style = MaterialTheme.typography.labelMedium)
                    val intervals = listOf(0 to "Off", 15 to "15 min", 60 to "1 hour", 360 to "6 hours", 1440 to "Daily")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(intervals) { (minutes, label) ->
                            FilterChip(
                                selected = autoSlideMinutes == minutes,
                                onClick = { autoSlideMinutes = minutes },
                                label = { Text(label) }
                            )
                        }
                    }
                }
            }
        }
    }
}
