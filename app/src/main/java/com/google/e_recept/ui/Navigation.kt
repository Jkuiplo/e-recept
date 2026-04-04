package com.google.e_recept.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.ui.graphics.vector.ImageVector

enum class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    Home("home", "Главная", Icons.Default.Home),
    Patients("patients", "Пациенты", Icons.Default.People),
    Recipes("recipes", "Рецепты", Icons.Default.Assignment),
    History("history", "История", Icons.Default.History),
    Profile("profile", "Профиль", Icons.Default.AccountCircle)
}