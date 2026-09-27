package com.widgetcraft.app.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class WidgetStorage(private val context: Context) {
    private val prefs = context.getSharedPreferences("widget_craft_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val imagesDir: File by lazy {
        File(context.filesDir, "widget_images").apply { if (!exists()) mkdirs() }
    }

    // --- Instance to Preset Bindings ---

    fun getAllBindings(): List<WidgetInstanceBinding> {
        val json = prefs.getString("widget_instance_bindings", null) ?: return emptyList()
        val type = object : TypeToken<List<WidgetInstanceBinding>>() {}.type
        return try {
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun bindWidgetIdToPreset(appWidgetId: Int, presetId: String, widgetType: String) {
        val list = getAllBindings().filter { it.appWidgetId != appWidgetId }.toMutableList()
        list.add(WidgetInstanceBinding(appWidgetId, presetId, widgetType))
        prefs.edit().putString("widget_instance_bindings", gson.toJson(list)).apply()
    }

    fun getBindingForWidgetId(appWidgetId: Int): WidgetInstanceBinding? {
        return getAllBindings().find { it.appWidgetId == appWidgetId }
    }

    fun getWidgetIdsForPreset(presetId: String): List<Int> {
        return getAllBindings().filter { it.presetId == presetId }.map { it.appWidgetId }
    }

    fun removeBinding(appWidgetId: Int) {
        val list = getAllBindings().filter { it.appWidgetId != appWidgetId }
        prefs.edit().putString("widget_instance_bindings", gson.toJson(list)).apply()
    }

    // --- Image Widgets Presets ---

    fun getAllImageConfigs(): List<ImageWidgetConfig> {
        val json = prefs.getString("image_widgets_list", null) ?: return emptyList()
        val type = object : TypeToken<List<ImageWidgetConfig>>() {}.type
        return try {
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getImageConfig(id: String): ImageWidgetConfig? {
        return getAllImageConfigs().find { it.id == id }
    }

    fun getImageConfigForWidgetId(appWidgetId: Int): ImageWidgetConfig? {
        val binding = getBindingForWidgetId(appWidgetId)
        if (binding != null) {
            val config = getImageConfig(binding.presetId)
            if (config != null) return config
        }
        return getAllImageConfigs().firstOrNull() ?: ImageWidgetConfig()
    }

    fun saveImageConfig(config: ImageWidgetConfig) {
        val list = getAllImageConfigs().toMutableList()
        val index = list.indexOfFirst { it.id == config.id }
        if (index >= 0) {
            list[index] = config
        } else {
            list.add(config)
        }
        prefs.edit().putString("image_widgets_list", gson.toJson(list)).apply()
    }

    fun deleteImageConfig(id: String) {
        val list = getAllImageConfigs().toMutableList()
        list.removeAll { it.id == id }
        prefs.edit().putString("image_widgets_list", gson.toJson(list)).apply()
    }

    // --- Clock Widgets Presets ---

    fun getAllClockConfigs(): List<ClockWidgetConfig> {
        val json = prefs.getString("clock_widgets_list", null) ?: return emptyList()
        val type = object : TypeToken<List<ClockWidgetConfig>>() {}.type
        return try {
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getClockConfig(id: String): ClockWidgetConfig? {
        return getAllClockConfigs().find { it.id == id }
    }

    fun getClockConfigForWidgetId(appWidgetId: Int): ClockWidgetConfig? {
        val binding = getBindingForWidgetId(appWidgetId)
        if (binding != null) {
            val config = getClockConfig(binding.presetId)
            if (config != null) return config
        }
        return getAllClockConfigs().firstOrNull() ?: ClockWidgetConfig()
    }

    fun saveClockConfig(config: ClockWidgetConfig) {
        val list = getAllClockConfigs().toMutableList()
        val index = list.indexOfFirst { it.id == config.id }
        if (index >= 0) {
            list[index] = config
        } else {
            list.add(config)
        }
        prefs.edit().putString("clock_widgets_list", gson.toJson(list)).apply()
    }

    fun deleteClockConfig(id: String) {
        val list = getAllClockConfigs().toMutableList()
        list.removeAll { it.id == id }
        prefs.edit().putString("clock_widgets_list", gson.toJson(list)).apply()
    }

    // --- Icon Widgets Presets ---

    fun getAllIconConfigs(): List<IconWidgetConfig> {
        val json = prefs.getString("icon_widgets_list", null) ?: return emptyList()
        val type = object : TypeToken<List<IconWidgetConfig>>() {}.type
        return try {
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getIconConfig(id: String): IconWidgetConfig? {
        return getAllIconConfigs().find { it.id == id }
    }

    fun getIconConfigForWidgetId(appWidgetId: Int): IconWidgetConfig? {
        val binding = getBindingForWidgetId(appWidgetId)
        if (binding != null) {
            val config = getIconConfig(binding.presetId)
            if (config != null) return config
        }
        return getAllIconConfigs().firstOrNull() ?: IconWidgetConfig()
    }

    fun saveIconConfig(config: IconWidgetConfig) {
        val list = getAllIconConfigs().toMutableList()
        val index = list.indexOfFirst { it.id == config.id }
        if (index >= 0) {
            list[index] = config
        } else {
            list.add(config)
        }
        prefs.edit().putString("icon_widgets_list", gson.toJson(list)).apply()
    }

    fun deleteIconConfig(id: String) {
        val list = getAllIconConfigs().toMutableList()
        list.removeAll { it.id == id }
        prefs.edit().putString("icon_widgets_list", gson.toJson(list)).apply()
    }

    // --- Image Storage ---

    fun copyUriToInternalStorage(sourceUri: Uri): String? {
        return try {
            val fileName = "img_${UUID.randomUUID()}.jpg"
            val targetFile = File(imagesDir, fileName)
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }
            targetFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun loadBitmap(pathOrUri: String): Bitmap? {
        return try {
            if (pathOrUri.startsWith("/")) {
                BitmapFactory.decodeFile(pathOrUri)
            } else {
                val uri = Uri.parse(pathOrUri)
                context.contentResolver.openInputStream(uri)?.use { input ->
                    BitmapFactory.decodeStream(input)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
