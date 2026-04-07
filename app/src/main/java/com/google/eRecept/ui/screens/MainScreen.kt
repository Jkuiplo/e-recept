package com.google.eRecept.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.eRecept.ui.BottomNavItem
import com.google.eRecept.ui.theme.MainAc

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    var showMedSearchSheet by remember { mutableStateOf(false) }
    var showAddPatientSheet by remember { mutableStateOf(false) }
    var showCreateRecipeSheet by remember { mutableStateOf(false) }

    val addPatientSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val createRecipeSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier.padding(horizontal = 5.dp),
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.onBackground,
                tonalElevation = 0.dp,
            ) {
                BottomNavItem.entries.forEach { item ->
                    val isSelected = currentRoute?.startsWith(item.route) == true

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
                HomeScreen(
                    onProfileClick = { navController.navigate(BottomNavItem.Profile.route) },
                    onCreateRecipeClick = { showCreateRecipeSheet = true },
                    onSearchPatientsClick = { navController.navigate("${BottomNavItem.Patients.route}?focus=true") },
                    onSearchMedsClick = { showMedSearchSheet = true },
                    onAddPatientClick = { showAddPatientSheet = true }
                )
            }

            composable("${BottomNavItem.Patients.route}?focus={focus}") { backStackEntry ->
                val focus = backStackEntry.arguments?.getString("focus") == "true"
                PatientsScreen(
                    focusSearchOnStart = focus,
                    onAddPatientClick = { showAddPatientSheet = true }
                )
            }

            composable(BottomNavItem.Patients.route) {
                PatientsScreen(
                    focusSearchOnStart = false,
                    onAddPatientClick = { showAddPatientSheet = true }
                )
            }

            composable(BottomNavItem.Recipes.route) {
                CreateRecipeScreen()
            }

            composable(BottomNavItem.History.route) {
                HistoryScreen(
                    onCreateRecipeClick = { showCreateRecipeSheet = true }
                )
            }

            composable(BottomNavItem.Profile.route) {
                ProfileScreen(onLogoutClick = { /* TODO: Логика выхода */ })
            }
        }
    }

    if (showMedSearchSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMedSearchSheet = false },
            containerColor = MaterialTheme.colorScheme.background,
        ) {
            Column(modifier = Modifier.padding(20.dp).fillMaxWidth().height(400.dp)) {
                Text("Поиск препаратов", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    placeholder = { Text("Название препарата") },
                    trailingIcon = { Icon(Icons.Default.Search, "") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                )
            }
        }
    }

    if (showAddPatientSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddPatientSheet = false },
            sheetState = addPatientSheetState,
            containerColor = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxHeight(0.9f),
            dragHandle = null
        ) {
            AddPatientScreenContent(onBackClick = { showAddPatientSheet = false })
        }
    }

    if (showCreateRecipeSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCreateRecipeSheet = false },
            sheetState = createRecipeSheetState,
            containerColor = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxHeight(0.9f),
            dragHandle = null
        ) {
            CreateRecipeContent(onBackClick = { showCreateRecipeSheet = false })
        }
    }
}
