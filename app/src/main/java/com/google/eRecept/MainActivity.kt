package com.google.eRecept

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.eRecept.ui.screens.MainScreen
import com.google.eRecept.ui.screens.authorization.LoginScreen
import com.google.eRecept.ui.theme.EreceptTheme
import com.google.eRecept.ui.viewmodels.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            EreceptTheme {
                val focusManager = LocalFocusManager.current
                val navController = rememberNavController()
                val authViewModel: AuthViewModel = viewModel()

                val startDestination = "main"

                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTapGestures(onTap = {
                                    focusManager.clearFocus()
                                })
                            },
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = startDestination,
                    ) {
                        composable("login") {
                            LoginScreen(onLoginSuccess = {
                                navController.navigate("main") {
                                    popUpTo("login") { inclusive = true }
                                }
                            })
                        }
                        composable("main") {
                            MainScreen(onLogout = {
                                authViewModel.logout()
                                navController.navigate("login") {
                                    popUpTo("main") { inclusive = true }
                                }
                            })
                        }
                    }
                }
            }
        }
    }
}
