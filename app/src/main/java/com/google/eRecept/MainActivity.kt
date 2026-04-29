package com.google.eRecept

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.google.eRecept.feature.profile.ChangePasswordScreen
import com.google.eRecept.feature.auth.ui.ForgotPasswordScreen
import com.google.eRecept.feature.auth.ui.LoginScreen
import com.google.eRecept.feature.auth.ui.ResetPasswordScreen
import com.google.eRecept.core.theme.EreceptTheme
import com.google.eRecept.feature.auth.AuthViewModel
import com.google.eRecept.feature.profile.ChangePasswordViewModel
import com.google.eRecept.feature.profile.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            // Получаем ProfileViewModel прямо на уровне Activity
            val profileViewModel: ProfileViewModel = viewModel()
            val themeMode by profileViewModel.themeMode.collectAsState()

            // Определяем, какую тему рисовать прямо сейчас
            val isSystemDark = isSystemInDarkTheme()
            val useDarkTheme =
                when (themeMode) {
                    0 -> false

                    // Принудительно светлая
                    1 -> true

                    // Принудительно темная
                    else -> isSystemDark
                }

            EreceptTheme(darkTheme = useDarkTheme) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val focusManager = LocalFocusManager.current
                    val navController = rememberNavController()
                    val authViewModel: AuthViewModel = viewModel()

                    val context = LocalContext.current
                    val prefs = context.getSharedPreferences("erecept_prefs", MODE_PRIVATE)
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
                                deepLinks =
                                    listOf(
                                        navDeepLink { uriPattern = "https://e-recepta.vercel.app/reset-password?token={token}" },
                                    ),
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
                                MainScreen(
                                    onLogout = {
                                        authViewModel.logout()
                                        navController.navigate("login") {
                                            popUpTo("main") { inclusive = true }
                                        }
                                    },
                                    onChangePasswordClick = {
                                        navController.navigate("change_password")
                                    },
                                    profileViewModel = profileViewModel,
                                )
                            }
                            composable("change_password") {
                                val changePasswordViewModel: ChangePasswordViewModel = hiltViewModel()

                                ChangePasswordScreen(
                                    onNavigateBack = { navController.popBackStack() },
                                    viewModel = changePasswordViewModel,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
