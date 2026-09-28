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
import com.widgetcraft.app.data.NoteStyle
import com.widgetcraft.app.data.NoteWidgetConfig
import com.widgetcraft.app.data.WidgetStorage
import com.widgetcraft.app.widget.NoteWidgetProvider
import com.widgetcraft.app.widget.WidgetPinManager
import com.widgetcraft.app.widget.WidgetRenderer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteWidgetEditorScreen(
    storage: WidgetStorage,
    presetId: String?,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val existingConfig = remember {
        presetId?.let { storage.getNoteConfig(it) } ?: NoteWidgetConfig()
    }

    var name by remember { mutableStateOf(existingConfig.name) }
    var title by remember { mutableStateOf(existingConfig.title) }
    var content by remember { mutableStateOf(existingConfig.content) }
    var selectedStyle by remember { mutableStateOf(existingConfig.style) }
    var isChecklist by remember { mutableStateOf(existingConfig.isChecklist) }
    var showDate by remember { mutableStateOf(existingConfig.showDate) }
    var fontSize by remember { mutableFloatStateOf(existingConfig.fontSizeSp) }
    var cornerRadius by remember { mutableFloatStateOf(existingConfig.cornerRadiusDp) }
    var textColor by remember { mutableStateOf(existingConfig.textColorHex) }
    var backgroundColor by remember { mutableStateOf(existingConfig.backgroundColorHex) }
    var accentColor by remember { mutableStateOf(existingConfig.accentColorHex) }

    fun applyStyleDefaults(style: NoteStyle) {
        selectedStyle = style
        when (style) {
            NoteStyle.STICKY_YELLOW -> {
                backgroundColor = "#FEF08A"
                textColor = "#2D3748"
                accentColor = "#EAB308"
            }
            NoteStyle.OBSIDIAN_DARK -> {
                backgroundColor = "#18181B"
                textColor = "#F4F4F5"
                accentColor = "#A78BFA"
            }
            NoteStyle.CYBER_TERMINAL -> {
                backgroundColor = "#0D1117"
                textColor = "#00FF66"
                accentColor = "#00F0FF"
            }
            NoteStyle.MINT_GREEN -> {
                backgroundColor = "#DCFCE7"
                textColor = "#064E3B"
                accentColor = "#10B981"
            }
            NoteStyle.ROSE_QUARTZ -> {
                backgroundColor = "#FFE4E6"
                textColor = "#881337"
                accentColor = "#F43F5E"
            }
            NoteStyle.LAVENDER_DREAM -> {
                backgroundColor = "#F3E8FF"
                textColor = "#581C87"
                accentColor = "#A855F7"
            }
        }
    }

    val currentConfig = remember(
        name, title, content, selectedStyle, isChecklist, showDate, fontSize,
        cornerRadius, textColor, backgroundColor, accentColor
    ) {
        existingConfig.copy(
            name = name,
            title = title,
            content = content,
            style = selectedStyle,
            isChecklist = isChecklist,
            showDate = showDate,
            fontSizeSp = fontSize,
            cornerRadiusDp = cornerRadius,
            textColorHex = textColor,
            backgroundColorHex = backgroundColor,
            accentColorHex = accentColor
        )
    }

    fun saveAndSync() {
        storage.saveNoteConfig(currentConfig)
        NoteWidgetProvider.refreshAllWidgets(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (presetId == null) "New Note Widget" else "Edit Note Widget") },
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
            // Live Preview
            val previewBitmap = remember(currentConfig) {
                WidgetRenderer.renderNoteWidget(context, currentConfig, targetWidth = 600, targetHeight = 600)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = previewBitmap.asImageBitmap(),
                    contentDescription = "Note Preview",
                    modifier = Modifier
                        .size(240.dp)
                        .padding(8.dp)
                )
            }

            // Quick Pin Action
            Button(
                onClick = {
                    saveAndSync()
                    WidgetPinManager.pinNoteWidget(
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

            // Title Field
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Card Header / Title") },
                modifier = Modifier.fillMaxWidth()
            )

            // Content Editor
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Note & Checklist Content") },
                supportingText = {
                    Text("Tip: Use '[x]' for checked, '[ ]' for unchecked, or '•' for bullet points.")
                },
                minLines = 4,
                maxLines = 8,
                modifier = Modifier.fillMaxWidth()
            )

            // Note Theme Style
            Column {
                Text("Theme & Style", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(NoteStyle.values()) { style ->
                        FilterChip(
                            selected = selectedStyle == style,
                            onClick = { applyStyleDefaults(style) },
                            label = { Text(style.name.replace('_', ' ')) }
                        )
                    }
                }
            }

            // Checkbox / Date Controls
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
                        Text("Checklist Mode (Interactive Checkboxes)")
                        Switch(checked = isChecklist, onCheckedChange = { isChecklist = it })
                    }

                    Divider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Show Date Header")
                        Switch(checked = showDate, onCheckedChange = { showDate = it })
                    }
                }
            }

            // Typography Size Slider
            Column {
                Text("Font Size: ${fontSize.toInt()} sp")
                Slider(
                    value = fontSize,
                    onValueChange = { fontSize = it },
                    valueRange = 11f..24f
                )
            }

            // Corner Radius Slider
            Column {
                Text("Corner Radius: ${cornerRadius.toInt()} dp")
                Slider(
                    value = cornerRadius,
                    onValueChange = { cornerRadius = it },
                    valueRange = 0f..40f
                )
            }

            // Accent Color Palette
            Column {
                Text("Accent Highlight Color", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                val accents = listOf("#EAB308", "#10B981", "#06B6D4", "#F43F5E", "#A855F7", "#F97316", "#FFFFFF")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    accents.forEach { hex ->
                        val parsed = try { Color(android.graphics.Color.parseColor(hex)) } catch (e: Exception) { Color.Yellow }
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
