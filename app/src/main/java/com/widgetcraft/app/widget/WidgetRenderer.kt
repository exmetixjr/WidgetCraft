package com.widgetcraft.app.widget

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.*
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
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

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = parseColor(config.backgroundColorHex, Color.DKGRAY)
        }

        val path = createShapePath(
            config.shape,
            RectF(0f, 0f, targetWidth.toFloat(), targetHeight.toFloat()),
            config.cornerRadiusDp * (targetWidth / 200f)
        )
        canvas.clipPath(path)
        canvas.drawRect(0f, 0f, targetWidth.toFloat(), targetHeight.toFloat(), bgPaint)

        val rawBitmaps = config.imageUris.mapNotNull { uri -> storage.loadBitmap(uri) }
        val bitmaps = rawBitmaps.map { applyFilter(it, config.filter) }

        if (bitmaps.isEmpty()) {
            drawPlaceholder(canvas, targetWidth, targetHeight, "Tap to configure photo")
        } else {
            when (config.layout) {
                CollageLayout.SINGLE -> {
                    val activeIndex = config.currentImageIndex.coerceIn(0, bitmaps.size - 1)
                    drawBitmapToRect(canvas, bitmaps[activeIndex], RectF(0f, 0f, targetWidth.toFloat(), targetHeight.toFloat()), config.opacity, config.scaleType)
                }
                CollageLayout.SPLIT_HORIZONTAL -> {
                    val halfW = targetWidth / 2f
                    drawBitmapToRect(canvas, bitmaps[0], RectF(0f, 0f, halfW - 2f, targetHeight.toFloat()), config.opacity, config.scaleType)
                    val second = bitmaps.getOrElse(1) { bitmaps[0] }
                    drawBitmapToRect(canvas, second, RectF(halfW + 2f, 0f, targetWidth.toFloat(), targetHeight.toFloat()), config.opacity, config.scaleType)
                }
                CollageLayout.SPLIT_VERTICAL -> {
                    val halfH = targetHeight / 2f
                    drawBitmapToRect(canvas, bitmaps[0], RectF(0f, 0f, targetWidth.toFloat(), halfH - 2f), config.opacity, config.scaleType)
                    val second = bitmaps.getOrElse(1) { bitmaps[0] }
                    drawBitmapToRect(canvas, second, RectF(0f, halfH + 2f, targetWidth.toFloat(), targetHeight.toFloat()), config.opacity, config.scaleType)
                }
                CollageLayout.GRID_2X2 -> {
                    val halfW = targetWidth / 2f
                    val halfH = targetHeight / 2f
                    drawBitmapToRect(canvas, bitmaps[0], RectF(0f, 0f, halfW - 2f, halfH - 2f), config.opacity, config.scaleType)
                    drawBitmapToRect(canvas, bitmaps.getOrElse(1) { bitmaps[0] }, RectF(halfW + 2f, 0f, targetWidth.toFloat(), halfH - 2f), config.opacity, config.scaleType)
                    drawBitmapToRect(canvas, bitmaps.getOrElse(2) { bitmaps[0] }, RectF(0f, halfH + 2f, halfW - 2f, targetHeight.toFloat()), config.opacity, config.scaleType)
                    drawBitmapToRect(canvas, bitmaps.getOrElse(3) { bitmaps[0] }, RectF(halfW + 2f, halfH + 2f, targetWidth.toFloat(), targetHeight.toFloat()), config.opacity, config.scaleType)
                }
                CollageLayout.MASONRY_1_2 -> {
                    val halfW = targetWidth / 2f
                    val halfH = targetHeight / 2f
                    drawBitmapToRect(canvas, bitmaps[0], RectF(0f, 0f, halfW - 2f, targetHeight.toFloat()), config.opacity, config.scaleType)
                    drawBitmapToRect(canvas, bitmaps.getOrElse(1) { bitmaps[0] }, RectF(halfW + 2f, 0f, targetWidth.toFloat(), halfH - 2f), config.opacity, config.scaleType)
                    drawBitmapToRect(canvas, bitmaps.getOrElse(2) { bitmaps[0] }, RectF(halfW + 2f, halfH + 2f, targetWidth.toFloat(), targetHeight.toFloat()), config.opacity, config.scaleType)
                }
                CollageLayout.MASONRY_2_1 -> {
                    val halfW = targetWidth / 2f
                    val halfH = targetHeight / 2f
                    drawBitmapToRect(canvas, bitmaps[0], RectF(0f, 0f, halfW - 2f, halfH - 2f), config.opacity, config.scaleType)
                    drawBitmapToRect(canvas, bitmaps.getOrElse(1) { bitmaps[0] }, RectF(0f, halfH + 2f, halfW - 2f, targetHeight.toFloat()), config.opacity, config.scaleType)
                    drawBitmapToRect(canvas, bitmaps.getOrElse(2) { bitmaps[0] }, RectF(halfW + 2f, 0f, targetWidth.toFloat(), targetHeight.toFloat()), config.opacity, config.scaleType)
                }
            }
        }

        // Draw caption or date overlay if configured
        if (config.captionText.isNotBlank()) {
            drawCaptionOverlay(canvas, targetWidth, targetHeight, config.captionText, config.captionColorHex, config.captionSizeSp)
        } else if (config.showDateTag) {
            val dateTag = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date())
            drawCaptionOverlay(canvas, targetWidth, targetHeight, dateTag, config.captionColorHex, 13f)
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
                    textSize = targetHeight * 0.44f
                    typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
                }
                val amPmPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = accentColor
                    textSize = targetHeight * 0.16f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }
                val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.LTGRAY
                    textSize = targetHeight * 0.12f
                    typeface = Typeface.DEFAULT
                }

                val timeY = targetHeight * 0.50f
                canvas.drawText(timeString, 48f, timeY, timePaint)
                if (amPmString.isNotEmpty()) {
                    val timeWidth = timePaint.measureText(timeString)
                    canvas.drawText(amPmString, 56f + timeWidth, timeY - (targetHeight * 0.20f), amPmPaint)
                }

                if (config.showDate) {
                    canvas.drawText(dateString, 48f, timeY + targetHeight * 0.24f, datePaint)
                }
            }
            ClockStyle.DIGITAL_SEVEN_SEGMENT -> {
                val timePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = accentColor
                    textSize = targetHeight * 0.46f
                    typeface = Typeface.MONOSPACE
                    setShadowLayer(14f, 0f, 0f, accentColor)
                }
                val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = textColor
                    textSize = targetHeight * 0.11f
                    typeface = Typeface.MONOSPACE
                }
                canvas.drawText(timeString, 44f, targetHeight * 0.52f, timePaint)
                if (config.showDate) {
                    canvas.drawText(dateString.uppercase(), 48f, targetHeight * 0.76f, datePaint)
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
            }
            ClockStyle.ANALOG_MINIMAL, ClockStyle.ANALOG_CLASSIC -> {
                drawAnalogClock(canvas, targetWidth, targetHeight, now, textColor, accentColor, config.style == ClockStyle.ANALOG_CLASSIC)
            }
        }

        drawStatusBadges(context, canvas, targetWidth, targetHeight, config, accentColor)

        return output
    }

    // --- Note & Checklist Widget Rendering ---

    fun renderNoteWidget(
        context: Context,
        config: NoteWidgetConfig,
        targetWidth: Int = 800,
        targetHeight: Int = 800
    ): Bitmap {
        val output = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val bgColor = parseColor(config.backgroundColorHex, Color.parseColor("#FEF08A"))
        val textColor = parseColor(config.textColorHex, Color.parseColor("#2D3748"))
        val accentColor = parseColor(config.accentColorHex, Color.parseColor("#EAB308"))

        val cornerRadius = config.cornerRadiusDp * (targetWidth / 250f)
        val cardRect = RectF(0f, 0f, targetWidth.toFloat(), targetHeight.toFloat())

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = bgColor
            style = Paint.Style.FILL
        }

        canvas.drawRoundRect(cardRect, cornerRadius, cornerRadius, bgPaint)

        // Accent details depending on style
        when (config.style) {
            NoteStyle.STICKY_YELLOW -> {
                val tapePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#44D97706")
                    style = Paint.Style.FILL
                }
                val tapeRect = RectF(targetWidth * 0.35f, 0f, targetWidth * 0.65f, targetHeight * 0.045f)
                canvas.drawRoundRect(tapeRect, 6f, 6f, tapePaint)
            }
            NoteStyle.CYBER_TERMINAL -> {
                val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = accentColor
                    style = Paint.Style.STROKE
                    strokeWidth = 6f
                }
                canvas.drawRoundRect(cardRect, cornerRadius, cornerRadius, strokePaint)

                val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#1E293B")
                    style = Paint.Style.FILL
                }
                val headerRect = RectF(0f, 0f, targetWidth.toFloat(), targetHeight * 0.11f)
                canvas.drawRoundRect(headerRect, cornerRadius, cornerRadius, headerPaint)

                val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
                dotPaint.color = Color.parseColor("#EF4444")
                canvas.drawCircle(40f, targetHeight * 0.055f, 10f, dotPaint)
                dotPaint.color = Color.parseColor("#F59E0B")
                canvas.drawCircle(72f, targetHeight * 0.055f, 10f, dotPaint)
                dotPaint.color = Color.parseColor("#10B981")
                canvas.drawCircle(104f, targetHeight * 0.055f, 10f, dotPaint)
            }
            NoteStyle.OBSIDIAN_DARK -> {
                val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#33FFFFFF")
                    style = Paint.Style.STROKE
                    strokeWidth = 3f
                }
                canvas.drawRoundRect(cardRect, cornerRadius, cornerRadius, strokePaint)
            }
            else -> {}
        }

        val paddingLeft = targetWidth * 0.08f
        val paddingRight = targetWidth * 0.08f
        var currentY = if (config.style == NoteStyle.CYBER_TERMINAL) targetHeight * 0.17f else targetHeight * 0.12f

        // Date Tag
        if (config.showDate) {
            val dateStr = SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(Date()).uppercase()
            val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = accentColor
                textSize = targetWidth * 0.038f
                typeface = if (config.style == NoteStyle.CYBER_TERMINAL) Typeface.MONOSPACE else Typeface.DEFAULT_BOLD
            }
            canvas.drawText(dateStr, paddingLeft, currentY, datePaint)
            currentY += targetWidth * 0.065f
        }

        // Title
        if (config.title.isNotBlank()) {
            val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = textColor
                textSize = targetWidth * 0.062f
                typeface = when (config.style) {
                    NoteStyle.CYBER_TERMINAL -> Typeface.MONOSPACE
                    NoteStyle.OBSIDIAN_DARK -> Typeface.DEFAULT_BOLD
                    else -> Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }
            }
            canvas.drawText(config.title, paddingLeft, currentY, titlePaint)
            currentY += targetWidth * 0.03f

            val divPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = accentColor
                alpha = 90
                strokeWidth = 3f
            }
            currentY += 12f
            canvas.drawLine(paddingLeft, currentY, targetWidth - paddingRight, currentY, divPaint)
            currentY += 28f
        }

        // Body Content
        val maxContentWidth = (targetWidth - paddingLeft - paddingRight).toInt()
        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColor
            textSize = config.fontSizeSp * (targetWidth / 350f)
            typeface = if (config.style == NoteStyle.CYBER_TERMINAL) Typeface.MONOSPACE else Typeface.DEFAULT
        }

        val lines = config.content.lines()

        for (line in lines) {
            if (currentY > targetHeight - 30f) break

            var lineText = line
            var isChecked = false
            var isItem = false

            if (config.isChecklist) {
                val trimmed = line.trim()
                if (trimmed.startsWith("[x]", ignoreCase = true)) {
                    isChecked = true
                    isItem = true
                    lineText = trimmed.substring(3).trim()
                } else if (trimmed.startsWith("[ ]")) {
                    isChecked = false
                    isItem = true
                    lineText = trimmed.substring(3).trim()
                } else if (trimmed.startsWith("•") || trimmed.startsWith("-")) {
                    isItem = true
                    lineText = trimmed.substring(1).trim()
                }
            }

            if (isItem) {
                val boxSize = textPaint.textSize * 0.85f
                val boxY = currentY - boxSize * 0.85f
                val boxRect = RectF(paddingLeft, boxY, paddingLeft + boxSize, boxY + boxSize)

                if (isChecked) {
                    val checkedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = accentColor
                        style = Paint.Style.FILL
                    }
                    canvas.drawRoundRect(boxRect, 6f, 6f, checkedPaint)
                    val markPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = Color.WHITE
                        style = Paint.Style.STROKE
                        strokeWidth = 4f
                    }
                    canvas.drawLine(boxRect.left + 5f, boxRect.centerY(), boxRect.centerX(), boxRect.bottom - 6f, markPaint)
                    canvas.drawLine(boxRect.centerX(), boxRect.bottom - 6f, boxRect.right - 5f, boxRect.top + 6f, markPaint)

                    textPaint.isStrikeThruText = true
                    textPaint.alpha = 140
                } else {
                    val boxBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = accentColor
                        style = Paint.Style.STROKE
                        strokeWidth = 3f
                    }
                    canvas.drawRoundRect(boxRect, 6f, 6f, boxBorderPaint)
                    textPaint.isStrikeThruText = false
                    textPaint.alpha = 255
                }

                val itemTextX = paddingLeft + boxSize + 16f
                val availableWidth = (targetWidth - paddingRight - itemTextX).toInt()
                if (availableWidth > 50) {
                    val layout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        StaticLayout.Builder.obtain(lineText, 0, lineText.length, textPaint, availableWidth)
                            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                            .setIncludePad(false)
                            .build()
                    } else {
                        @Suppress("DEPRECATION")
                        StaticLayout(lineText, textPaint, availableWidth, Layout.Alignment.ALIGN_NORMAL, 1.1f, 4f, false)
                    }
                    canvas.save()
                    canvas.translate(itemTextX, currentY - textPaint.textSize * 0.85f)
                    layout.draw(canvas)
                    canvas.restore()
                    currentY += layout.height.toFloat() + 16f
                }
                textPaint.isStrikeThruText = false
                textPaint.alpha = 255
            } else {
                if (lineText.isNotBlank()) {
                    val layout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        StaticLayout.Builder.obtain(lineText, 0, lineText.length, textPaint, maxContentWidth)
                            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                            .setIncludePad(false)
                            .build()
                    } else {
                        @Suppress("DEPRECATION")
                        StaticLayout(lineText, textPaint, maxContentWidth, Layout.Alignment.ALIGN_NORMAL, 1.1f, 4f, false)
                    }
                    canvas.save()
                    canvas.translate(paddingLeft, currentY - textPaint.textSize * 0.85f)
                    layout.draw(canvas)
                    canvas.restore()
                    currentY += layout.height.toFloat() + 14f
                } else {
                    currentY += textPaint.fontSpacing * 0.6f
                }
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
            color = when (config.presetStyle) {
                IconPresetStyle.NEON_CYBER -> Color.parseColor("#0F172A")
                IconPresetStyle.DARK_GOLD -> Color.parseColor("#18181B")
                IconPresetStyle.PASTEL_POP -> Color.parseColor("#FDE68A")
                IconPresetStyle.MINIMAL_GLYPH -> Color.parseColor("#27272A")
                IconPresetStyle.SQUIRCLE_GLASS -> Color.parseColor("#334155")
                IconPresetStyle.ORIGINAL -> parseColor(config.iconBackgroundColorHex, Color.parseColor("#2B2B2B"))
            }
        }

        val path = createShapePath(
            config.iconShape,
            RectF(0f, 0f, targetSize.toFloat(), targetSize.toFloat()),
            config.cornerRadiusDp * (targetSize / 100f)
        )
        canvas.clipPath(path)
        canvas.drawRect(0f, 0f, targetSize.toFloat(), targetSize.toFloat(), bgPaint)

        val rawBmp = config.iconImageUri?.let { storage.loadBitmap(it) }
            ?: InstalledAppHelper(context).getAppIcon(config.targetPackageName)?.let {
                InstalledAppHelper(context).drawableToBitmap(it)
            }

        if (rawBmp != null) {
            val filter = when (config.presetStyle) {
                IconPresetStyle.MINIMAL_GLYPH -> ImageFilterType.GRAYSCALE
                IconPresetStyle.DARK_GOLD -> ImageFilterType.SEPIA
                IconPresetStyle.NEON_CYBER -> ImageFilterType.INVERT
                else -> ImageFilterType.NONE
            }
            val styledBmp = applyFilter(rawBmp, filter)
            val padding = targetSize * 0.14f
            val dstRect = RectF(padding, padding, targetSize - padding, targetSize - padding)
            drawBitmapToRect(canvas, styledBmp, dstRect, 1.0f, WidgetScaleType.FIT_CENTER)
        }

        if (config.presetStyle == IconPresetStyle.NEON_CYBER) {
            val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 6f
                color = Color.parseColor("#38BDF8")
            }
            canvas.drawPath(path, strokePaint)
        }

        return output
    }

    // --- Filters & Helpers ---

    private fun applyFilter(src: Bitmap, filter: ImageFilterType): Bitmap {
        if (filter == ImageFilterType.NONE) return src

        val out = Bitmap.createBitmap(src.width, src.height, src.config ?: Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val matrix = ColorMatrix()
        when (filter) {
            ImageFilterType.GRAYSCALE -> matrix.setSaturation(0f)
            ImageFilterType.SEPIA -> {
                matrix.setSaturation(0f)
                val sepiaMatrix = ColorMatrix(floatArrayOf(
                    1.0f, 0.0f, 0.0f, 0.0f, 30f,
                    0.0f, 0.9f, 0.0f, 0.0f, 15f,
                    0.0f, 0.0f, 0.7f, 0.0f, 0f,
                    0.0f, 0.0f, 0.0f, 1.0f, 0f
                ))
                matrix.postConcat(sepiaMatrix)
            }
            ImageFilterType.VINTAGE -> {
                matrix.set(floatArrayOf(
                    0.9f, 0.0f, 0.0f, 0.0f, 20f,
                    0.0f, 0.8f, 0.0f, 0.0f, 10f,
                    0.0f, 0.0f, 0.6f, 0.0f, 10f,
                    0.0f, 0.0f, 0.0f, 1.0f, 0f
                ))
            }
            ImageFilterType.WARM -> {
                matrix.set(floatArrayOf(
                    1.1f, 0.0f, 0.0f, 0.0f, 20f,
                    0.0f, 1.0f, 0.0f, 0.0f, 10f,
                    0.0f, 0.0f, 0.85f, 0.0f, -10f,
                    0.0f, 0.0f, 0.0f, 1.0f, 0f
                ))
            }
            ImageFilterType.COOL -> {
                matrix.set(floatArrayOf(
                    0.85f, 0.0f, 0.0f, 0.0f, -10f,
                    0.0f, 1.0f, 0.0f, 0.0f, 5f,
                    0.0f, 0.0f, 1.2f, 0.0f, 20f,
                    0.0f, 0.0f, 0.0f, 1.0f, 0f
                ))
            }
            ImageFilterType.INVERT -> {
                matrix.set(floatArrayOf(
                    -1f, 0f, 0f, 0f, 255f,
                    0f, -1f, 0f, 0f, 255f,
                    0f, 0f, -1f, 0f, 255f,
                    0f, 0f, 0f, 1f, 0f
                ))
            }
            ImageFilterType.NONE -> {}
        }
        paint.colorFilter = ColorMatrixColorFilter(matrix)
        canvas.drawBitmap(src, 0f, 0f, paint)
        return out
    }

    private fun drawBitmapToRect(canvas: Canvas, bitmap: Bitmap, dst: RectF, opacity: Float, scaleType: WidgetScaleType) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
            alpha = (opacity * 255).toInt().coerceIn(0, 255)
        }

        when (scaleType) {
            WidgetScaleType.FILL -> {
                canvas.drawBitmap(bitmap, null, dst, paint)
            }
            WidgetScaleType.FIT_CENTER -> {
                val scale = minOf(dst.width() / bitmap.width, dst.height() / bitmap.height)
                val w = bitmap.width * scale
                val h = bitmap.height * scale
                val left = dst.left + (dst.width() - w) / 2f
                val top = dst.top + (dst.height() - h) / 2f
                canvas.drawBitmap(bitmap, null, RectF(left, top, left + w, top + h), paint)
            }
            WidgetScaleType.CENTER_CROP -> {
                val scale = maxOf(dst.width() / bitmap.width, dst.height() / bitmap.height)
                val w = bitmap.width * scale
                val h = bitmap.height * scale
                val left = dst.left + (dst.width() - w) / 2f
                val top = dst.top + (dst.height() - h) / 2f
                canvas.save()
                canvas.clipRect(dst)
                canvas.drawBitmap(bitmap, null, RectF(left, top, left + w, top + h), paint)
                canvas.restore()
            }
        }
    }

    private fun drawCaptionOverlay(canvas: Canvas, w: Int, h: Int, text: String, colorHex: String, sizeSp: Float) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = parseColor(colorHex, Color.WHITE)
            textSize = sizeSp * (w / 350f)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            setShadowLayer(8f, 0f, 2f, Color.BLACK)
        }
        val pillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#80000000")
        }

        val textWidth = paint.measureText(text)
        val pillRect = RectF(
            (w / 2f) - (textWidth / 2f) - 24f,
            h - 70f,
            (w / 2f) + (textWidth / 2f) + 24f,
            h - 16f
        )
        canvas.drawRoundRect(pillRect, 20f, 20f, pillPaint)
        canvas.drawText(text, w / 2f, h - 34f, paint)
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
            textSize = 34f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(text, w / 2f, h / 2f, paint)
    }

    private fun drawStatusBadges(
        context: Context,
        canvas: Canvas,
        targetWidth: Int,
        targetHeight: Int,
        config: ClockWidgetConfig,
        accentColor: Int
    ) {
        var badgeRightX = targetWidth - 28f
        val badgeY = 22f
        val badgeWidth = 140f
        val spacing = 12f

        if (config.showBattery) {
            drawBatteryBadge(context, canvas, badgeRightX - badgeWidth, badgeY, accentColor)
            badgeRightX -= (badgeWidth + spacing)
        }
        if (config.showStorage) {
            drawStorageBadge(canvas, badgeRightX - badgeWidth, badgeY, accentColor)
            badgeRightX -= (badgeWidth + spacing)
        }
        if (config.showRam) {
            drawRamBadge(context, canvas, badgeRightX - badgeWidth, badgeY, accentColor)
            badgeRightX -= (badgeWidth + spacing)
        }
        if (config.showWeather) {
            drawWeatherBadge(canvas, badgeRightX - badgeWidth, badgeY, accentColor, config.weatherTemp)
            badgeRightX -= (badgeWidth + spacing)
        }
    }

    private fun drawBatteryBadge(context: Context, canvas: Canvas, x: Float, y: Float, accentColor: Int) {
        val (batteryPct, _) = SystemStatsHelper.getBatteryStats(context)
        val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#33FFFFFF")
            style = Paint.Style.FILL
        }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 21f
            typeface = Typeface.DEFAULT_BOLD
        }
        val rect = RectF(x, y, x + 140f, y + 44f)
        canvas.drawRoundRect(rect, 22f, 22f, badgePaint)

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            style = Paint.Style.FILL
        }
        val fillWidth = (140f * (batteryPct / 100f)).coerceIn(10f, 140f)
        canvas.drawRoundRect(RectF(x, y, x + fillWidth, y + 44f), 22f, 22f, fillPaint)

        canvas.drawText("⚡ $batteryPct%", x + 18f, y + 30f, textPaint)
    }

    private fun drawStorageBadge(canvas: Canvas, x: Float, y: Float, accentColor: Int) {
        val (freeGb, usedPct) = SystemStatsHelper.getStorageStats()

        val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#33FFFFFF")
            style = Paint.Style.FILL
        }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 21f
            typeface = Typeface.DEFAULT_BOLD
        }
        val rect = RectF(x, y, x + 140f, y + 44f)
        canvas.drawRoundRect(rect, 22f, 22f, badgePaint)

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            style = Paint.Style.FILL
        }
        val fillWidth = (140f * (usedPct / 100f)).coerceIn(10f, 140f)
        canvas.drawRoundRect(RectF(x, y, x + fillWidth, y + 44f), 22f, 22f, fillPaint)

        canvas.drawText("💾 ${freeGb}G", x + 18f, y + 30f, textPaint)
    }

    private fun drawRamBadge(context: Context, canvas: Canvas, x: Float, y: Float, accentColor: Int) {
        val (_, _, pct) = SystemStatsHelper.getRamStats(context)
        val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#33FFFFFF")
            style = Paint.Style.FILL
        }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 21f
            typeface = Typeface.DEFAULT_BOLD
        }
        val rect = RectF(x, y, x + 140f, y + 44f)
        canvas.drawRoundRect(rect, 22f, 22f, badgePaint)

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            style = Paint.Style.FILL
        }
        val fillWidth = (140f * (pct / 100f)).coerceIn(10f, 140f)
        canvas.drawRoundRect(RectF(x, y, x + fillWidth, y + 44f), 22f, 22f, fillPaint)

        canvas.drawText("RAM $pct%", x + 14f, y + 30f, textPaint)
    }

    private fun drawWeatherBadge(canvas: Canvas, x: Float, y: Float, accentColor: Int, temp: String) {
        val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#33FFFFFF")
            style = Paint.Style.FILL
        }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 21f
            typeface = Typeface.DEFAULT_BOLD
        }
        val rect = RectF(x, y, x + 140f, y + 44f)
        canvas.drawRoundRect(rect, 22f, 22f, badgePaint)
        canvas.drawText("☀️ $temp", x + 16f, y + 30f, textPaint)
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

        val hourAngle = Math.toRadians(((hours + minutes / 60.0) * 30).toDouble())
        val hx = cx + (radius * 0.5f) * sin(hourAngle).toFloat()
        val hy = cy - (radius * 0.5f) * cos(hourAngle).toFloat()
        val hourPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColor
            strokeWidth = 14f
            strokeCap = Paint.Cap.ROUND
        }
        canvas.drawLine(cx, cy, hx, hy, hourPaint)

        val minAngle = Math.toRadians((minutes * 6.0).toDouble())
        val mx = cx + (radius * 0.75f) * sin(minAngle).toFloat()
        val my = cy - (radius * 0.75f) * cos(minAngle).toFloat()
        val minPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            strokeWidth = 8f
            strokeCap = Paint.Cap.ROUND
        }
        canvas.drawLine(cx, cy, mx, my, minPaint)

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
