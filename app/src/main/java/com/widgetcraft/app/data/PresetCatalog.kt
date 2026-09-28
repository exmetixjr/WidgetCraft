package com.widgetcraft.app.data

object PresetCatalog {

    val palettes = listOf(
        ThemePalette("Cyberpunk Neon", "#0B0F19", "#111827", "#06B6D4", "#F43F5E"),
        ThemePalette("Catppuccin Mocha", "#1E1E2E", "#181825", "#CBA6F7", "#CDD6F4"),
        ThemePalette("Nordic Frost", "#2E3440", "#3B4252", "#88C0D0", "#ECEFF4"),
        ThemePalette("Tokyo Night", "#1A1B26", "#16161E", "#7AA2F7", "#A9B1D6"),
        ThemePalette("Obsidian Gold", "#121214", "#18181B", "#F59E0B", "#F4F4F5"),
        ThemePalette("Emerald Forest", "#062E24", "#064E3B", "#10B981", "#ECFDF5"),
        ThemePalette("Dracula Dark", "#282A36", "#21222C", "#BD93F9", "#F8F8F2"),
        ThemePalette("Monochrome Stealth", "#000000", "#18181B", "#FFFFFF", "#E4E4E7")
    )

    fun getDefaultClockPresets(): List<ClockWidgetConfig> {
        return listOf(
            ClockWidgetConfig(
                id = "preset_clock_cyberpunk",
                name = "Tokyo Cyberpunk",
                style = ClockStyle.DIGITAL_SEVEN_SEGMENT,
                is24Hour = true,
                showDate = true,
                showBattery = true,
                showStorage = true,
                showRam = true,
                showWeather = true,
                textColorHex = "#38BDF8",
                accentColorHex = "#F43F5E",
                backgroundColorHex = "#0B0F19",
                cornerRadiusDp = 24f
            ),
            ClockWidgetConfig(
                id = "preset_clock_editorial",
                name = "Editorial Vogue",
                style = ClockStyle.BOLD_EDITORIAL,
                is24Hour = false,
                showDate = true,
                showBattery = true,
                showStorage = false,
                textColorHex = "#FFFFFF",
                accentColorHex = "#F59E0B",
                backgroundColorHex = "#18181B",
                cornerRadiusDp = 28f
            ),
            ClockWidgetConfig(
                id = "preset_clock_minimal",
                name = "Swiss Minimalist",
                style = ClockStyle.MINIMAL,
                is24Hour = false,
                showDate = true,
                showBattery = true,
                textColorHex = "#FFFFFF",
                accentColorHex = "#D0BCFF",
                backgroundColorHex = "#121212",
                cornerRadiusDp = 22f
            ),
            ClockWidgetConfig(
                id = "preset_clock_terminal",
                name = "Hacker Terminal",
                style = ClockStyle.TERMINAL,
                is24Hour = true,
                showDate = true,
                showBattery = true,
                showStorage = true,
                textColorHex = "#A7F3D0",
                accentColorHex = "#10B981",
                backgroundColorHex = "#062E24",
                cornerRadiusDp = 18f
            ),
            ClockWidgetConfig(
                id = "preset_clock_analog",
                name = "Classic Analog Dial",
                style = ClockStyle.ANALOG_CLASSIC,
                is24Hour = false,
                showDate = false,
                showBattery = false,
                textColorHex = "#E4E4E7",
                accentColorHex = "#06B6D4",
                backgroundColorHex = "#18181B",
                cornerRadiusDp = 32f
            )
        )
    }

    fun getDefaultImagePresets(): List<ImageWidgetConfig> {
        return listOf(
            ImageWidgetConfig(
                id = "preset_img_polaroid",
                name = "Vintage Polaroid",
                layout = CollageLayout.SINGLE,
                shape = ShapeType.POLAROID,
                filter = ImageFilterType.VINTAGE,
                cornerRadiusDp = 16f,
                borderWidthDp = 8f,
                borderColorHex = "#F8FAFC",
                backgroundColorHex = "#FFFFFF",
                captionText = "Golden Moments",
                captionColorHex = "#1E293B",
                showDateTag = true
            ),
            ImageWidgetConfig(
                id = "preset_img_bento",
                name = "Bento 4-Photo Grid",
                layout = CollageLayout.GRID_2X2,
                shape = ShapeType.ROUNDED,
                filter = ImageFilterType.WARM,
                cornerRadiusDp = 26f,
                borderWidthDp = 2f,
                borderColorHex = "#38BDF8",
                backgroundColorHex = "#0F172A"
            ),
            ImageWidgetConfig(
                id = "preset_img_squircle",
                name = "Minimal Squircle",
                layout = CollageLayout.SINGLE,
                shape = ShapeType.SQUIRCLE,
                filter = ImageFilterType.NONE,
                cornerRadiusDp = 30f,
                borderWidthDp = 0f,
                backgroundColorHex = "#1E1E1E"
            )
        )
    }

    fun getDefaultNotePresets(): List<NoteWidgetConfig> {
        return listOf(
            NoteWidgetConfig(
                id = "preset_note_yellow",
                name = "Daily Focus",
                title = "Today's Priorities",
                content = "• Finish high-priority deliverable\n• Review code diffs\n• 30-min workout",
                style = NoteStyle.STICKY_YELLOW,
                textColorHex = "#713F12",
                backgroundColorHex = "#FEF08A",
                accentColorHex = "#CA8A04",
                cornerRadiusDp = 20f
            ),
            NoteWidgetConfig(
                id = "preset_note_cyber",
                name = "Dev Scratchpad",
                title = "SYSTEM LOG",
                content = "> git push origin main [OK]\n> Memory: 42% nominal\n> Next: release build v2.1",
                style = NoteStyle.CYBER_TERMINAL,
                textColorHex = "#38BDF8",
                backgroundColorHex = "#0B0F19",
                accentColorHex = "#06B6D4",
                cornerRadiusDp = 18f
            ),
            NoteWidgetConfig(
                id = "preset_note_quote",
                name = "Daily Stoic Quote",
                title = "MINDSET",
                content = "\"We suffer more often in imagination than in reality.\"\n\n— Seneca",
                style = NoteStyle.OBSIDIAN_DARK,
                textColorHex = "#F4F4F5",
                backgroundColorHex = "#18181B",
                accentColorHex = "#A855F7",
                cornerRadiusDp = 24f
            )
        )
    }

    fun getDefaultMusicPresets(): List<MusicWidgetConfig> {
        return listOf(
            MusicWidgetConfig(
                id = "preset_music_nothing",
                name = "Nothing Dot Player",
                style = MusicStyle.NOTHING_DOT,
                trackTitle = "Blinding Lights",
                artistName = "The Weeknd",
                isPlaying = true,
                backgroundColorHex = "#000000",
                textColorHex = "#FFFFFF",
                accentColorHex = "#D71921",
                cornerRadiusDp = 26f
            ),
            MusicWidgetConfig(
                id = "preset_music_vinyl",
                name = "Retro Vinyl Disc",
                style = MusicStyle.VINYL_DISC,
                trackTitle = "Random Access Memories",
                artistName = "Daft Punk",
                isPlaying = true,
                backgroundColorHex = "#141416",
                textColorHex = "#F59E0B",
                accentColorHex = "#EAB308",
                cornerRadiusDp = 24f
            ),
            MusicWidgetConfig(
                id = "preset_music_glass",
                name = "Frosted Glass Player",
                style = MusicStyle.FROSTED_GLASS,
                trackTitle = "Midnight City",
                artistName = "M83",
                isPlaying = false,
                backgroundColorHex = "#1E293B",
                textColorHex = "#FFFFFF",
                accentColorHex = "#38BDF8",
                cornerRadiusDp = 28f
            )
        )
    }

    fun getDefaultBentoPresets(): List<BentoWidgetConfig> {
        return listOf(
            BentoWidgetConfig(
                id = "preset_bento_nothing",
                name = "Nothing Bento Hub",
                style = BentoStyle.NOTHING_OS,
                backgroundColorHex = "#000000",
                accentColorHex = "#D71921",
                textColorHex = "#FFFFFF",
                cornerRadiusDp = 28f
            ),
            BentoWidgetConfig(
                id = "preset_bento_cyber",
                name = "Cyberpunk Telemetry",
                style = BentoStyle.CYBERPUNK_HUD,
                backgroundColorHex = "#090D16",
                accentColorHex = "#06B6D4",
                textColorHex = "#38BDF8",
                cornerRadiusDp = 22f
            ),
            BentoWidgetConfig(
                id = "preset_bento_glass",
                name = "Frosted Bento Suite",
                style = BentoStyle.FROSTED_ACRYLIC,
                backgroundColorHex = "#1E293B",
                accentColorHex = "#A855F7",
                textColorHex = "#FFFFFF",
                cornerRadiusDp = 26f
            )
        )
    }
}

