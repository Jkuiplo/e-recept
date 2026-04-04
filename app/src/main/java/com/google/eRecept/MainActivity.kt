package com.google.eRecept

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.google.eRecept.ui.screens.MainScreen
import com.google.eRecept.ui.theme.EreceptTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EreceptTheme {
                MainScreen()
            }
        }
    }
}
