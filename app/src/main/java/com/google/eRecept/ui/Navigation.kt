package com.google.eRecept.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Search
import androidx.compose.ui.graphics.vector.ImageVector

enum class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    Schedule(
        route = "schedule",
        title = "Расписание",
        selectedIcon = Icons.Filled.CalendarToday, // Иконка календаря для рабочего дня
        unselectedIcon = Icons.Outlined.CalendarToday,
    ),
    Recipes(
        route = "recipes",
        title = "Рецепты",
        selectedIcon = Icons.AutoMirrored.Filled.Assignment, // Иконка документа (рецепта)
        unselectedIcon = Icons.AutoMirrored.Outlined.Assignment,
    ),
    Search(
        route = "search",
        title = "Поиск",
        selectedIcon = Icons.Filled.Search, // Иконка лупы
        unselectedIcon = Icons.Outlined.Search,
    ),
    Profile(
        route = "profile",
        title = "Профиль",
        selectedIcon = Icons.Filled.AccountCircle, // Иконка силуэта
        unselectedIcon = Icons.Outlined.AccountCircle,
    ),
}
