package com.google.eRecept.feature.experimental

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.eRecept.R
import com.google.eRecept.core.ui.components.CustomSegmentedControl
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExperimentalFeaturesScreen(
    onNavigateBack: () -> Unit,
    viewModel: ExperimentalFeaturesViewModel
) {
    val voiceEnabled by viewModel.isVoiceEnabled.collectAsStateWithLifecycle(initialValue = false)
    val aiEnabled by viewModel.isAiEnabled.collectAsStateWithLifecycle(initialValue = false)

    val languages = listOf("Русский", "Қазақша", "English")

    val currentLanguageTag = AppCompatDelegate.getApplicationLocales().get(0)?.language ?: "ru"
    var selectedLanguageIndex by remember {
        mutableIntStateOf(
            when (currentLanguageTag) {
                "kk" -> 1
                "en" -> 2
                else -> 0
            },
        )
    }

    var isRecreating by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.lab)) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack, enabled = !isRecreating) {
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
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.warning_msg),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 24.dp, start = 8.dp, end = 8.dp)
                )

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {

                        ExperimentalItem(
                            title = stringResource(R.string.voice_heading),
                            description = stringResource(R.string.voice_desc),
                            isEnabled = voiceEnabled,
                            onToggle = { viewModel.toggleVoice(it) }
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )

                        ExperimentalItem(
                            title = stringResource(R.string.assistant_heading),
                            description = stringResource(R.string.assistant_desc),
                            isEnabled = aiEnabled,
                            onToggle = { viewModel.toggleAi(it) }
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.app_language),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Изменение языка перезагрузит интерфейс",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            CustomSegmentedControl(
                                options = languages,
                                selectedOption = languages[selectedLanguageIndex],
                                onOptionSelected = { selectedLanguage ->
                                    val index = languages.indexOf(selectedLanguage)
                                    if (index == selectedLanguageIndex) return@CustomSegmentedControl

                                    selectedLanguageIndex = index
                                    isRecreating = true

                                    val tag = when (index) {
                                        1 -> "kk"
                                        2 -> "en"
                                        else -> "ru"
                                    }

                                    scope.launch {
                                        delay(300)
                                        viewModel.updateLanguage(tag)
                                        AppCompatDelegate.setApplicationLocales(
                                            LocaleListCompat.forLanguageTags(tag)
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = isRecreating,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.8f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary
                )
            }
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
            .clickable { onToggle(!isEnabled) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Switch(
            checked = isEnabled,
            onCheckedChange = onToggle
        )
    }
}