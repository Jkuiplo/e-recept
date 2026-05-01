@file:Suppress("DEPRECATION")

package com.google.eRecept.core.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme =
    lightColorScheme(
        background = Color(0xFFF0F4FA),
        onBackground = Color(0xFF1A2B45) ,

        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF1A2B45) ,

        primary = Color(0xFF1A73E8) ,
        onPrimary = Color.White,

        secondary = Color(0xFF0288D1),
        onSecondary = Color.White,

        tertiary = Color(0xFFE3F2FD),
        onTertiary = Color(0xFF0D47A1),

        surfaceVariant = Color(0xFFE1E9F5),
        onSurfaceVariant = Color(0xFF5B7299),
    )

private val DarkColorScheme =
    darkColorScheme(
        background = Color(0xFF0D1117),
        onBackground = Color(0xFFE8EFF9),

        surface = Color(0xFF161D2A),
        onSurface = Color(0xFFE8EFF9),

        primary = Color(0xFF4A9EFF),
        onPrimary = Color.White,

        secondary = Color(0xFF29B6F6),
        onSecondary = Color(0xFF0A1929),

        tertiary = Color(0xFF1E3A5F),
        onTertiary = Color(0xFF90C4F9),

        surfaceVariant = Color(0xFF1C2B3E),
        onSurfaceVariant = Color(0xFF8FAEC8),
    )
@Composable
fun EreceptTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }

            darkTheme -> {
                DarkColorScheme
            }

            else -> {
                LightColorScheme
            }
        }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context as? Activity
            if (activity != null && !activity.isFinishing && !activity.isDestroyed) {
                val window = activity.window
                window.statusBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
