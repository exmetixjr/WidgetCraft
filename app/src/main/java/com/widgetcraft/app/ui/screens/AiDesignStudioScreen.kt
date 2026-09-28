package com.widgetcraft.app.ui.screens

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddToHomeScreen
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Edit
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
import com.widgetcraft.app.widget.ClockWidgetProvider
import com.widgetcraft.app.widget.NoteWidgetProvider
import com.widgetcraft.app.widget.WidgetPinManager
import com.widgetcraft.app.widget.WidgetRenderer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiDesignStudioScreen(
    storage: WidgetStorage,
    onNavigateBack: () -> Unit,
    onEditClock: (String) -> Unit,
    onEditNote: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var prompt by remember { mutableStateOf("") }
    var apiKey by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    var generatedClockConfig by remember { mutableStateOf<ClockWidgetConfig?>(null) }
    var generatedNoteConfig by remember { mutableStateOf<NoteWidgetConfig?>(null) }

    val suggestionPrompts = listOf(
        "Tokyo Cyberpunk clock with RAM and storage monitors",
        "Obsidian Gold daily engineering priorities checklist",
        "Swiss Minimalist typography clock with battery gauge",
        "Daily Stoic contemplation note in rose quartz theme",
        "Hacker terminal clock with green phosphor glow"
    )

    fun synthesizeWidgetLocally(userPrompt: String) {
        val lower = userPrompt.lowercase()
        if (lower.contains("note") || lower.contains("check") || lower.contains("task") || lower.contains("todo") || lower.contains("stoic") || lower.contains("quote")) {
            val style = when {
                lower.contains("cyber") || lower.contains("terminal") || lower.contains("hacker") -> NoteStyle.CYBER_TERMINAL
                lower.contains("gold") || lower.contains("obsidian") || lower.contains("dark") -> NoteStyle.OBSIDIAN_DARK
                lower.contains("mint") || lower.contains("sage") || lower.contains("green") -> NoteStyle.MINT_GREEN
                lower.contains("rose") || lower.contains("pink") -> NoteStyle.ROSE_QUARTZ
                lower.contains("lavender") || lower.contains("purple") -> NoteStyle.LAVENDER_DREAM
                else -> NoteStyle.STICKY_YELLOW
            }
            val title = when {
                lower.contains("stoic") -> "Daily Stoic"
                lower.contains("priority") || lower.contains("priorities") -> "Top Priorities"
                lower.contains("task") || lower.contains("todo") -> "Action Items"
                else -> "AI Generated Note"
            }
            val content = when {
                lower.contains("stoic") -> "• 'You have power over your mind - not outside events.'\n• Focus on what is within control.\n• Practice gratitude today."
                else -> "[ ] Finish primary feature sprint\n[ ] Review system telemetry\n[x] Initialize WidgetCraft v2"
            }
            val config = NoteWidgetConfig(
                name = title,
                title = title,
                content = content,
                style = style,
                isChecklist = true,
                showDate = true,
                backgroundColorHex = when (style) {
                    NoteStyle.CYBER_TERMINAL -> "#0D1117"
                    NoteStyle.OBSIDIAN_DARK -> "#18181B"
                    NoteStyle.MINT_GREEN -> "#DCFCE7"
                    NoteStyle.ROSE_QUARTZ -> "#FFE4E6"
                    NoteStyle.LAVENDER_DREAM -> "#F3E8FF"
                    NoteStyle.STICKY_YELLOW -> "#FEF08A"
                },
                accentColorHex = when (style) {
                    NoteStyle.CYBER_TERMINAL -> "#00F0FF"
                    NoteStyle.OBSIDIAN_DARK -> "#D97706"
                    NoteStyle.MINT_GREEN -> "#10B981"
                    NoteStyle.ROSE_QUARTZ -> "#F43F5E"
                    NoteStyle.LAVENDER_DREAM -> "#A855F7"
                    NoteStyle.STICKY_YELLOW -> "#EAB308"
                },
                textColorHex = when (style) {
                    NoteStyle.CYBER_TERMINAL -> "#00FF66"
                    NoteStyle.OBSIDIAN_DARK -> "#F4F4F5"
                    NoteStyle.MINT_GREEN -> "#064E3B"
                    NoteStyle.ROSE_QUARTZ -> "#881337"
                    NoteStyle.LAVENDER_DREAM -> "#581C87"
                    NoteStyle.STICKY_YELLOW -> "#2D3748"
                }
            )
            generatedNoteConfig = config
            generatedClockConfig = null
        } else {
            val style = when {
                lower.contains("cyber") || lower.contains("hacker") || lower.contains("terminal") -> ClockStyle.TERMINAL
                lower.contains("vogue") || lower.contains("editorial") -> ClockStyle.BOLD_EDITORIAL
                lower.contains("seven") || lower.contains("digital") -> ClockStyle.DIGITAL_SEVEN_SEGMENT
                lower.contains("analog") || lower.contains("classic") -> ClockStyle.ANALOG_CLASSIC
                else -> ClockStyle.MINIMAL
            }
            val accent = when {
                lower.contains("cyan") || lower.contains("neon") -> "#00F0FF"
                lower.contains("gold") -> "#F59E0B"
                lower.contains("rose") || lower.contains("pink") -> "#F43F5E"
                lower.contains("green") -> "#10B981"
                else -> "#D0BCFF"
            }
            val config = ClockWidgetConfig(
                name = "AI Clock",
                style = style,
                showBattery = true,
                showStorage = lower.contains("storage") || lower.contains("disk"),
                showRam = lower.contains("ram") || lower.contains("memory"),
                showWeather = lower.contains("weather"),
                accentColorHex = accent,
                backgroundColorHex = if (lower.contains("light") || lower.contains("white")) "#F8FAFC" else "#121212",
                textColorHex = if (lower.contains("light") || lower.contains("white")) "#0F172A" else "#FFFFFF"
            )
            generatedClockConfig = config
            generatedNoteConfig = null
        }
    }

    suspend fun callLlmApi(promptText: String, key: String) {
        withContext(Dispatchers.IO) {
            val endpoint = if (key.startsWith("gsk_")) {
                "https://api.groq.com/openai/v1/chat/completions"
            } else {
                "https://api.groq.com/openai/v1/chat/completions"
            }
            val modelName = if (key.startsWith("gsk_")) "llama-3.3-70b-versatile" else "llama-3.3-70b-versatile"

            val url = URL(endpoint)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Authorization", "Bearer $key")
            conn.doOutput = true
            conn.connectTimeout = 15000
            conn.readTimeout = 20000

            val sysMsg = "You are WidgetCraft AI. Output ONLY a valid JSON object matching either ClockWidgetConfig (type: 'CLOCK', name, style, showBattery, showStorage, showRam, showWeather, textColorHex, accentColorHex, backgroundColorHex) or NoteWidgetConfig (type: 'NOTE', name, title, content, style, isChecklist, showDate, textColorHex, backgroundColorHex, accentColorHex)."

            val body = JSONObject().apply {
                put("model", modelName)
                put("messages", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", sysMsg)
                    })
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", promptText)
                    })
                })
                put("temperature", 0.7)
            }

            OutputStreamWriter(conn.outputStream).use { it.write(body.toString()) }

            if (conn.responseCode in 200..299) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val respStr = reader.readText()
                val json = JSONObject(respStr)
                val rawContent = json.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content")

                val jsonStart = rawContent.indexOf("{")
                val jsonEnd = rawContent.lastIndexOf("}")
                if (jsonStart >= 0 && jsonEnd > jsonStart) {
                    val cleanJson = JSONObject(rawContent.substring(jsonStart, jsonEnd + 1))
                    val type = cleanJson.optString("type", "CLOCK").uppercase()
                    withContext(Dispatchers.Main) {
                        if (type == "NOTE") {
                            val config = NoteWidgetConfig(
                                name = cleanJson.optString("name", "AI Note"),
                                title = cleanJson.optString("title", "Generated Note"),
                                content = cleanJson.optString("content", "• AI generated task item"),
                                style = try { NoteStyle.valueOf(cleanJson.optString("style", "STICKY_YELLOW")) } catch (e: Exception) { NoteStyle.STICKY_YELLOW },
                                isChecklist = cleanJson.optBoolean("isChecklist", true),
                                showDate = cleanJson.optBoolean("showDate", true),
                                textColorHex = cleanJson.optString("textColorHex", "#2D3748"),
                                backgroundColorHex = cleanJson.optString("backgroundColorHex", "#FEF08A"),
                                accentColorHex = cleanJson.optString("accentColorHex", "#EAB308")
                            )
                            generatedNoteConfig = config
                            generatedClockConfig = null
                        } else {
                            val config = ClockWidgetConfig(
                                name = cleanJson.optString("name", "AI Clock"),
                                style = try { ClockStyle.valueOf(cleanJson.optString("style", "BOLD_EDITORIAL")) } catch (e: Exception) { ClockStyle.BOLD_EDITORIAL },
                                showBattery = cleanJson.optBoolean("showBattery", true),
                                showStorage = cleanJson.optBoolean("showStorage", false),
                                showRam = cleanJson.optBoolean("showRam", false),
                                showWeather = cleanJson.optBoolean("showWeather", false),
                                textColorHex = cleanJson.optString("textColorHex", "#FFFFFF"),
                                accentColorHex = cleanJson.optString("accentColorHex", "#D0BCFF"),
                                backgroundColorHex = cleanJson.optString("backgroundColorHex", "#1E1E1E")
                            )
                            generatedClockConfig = config
                            generatedNoteConfig = null
                        }
                        statusMessage = "AI widget designed successfully!"
                    }
                }
            } else {
                throw Exception("HTTP ${conn.responseCode}")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Design Studio") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Generate custom widgets with natural language. Powered by LLM synthesis with instant offline fallback.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Prompt Input
            OutlinedTextField(
                value = prompt,
                onValueChange = { prompt = it },
                label = { Text("Describe your dream widget") },
                placeholder = { Text("e.g. Cyberpunk clock with RAM meter and cyan glow") },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth()
            )

            // Suggestion Chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(suggestionPrompts) { sug ->
                    SuggestionChip(
                        onClick = { prompt = sug },
                        label = { Text(sug, maxLines = 1) }
                    )
                }
            }

            // Optional API Key
            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it },
                label = { Text("Groq / LLM API Key (Optional)") },
                placeholder = { Text("Leave empty for instant offline AI synthesis") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Generate Button
            Button(
                onClick = {
                    if (prompt.isBlank()) return@Button
                    isGenerating = true
                    statusMessage = null
                    scope.launch {
                        try {
                            if (apiKey.isNotBlank()) {
                                callLlmApi(prompt, apiKey.trim())
                            } else {
                                synthesizeWidgetLocally(prompt)
                                statusMessage = "Designed via Smart Synthesis Engine!"
                            }
                        } catch (e: Exception) {
                            synthesizeWidgetLocally(prompt)
                            statusMessage = "Synthesized via local fallback (${e.message})"
                        } finally {
                            isGenerating = false
                        }
                    }
                },
                enabled = prompt.isNotBlank() && !isGenerating,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Designing Widget...")
                } else {
                    Icon(Icons.Default.AutoAwesome, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Generate Widget")
                }
            }

            statusMessage?.let { msg ->
                Text(msg, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }

            // Generated Preview Area
            generatedClockConfig?.let { clock ->
                val previewBitmap = remember(clock) {
                    WidgetRenderer.renderClockWidget(context, clock, 700, 350)
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Generated Clock: ${clock.name}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surface),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                bitmap = previewBitmap.asImageBitmap(),
                                contentDescription = "Clock Preview",
                                modifier = Modifier.fillMaxWidth().height(140.dp).padding(8.dp)
                            )
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    storage.saveClockConfig(clock)
                                    ClockWidgetProvider.refreshAllWidgets(context)
                                    WidgetPinManager.requestPinWidget(
                                        context = context,
                                        providerClass = ClockWidgetProvider::class.java,
                                        presetId = clock.id,
                                        widgetType = "CLOCK",
                                        previewBitmap = previewBitmap
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.AddToHomeScreen, null)
                                Spacer(Modifier.width(6.dp))
                                Text("Pin to Home")
                            }

                            OutlinedButton(
                                onClick = {
                                    storage.saveClockConfig(clock)
                                    onEditClock(clock.id)
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Edit, null)
                                Spacer(Modifier.width(6.dp))
                                Text("Customize")
                            }
                        }
                    }
                }
            }

            generatedNoteConfig?.let { note ->
                val previewBitmap = remember(note) {
                    WidgetRenderer.renderNoteWidget(context, note, 600, 600)
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Generated Note: ${note.name}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surface),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                bitmap = previewBitmap.asImageBitmap(),
                                contentDescription = "Note Preview",
                                modifier = Modifier.size(200.dp).padding(8.dp)
                            )
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    storage.saveNoteConfig(note)
                                    NoteWidgetProvider.refreshAllWidgets(context)
                                    WidgetPinManager.pinNoteWidget(
                                        context = context,
                                        presetId = note.id,
                                        previewBitmap = previewBitmap
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.AddToHomeScreen, null)
                                Spacer(Modifier.width(6.dp))
                                Text("Pin to Home")
                            }

                            OutlinedButton(
                                onClick = {
                                    storage.saveNoteConfig(note)
                                    onEditNote(note.id)
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Edit, null)
                                Spacer(Modifier.width(6.dp))
                                Text("Customize")
                            }
                        }
                    }
                }
            }
        }
    }
}
