package com.widgetcraft.app.data

enum class ShapeType {
    RECTANGLE,
    ROUNDED,
    CIRCLE,
    PILL,
    SQUIRCLE,
    POLAROID
}

enum class CollageLayout {
    SINGLE,
    SPLIT_HORIZONTAL,
    SPLIT_VERTICAL,
    GRID_2X2,
    MASONRY_1_2,
    MASONRY_2_1
}

enum class TapActionType {
    NONE,
    OPEN_APP,
    OPEN_URL,
    CYCLE_IMAGES,
    OPEN_GALLERY
}

enum class ImageFilterType {
    NONE,
    GRAYSCALE,
    SEPIA,
    VINTAGE,
    WARM,
    COOL,
    INVERT
}

enum class WidgetScaleType {
    CENTER_CROP,
    FIT_CENTER,
    FILL
}

data class ImageWidgetConfig(
    val id: String = java.util.UUID.randomUUID().toString(),
    var name: String = "My Photo Widget",
    var imageUris: MutableList<String> = mutableListOf(),
    var currentImageIndex: Int = 0,
    var layout: CollageLayout = CollageLayout.SINGLE,
    var shape: ShapeType = ShapeType.ROUNDED,
    var filter: ImageFilterType = ImageFilterType.NONE,
    var scaleType: WidgetScaleType = WidgetScaleType.CENTER_CROP,
    var cornerRadiusDp: Float = 24f,
    var borderWidthDp: Float = 0f,
    var borderColorHex: String = "#FFFFFF",
    var backgroundColorHex: String = "#1E1E1E",
    var opacity: Float = 1.0f,
    var captionText: String = "",
    var captionColorHex: String = "#FFFFFF",
    var captionSizeSp: Float = 14f,
    var showDateTag: Boolean = false,
    var tapAction: TapActionType = TapActionType.NONE,
    var tapActionTarget: String = ""
)

enum class ClockStyle {
    BOLD_EDITORIAL,
    MINIMAL,
    TERMINAL,
    DIGITAL_SEVEN_SEGMENT,
    ANALOG_MINIMAL,
    ANALOG_CLASSIC,
    NOTHING_DOT_MATRIX,
    FROSTED_GLASS
}

data class ClockWidgetConfig(
    val id: String = java.util.UUID.randomUUID().toString(),
    var name: String = "My Clock Widget",
    var style: ClockStyle = ClockStyle.BOLD_EDITORIAL,
    var is24Hour: Boolean = false,
    var showDate: Boolean = true,
    var showBattery: Boolean = true,
    var showStorage: Boolean = false,
    var showRam: Boolean = false,
    var showWeather: Boolean = false,
    var showSteps: Boolean = false,
    var weatherTemp: String = "24°C",
    var weatherCondition: String = "SUNNY",
    var textColorHex: String = "#FFFFFF",
    var accentColorHex: String = "#D0BCFF",
    var backgroundColorHex: String = "#1E1E1E",
    var cornerRadiusDp: Float = 24f,
    var tapPackageTarget: String = "com.google.android.deskclock"
)

enum class NoteStyle {
    STICKY_YELLOW,
    OBSIDIAN_DARK,
    ROSE_QUARTZ,
    MINT_GREEN,
    LAVENDER_DREAM,
    CYBER_TERMINAL
}

data class NoteWidgetConfig(
    val id: String = java.util.UUID.randomUUID().toString(),
    var name: String = "Daily Note",
    var title: String = "Today's Focus",
    var content: String = "• Finish high-priority task\n• Review pull requests\n• Take a walk outside",
    var style: NoteStyle = NoteStyle.STICKY_YELLOW,
    var fontSizeSp: Float = 14f,
    var textColorHex: String = "#2D3748",
    var backgroundColorHex: String = "#FEF08A",
    var accentColorHex: String = "#EAB308",
    var cornerRadiusDp: Float = 20f,
    var isChecklist: Boolean = true,
    var showDate: Boolean = true
)

enum class IconPresetStyle {
    ORIGINAL,
    MINIMAL_GLYPH,
    NEON_CYBER,
    PASTEL_POP,
    DARK_GOLD,
    SQUIRCLE_GLASS
}

data class IconWidgetConfig(
    val id: String = java.util.UUID.randomUUID().toString(),
    var targetPackageName: String = "",
    var targetActivityName: String = "",
    var label: String = "",
    var iconImageUri: String? = null,
    var iconShape: ShapeType = ShapeType.SQUIRCLE,
    var presetStyle: IconPresetStyle = IconPresetStyle.ORIGINAL,
    var cornerRadiusDp: Float = 18f,
    var iconBackgroundColorHex: String = "#2B2B2B"
)

data class WidgetInstanceBinding(
    val appWidgetId: Int,
    val presetId: String,
    val widgetType: String // "IMAGE", "CLOCK", "NOTE", "ICON"
)

data class ThemePalette(
    val name: String,
    val backgroundHex: String,
    val cardHex: String,
    val accentHex: String,
    val textHex: String
)

data class InstalledAppInfo(
    val packageName: String,
    val appName: String,
    val activityName: String
)

enum class MusicStyle {
    VINYL_DISC,
    NOTHING_DOT,
    FROSTED_GLASS,
    MINIMAL_CARD,
    CYBERPUNK_NEON
}

data class MusicWidgetConfig(
    val id: String = java.util.UUID.randomUUID().toString(),
    var name: String = "Now Playing",
    var style: MusicStyle = MusicStyle.NOTHING_DOT,
    var trackTitle: String = "Starboy",
    var artistName: String = "The Weeknd",
    var isPlaying: Boolean = false,
    var progressMs: Long = 85000,
    var durationMs: Long = 230000,
    var albumArtUri: String? = null,
    var backgroundColorHex: String = "#0A0A0A",
    var textColorHex: String = "#FFFFFF",
    var accentColorHex: String = "#D71921",
    var cornerRadiusDp: Float = 24f
)

enum class BentoStyle {
    NOTHING_OS,
    CYBERPUNK_HUD,
    MINIMAL_MONOCHROME,
    FROSTED_ACRYLIC
}

data class BentoWidgetConfig(
    val id: String = java.util.UUID.randomUUID().toString(),
    var name: String = "Bento Command Center",
    var style: BentoStyle = BentoStyle.NOTHING_OS,
    var showClock: Boolean = true,
    var showWeather: Boolean = true,
    var showBattery: Boolean = true,
    var showRam: Boolean = true,
    var showSteps: Boolean = true,
    var showMusicSnippet: Boolean = true,
    var backgroundColorHex: String = "#121212",
    var accentColorHex: String = "#D71921",
    var textColorHex: String = "#FFFFFF",
    var cornerRadiusDp: Float = 28f
)

data class WeatherForecastData(
    val temp: String = "24°C",
    val condition: String = "Clear Sky",
    val wmoCode: Int = 0,
    val windSpeed: String = "12 km/h",
    val humidity: String = "48%",
    val locationName: String = "Local Area",
    val dailyHigh: String = "27°C",
    val dailyLow: String = "18°C",
    val lastUpdatedMs: Long = System.currentTimeMillis()
)

