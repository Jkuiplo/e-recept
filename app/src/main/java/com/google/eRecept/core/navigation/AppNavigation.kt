package com.google.eRecept.core.navigation

import android.content.Context.MODE_PRIVATE
import android.widget.Toast
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.google.eRecept.MainScreen
import com.google.eRecept.feature.auth.AuthViewModel
import com.google.eRecept.feature.auth.ui.ForgotPasswordScreen
import com.google.eRecept.feature.auth.ui.LoginScreen
import com.google.eRecept.feature.auth.ui.ResetPasswordScreen
import com.google.eRecept.feature.home.CreateAppointmentScreen
import com.google.eRecept.feature.home.HomeViewModel
import com.google.eRecept.feature.profile.ChangePasswordScreen
import com.google.eRecept.feature.profile.ChangePasswordViewModel
import com.google.eRecept.feature.profile.ProfileViewModel
import com.google.eRecept.feature.recipe.CreateRecipeScreen
import com.google.eRecept.feature.recipe.EditRecipeScreen
import com.google.eRecept.feature.recipe.RecipeViewModel
import com.google.eRecept.feature.search.MedicationDetailsScreen
import com.google.eRecept.feature.search.PatientDetailsScreen
import com.google.eRecept.feature.search.SearchViewModel
import com.google.eRecept.feature.experimental.ExperimentalFeaturesScreen
import com.google.eRecept.feature.experimental.ExperimentalFeaturesViewModel

@Composable
fun RootNavGraph(
    profileViewModel: ProfileViewModel,
    navController: NavHostController
){
    val focusManager = LocalFocusManager.current
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
                    onChangePasswordClick = { navController.navigate("change_password") },
                    onNavigateToCreateAppointment = { date ->
                        navController.navigate("create_appointment?date=$date")
                    },
                    onNavigateToCreateRecipe = { iin ->
                        if (iin.isNotBlank()) {
                            navController.navigate("create_recipe?iin=$iin")
                        } else {
                            navController.navigate("create_recipe")
                        }
                    },
                    onEditRecipe = { navController.navigate("edit_recipe") },
                    onNavigateToPatientDetails = { iin ->
                        navController.navigate("patient_details/$iin")
                    },
                    onNavigateToMedicationDetails = { id ->
                        navController.navigate("medication_details/$id")
                    },
                    onNavigateToExperimental = {
                        navController.navigate("experimental_features")
                    },
                    profileViewModel = profileViewModel,
                )
            }

            composable(
                route = "patient_details/{iin}",
                arguments = listOf(navArgument("iin") { type = NavType.StringType })
            ) { backStackEntry ->
                val iin = backStackEntry.arguments?.getString("iin") ?: ""
                val searchViewModel: SearchViewModel = hiltViewModel()
                val homeViewModel: HomeViewModel = hiltViewModel()
                val recipeViewModel: RecipeViewModel = hiltViewModel()

                PatientDetailsScreen(
                    iin = iin,
                    searchViewModel = searchViewModel,
                    homeViewModel = homeViewModel,
                    recipeViewModel = recipeViewModel,
                    onNavigateBack = { navController.popBackStack() },
                )
            }

            composable(
                route = "medication_details/{medicationId}",
                arguments = listOf(navArgument("medicationId") { type = NavType.StringType })
            ) { backStackEntry ->
                val medicationId = backStackEntry.arguments?.getString("medicationId") ?: ""
                val searchViewModel: SearchViewModel = hiltViewModel()

                MedicationDetailsScreen(
                    medicationId = medicationId,
                    viewModel = searchViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(
                route = "create_appointment?date={date}",
                arguments = listOf(navArgument("date") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                })
            ) { backStackEntry ->
                val homeViewModel: HomeViewModel = hiltViewModel()
                val passedDate = backStackEntry.arguments?.getString("date")

                CreateAppointmentScreen(
                    viewModel = homeViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    passedDate = passedDate
                )
            }

            composable(
                route = "create_recipe?iin={iin}",
                arguments = listOf(navArgument("iin") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                })
            ) { backStackEntry ->
                val parentEntry = remember(backStackEntry) { navController.getBackStackEntry("main") }
                val recipeViewModel: RecipeViewModel = hiltViewModel(parentEntry)
                val homeViewModel: HomeViewModel = hiltViewModel(parentEntry)

                val passedIin = backStackEntry.arguments?.getString("iin")

                LaunchedEffect(passedIin) {
                    if (!passedIin.isNullOrBlank()) {
                        recipeViewModel.updateDraftIin(passedIin)
                    }
                }

                CreateRecipeScreen(
                    viewModel = recipeViewModel,
                    homeViewModel = homeViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("edit_recipe") { backStackEntry ->
                val parentEntry = remember(backStackEntry) { navController.getBackStackEntry("main") }
                val recipeViewModel: RecipeViewModel = hiltViewModel(parentEntry)
                val homeViewModel: HomeViewModel = hiltViewModel(parentEntry)

                EditRecipeScreen(
                    viewModel = recipeViewModel,
                    homeViewModel = homeViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable("change_password") {
                val changePasswordViewModel: ChangePasswordViewModel = hiltViewModel()

                ChangePasswordScreen(
                    onNavigateBack = { navController.popBackStack() },
                    viewModel = changePasswordViewModel,
                )
            }
            composable("experimental_features") {
                val experimentalViewModel: ExperimentalFeaturesViewModel = hiltViewModel()

                ExperimentalFeaturesScreen(
                    onNavigateBack = { navController.popBackStack() },
                    viewModel = experimentalViewModel,
                )
            }
        }
    }
}