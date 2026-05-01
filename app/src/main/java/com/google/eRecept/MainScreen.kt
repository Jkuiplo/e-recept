package com.google.eRecept

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.eRecept.core.navigation.BottomNavItem
import com.google.eRecept.feature.home.HomeScreen
import com.google.eRecept.feature.home.HomeViewModel
import com.google.eRecept.feature.profile.ProfileScreen
import com.google.eRecept.feature.profile.ProfileViewModel
import com.google.eRecept.feature.recipe.RecipeScreen
import com.google.eRecept.feature.recipe.RecipeViewModel
import com.google.eRecept.feature.search.SearchScreen
import com.google.eRecept.feature.search.SearchViewModel

@Composable
fun MainScreen(
    onLogout: () -> Unit,
    onChangePasswordClick: () -> Unit,
    onNavigateToCreateAppointment: () -> Unit,
    onNavigateToCreateRecipe: (String) -> Unit, // <-- Now expects a String (IIN)
    onEditRecipe: () -> Unit,
    profileViewModel: ProfileViewModel,

) {
    val homeViewModel: HomeViewModel = hiltViewModel()
    val recipeViewModel: RecipeViewModel = hiltViewModel()
    val searchViewModel: SearchViewModel = hiltViewModel()

    val bottomNavController = rememberNavController()

    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val navItems = BottomNavItem.entries.toTypedArray()

    Scaffold(
        bottomBar = {
            NavigationBar {
                navItems.forEach { item ->
                    val itemRoute = item.name.lowercase()
                    val isSelected = currentRoute == itemRoute

                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.title,
                            )
                        },
                        label = { Text(item.title) },
                        selected = isSelected,
                        onClick = {
                            bottomNavController.navigate(itemRoute) {
                                popUpTo(bottomNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                    )
                }
            }
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            NavHost(
                navController = bottomNavController,
                startDestination = BottomNavItem.Schedule.route,
            ) {
                composable(BottomNavItem.Schedule.route) {
                    HomeScreen(
                        viewModel = homeViewModel,
                        onProfileClick = {
                            bottomNavController.navigate(BottomNavItem.Profile.route)
                        },
                        onNavigateToCreateAppointment = onNavigateToCreateAppointment,
                        onCreateRecipeClick = { iin ->
                            onNavigateToCreateRecipe(iin)
                        }
                    )
                }

                composable(BottomNavItem.Recipes.route) {
                    RecipeScreen(
                        viewModel = recipeViewModel,
                        onNavigateToCreateRecipe = { onNavigateToCreateRecipe("") },
                        onEditRecipe = onEditRecipe )
                }

                composable(BottomNavItem.Search.route) {
                    SearchScreen(
                        viewModel = searchViewModel,
                        recipeViewModel = recipeViewModel,
                        onEditRecipe = onEditRecipe,
                    )
                }

                composable(BottomNavItem.Profile.route) {
                    ProfileScreen(
                        onLogout = onLogout,
                        onChangePasswordClick = onChangePasswordClick,
                        viewModel = profileViewModel,

                    )
                }
            }
        }
    }
}