package com.google.eRecept

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.eRecept.ui.screens.LoginScreen
import com.google.eRecept.ui.screens.MainScreen
import com.google.eRecept.ui.theme.EreceptTheme
import com.google.eRecept.ui.viewmodels.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Включаем edge-to-edge для корректной работы с инсетами
        enableEdgeToEdge()
        
        setContent {
            EreceptTheme {
                val navController = rememberNavController()
                val authViewModel: AuthViewModel = viewModel()
                
                // Используем remember для startDestination, чтобы предотвратить
                // пересоздание NavHost при изменении authState. Это стабилизирует
                // жизненный цикл окон и предотвращает DeadObjectException при переходах.
                val startDestination = remember {
                    if (authViewModel.authState.value is AuthViewModel.AuthState.Authenticated) "main" else "login"
                }

                NavHost(
                    navController = navController,
                    startDestination = startDestination
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
