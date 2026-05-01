package com.google.eRecept.feature.profile

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import com.google.eRecept.R
import com.google.eRecept.core.ui.components.CustomSegmentedControl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onChangePasswordClick: () -> Unit,
    viewModel: ProfileViewModel,
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    val doctorProfile by viewModel.doctorProfile.collectAsState()

    val doctorName by viewModel.doctorName.collectAsState()

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

    val currentThemeIndex by viewModel.themeMode.collectAsState()
    val themes = listOf("Светлая", "Темная", "Система")

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Теперь doctorName доступен, используем его для инициала
        val initial = if (doctorName.isNotBlank()) doctorName.first().uppercase() else "В"

        Box(
            modifier =
                Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = initial,
                style =
                    MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                    ),
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = doctorName,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = doctorProfile?.specialization ?: "",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(32.dp))

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
        ) {
            Text(
                text = stringResource(R.string.settings),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp, start = 8.dp),
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    ),
                shape = MaterialTheme.shapes.large,
            ) {
                Column(modifier = Modifier.padding(vertical = 12.dp)) {
                    Text(
                        text = stringResource(R.string.app_language),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )

                    CustomSegmentedControl(
                        options = languages,
                        selectedOption = languages[selectedLanguageIndex],
                        onOptionSelected = { selectedLanguage ->
                            val index = languages.indexOf(selectedLanguage)
                            selectedLanguageIndex = index

                            val tag =
                                when (index) {
                                    1 -> "kk"
                                    2 -> "en"
                                    else -> "ru"
                                }

                            AppCompatDelegate.setApplicationLocales(
                                LocaleListCompat.forLanguageTags(
                                    tag
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    )

                    Text(
                        text = stringResource(R.string.app_theme),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )

                    CustomSegmentedControl(
                        options = themes,
                        selectedOption = themes[currentThemeIndex],
                        onOptionSelected = { selectedTheme ->
                            val index = themes.indexOf(selectedTheme)
                            viewModel.updateTheme(index)
                        },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                onClick = { onChangePasswordClick() },
                modifier = Modifier.fillMaxWidth(),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    ),
                shape = MaterialTheme.shapes.large,
            ) {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.change_password)) },
                    leadingContent = {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    trailingContent = {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            val softRed = Color(0xFFD32F2F)

            Button(
                onClick = { showLogoutDialog = true },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                shape = MaterialTheme.shapes.large,
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = softRed,
                        contentColor = Color.White,
                    ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(12.dp))
                Text(stringResource(R.string.logout), style = MaterialTheme.typography.titleMedium)
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) },
            title = { Text(stringResource(R.string.logout_title)) },
            text = { Text(stringResource(R.string.logout_confirmation)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) {
                    Text(stringResource(R.string.logout_action))
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(stringResource(R.string.cancel), color = MaterialTheme.colorScheme.onSurface)
                }
            },
        )
    }
}
