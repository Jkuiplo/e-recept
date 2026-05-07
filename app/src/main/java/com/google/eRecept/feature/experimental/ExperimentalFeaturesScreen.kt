package com.google.eRecept.feature.experimental

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.eRecept.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExperimentalFeaturesScreen(
    onNavigateBack: () -> Unit,
    viewModel: ExperimentalFeaturesViewModel
) {
    val voiceEnabled by viewModel.isVoiceEnabled.collectAsStateWithLifecycle(initialValue = false)
    val aiEnabled by viewModel.isAiEnabled.collectAsStateWithLifecycle(initialValue = false)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.lab))
                        },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.warning_msg),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            ExperimentalItem(
                title = stringResource(R.string.voice_heading),
                description = stringResource(R.string.voice_desc),
                isEnabled = voiceEnabled,
                onToggle = { viewModel.toggleVoice(it) }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            ExperimentalItem(
                title = stringResource(R.string.assistant_heading),
                description = stringResource(R.string.assistant_desc),
                isEnabled = aiEnabled,
                onToggle = { viewModel.toggleAi(it) }
            )
        }
    }
}

@Composable
fun ExperimentalItem(
    title: String,
    description: String,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = isEnabled, onCheckedChange = onToggle)
    }
}