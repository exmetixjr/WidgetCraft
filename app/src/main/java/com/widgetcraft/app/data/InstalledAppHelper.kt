package com.widgetcraft.app.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import java.util.UUID

class InstalledAppHelper(private val context: Context) {
    private val pm: PackageManager = context.packageManager

    fun getInstalledLaunchableApps(): List<InstalledAppInfo> {
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(intent, 0)
        return resolveInfos.mapNotNull { resolveInfo ->
            val packageName = resolveInfo.activityInfo.packageName
            val activityName = resolveInfo.activityInfo.name
            val appName = resolveInfo.loadLabel(pm).toString()
            if (packageName.isNotEmpty()) {
                InstalledAppInfo(
                    packageName = packageName,
                    appName = appName,
                    activityName = activityName
                )
            } else null
        }.sortedBy { it.appName.lowercase() }
    }

    fun getAppIcon(packageName: String): Drawable? {
        return try {
            pm.getApplicationIcon(packageName)
        } catch (e: Exception) {
            null
        }
    }

    fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            return drawable.bitmap
        }
        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 96
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 96
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    fun createPinnedShortcut(
        targetPackageName: String,
        targetActivityName: String,
        label: String,
        iconBitmap: Bitmap
    ): Boolean {
        if (!ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
            return false
        }

        val launchIntent = pm.getLaunchIntentForPackage(targetPackageName) ?: Intent().apply {
            setClassName(targetPackageName, targetActivityName)
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP

        val shortcutInfo = ShortcutInfoCompat.Builder(context, "shortcut_${UUID.randomUUID()}")
            .setShortLabel(if (label.isBlank()) " " else label)
            .setIcon(IconCompat.createWithBitmap(iconBitmap))
            .setIntent(launchIntent)
            .build()

        return ShortcutManagerCompat.requestPinShortcut(context, shortcutInfo, null)
    }
}
