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

    init {
        ensureStarterPresets()
    }

    private fun ensureStarterPresets() {
        val hasInitialized = prefs.getBoolean("has_initialized_starter_presets_v2", false)
        if (!hasInitialized) {
            val clockList = getAllClockConfigs().toMutableList()
            if (clockList.isEmpty()) {
                clockList.addAll(PresetCatalog.getDefaultClockPresets())
                prefs.edit().putString("clock_widgets_list", gson.toJson(clockList)).apply()
            }

            val imgList = getAllImageConfigs().toMutableList()
            if (imgList.isEmpty()) {
                imgList.addAll(PresetCatalog.getDefaultImagePresets())
                prefs.edit().putString("image_widgets_list", gson.toJson(imgList)).apply()
            }

            val noteList = getAllNoteConfigs().toMutableList()
            if (noteList.isEmpty()) {
                noteList.addAll(PresetCatalog.getDefaultNotePresets())
                prefs.edit().putString("note_widgets_list", gson.toJson(noteList)).apply()
            }

            val musicList = getAllMusicConfigs().toMutableList()
            if (musicList.isEmpty()) {
                musicList.addAll(PresetCatalog.getDefaultMusicPresets())
                prefs.edit().putString("music_widgets_list", gson.toJson(musicList)).apply()
            }

            val bentoList = getAllBentoConfigs().toMutableList()
            if (bentoList.isEmpty()) {
                bentoList.addAll(PresetCatalog.getDefaultBentoPresets())
                prefs.edit().putString("bento_widgets_list", gson.toJson(bentoList)).apply()
            }

            prefs.edit().putBoolean("has_initialized_starter_presets_v2", true).apply()
        }
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

    fun setPendingPin(presetId: String, widgetType: String) {
        prefs.edit()
            .putString("pending_pin_preset_id", presetId)
            .putString("pending_pin_widget_type", widgetType)
            .putLong("pending_pin_timestamp", System.currentTimeMillis())
            .apply()
    }

    fun consumePendingPin(widgetType: String): String? {
        val savedType = prefs.getString("pending_pin_widget_type", null)
        val savedPreset = prefs.getString("pending_pin_preset_id", null)
        val timestamp = prefs.getLong("pending_pin_timestamp", 0L)
        if (savedType == widgetType && savedPreset != null && (System.currentTimeMillis() - timestamp) < 120_000L) {
            prefs.edit().remove("pending_pin_preset_id").remove("pending_pin_widget_type").apply()
            return savedPreset
        }
        return null
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
        val pending = consumePendingPin("IMAGE")
        if (pending != null) {
            bindWidgetIdToPreset(appWidgetId, pending, "IMAGE")
            getImageConfig(pending)?.let { return it }
        }
        val all = getAllImageConfigs()
        if (all.isNotEmpty()) {
            val preset = all.first()
            bindWidgetIdToPreset(appWidgetId, preset.id, "IMAGE")
            return preset
        }
        val newDefault = ImageWidgetConfig()
        saveImageConfig(newDefault)
        bindWidgetIdToPreset(appWidgetId, newDefault.id, "IMAGE")
        return newDefault
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
        val pending = consumePendingPin("CLOCK")
        if (pending != null) {
            bindWidgetIdToPreset(appWidgetId, pending, "CLOCK")
            getClockConfig(pending)?.let { return it }
        }
        val all = getAllClockConfigs()
        if (all.isNotEmpty()) {
            val preset = all.first()
            bindWidgetIdToPreset(appWidgetId, preset.id, "CLOCK")
            return preset
        }
        val newDefault = ClockWidgetConfig()
        saveClockConfig(newDefault)
        bindWidgetIdToPreset(appWidgetId, newDefault.id, "CLOCK")
        return newDefault
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

    // --- Note & Quote Widgets Presets ---

    fun getAllNoteConfigs(): List<NoteWidgetConfig> {
        val json = prefs.getString("note_widgets_list", null) ?: return emptyList()
        val type = object : TypeToken<List<NoteWidgetConfig>>() {}.type
        return try {
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getNoteConfig(id: String): NoteWidgetConfig? {
        return getAllNoteConfigs().find { it.id == id }
    }

    fun getNoteConfigForWidgetId(appWidgetId: Int): NoteWidgetConfig? {
        val binding = getBindingForWidgetId(appWidgetId)
        if (binding != null) {
            val config = getNoteConfig(binding.presetId)
            if (config != null) return config
        }
        val pending = consumePendingPin("NOTE")
        if (pending != null) {
            bindWidgetIdToPreset(appWidgetId, pending, "NOTE")
            getNoteConfig(pending)?.let { return it }
        }
        val all = getAllNoteConfigs()
        if (all.isNotEmpty()) {
            val preset = all.first()
            bindWidgetIdToPreset(appWidgetId, preset.id, "NOTE")
            return preset
        }
        val newDefault = NoteWidgetConfig()
        saveNoteConfig(newDefault)
        bindWidgetIdToPreset(appWidgetId, newDefault.id, "NOTE")
        return newDefault
    }

    fun saveNoteConfig(config: NoteWidgetConfig) {
        val list = getAllNoteConfigs().toMutableList()
        val index = list.indexOfFirst { it.id == config.id }
        if (index >= 0) {
            list[index] = config
        } else {
            list.add(config)
        }
        prefs.edit().putString("note_widgets_list", gson.toJson(list)).apply()
    }

    fun deleteNoteConfig(id: String) {
        val list = getAllNoteConfigs().toMutableList()
        list.removeAll { it.id == id }
        prefs.edit().putString("note_widgets_list", gson.toJson(list)).apply()
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
        val pending = consumePendingPin("ICON")
        if (pending != null) {
            bindWidgetIdToPreset(appWidgetId, pending, "ICON")
            getIconConfig(pending)?.let { return it }
        }
        val all = getAllIconConfigs()
        if (all.isNotEmpty()) {
            val preset = all.first()
            val distinctCopy = preset.copy(id = UUID.randomUUID().toString())
            saveIconConfig(distinctCopy)
            bindWidgetIdToPreset(appWidgetId, distinctCopy.id, "ICON")
            return distinctCopy
        }
        val newDefault = IconWidgetConfig()
        saveIconConfig(newDefault)
        bindWidgetIdToPreset(appWidgetId, newDefault.id, "ICON")
        return newDefault
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

    // --- Music Widgets Presets ---

    fun getAllMusicConfigs(): List<MusicWidgetConfig> {
        val json = prefs.getString("music_widgets_list", null) ?: return emptyList()
        val type = object : TypeToken<List<MusicWidgetConfig>>() {}.type
        return try {
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getMusicConfig(id: String): MusicWidgetConfig? {
        return getAllMusicConfigs().find { it.id == id }
    }

    fun getMusicConfigForWidgetId(appWidgetId: Int): MusicWidgetConfig? {
        val binding = getBindingForWidgetId(appWidgetId)
        if (binding != null) {
            val config = getMusicConfig(binding.presetId)
            if (config != null) return config
        }
        val pending = consumePendingPin("MUSIC")
        if (pending != null) {
            bindWidgetIdToPreset(appWidgetId, pending, "MUSIC")
            getMusicConfig(pending)?.let { return it }
        }
        val all = getAllMusicConfigs()
        if (all.isNotEmpty()) {
            val preset = all.first()
            bindWidgetIdToPreset(appWidgetId, preset.id, "MUSIC")
            return preset
        }
        val newDefault = MusicWidgetConfig()
        saveMusicConfig(newDefault)
        bindWidgetIdToPreset(appWidgetId, newDefault.id, "MUSIC")
        return newDefault
    }

    fun saveMusicConfig(config: MusicWidgetConfig) {
        val list = getAllMusicConfigs().toMutableList()
        val index = list.indexOfFirst { it.id == config.id }
        if (index >= 0) {
            list[index] = config
        } else {
            list.add(config)
        }
        prefs.edit().putString("music_widgets_list", gson.toJson(list)).apply()
    }

    fun deleteMusicConfig(id: String) {
        val list = getAllMusicConfigs().toMutableList()
        list.removeAll { it.id == id }
        prefs.edit().putString("music_widgets_list", gson.toJson(list)).apply()
    }

    // --- Bento Widgets Presets ---

    fun getAllBentoConfigs(): List<BentoWidgetConfig> {
        val json = prefs.getString("bento_widgets_list", null) ?: return emptyList()
        val type = object : TypeToken<List<BentoWidgetConfig>>() {}.type
        return try {
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getBentoConfig(id: String): BentoWidgetConfig? {
        return getAllBentoConfigs().find { it.id == id }
    }

    fun getBentoConfigForWidgetId(appWidgetId: Int): BentoWidgetConfig? {
        val binding = getBindingForWidgetId(appWidgetId)
        if (binding != null) {
            val config = getBentoConfig(binding.presetId)
            if (config != null) return config
        }
        val pending = consumePendingPin("BENTO")
        if (pending != null) {
            bindWidgetIdToPreset(appWidgetId, pending, "BENTO")
            getBentoConfig(pending)?.let { return it }
        }
        val all = getAllBentoConfigs()
        if (all.isNotEmpty()) {
            val preset = all.first()
            bindWidgetIdToPreset(appWidgetId, preset.id, "BENTO")
            return preset
        }
        val newDefault = BentoWidgetConfig()
        saveBentoConfig(newDefault)
        bindWidgetIdToPreset(appWidgetId, newDefault.id, "BENTO")
        return newDefault
    }

    fun saveBentoConfig(config: BentoWidgetConfig) {
        val list = getAllBentoConfigs().toMutableList()
        val index = list.indexOfFirst { it.id == config.id }
        if (index >= 0) {
            list[index] = config
        } else {
            list.add(config)
        }
        prefs.edit().putString("bento_widgets_list", gson.toJson(list)).apply()
    }

    fun deleteBentoConfig(id: String) {
        val list = getAllBentoConfigs().toMutableList()
        list.removeAll { it.id == id }
        prefs.edit().putString("bento_widgets_list", gson.toJson(list)).apply()
    }

    // --- Backup & Restore Engine ---

    data class FullBackupPayload(
        val images: List<ImageWidgetConfig>,
        val clocks: List<ClockWidgetConfig>,
        val notes: List<NoteWidgetConfig>,
        val icons: List<IconWidgetConfig>,
        val music: List<MusicWidgetConfig> = emptyList(),
        val bento: List<BentoWidgetConfig> = emptyList(),
        val bindings: List<WidgetInstanceBinding>
    )

    fun exportBackupJson(): String {
        val payload = FullBackupPayload(
            images = getAllImageConfigs(),
            clocks = getAllClockConfigs(),
            notes = getAllNoteConfigs(),
            icons = getAllIconConfigs(),
            music = getAllMusicConfigs(),
            bento = getAllBentoConfigs(),
            bindings = getAllBindings()
        )
        return gson.toJson(payload)
    }

    fun importBackupJson(json: String): Boolean {
        return try {
            val payload = gson.fromJson(json, FullBackupPayload::class.java)
            if (payload != null) {
                prefs.edit()
                    .putString("image_widgets_list", gson.toJson(payload.images))
                    .putString("clock_widgets_list", gson.toJson(payload.clocks))
                    .putString("note_widgets_list", gson.toJson(payload.notes))
                    .putString("icon_widgets_list", gson.toJson(payload.icons))
                    .putString("music_widgets_list", gson.toJson(payload.music))
                    .putString("bento_widgets_list", gson.toJson(payload.bento))
                    .putString("widget_instance_bindings", gson.toJson(payload.bindings))
                    .apply()
                true
            } else false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
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
