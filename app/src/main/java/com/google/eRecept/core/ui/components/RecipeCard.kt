package com.google.eRecept.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.eRecept.R
import com.google.eRecept.data.model.Recipe
import com.google.eRecept.feature.recipe.RecipeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RecipeCard(
    recipe: Recipe,
    onClick: () -> Unit,
    viewModel: RecipeViewModel,
    onEdit: () -> Unit,
) {
    val sdf = SimpleDateFormat("d MMMM yyyy", Locale("ru"))
    val dateStr = sdf.format(Date(recipe.date))
    val expireStr = sdf.format(Date(recipe.expire_date))
    val recipeNum = recipe.id.takeLast(4).uppercase()

    var showMenu by remember { mutableStateOf(false) }
    var showRevokeConfirm by remember { mutableStateOf(false) }
    val isRevoking by viewModel.isRevoking.collectAsStateWithLifecycle(initialValue = false)

    val isExpired = recipe.expire_date < System.currentTimeMillis()
    val displayStatus =
        when {
            recipe.status == "Активен" && isExpired -> stringResource(R.string.status_expired)
            recipe.status == "Активен" -> stringResource(R.string.status_active)
            else -> recipe.status
        }
    val isGreenBadge = recipe.status == "Активен" && !isExpired
    val isRevoked = recipe.status.equals("Отозван", ignoreCase = true)

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.recipe_number_format, recipeNum),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = recipe.patient_name,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            val badgeContainerColor = if (isGreenBadge) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
            val badgeContentColor = if (isGreenBadge) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer

            Column(horizontalAlignment = Alignment.End) {
                Box {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(badgeContainerColor)
                            .then(
                                if (isGreenBadge) Modifier.clickable { showMenu = true } else Modifier
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = displayStatus,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = badgeContentColor,
                            )
                            if (isGreenBadge) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Опции",
                                    modifier = Modifier.size(16.dp),
                                    tint = badgeContentColor
                                )
                            }
                        }
                    }

                    if (isGreenBadge) {
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.edit)) },
                                onClick = {
                                    showMenu = false
                                    viewModel.openEditSheet(recipe)
                                    onEdit()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Edit, contentDescription = null)
                                },
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.revoke), color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    showMenu = false
                                    showRevokeConfirm = true
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Cancel,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                    )
                                },
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                if (!isRevoked) {
                    Text(
                        text = stringResource(R.string.until_date_format, expireStr),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }

    if (showRevokeConfirm) {
        AlertDialog(
            onDismissRequest = { if (!isRevoking) showRevokeConfirm = false },
            title = { Text(stringResource(R.string.revoke_recipe), fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    stringResource(R.string.confirmation_recipe_revoking),
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.revokeRecipe(recipe.id) {
                            showRevokeConfirm = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                ) {
                    if (isRevoking) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onError,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(stringResource(R.string.revoke), color = MaterialTheme.colorScheme.onError)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showRevokeConfirm = false }, enabled = !isRevoking) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}