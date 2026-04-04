package com.google.e_recept

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.google.e_recept.ui.screens.LoginScreen // Импортируем наш новый экран
import com.google.e_recept.ui.theme.EreceptTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EreceptTheme {
                // Просто вызываем наш экран здесь
                LoginScreen()
            }
        }
    }
}