package com.google.eRecept.feature.ai

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import com.google.eRecept.data.local.entity.ChatEntity
import dev.jeziellago.compose.markdowntext.MarkdownText

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
                        HistoryDrawerContent(
                            chats = uiState.chatHistory,
                            onNewChat = {
                                viewModel.createNewChat()
                                scope.launch { drawerState.close() }
                            },
                            onChatSelected = { chatId ->
                                viewModel.loadChat(chatId)
                                scope.launch { drawerState.close() }
                            },
                            onRenameChat = { id, newTitle -> viewModel.renameChat(id, newTitle) }
                        )
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
                        onSend = { text -> viewModel.sendMessage(text) },
                        onEditMessage = { msgId, newText -> viewModel.editMessage(msgId, newText) },
                        onRegenerate = { viewModel.regenerateLastResponse() }
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
                            onModelChange("gemini-2.5-pro")
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
    onSend: (String) -> Unit,
    onEditMessage: (String, String) -> Unit,
    onRegenerate: () -> Unit
) {
    val scrollState = rememberLazyListState()
    var inputText by remember { mutableStateOf("") }

    var editingMessage by remember { mutableStateOf<AiMessage?>(null) }

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            scrollState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Column(modifier = modifier) {
        LazyColumn(
            state = scrollState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(uiState.messages.size) { index ->
                val msg = uiState.messages[index]
                val isLastAiMessage = !msg.isUser && index == uiState.messages.lastIndex

                MessageBubble(
                    msg = msg,
                    isLastAiMessage = isLastAiMessage,
                    onRegenerate = onRegenerate,
                    onEditClick = { editingMessage = msg }
                )
            }
            if (uiState.isLoading) {
                item { LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .windowInsetsPadding(WindowInsets.ime.only(WindowInsetsSides.Bottom))
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Задайте вопрос ИИ...") },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
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
                enabled = inputText.isNotBlank() && !uiState.isLoading
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
            }
        }
    }

    if (editingMessage != null) {
        var newText by remember { mutableStateOf(editingMessage!!.text) }
        AlertDialog(
            onDismissRequest = { editingMessage = null },
            title = { Text("Редактировать запрос") },
            text = {
                OutlinedTextField(
                    value = newText,
                    onValueChange = { newText = it },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onEditMessage(editingMessage!!.id, newText)
                    editingMessage = null
                }) { Text("Отправить заново") }
            },
            dismissButton = {
                TextButton(onClick = { editingMessage = null }) { Text("Отмена") }
            }
        )
    }
}

@Composable
fun MessageBubble(
    msg: AiMessage,
    isLastAiMessage: Boolean,
    onRegenerate: () -> Unit,
    onEditClick: () -> Unit
) {
    val alignment = if (msg.isUser) Alignment.End else Alignment.Start
    val color = if (msg.isUser) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent
    val textColor = MaterialTheme.colorScheme.onSurface

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = alignment) {
        Box(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(color)
                .padding(12.dp)
        ) {
            if (msg.isUser) {
                Text(text = msg.text, color = textColor, fontSize = 16.sp, lineHeight = 22.sp)
            } else {
                MarkdownText(
                    markdown = msg.text,
                    isTextSelectable = true
                )
            }
        }

        Row(modifier = Modifier.padding(top = 4.dp, start = 8.dp, end = 8.dp)) {
            if (msg.isUser) {
                IconButton(onClick = onEditClick, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Изменить", tint = Color.Gray, modifier = Modifier.size(16.dp))
                }
            } else if (isLastAiMessage) {
                IconButton(onClick = onRegenerate, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Refresh, contentDescription = "Перегенерировать", tint = Color.Gray, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistoryDrawerContent(
    chats: List<ChatEntity>,
    onNewChat: () -> Unit,
    onChatSelected: (String) -> Unit,
    onRenameChat: (String, String) -> Unit
) {
    var chatToRename by remember { mutableStateOf<ChatEntity?>(null) }

    Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
        Text("История чатов", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(bottom = 16.dp))
        Button(onClick = onNewChat, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Icon(Icons.Default.Add, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Новый чат")
        }
        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn {
            items(chats.size) { index ->
                val chat = chats[index]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = { onChatSelected(chat.id) },
                            onLongClick = { chatToRename = chat }
                        )
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.ChatBubbleOutline, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(chat.title, maxLines = 1, style = MaterialTheme.typography.bodyMedium)
                    }
                    IconButton(onClick = { chatToRename = chat }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Переименовать", tint = Color.Gray, modifier = Modifier.size(16.dp))
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            }
        }
    }

    if (chatToRename != null) {
        var newTitle by remember { mutableStateOf(chatToRename!!.title) }
        AlertDialog(
            onDismissRequest = { chatToRename = null },
            title = { Text("Переименовать чат") },
            text = {
                OutlinedTextField(
                    value = newTitle,
                    onValueChange = { newTitle = it },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newTitle.isNotBlank()) {
                        onRenameChat(chatToRename!!.id, newTitle)
                    }
                    chatToRename = null
                }) { Text("Сохранить") }
            },
            dismissButton = {
                TextButton(onClick = { chatToRename = null }) { Text("Отмена") }
            }
        )
    }
}