package org.octanelab.toolapp.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = IOSBlue,
    secondary = IOSPurple,
    tertiary = IOSGreen,
    background = IOSBackground,
    surface = IOSGroupedBackground,
    surfaceVariant = IOSCardBackground,
    onPrimary = IOSTextPrimary,
    onSecondary = IOSTextPrimary,
    onBackground = IOSTextPrimary,
    onSurface = IOSTextPrimary,
    onSurfaceVariant = IOSTextSecondary,
    error = IOSRed
)

private val LightColorScheme = darkColorScheme(
    primary = IOSBlue,
    secondary = IOSPurple,
    tertiary = IOSGreen,
    background = IOSBackground,
    surface = IOSGroupedBackground,
    surfaceVariant = IOSCardBackground,
    onPrimary = IOSTextPrimary,
    onSecondary = IOSTextPrimary,
    onBackground = IOSTextPrimary,
    onSurface = IOSTextPrimary,
    onSurfaceVariant = IOSTextSecondary,
    error = IOSRed
)

@Composable
fun ToolAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = IOSBackground.toArgb()
            window.navigationBarColor = IOSBackground.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}