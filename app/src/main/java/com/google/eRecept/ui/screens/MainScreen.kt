package com.google.eRecept.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.eRecept.ui.BottomNavItem
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun MainScreen() {
    val items = BottomNavItem.entries
    val pagerState = rememberPagerState(pageCount = { items.size })
    val scope = rememberCoroutineScope()

    Scaffold(
        bottomBar = {
            CustomBottomNavigation(
                selectedIndex = pagerState.currentPage,
                onItemClick = { index ->
                    scope.launch {
                        val currentPage = pagerState.currentPage

                        // "Умный скролл": если прыгаем больше чем на 1 страницу
                        if (abs(currentPage - index) > 1) {
                            // Незаметно телепортируемся на соседнюю с нужной страницу
                            val jumpTo = if (currentPage < index) index - 1 else index + 1
                            pagerState.scrollToPage(jumpTo)
                        }

                        // Плавно доскролливаем последний шаг
                        pagerState.animateScrollToPage(index)
                    }
                },
            )
        },
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.padding(innerPadding),
            userScrollEnabled = true, // Свайпы включены
        ) { page ->
            when (items[page]) {
                BottomNavItem.Schedule -> HomeScreen()
                BottomNavItem.Recipes -> RecipeScreen()
                BottomNavItem.Search -> SearchScreen()
                BottomNavItem.Profile -> ProfileScreen()
            }
        }
    }
}

@Composable
fun CustomBottomNavigation(
    selectedIndex: Int,
    onItemClick: (Int) -> Unit,
) {
    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        color = Color.Transparent,
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)),
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BottomNavItem.entries.forEachIndexed { index, item ->
                    val isSelected = selectedIndex == index

                    SalomonNavItem(
                        item = item,
                        isSelected = isSelected,
                        onClick = { onItemClick(index) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SalomonNavItem(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent,
        label = "backgroundColor",
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        label = "contentColor",
    )

    Row(
        modifier =
            Modifier
                .height(40.dp)
                .clip(RoundedCornerShape(50))
                .background(backgroundColor)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick,
                ).padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
            contentDescription = item.title,
            tint = contentColor,
            modifier = Modifier.size(24.dp),
        )

        AnimatedVisibility(
            visible = isSelected,
            enter =
                fadeIn() +
                    expandHorizontally(
                        animationSpec = spring(stiffness = Spring.StiffnessLow),
                    ),
            exit =
                fadeOut() +
                    shrinkHorizontally(
                        animationSpec = spring(stiffness = Spring.StiffnessLow),
                    ),
        ) {
            Text(
                text = item.title,
                color = contentColor,
                style =
                    MaterialTheme.typography.labelMedium.copy(`
                        fontWeight = FontWeight.Bold,
                    ),
                modifier = Modifier.padding(start = 8.dp),
                maxLines = 1,
            )
        }
    }
}
