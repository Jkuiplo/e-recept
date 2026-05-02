package com.google.eRecept

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.eRecept.core.navigation.BottomNavItem
import com.google.eRecept.feature.home.HomeScreen
import com.google.eRecept.feature.home.HomeViewModel
import com.google.eRecept.feature.profile.ProfileScreen
import com.google.eRecept.feature.profile.ProfileViewModel
import com.google.eRecept.feature.recipe.RecipeScreen
import com.google.eRecept.feature.recipe.RecipeViewModel
import com.google.eRecept.feature.search.SearchScreen
import com.google.eRecept.feature.search.SearchViewModel
import kotlinx.coroutines.launch

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

    val navItems = BottomNavItem.entries.toTypedArray()
    val pagerState = rememberPagerState(pageCount = { navItems.size })
    val coroutineScope = rememberCoroutineScope()

    val isParentNavigating = pagerState.isScrollInProgress

    Scaffold(
        bottomBar = {
            NavigationBar {
                navItems.forEachIndexed { index, item ->
                    val isSelected = pagerState.currentPage == index

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
                            coroutineScope.launch {
                                pagerState.scrollToPage(index)
                            }
                        },
                    )
                }
            }
        },
    ) { paddingValues ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            beyondViewportPageCount = 1,
            flingBehavior = PagerDefaults.flingBehavior(
                state = pagerState,
                snapAnimationSpec = spring(
                    stiffness = Spring.StiffnessMedium,
                    dampingRatio = Spring.DampingRatioNoBouncy
                ),
                snapPositionalThreshold = 0.15f
            )
        ) { page ->
            when (navItems[page]) {
                BottomNavItem.Schedule -> {
                    HomeScreen(
                        viewModel = homeViewModel,
                        onProfileClick = {
                            coroutineScope.launch { pagerState.scrollToPage(3) }
                        },
                        onNavigateToCreateAppointment = onNavigateToCreateAppointment,
                        onCreateRecipeClick = { iin ->
                            onNavigateToCreateRecipe(iin)
                        },
                        isParentNavigating = isParentNavigating
                    )
                }

                BottomNavItem.Recipes -> {
                    RecipeScreen(
                        viewModel = recipeViewModel,
                        onNavigateToCreateRecipe = { onNavigateToCreateRecipe("") },
                        onEditRecipe = onEditRecipe
                    )
                }

                BottomNavItem.Search -> {
                    SearchScreen(
                        viewModel = searchViewModel,
                        recipeViewModel = recipeViewModel,
                        onEditRecipe = onEditRecipe,
                        isParentNavigating = isParentNavigating
                    )
                }

                BottomNavItem.Profile -> {
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