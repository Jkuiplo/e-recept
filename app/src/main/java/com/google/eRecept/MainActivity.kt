package com.google.eRecept

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.google.eRecept.core.theme.EreceptTheme
import com.google.eRecept.feature.profile.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import com.google.eRecept.core.navigation.RootNavGraph

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val profileViewModel: ProfileViewModel = viewModel()
            val themeMode by profileViewModel.themeMode.collectAsState()

            val isSystemDark = isSystemInDarkTheme()
            val useDarkTheme =
                when (themeMode) {
                    0 -> false

                    1 -> true

                    else -> isSystemDark
                }

            val navController = rememberNavController()

            EreceptTheme(darkTheme = useDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RootNavGraph(
                        profileViewModel = profileViewModel,
                        navController = navController
                    )
                }
            }
        }
    }
}
