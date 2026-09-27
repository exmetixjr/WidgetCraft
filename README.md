# WidgetCraft 🎨

An open-source, private, ad-free Android home screen customization suite built with **Kotlin**, **Jetpack Compose**, and **AppWidgetProvider**.

---

## ✨ Features

### 1. Custom Photo & Collage Widgets
* **Single & Multi-Photo Collages**: Support for Single, Split Horizontal (2 photos), Split Vertical (2 photos), 2x2 Grid (4 photos), and Masonry layouts (1+2, 2+1).
* **Custom Mask Shapes**: Squircle, Rounded Rectangle (0–60dp radius), Full Circle, Pill, Sharp Rectangle, and Polaroid styles.
* **Styling**: Customizable border width, border colors (palette + custom hex), opacity/dimming, and background colors.
* **Tap Actions**:
  * Tap to cycle through next photos in an album
  * Tap to open system gallery
  * Tap to launch target application
  * Tap to open web URL

### 2. Scalable Clock & Battery Widgets
* **Typography Styles**: Bold Editorial Serif, Clean Minimalist, Retro Terminal / Hacker, and Analog Clock dials with hour/minute hands.
* **Live Indicators**: Integrated live battery level bar/percentage and current date line.
* **Formats**: 12-hour (with AM/PM) or 24-hour time.
* **System Efficiency**: Listens to system time ticks and battery changes without high-frequency polling, preserving your phone's battery life.

### 3. Custom App Icon Changer (Zero Ads, Zero Subscriptions)
* **Micro-Widget Method (Recommended)**:
  * Creates a borderless 1x1 home screen widget displaying your chosen image or icon.
  * Tapping launches the target app instantly.
  * **Result**: **Zero shortcut watermark/arrow badge** on virtually any launcher (Samsung One UI, Google Pixel, Xiaomi HyperOS, Nova Launcher, etc.).
* **Native Pinned Shortcut Method**:
  * Directly requests standard Android shortcuts via `ShortcutManager`.
* **In-App App Picker**:
  * Lists and searches all launchable apps installed on your device.
  * Pick custom graphics from your gallery or apply custom mask shapes to the original app icon.
  * Custom labels (or blank label for a clean, minimalist aesthetic).

---

## 🛠️ How to Build

### Option A: GitHub Actions (Recommended & Fastest)
1. Push this repository to GitHub:
   ```bash
   git remote add origin https://github.com/<your-username>/<repo-name>.git
   git push -u origin main
   ```
2. GitHub Actions will automatically run `.github/workflows/build-apk.yml`.
3. Go to the **Actions** tab on GitHub, click the latest build, and download `WidgetCraft-Debug-APK.zip` to install directly on your Android device!

### Option B: Local Gradle Build
```bash
./gradlew assembleDebug
```
The compiled APK will be located at:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 📱 License
MIT License. Free and open source forever.
