package com.widgetcraft.app.widget

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.*
import android.os.BatteryManager
import com.widgetcraft.app.data.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

object WidgetRenderer {

    // --- Image Widget Rendering ---

    fun renderImageWidget(
        context: Context,
        config: ImageWidgetConfig,
        targetWidth: Int = 800,
        targetHeight: Int = 800
    ): Bitmap {
        val output = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val storage = WidgetStorage(context)

        // Background / Canvas
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = parseColor(config.backgroundColorHex, Color.DKGRAY)
        }

        // Clip path for custom shape
        val path = createShapePath(
            config.shape,
            RectF(0f, 0f, targetWidth.toFloat(), targetHeight.toFloat()),
            config.cornerRadiusDp * (targetWidth / 200f)
        )
        canvas.clipPath(path)
        canvas.drawRect(0f, 0f, targetWidth.toFloat(), targetHeight.toFloat(), bgPaint)

        val bitmaps = config.imageUris.mapNotNull { uri -> storage.loadBitmap(uri) }

        if (bitmaps.isEmpty()) {
            drawPlaceholder(canvas, targetWidth, targetHeight, "Tap to configure photo")
        } else {
            when (config.layout) {
                CollageLayout.SINGLE -> {
                    val activeIndex = config.currentImageIndex.coerceIn(0, bitmaps.size - 1)
                    drawBitmapToRect(canvas, bitmaps[activeIndex], RectF(0f, 0f, targetWidth.toFloat(), targetHeight.toFloat()), config.opacity)
                }
                CollageLayout.SPLIT_HORIZONTAL -> {
                    val halfW = targetWidth / 2f
                    drawBitmapToRect(canvas, bitmaps[0], RectF(0f, 0f, halfW - 2f, targetHeight.toFloat()), config.opacity)
                    val second = bitmaps.getOrElse(1) { bitmaps[0] }
                    drawBitmapToRect(canvas, second, RectF(halfW + 2f, 0f, targetWidth.toFloat(), targetHeight.toFloat()), config.opacity)
                }
                CollageLayout.SPLIT_VERTICAL -> {
                    val halfH = targetHeight / 2f
                    drawBitmapToRect(canvas, bitmaps[0], RectF(0f, 0f, targetWidth.toFloat(), halfH - 2f), config.opacity)
                    val second = bitmaps.getOrElse(1) { bitmaps[0] }
                    drawBitmapToRect(canvas, second, RectF(0f, halfH + 2f, targetWidth.toFloat(), targetHeight.toFloat()), config.opacity)
                }
                CollageLayout.GRID_2X2 -> {
                    val halfW = targetWidth / 2f
                    val halfH = targetHeight / 2f
                    drawBitmapToRect(canvas, bitmaps[0], RectF(0f, 0f, halfW - 2f, halfH - 2f), config.opacity)
                    drawBitmapToRect(canvas, bitmaps.getOrElse(1) { bitmaps[0] }, RectF(halfW + 2f, 0f, targetWidth.toFloat(), halfH - 2f), config.opacity)
                    drawBitmapToRect(canvas, bitmaps.getOrElse(2) { bitmaps[0] }, RectF(0f, halfH + 2f, halfW - 2f, targetHeight.toFloat()), config.opacity)
                    drawBitmapToRect(canvas, bitmaps.getOrElse(3) { bitmaps[0] }, RectF(halfW + 2f, halfH + 2f, targetWidth.toFloat(), targetHeight.toFloat()), config.opacity)
                }
                CollageLayout.MASONRY_1_2 -> {
                    val halfW = targetWidth / 2f
                    val halfH = targetHeight / 2f
                    drawBitmapToRect(canvas, bitmaps[0], RectF(0f, 0f, halfW - 2f, targetHeight.toFloat()), config.opacity)
                    drawBitmapToRect(canvas, bitmaps.getOrElse(1) { bitmaps[0] }, RectF(halfW + 2f, 0f, targetWidth.toFloat(), halfH - 2f), config.opacity)
                    drawBitmapToRect(canvas, bitmaps.getOrElse(2) { bitmaps[0] }, RectF(halfW + 2f, halfH + 2f, targetWidth.toFloat(), targetHeight.toFloat()), config.opacity)
                }
                CollageLayout.MASONRY_2_1 -> {
                    val halfW = targetWidth / 2f
                    val halfH = targetHeight / 2f
                    drawBitmapToRect(canvas, bitmaps[0], RectF(0f, 0f, halfW - 2f, halfH - 2f), config.opacity)
                    drawBitmapToRect(canvas, bitmaps.getOrElse(1) { bitmaps[0] }, RectF(0f, halfH + 2f, halfW - 2f, targetHeight.toFloat()), config.opacity)
                    drawBitmapToRect(canvas, bitmaps.getOrElse(2) { bitmaps[0] }, RectF(halfW + 2f, 0f, targetWidth.toFloat(), targetHeight.toFloat()), config.opacity)
                }
            }
        }

        // Draw border if configured
        if (config.borderWidthDp > 0f) {
            val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = config.borderWidthDp * (targetWidth / 200f)
                color = parseColor(config.borderColorHex, Color.WHITE)
            }
            canvas.drawPath(path, strokePaint)
        }

        return output
    }

    // --- Clock Widget Rendering ---

    fun renderClockWidget(
        context: Context,
        config: ClockWidgetConfig,
        targetWidth: Int = 900,
        targetHeight: Int = 450
    ): Bitmap {
        val output = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = parseColor(config.backgroundColorHex, Color.parseColor("#1E1E1E"))
        }

        val cornerRadius = config.cornerRadiusDp * (targetWidth / 250f)
        val rect = RectF(0f, 0f, targetWidth.toFloat(), targetHeight.toFloat())
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, bgPaint)

        val textColor = parseColor(config.textColorHex, Color.WHITE)
        val accentColor = parseColor(config.accentColorHex, Color.parseColor("#D0BCFF"))

        val now = Calendar.getInstance()
        val timePattern = if (config.is24Hour) "HH:mm" else "hh:mm"
        val timeString = SimpleDateFormat(timePattern, Locale.getDefault()).format(now.time)
        val amPmString = if (!config.is24Hour) SimpleDateFormat("a", Locale.getDefault()).format(now.time) else ""
        val dateString = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(now.time)

        when (config.style) {
            ClockStyle.BOLD_EDITORIAL -> {
                val timePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = textColor
                    textSize = targetHeight * 0.42f
                    typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
                }
                val amPmPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = accentColor
                    textSize = targetHeight * 0.15f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }
                val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.LTGRAY
                    textSize = targetHeight * 0.12f
                    typeface = Typeface.DEFAULT
                }

                val timeY = targetHeight * 0.48f
                canvas.drawText(timeString, 48f, timeY, timePaint)
                if (amPmString.isNotEmpty()) {
                    val timeWidth = timePaint.measureText(timeString)
                    canvas.drawText(amPmString, 54f + timeWidth, timeY - (targetHeight * 0.22f), amPmPaint)
                }

                if (config.showDate) {
                    canvas.drawText(dateString, 48f, timeY + targetHeight * 0.22f, datePaint)
                }

                if (config.showBattery) {
                    drawBatteryBadge(context, canvas, targetWidth - 220f, targetHeight * 0.20f, accentColor)
                }
            }
            ClockStyle.TERMINAL -> {
                val termPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = accentColor
                    textSize = targetHeight * 0.38f
                    typeface = Typeface.MONOSPACE
                }
                val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = textColor
                    textSize = targetHeight * 0.11f
                    typeface = Typeface.MONOSPACE
                }
                canvas.drawText("> $timeString", 40f, targetHeight * 0.45f, termPaint)
                if (config.showDate) {
                    canvas.drawText("$dateString", 40f, targetHeight * 0.70f, datePaint)
                }
                if (config.showBattery) {
                    drawBatteryBadge(context, canvas, targetWidth - 220f, targetHeight * 0.20f, accentColor)
                }
            }
            ClockStyle.MINIMAL -> {
                val timePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = textColor
                    textSize = targetHeight * 0.48f
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                }
                val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.GRAY
                    textSize = targetHeight * 0.12f
                    typeface = Typeface.SANS_SERIF
                }
                canvas.drawText(timeString, 40f, targetHeight * 0.52f, timePaint)
                if (config.showDate) {
                    canvas.drawText(dateString, 44f, targetHeight * 0.75f, datePaint)
                }
                if (config.showBattery) {
                    drawBatteryBadge(context, canvas, targetWidth - 220f, targetHeight * 0.20f, accentColor)
                }
            }
            ClockStyle.ANALOG_MINIMAL, ClockStyle.ANALOG_CLASSIC -> {
                drawAnalogClock(canvas, targetWidth, targetHeight, now, textColor, accentColor, config.style == ClockStyle.ANALOG_CLASSIC)
            }
        }

        return output
    }

    // --- Custom Icon Rendering ---

    fun renderIcon(
        context: Context,
        config: IconWidgetConfig,
        targetSize: Int = 256
    ): Bitmap {
        val output = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val storage = WidgetStorage(context)

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = parseColor(config.iconBackgroundColorHex, Color.parseColor("#2B2B2B"))
        }

        val path = createShapePath(
            config.iconShape,
            RectF(0f, 0f, targetSize.toFloat(), targetSize.toFloat()),
            config.cornerRadiusDp * (targetSize / 100f)
        )
        canvas.clipPath(path)
        canvas.drawRect(0f, 0f, targetSize.toFloat(), targetSize.toFloat(), bgPaint)

        val bitmap = config.iconImageUri?.let { storage.loadBitmap(it) }
            ?: InstalledAppHelper(context).getAppIcon(config.targetPackageName)?.let {
                InstalledAppHelper(context).drawableToBitmap(it)
            }

        if (bitmap != null) {
            val padding = targetSize * 0.12f
            val dstRect = RectF(padding, padding, targetSize - padding, targetSize - padding)
            drawBitmapToRect(canvas, bitmap, dstRect, 1.0f)
        }

        return output
    }

    // --- Helper Utilities ---

    private fun drawBitmapToRect(canvas: Canvas, bitmap: Bitmap, dst: RectF, opacity: Float) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
            alpha = (opacity * 255).toInt().coerceIn(0, 255)
        }
        val src = Rect(0, 0, bitmap.width, bitmap.height)

        // Center Crop scaling
        val scale = maxOf(dst.width() / bitmap.width, dst.height() / bitmap.height)
        val scaledW = bitmap.width * scale
        val scaledH = bitmap.height * scale
        val left = dst.left + (dst.width() - scaledW) / 2f
        val top = dst.top + (dst.height() - scaledH) / 2f

        canvas.save()
        canvas.clipRect(dst)
        canvas.drawBitmap(bitmap, null, RectF(left, top, left + scaledW, top + scaledH), paint)
        canvas.restore()
    }

    private fun createShapePath(shape: ShapeType, rect: RectF, radius: Float): Path {
        val path = Path()
        when (shape) {
            ShapeType.RECTANGLE -> path.addRect(rect, Path.Direction.CW)
            ShapeType.ROUNDED -> path.addRoundRect(rect, radius, radius, Path.Direction.CW)
            ShapeType.CIRCLE -> {
                val size = min(rect.width(), rect.height())
                path.addCircle(rect.centerX(), rect.centerY(), size / 2f, Path.Direction.CW)
            }
            ShapeType.PILL -> {
                val pillRadius = min(rect.width(), rect.height()) / 2f
                path.addRoundRect(rect, pillRadius, pillRadius, Path.Direction.CW)
            }
            ShapeType.SQUIRCLE -> {
                val squircleRadius = min(rect.width(), rect.height()) * 0.28f
                path.addRoundRect(rect, squircleRadius, squircleRadius, Path.Direction.CW)
            }
            ShapeType.POLAROID -> {
                path.addRect(rect, Path.Direction.CW)
            }
        }
        return path
    }

    private fun drawPlaceholder(canvas: Canvas, w: Int, h: Int, text: String) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.LTGRAY
            textSize = 36f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(text, w / 2f, h / 2f, paint)
    }

    private fun drawBatteryBadge(context: Context, canvas: Canvas, x: Float, y: Float, accentColor: Int) {
        val batteryPct = getBatteryPercentage(context)
        val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#33FFFFFF")
            style = Paint.Style.FILL
        }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 28f
            typeface = Typeface.DEFAULT_BOLD
        }
        val rect = RectF(x, y, x + 160f, y + 54f)
        canvas.drawRoundRect(rect, 27f, 27f, badgePaint)

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            style = Paint.Style.FILL
        }
        val fillWidth = (160f * (batteryPct / 100f)).coerceIn(10f, 160f)
        canvas.drawRoundRect(RectF(x, y, x + fillWidth, y + 54f), 27f, 27f, fillPaint)

        canvas.drawText("$batteryPct%", x + 40f, y + 37f, textPaint)
    }

    private fun getBatteryPercentage(context: Context): Int {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        return if (level >= 0 && scale > 0) ((level.toFloat() / scale.toFloat()) * 100).toInt() else 100
    }

    private fun drawAnalogClock(
        canvas: Canvas,
        w: Int,
        h: Int,
        cal: Calendar,
        textColor: Int,
        accentColor: Int,
        showNumbers: Boolean
    ) {
        val cx = w / 2f
        val cy = h / 2f
        val radius = min(cx, cy) * 0.85f

        val dialPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColor
            style = Paint.Style.STROKE
            strokeWidth = 6f
        }
        canvas.drawCircle(cx, cy, radius, dialPaint)

        // Hour ticks
        for (i in 0 until 12) {
            val angle = Math.toRadians((i * 30).toDouble())
            val x1 = cx + (radius - 20) * sin(angle).toFloat()
            val y1 = cy - (radius - 20) * cos(angle).toFloat()
            val x2 = cx + radius * sin(angle).toFloat()
            val y2 = cy - radius * cos(angle).toFloat()
            canvas.drawLine(x1, y1, x2, y2, dialPaint)
        }

        val hours = cal.get(Calendar.HOUR)
        val minutes = cal.get(Calendar.MINUTE)

        // Hour hand
        val hourAngle = Math.toRadians(((hours + minutes / 60.0) * 30).toDouble())
        val hx = cx + (radius * 0.5f) * sin(hourAngle).toFloat()
        val hy = cy - (radius * 0.5f) * cos(hourAngle).toFloat()
        val hourPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColor
            strokeWidth = 14f
            strokeCap = Paint.Cap.ROUND
        }
        canvas.drawLine(cx, cy, hx, hy, hourPaint)

        // Minute hand
        val minAngle = Math.toRadians((minutes * 6.0).toDouble())
        val mx = cx + (radius * 0.75f) * sin(minAngle).toFloat()
        val my = cy - (radius * 0.75f) * cos(minAngle).toFloat()
        val minPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            strokeWidth = 8f
            strokeCap = Paint.Cap.ROUND
        }
        canvas.drawLine(cx, cy, mx, my, minPaint)

        // Center dot
        val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            style = Paint.Style.FILL
        }
        canvas.drawCircle(cx, cy, 10f, dotPaint)
    }

    private fun parseColor(hex: String, fallback: Int): Int {
        return try {
            Color.parseColor(hex)
        } catch (e: Exception) {
            fallback
        }
    }
}
