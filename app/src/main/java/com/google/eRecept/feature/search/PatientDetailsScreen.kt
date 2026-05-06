package com.google.eRecept.feature.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.eRecept.R
import com.google.eRecept.core.ui.components.PatientInfoCard
import com.google.eRecept.core.ui.components.RecipeCard
import com.google.eRecept.data.model.Patient
import com.google.eRecept.data.model.Recipe
import com.google.eRecept.feature.home.HomeViewModel
import com.google.eRecept.feature.recipe.RecipeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientDetailsScreen(
    iin: String,
    searchViewModel: SearchViewModel,
    homeViewModel: HomeViewModel,
    recipeViewModel: RecipeViewModel,
    onNavigateBack: () -> Unit,
) {
    val patientResults by searchViewModel.patientResults.collectAsState()
    val patient = patientResults.find { it.iin == iin } ?: Patient(iin = iin, full_name = "Загрузка...")
    
    val recipes by recipeViewModel.recipes.collectAsState()
    val patientHistory = recipes.filter { it.patient_iin == iin }
    var selectedRecipe by remember { mutableStateOf<Recipe?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Детали пациента") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                PatientInfoCard(
                    patient = patient,
                )
            }

            item {
                Text(
                    text = "История назначений",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (patientHistory.isEmpty()) {
                item {
                    Text(
                        text = "История назначений пуста",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(patientHistory, key = { it.id }) { recipe ->
                    RecipeCard(
                        recipe = recipe,
                        onClick = { selectedRecipe = recipe },
                        viewModel = recipeViewModel,
                        onEdit = { /* Delegate to Main edit action if needed */ }
                    )
                }
            }
        }
    }

    if (selectedRecipe != null) {
        SearchRecipeDetailsDialog(
            recipe = selectedRecipe!!,
            onDismiss = { selectedRecipe = null },
            viewModel = recipeViewModel,
            onEdit = { /* Delegate to Main edit action if needed */ }
        )
    }
}
