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

data class ImageWidgetConfig(
    val id: String = java.util.UUID.randomUUID().toString(),
    var appWidgetId: Int = 0,
    var name: String = "My Photo Widget",
    var imageUris: MutableList<String> = mutableListOf(),
    var currentImageIndex: Int = 0,
    var layout: CollageLayout = CollageLayout.SINGLE,
    var shape: ShapeType = ShapeType.ROUNDED,
    var cornerRadiusDp: Float = 24f,
    var borderWidthDp: Float = 0f,
    var borderColorHex: String = "#FFFFFF",
    var backgroundColorHex: String = "#1E1E1E",
    var opacity: Float = 1.0f,
    var tapAction: TapActionType = TapActionType.NONE,
    var tapActionTarget: String = ""
)

enum class ClockStyle {
    MINIMAL,
    BOLD_EDITORIAL,
    TERMINAL,
    ANALOG_MINIMAL,
    ANALOG_CLASSIC
}

data class ClockWidgetConfig(
    val id: String = java.util.UUID.randomUUID().toString(),
    var appWidgetId: Int = 0,
    var name: String = "My Clock Widget",
    var style: ClockStyle = ClockStyle.BOLD_EDITORIAL,
    var is24Hour: Boolean = false,
    var showDate: Boolean = true,
    var showBattery: Boolean = true,
    var textColorHex: String = "#FFFFFF",
    var accentColorHex: String = "#D0BCFF",
    var backgroundColorHex: String = "#1E1E1E",
    var cornerRadiusDp: Float = 24f,
    var tapPackageTarget: String = "com.google.android.deskclock"
)

data class IconWidgetConfig(
    val id: String = java.util.UUID.randomUUID().toString(),
    var appWidgetId: Int = 0,
    var targetPackageName: String = "",
    var targetActivityName: String = "",
    var label: String = "",
    var iconImageUri: String? = null,
    var iconShape: ShapeType = ShapeType.ROUNDED,
    var cornerRadiusDp: Float = 18f,
    var iconBackgroundColorHex: String = "#2B2B2B"
)

data class InstalledAppInfo(
    val packageName: String,
    val appName: String,
    val activityName: String
)
