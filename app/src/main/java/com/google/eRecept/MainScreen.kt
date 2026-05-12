package com.google.eRecept

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.eRecept.core.navigation.BottomNavItem
import com.google.eRecept.feature.ai.AiAssistantScreen
import com.google.eRecept.feature.experimental.ExperimentalFeaturesViewModel
import com.google.eRecept.feature.home.HomeScreen
import com.google.eRecept.feature.home.HomeViewModel
import com.google.eRecept.feature.profile.ProfileScreen
import com.google.eRecept.feature.profile.ProfileViewModel
import com.google.eRecept.feature.recipe.RecipeScreen
import com.google.eRecept.feature.recipe.RecipeViewModel
import com.google.eRecept.feature.search.SearchScreen
import com.google.eRecept.feature.search.SearchViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onLogout: () -> Unit,
    onChangePasswordClick: () -> Unit,
    onNavigateToCreateAppointment: (String) -> Unit,
    onNavigateToCreateRecipe: (String) -> Unit,
    onEditRecipe: () -> Unit,
    onNavigateToPatientDetails: (String) -> Unit,
    onNavigateToMedicationDetails: (String) -> Unit,
    onNavigateToExperimental: () -> Unit,
    profileViewModel: ProfileViewModel,
) {
    val homeViewModel: HomeViewModel = hiltViewModel()
    val recipeViewModel: RecipeViewModel = hiltViewModel()
    val searchViewModel: SearchViewModel = hiltViewModel()

    val experimentalViewModel: ExperimentalFeaturesViewModel = hiltViewModel()
    val isAiEnabled by experimentalViewModel.isAiEnabled.collectAsStateWithLifecycle(initialValue = false)

    val navItems = remember(isAiEnabled) {
        if (isAiEnabled) {
            BottomNavItem.entries.toTypedArray()
        } else {
            BottomNavItem.entries.filter { it != BottomNavItem.AiAssistant }.toTypedArray()
        }
    }
    val pagerState = rememberPagerState(pageCount = { navItems.size })
    val coroutineScope = rememberCoroutineScope()

    val isParentNavigating = pagerState.isScrollInProgress
    val currentTitle = stringResource(navItems[pagerState.currentPage].title)
    val currentItem = navItems[pagerState.currentPage]


    Scaffold(
        topBar = {
            if (currentItem != BottomNavItem.AiAssistant) {
                TopAppBar(
                    title = {
                        AnimatedContent(
                            targetState = currentTitle,
                            transitionSpec = {
                                fadeIn(tween(300, easing = FastOutSlowInEasing)) togetherWith
                                        fadeOut(tween(300, easing = FastOutSlowInEasing))
                            },
                            label = "TitleAnimation"
                        ) { title ->
                            Text(
                                text = title,
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 32.sp
                                ),
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        },
        bottomBar = {
            NavigationBar {
                navItems.forEachIndexed { index, item ->
                    val isSelected = pagerState.currentPage == index

                    val title = stringResource(item.title)

                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = title,
                            )
                        },
                        label = {
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
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
            when (navItems.getOrNull(page)) {
                BottomNavItem.Schedule -> {
                    HomeScreen(
                        viewModel = homeViewModel,
                        onNavigateToCreateAppointment = onNavigateToCreateAppointment,
                        onNavigateToPatientDetails = onNavigateToPatientDetails,
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
                        onEditRecipe = onEditRecipe,
                        onNavigateToPatientDetails = onNavigateToPatientDetails
                    )
                }
                BottomNavItem.AiAssistant -> {
                    AiAssistantScreen()
                }
                BottomNavItem.Search -> {
                    SearchScreen(
                        viewModel = searchViewModel,
                        recipeViewModel = recipeViewModel,
                        onEditRecipe = onEditRecipe,
                        onNavigateToPatientDetails = onNavigateToPatientDetails,
                        onNavigateToMedicationDetails = onNavigateToMedicationDetails,
                        isParentNavigating = isParentNavigating
                    )
                }
                BottomNavItem.Profile -> {
                    ProfileScreen(
                        onLogout = onLogout,
                        onChangePasswordClick = onChangePasswordClick,
                        onNavigateToExperimental = onNavigateToExperimental,
                        viewModel = profileViewModel,
                    )
                }
                null -> {}
            }
        }
    }
}