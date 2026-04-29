package com.google.eRecept

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import com.google.eRecept.feature.home.HomeScreen
import com.google.eRecept.core.navigation.BottomNavItem
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
    profileViewModel: ProfileViewModel,
) {
    val homeViewModel: HomeViewModel = hiltViewModel()
    val recipeViewModel: RecipeViewModel = hiltViewModel()
    val searchViewModel: SearchViewModel = hiltViewModel()

    val navItems = BottomNavItem.entries.toTypedArray()

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
                navItems.forEachIndexed { index, item ->
                    val isSelected = selectedTab == index
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.title,
                            )
                        },
                        label = { Text(item.title) },
                        selected = isSelected,
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
                    RecipeScreen(viewModel = recipeViewModel, homeViewModel = homeViewModel)
                }

                2 -> {
                    SearchScreen(
                        viewModel = searchViewModel,
                        recipeViewModel = recipeViewModel,
                        onEditRecipe = {
                            selectedTab = 1
                        },
                    )
                }

                3 -> {
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
