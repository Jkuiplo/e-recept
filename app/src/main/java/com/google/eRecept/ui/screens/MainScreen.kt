package com.google.eRecept.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.eRecept.ui.viewmodels.HomeViewModel
import com.google.eRecept.ui.viewmodels.RecipeViewModel
import com.google.eRecept.ui.viewmodels.SearchViewModel

@Composable
fun MainScreen(onLogout: () -> Unit) {
    val homeViewModel: HomeViewModel = hiltViewModel()
    val recipeViewModel: RecipeViewModel = hiltViewModel()
    val searchViewModel: SearchViewModel = hiltViewModel()

    val tabs = listOf("Расписание", "Рецепты", "Поиск", "Профиль")
    val icons =
        listOf(
            Icons.Default.CalendarToday,
            Icons.Default.ListAlt,
            Icons.Default.Search,
            Icons.Default.Person,
        )

    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    BackHandler(enabled = selectedTab != 0) {
        selectedTab = 0
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ) {
                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        icon = { Icon(icons[index], contentDescription = title) },
                        label = { Text(title) },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                    )
                }
            }
        },
    ) { paddingValues ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            when (selectedTab) {
                0 -> {
                    HomeScreen(
                        viewModel = homeViewModel,
                        onProfileClick = { selectedTab = 3 },
                        onCreateRecipeClick = { iin ->
                            recipeViewModel.openCreateSheet(iin)
                            selectedTab = 1
                        },
                    )
                }

                1 -> {
                    RecipeScreen(
                        viewModel = recipeViewModel,
                        homeViewModel = homeViewModel,
                    )
                }

                2 -> {
                    SearchScreen(viewModel = searchViewModel)
                }

                3 -> {
                    ProfileScreen(onLogout = onLogout)
                }
            }
        }
    }
}
