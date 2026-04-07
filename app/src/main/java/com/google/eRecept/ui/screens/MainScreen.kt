package com.google.eRecept.ui.screens

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.eRecept.ui.BottomNavItem

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.onBackground,
                tonalElevation = 8.dp, // Добавляем мягкую тень над навигацией
            ) {
                BottomNavItem.entries.forEach { item ->
                    val isSelected = currentRoute == item.route

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (!isSelected) {
                                navController.navigate(item.route) {
                                    // Избегаем создания бесконечного стека при кликах по табам
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    // Предотвращаем создание дубликатов одного экрана
                                    launchSingleTop = true
                                    // Восстанавливаем состояние (например, скролл), если возвращаемся на таб
                                    restoreState = true
                                }
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
                                // Цвет иконки внутри активного "пузыря"
                                selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.onBackground,
                                // Цвет самого "пузыря" выделения (используем мягкий акцентный цвет)
                                indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Schedule.route,
            modifier = Modifier.padding(innerPadding),
            // Настраиваем плавные анимации перехода: экраны будут мягко растворяться друг в друге
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) },
            popEnterTransition = { fadeIn(animationSpec = tween(300)) },
            popExitTransition = { fadeOut(animationSpec = tween(300)) },
        ) {
            // Временно подставляем старые экраны в новые роуты.
            // Позже мы их полностью перепишем.

            composable(BottomNavItem.Schedule.route) {
                HomeScreen() // Будущее "Расписание"
            }

            composable(BottomNavItem.Recipes.route) {
                RecipeScreen()
            }

            composable(BottomNavItem.Search.route) {
                // Будущий экран поиска с двумя вкладками
            }

            composable(BottomNavItem.Profile.route) {
                ProfileScreen(onLogoutClick = { /* TODO */ })
            }
        }
    }
}
