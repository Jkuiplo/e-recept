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
        background = MainBg,
        onBackground = MainTx,
        surface = SecBg,
        onSurface = SecTx,
        primary = PrimaryPurple,
        onPrimary = Color.White,
        secondary = MainAc,
        onSecondary = MainTx,
        tertiary = SecAc,
        onTertiary = MainTx,
    )

private val DarkColorScheme =
    darkColorScheme(
        // Пока оставим стандартной, Compose заполнит ее базовыми темными цветами
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
