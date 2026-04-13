package com.google.eRecept.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.eRecept.ui.viewmodels.HomeViewModel
import com.google.eRecept.ui.viewmodels.RecipeViewModel
import com.google.eRecept.ui.viewmodels.SearchViewModel
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun MainScreen(onLogout: () -> Unit) {
    // Поднимаем ViewModels, чтобы они жили вместе с MainScreen
    val homeViewModel: HomeViewModel = viewModel()
    val recipeViewModel: RecipeViewModel = viewModel()
    val searchViewModel: SearchViewModel = viewModel()

    val tabs = listOf("Расписание", "Рецепты", "Поиск", "Профиль")
    val icons =
        listOf(
            Icons.Default.CalendarToday,
            Icons.Default.ListAlt,
            Icons.Default.Search,
            Icons.Default.Person,
        )

    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()

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
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                if (abs(pagerState.currentPage - index) > 1) {
                                    pagerState.scrollToPage(index)
                                } else {
                                    pagerState.animateScrollToPage(index)
                                }
                            }
                        },
                    )
                }
            }
        },
    ) { paddingValues ->
        HorizontalPager(
            state = pagerState,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            beyondViewportPageCount = 3 // Держим все вкладки в памяти для стабильности
        ) { page ->
            when (page) {
                0 -> {
                    HomeScreen(
                        viewModel = homeViewModel,
                        onProfileClick = {
                            coroutineScope.launch { pagerState.scrollToPage(3) }
                        },
                        onCreateRecipeClick = {
                            coroutineScope.launch { pagerState.scrollToPage(1) }
                        },
                    )
                }

                1 -> {
                    RecipeScreen(viewModel = recipeViewModel)
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
