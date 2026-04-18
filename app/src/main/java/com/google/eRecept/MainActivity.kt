package com.google.eRecept

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.google.eRecept.ui.screens.MainScreen
import com.google.eRecept.ui.screens.authorization.ForgotPasswordScreen
import com.google.eRecept.ui.screens.authorization.LoginScreen
import com.google.eRecept.ui.screens.authorization.ResetPasswordScreen
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

                val context = LocalContext.current
                val prefs = context.getSharedPreferences("erecept_prefs", Context.MODE_PRIVATE)
                val hasToken = !prefs.getString("access_token", null).isNullOrBlank()

                val startDestination = if (hasToken) "main" else "login"

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
                            LoginScreen(
                                onLoginSuccess = {
                                    navController.navigate("main") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                },
                                onNavigateToForgot = {
                                    navController.navigate("forgot_password")
                                },
                            )
                        }

                        composable("forgot_password") {
                            ForgotPasswordScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToReset = {
                                    navController.popBackStack("login", inclusive = false)
                                },
                            )
                        }

                        // Deep linking тут
                        composable(
                            route = "reset_password?token={token}",
                            arguments = listOf(navArgument("token") { type = NavType.StringType }),
                            deepLinks = listOf(navDeepLink { uriPattern = "https://erecept.kz/reset-password?token={token}" }),
                        ) { backStackEntry ->
                            val token = backStackEntry.arguments?.getString("token") ?: ""

                            ResetPasswordScreen(
                                token = token,
                                onNavigateBack = { navController.popBackStack("login", inclusive = false) },
                                onResetSuccess = {
                                    navController.navigate("login") {
                                        popUpTo(0)
                                    }
                                },
                            )
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
