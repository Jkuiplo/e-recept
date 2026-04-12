package com.google.eRecept.ui.screens

import ProfileScreen
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun MainScreen() {
    val tabs = listOf("Расписание", "Рецепты", "Поиск", "Профиль")
    val icons =
        listOf(
            Icons.Default.CalendarToday,
            Icons.Default.ListAlt, // Иконка для рецептов (замени на нужную)
            Icons.Default.Search,
            Icons.Default.Person,
        )

    // Используем rememberPagerState для свайпа между экранами
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
                                // Главный фикс производительности:
                                // Если разница между текущим и выбранным табом больше 1,
                                // прыгаем без анимации (чтобы не рендерить промежуточные тяжелые экраны)
                                if (abs(pagerState.currentPage - index) > 1) {
                                    pagerState.scrollToPage(index)
                                } else {
                                    // Если соседний таб — анимируем скролл
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
            // userScrollEnabled = false // раскомментируй, если захочешь отключить свайп пальцем
        ) { page ->
            // Отрисовываем нужный экран в зависимости от текущей страницы
            when (page) {
                0 -> {
                    HomeScreen(
                        onProfileClick = {
                            coroutineScope.launch { pagerState.scrollToPage(3) }
                        },
                        onCreateRecipeClick = {
                            coroutineScope.launch { pagerState.scrollToPage(1) }
                        },
                        // ... остальные коллбеки
                    )
                }

                1 -> {
                    RecipeScreen()
                }

                // Заглушка, так как кода рецептов у нас пока нет
                2 -> {
                    SearchScreen()
                }

                3 -> {
                    ProfileScreen()
                }
            }
        }
    }
}
