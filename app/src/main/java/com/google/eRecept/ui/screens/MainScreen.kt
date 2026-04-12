package com.google.eRecept.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.google.eRecept.ui.BottomNavItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainScreen() {
    val items = BottomNavItem.entries
    var selectedIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = if (selectedIndex == index) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.title
                            )
                        },
                        label = { Text(item.title) },
                        selected = selectedIndex == index,
                        onClick = { selectedIndex = index }
                    )
                }
            }
        },
    ) { innerPadding ->
        AnimatedContent(
            targetState = selectedIndex,
            transitionSpec = {
                fadeIn(animationSpec = tween(220, delayMillis = 90)) togetherWith
                        fadeOut(animationSpec = tween(90))
            },
            modifier = Modifier.padding(innerPadding),
            label = "ScreenTransition"
        ) { targetIndex ->
            when (items[targetIndex]) {
                BottomNavItem.Schedule -> HomeScreen()
                BottomNavItem.Recipes -> RecipeScreen()
                BottomNavItem.Search -> SearchScreen()
                BottomNavItem.Profile -> ProfileScreen()
            }
        }
    }
}
