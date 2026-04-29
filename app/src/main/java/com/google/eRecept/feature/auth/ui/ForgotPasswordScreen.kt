package com.google.eRecept.feature.auth.ui

import android.util.Patterns
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.eRecept.R
import com.google.eRecept.feature.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    onNavigateToReset: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    var email by remember { mutableStateOf("") }
    var isSent by remember { mutableStateOf(false) }

    val authState by viewModel.authState.collectAsState()
    val timer by viewModel.resendTimer.collectAsState()

    // валидация почты
    val isEmailValid =
        Patterns.EMAIL_ADDRESS
            .matcher(email)
            .matches()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Восстановление") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (!isSent) {
                // Ввод почты
                Text(
                    text = "Восстановление доступа",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Введите почту, указанную при регистрации. Мы отправим письмо с инструкциями.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(32.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    isError = email.isNotEmpty() && !isEmailValid,
                    supportingText = {
                        if (email.isNotEmpty() && !isEmailValid) {
                            Text("Введите корректный адрес")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { viewModel.forgotPassword(email) { isSent = true } },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = isEmailValid && authState !is AuthViewModel.AuthState.Loading,
                ) {
                    if (authState is AuthViewModel.AuthState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                        )
                    } else {
                        Text("Отправить письмо")
                    }
                }
            } else {
                // Успешно отпправлено
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = Color(0xFF4CAF50),
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Письмо отправлено!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.check_email_spam, email),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(48.dp))

                TextButton(
                    onClick = { viewModel.forgotPassword(email) { } },
                    enabled = timer == 0 && authState !is AuthViewModel.AuthState.Loading,
                ) {
                    if (timer > 0) {
                        Text(stringResource(R.string.resend_timer, timer), color = Color.Gray)
                    } else {
                        Text("Отправить письмо еще раз")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onNavigateToReset,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.filledTonalButtonColors(),
                ) {
                    Text("Вернуться ко входу")
                }
            }

            // Показ ошибки бэка
            AnimatedVisibility(visible = authState is AuthViewModel.AuthState.Error) {
                val error = (authState as? AuthViewModel.AuthState.Error)?.message ?: ""
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 16.dp),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}
