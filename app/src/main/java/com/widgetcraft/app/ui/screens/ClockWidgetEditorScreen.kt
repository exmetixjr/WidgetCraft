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
import com.widgetcraft.app.data.ClockStyle
import com.widgetcraft.app.data.ClockWidgetConfig
import com.widgetcraft.app.data.WidgetStorage
import com.widgetcraft.app.widget.ClockWidgetProvider
import com.widgetcraft.app.widget.WidgetRenderer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClockWidgetEditorScreen(
    storage: WidgetStorage,
    widgetId: String?,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val existingConfig = remember {
        widgetId?.let { storage.getClockConfig(it) } ?: ClockWidgetConfig()
    }

    var name by remember { mutableStateOf(existingConfig.name) }
    var selectedStyle by remember { mutableStateOf(existingConfig.style) }
    var is24Hour by remember { mutableStateOf(existingConfig.is24Hour) }
    var showDate by remember { mutableStateOf(existingConfig.showDate) }
    var showBattery by remember { mutableStateOf(existingConfig.showBattery) }
    var textColor by remember { mutableStateOf(existingConfig.textColorHex) }
    var accentColor by remember { mutableStateOf(existingConfig.accentColorHex) }
    var backgroundColor by remember { mutableStateOf(existingConfig.backgroundColorHex) }
    var cornerRadius by remember { mutableFloatStateOf(existingConfig.cornerRadiusDp) }

    val currentConfig = remember(name, selectedStyle, is24Hour, showDate, showBattery, textColor, accentColor, backgroundColor, cornerRadius) {
        existingConfig.copy(
            name = name,
            style = selectedStyle,
            is24Hour = is24Hour,
            showDate = showDate,
            showBattery = showBattery,
            textColorHex = textColor,
            accentColorHex = accentColor,
            backgroundColorHex = backgroundColor,
            cornerRadiusDp = cornerRadius
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (widgetId == null) "New Clock Widget" else "Edit Clock Widget") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            storage.saveClockConfig(currentConfig)
                            ClockWidgetProvider.refreshAllWidgets(context)
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
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                val previewBitmap = remember(currentConfig) {
                    WidgetRenderer.renderClockWidget(context, currentConfig, targetWidth = 700, targetHeight = 350)
                }
                Image(
                    bitmap = previewBitmap.asImageBitmap(),
                    contentDescription = "Clock Preview",
                    modifier = Modifier.fillMaxWidth().height(180.dp).padding(12.dp)
                )
            }

            // Widget Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Widget Title") },
                modifier = Modifier.fillMaxWidth()
            )

            // Style Picker
            Column {
                Text("Typography & Clock Style", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(ClockStyle.values()) { style ->
                        FilterChip(
                            selected = selectedStyle == style,
                            onClick = { selectedStyle = style },
                            label = { Text(style.name.replace('_', ' ')) }
                        )
                    }
                }
            }

            // Switches: 24h, Date, Battery
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
                        Text("24-Hour Time Format")
                        Switch(checked = is24Hour, onCheckedChange = { is24Hour = it })
                    }

                    Divider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Show Date")
                        Switch(checked = showDate, onCheckedChange = { showDate = it })
                    }

                    Divider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Show Battery Status")
                        Switch(checked = showBattery, onCheckedChange = { showBattery = it })
                    }
                }
            }

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
                val accents = listOf("#D0BCFF", "#06B6D4", "#F43F5E", "#10B981", "#EAB308", "#FF8A65", "#FFFFFF")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    accents.forEach { hex ->
                        val parsed = try { Color(android.graphics.Color.parseColor(hex)) } catch (e: Exception) { Color.Cyan }
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

            // Background Color Palette
            Column {
                Text("Widget Background", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                val bgColors = listOf("#1E1E1E", "#000000", "#121A24", "#1F122B", "#1C281F", "#2A1F18")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    bgColors.forEach { hex ->
                        val parsed = try { Color(android.graphics.Color.parseColor(hex)) } catch (e: Exception) { Color.DarkGray }
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(parsed)
                                .border(
                                    width = if (backgroundColor == hex) 3.dp else 1.dp,
                                    color = if (backgroundColor == hex) MaterialTheme.colorScheme.primary else Color.Gray,
                                    shape = CircleShape
                                )
                                .clickable { backgroundColor = hex }
                        )
                    }
                }
            }
        }
    }
}
