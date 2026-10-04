package com.example.lunex.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = LunexAccent,
    onPrimary = LunexText,
    primaryContainer = LunexMineBubble,
    onPrimaryContainer = LunexText,
    secondary = LunexAccentDark,
    onSecondary = LunexText,
    background = LunexBg,
    onBackground = LunexText,
    surface = LunexPanel,
    onSurface = LunexText,
    surfaceVariant = LunexPanelElevated,
    onSurfaceVariant = LunexMuted,
    outline = LunexLine,
    error = LunexFull,
    onError = LunexText
)

@Composable
fun LunexTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = LunexBg.toArgb()
                window.navigationBarColor = LunexBg.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
