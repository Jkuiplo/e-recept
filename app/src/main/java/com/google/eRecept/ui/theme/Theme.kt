package com.google.eRecept.ui.theme

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

// Твои базовые цвета из Color.kt
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

// Заглушка для темной темы (потом мы сможем добавить в Color.kt темные оттенки)
private val DarkColorScheme =
    darkColorScheme(
        // Пока оставим стандартной, Compose заполнит ее базовыми темными цветами
    )

@Composable
fun EreceptTheme(
    // Проверяем, включена ли на телефоне темная тема
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Включаем поддержку динамических цветов
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    // Магия выбора цвета
    val colorScheme =
        when {
            // Если динамические цвета включены и версия Android 12+ (уровень S)
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                // Берем либо темную, либо светлую динамическую палитру
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }

            // Если Android старый, но включена темная тема
            darkTheme -> {
                DarkColorScheme
            }

            // Если Android старый и включена светлая тема (твоя палитра!)
            else -> {
                LightColorScheme
            }
        }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Красим статус-бар (верхнюю полоску с батареей) в цвет фона
            window.statusBarColor = colorScheme.background.toArgb()
            // Делаем иконки статус-бара темными для светлой темы и светлыми для темной
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Убедись, что файл Type.kt с Typography тоже есть в проекте
        content = content,
    )
}
