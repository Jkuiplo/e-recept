package com.google.eRecept.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.eRecept.ui.BottomNavItem
import com.google.eRecept.ui.screens.PatientsScreen.AddPatientScreen
import com.google.eRecept.ui.screens.PatientsScreen.PatientsScreen
import com.google.eRecept.ui.theme.MainAc

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier.padding(horizontal = 5.dp),
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.onBackground,
                tonalElevation = 0.dp,
            ) {
                BottomNavItem.entries.forEach { item ->
                    val isSelected =
                        currentRoute == item.route ||
                            (item.route == BottomNavItem.Patients.route && currentRoute == "add_patient")

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            val icon = if (isSelected) item.selectedIcon else item.unselectedIcon
                            Icon(imageVector = icon, contentDescription = item.title)
                        },
                        label = {
                            Text(
                                text = item.title,
                                style =
                                    MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    ),
                            )
                        },
                        colors =
                            NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onBackground,
                                selectedTextColor = MaterialTheme.colorScheme.onBackground,
                                indicatorColor = MainAc,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurface,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurface,
                            ),
                    )
                }
            }
        },
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(BottomNavItem.Home.route) {
                HomeScreen()
            }

            composable(BottomNavItem.Patients.route) {
                PatientsScreen(
                    onAddPatientClick = {
                        navController.navigate("add_patient")
                    },
                )
            }

            composable("add_patient") {
                AddPatientScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                )
            }

            composable(BottomNavItem.Recipes.route) {
                CreateRecipeScreen()
            }

            composable(BottomNavItem.History.route) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Экран Истории") }
            }

            composable(BottomNavItem.Profile.route) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Экран Профиля") }
            }
        }
    }
}
