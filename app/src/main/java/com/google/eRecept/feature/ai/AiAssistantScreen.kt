package com.google.eRecept.feature.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import com.google.eRecept.R
@Composable
fun AiAssistantScreen(
    viewModel: AiAssistantViewModel = hiltViewModel(),
    isParentNavigating: Boolean = false
) {
    val uiState by viewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    ModalDrawerSheet(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(300.dp),
                        drawerShape = AbsoluteRoundedCornerShape(
                            topLeft = 24.dp,
                            bottomLeft = 24.dp,
                            topRight = 0.dp,
                            bottomRight = 0.dp
                        )
                    ) {
                        HistoryDrawerContent(onNewChat = {
                            viewModel.createNewChat()
                            scope.launch { drawerState.close() }
                        })
                    }
                }
            }
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    AiChatTopBar(
                        selectedModel = uiState.selectedModel,
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onModelChange = { viewModel.updateModel(it) }
                    )

                    ChatMainContent(
                        modifier = Modifier.weight(1f),
                        uiState = uiState,
                        onSend = { text -> viewModel.sendMessage(text) }
                    )
                }
            }
        }
    }
}

@Composable
fun AiChatTopBar(
    selectedModel: String,
    onMenuClick: () -> Unit,
    onModelChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding( horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Text(
            text = stringResource(R.string.ai_title),
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            ),
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {

            var expanded by remember { mutableStateOf(false) }

            Box {
                TextButton(
                    onClick = { expanded = true },
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    val modelName = selectedModel
                        .replace("gemini-2.5-", "")
                        .replace("-latest", "")
                        .uppercase()

                    Text(modelName, fontSize = 14.sp)

                    Icon(Icons.Default.ArrowDropDown, null)
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Flash (Быстрый)") },
                        onClick = {
                            onModelChange("gemini-2.5-flash")
                            expanded = false
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Pro (Умный)") },
                        onClick = {
                            onModelChange("gemini-1.5-pro")
                            expanded = false
                        }
                    )
                }
            }

            IconButton(onClick = onMenuClick) {
                Icon(
                    Icons.Default.History,
                    contentDescription = "История чатов"
                )
            }
        }
    }
}

@Composable
fun ChatMainContent(
    modifier: Modifier,
    uiState: AiChatUiState,
    onSend: (String) -> Unit
) {
    val scrollState = rememberLazyListState()
    var inputText by remember { mutableStateOf("") }

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            scrollState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Column(modifier = modifier) {
        LazyColumn(
            state = scrollState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(uiState.messages) { msg ->
                MessageBubble(msg)
            }
            if (uiState.isLoading) {
                item { LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Transparent)
                .padding(horizontal = 8.dp)
                .windowInsetsPadding(
                    WindowInsets.ime.only(WindowInsetsSides.Bottom)
                )
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically

        ) {
            IconButton(onClick = { /* Логика прикрепления фото */ }) {
                Icon(Icons.Default.AddCircleOutline, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Задайте вопрос ИИ...") },
                shape = RoundedCornerShape(24.dp),
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )
            Spacer(modifier = Modifier.width(4.dp))
            FilledIconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        onSend(inputText)
                        inputText = ""
                    }
                },
                enabled = inputText.isNotBlank() && !uiState.isLoading,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
            }
        }
    }
}

@Composable
fun MessageBubble(msg: AiMessage) {
    val alignment = if (msg.isUser) Alignment.End else Alignment.Start
    val color = if (msg.isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (msg.isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = alignment) {
        Box(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp, topEnd = 16.dp,
                        bottomStart = if (msg.isUser) 16.dp else 4.dp,
                        bottomEnd = if (msg.isUser) 4.dp else 16.dp
                    ))
                .background(color)
                .padding(14.dp)
        ) {
            Text(text = msg.text, color = textColor, fontSize = 16.sp, lineHeight = 22.sp)
        }
    }
}

@Composable
fun HistoryDrawerContent(onNewChat: () -> Unit) {
    Column(modifier = Modifier
        .padding(16.dp)
        .fillMaxSize()) {
        Text("История чатов", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(bottom = 16.dp))
        Button(
            onClick = onNewChat,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Add, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Новый чат")
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("У вас пока нет сохраненных чатов", color = Color.Gray, fontSize = 14.sp)
    }
}