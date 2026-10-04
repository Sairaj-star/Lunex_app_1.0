package com.example.lunex.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.lunex.data.model.ChatMessage
import com.example.lunex.data.model.MessageType
import com.example.lunex.data.model.Occupant
import com.example.lunex.data.repository.HotelRepository
import com.example.lunex.ui.components.AudioPlayerHelper
import com.example.lunex.ui.components.AudioRecorderHelper
import com.example.lunex.ui.theme.LunexAccent
import com.example.lunex.ui.theme.LunexBg
import com.example.lunex.ui.theme.LunexDangerBg
import com.example.lunex.ui.theme.LunexDangerBorder
import com.example.lunex.ui.theme.LunexDangerText
import com.example.lunex.ui.theme.LunexFull
import com.example.lunex.ui.theme.LunexLine
import com.example.lunex.ui.theme.LunexMineBorder
import com.example.lunex.ui.theme.LunexMineBubble
import com.example.lunex.ui.theme.LunexMuted
import com.example.lunex.ui.theme.LunexOtherBorder
import com.example.lunex.ui.theme.LunexOtherBubble
import com.example.lunex.ui.theme.LunexPanel
import com.example.lunex.ui.theme.LunexText
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomChatScreen(
    roomNumber: Int,
    roomKey: String,
    occupant: Occupant,
    repository: HotelRepository,
    onExit: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val listState = rememberLazyListState()

    val roomsMap by repository.rooms.collectAsState()
    val currentRoom = roomsMap[roomNumber]

    var inputText by remember { mutableStateOf("") }
    var showCheckoutDialog by remember { mutableStateOf(false) }
    var showKeyDialog by remember { mutableStateOf(false) }
    var showPeopleSheet by remember { mutableStateOf(false) }

    // Edit message state
    var editingMessage by remember { mutableStateOf<ChatMessage?>(null) }
    var editInputText by remember { mutableStateOf("") }

    // Audio helpers
    val recorderHelper = remember { AudioRecorderHelper(context) }
    val playerHelper = remember { AudioPlayerHelper() }
    var isRecording by remember { mutableStateOf(false) }
    var recordingSeconds by remember { mutableIntStateOf(0) }
    var currentlyPlayingUri by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            recorderHelper.stopRecording()
            playerHelper.stop()
        }
    }

    // Recording timer
    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingSeconds = 0
            while (isRecording) {
                delay(1000)
                recordingSeconds++
            }
        }
    }

    // Auto scroll on new messages
    val messages = currentRoom?.messages ?: emptyList()
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Back handling: Stepping out to reception keeps room booked & private
    BackHandler {
        repository.leaveRoomChat(roomNumber, occupant.id)
        onExit()
    }

    // Media Picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileName = "photo_${System.currentTimeMillis()}.jpg"
            repository.sendMediaMessage(
                roomNumber = roomNumber,
                senderId = occupant.id,
                senderLabel = occupant.label,
                type = MessageType.IMAGE,
                uri = uri.toString(),
                name = fileName
            )
        }
    }

    // Audio permission launcher
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val file = recorderHelper.startRecording()
            if (file != null) {
                isRecording = true
            } else {
                Toast.makeText(context, "Failed to initialize microphone", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Audio recording requires microphone permission", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LunexBg)
            .imePadding()
    ) {
        // Chat Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(LunexPanel)
                .border(1.dp, LunexLine)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                        repository.leaveRoomChat(roomNumber, occupant.id)
                        onExit()
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Leave to Reception",
                        tint = LunexText
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(LunexAccent)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "ROOM $roomNumber",
                            color = LunexText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Private",
                            tint = LunexAccent,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    Text(
                        text = "${occupant.label} · Private Room",
                        color = LunexMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Occupancy pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0xFF1B1221))
                        .border(1.dp, LunexLine, RoundedCornerShape(999.dp))
                        .clickable { showPeopleSheet = true }
                        .padding(horizontal = 9.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = "Occupants",
                            tint = LunexAccent,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${currentRoom?.occupants?.size ?: 1} inside",
                            color = LunexText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Key info
                IconButton(
                    onClick = { showKeyDialog = true },
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = "Room Key Info",
                        tint = LunexAccent,
                        modifier = Modifier.size(17.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Check-out / Close options
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(LunexDangerBg)
                        .border(1.dp, LunexDangerBorder, RoundedCornerShape(10.dp))
                        .clickable { showCheckoutDialog = true }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("exit_room_button")
                ) {
                    Text(
                        text = "Check-out",
                        color = LunexDangerText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Messages timeline
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                contentPadding = PaddingValues(vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    MessageBubbleItem(
                        message = msg,
                        isMine = msg.senderId == occupant.id,
                        currentlyPlayingUri = currentlyPlayingUri,
                        onPlayAudio = { uri ->
                            if (currentlyPlayingUri == uri) {
                                playerHelper.stop()
                                currentlyPlayingUri = null
                            } else {
                                currentlyPlayingUri = uri
                                playerHelper.play(uri) {
                                    currentlyPlayingUri = null
                                }
                            }
                        },
                        onEdit = {
                            editingMessage = msg
                            editInputText = msg.text ?: ""
                        },
                        onDelete = {
                            repository.deleteMessage(roomNumber, msg.id, occupant.id)
                        }
                    )
                }
            }
        }

        // Recording in progress banner
        AnimatedVisibility(visible = isRecording) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1B0D15))
                    .border(1.dp, LunexMineBorder)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(LunexAccent)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Recording voice note… 00:%02d".format(recordingSeconds),
                        color = LunexText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Button(
                    onClick = {
                        val file = recorderHelper.stopRecording()
                        isRecording = false
                        if (file != null && file.exists()) {
                            repository.sendMediaMessage(
                                roomNumber = roomNumber,
                                senderId = occupant.id,
                                senderLabel = occupant.label,
                                type = MessageType.AUDIO,
                                uri = file.absolutePath,
                                name = "voice_note.m4a"
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LunexAccent),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Stop & Send", fontSize = 12.sp)
                }
            }
        }

        // Composer Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(LunexPanel)
                .border(1.dp, LunexLine)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Media attachment button
            IconButton(
                onClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF19101F))
                    .border(1.dp, LunexLine, RoundedCornerShape(12.dp))
                    .testTag("media_button")
            ) {
                Icon(
                    imageVector = Icons.Default.AddPhotoAlternate,
                    contentDescription = "Attach Photo",
                    tint = LunexText,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Voice recorder button
            IconButton(
                onClick = {
                    if (isRecording) {
                        val file = recorderHelper.stopRecording()
                        isRecording = false
                        if (file != null && file.exists()) {
                            repository.sendMediaMessage(
                                roomNumber = roomNumber,
                                senderId = occupant.id,
                                senderLabel = occupant.label,
                                type = MessageType.AUDIO,
                                uri = file.absolutePath,
                                name = "voice_note.m4a"
                            )
                        }
                    } else {
                        val hasPermission = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED

                        if (hasPermission) {
                            val file = recorderHelper.startRecording()
                            if (file != null) {
                                isRecording = true
                            }
                        } else {
                            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }
                },
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isRecording) LunexAccent else Color(0xFF19101F))
                    .border(1.dp, LunexLine, RoundedCornerShape(12.dp))
                    .testTag("voice_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Record Voice Note",
                    tint = if (isRecording) Color.White else LunexText,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Message text input
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("Write a message...", color = LunexMuted, fontSize = 14.sp) },
                maxLines = 4,
                modifier = Modifier
                    .weight(1f)
                    .testTag("message_input"),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = LunexBg,
                    unfocusedContainerColor = LunexBg,
                    focusedBorderColor = LunexAccent,
                    unfocusedBorderColor = LunexLine,
                    focusedTextColor = LunexText,
                    unfocusedTextColor = LunexText
                )
            )

            // Send button
            IconButton(
                onClick = {
                    val textToSend = inputText.trim()
                    if (textToSend.isNotEmpty()) {
                        val result = repository.sendMessage(
                            roomNumber = roomNumber,
                            senderId = occupant.id,
                            senderLabel = occupant.label,
                            text = textToSend
                        )
                        result.fold(
                            onSuccess = {
                                inputText = ""
                            },
                            onFailure = { err ->
                                Toast.makeText(context, err.message ?: "Failed to send", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                },
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(LunexAccent)
                    .testTag("send_message_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send Message",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }

    // Room Key Info Dialog
    if (showKeyDialog) {
        AlertDialog(
            onDismissRequest = { showKeyDialog = false },
            containerColor = LunexPanel,
            shape = RoundedCornerShape(22.dp),
            title = {
                Text(
                    text = "Room $roomNumber Key",
                    color = LunexText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(LunexBg)
                            .border(1.dp, LunexAccent.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = roomKey,
                            color = LunexText,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 3.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(roomKey))
                            Toast.makeText(context, "Key copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LunexLine)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = LunexAccent, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy Key", color = LunexText, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Share this 9-character key with anyone you want to invite. All invited users who enter this key can unlock and join this room.",
                        color = LunexMuted,
                        fontSize = 12.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showKeyDialog = false },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LunexAccent)
                ) {
                    Text("Close", color = Color.White)
                }
            }
        )
    }

    // People List Modal Bottom Sheet
    if (showPeopleSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPeopleSheet = false },
            containerColor = LunexPanel,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "ROOM OCCUPANTS",
                    color = LunexAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Guests in Room $roomNumber",
                    color = LunexText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                val occupantsList = currentRoom?.occupants ?: emptyList()
                if (occupantsList.isEmpty()) {
                    Text(
                        text = "${occupant.label} (you)",
                        color = LunexText,
                        fontSize = 14.sp
                    )
                } else {
                    occupantsList.forEach { occ ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF191020))
                                .border(1.dp, LunexLine, RoundedCornerShape(12.dp))
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (occ.id == occupant.id) LunexAccent else LunexMuted)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = occ.label + if (occ.id == occupant.id) " (you)" else "",
                                    color = LunexText,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Text(
                                text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(occ.joinedAt)),
                                color = LunexMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "This room is private. Only guests who have the 9-character key can join.",
                    color = LunexMuted,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Check-out or Step-Out Dialog
    if (showCheckoutDialog) {
        AlertDialog(
            onDismissRequest = { showCheckoutDialog = false },
            containerColor = LunexPanel,
            shape = RoundedCornerShape(22.dp),
            title = {
                Text(
                    text = "Room #$roomNumber Options",
                    color = LunexText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Choose whether to step out to the lobby while keeping your private room active, or permanently check out and release the room.",
                        color = LunexMuted,
                        fontSize = 13.sp
                    )
                }
            },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            showCheckoutDialog = false
                            repository.leaveRoomChat(roomNumber, occupant.id)
                            onExit()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LunexAccent),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Step out to Reception (Keep Room Active & Private)", color = Color.White, fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            showCheckoutDialog = false
                            repository.checkoutAndCloseRoom(roomNumber)
                            onExit()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = LunexDangerText),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LunexDangerBorder),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Check-out & Clear Room (Release to Hotel)", fontSize = 12.sp)
                    }
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showCheckoutDialog = false },
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LunexLine)
                ) {
                    Text("Stay in Chat", color = LunexText)
                }
            }
        )
    }

    // Edit message dialog
    editingMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = { editingMessage = null },
            containerColor = LunexPanel,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text("Edit Message", color = LunexText, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                OutlinedTextField(
                    value = editInputText,
                    onValueChange = { editInputText = it },
                    singleLine = false,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = LunexBg,
                        unfocusedContainerColor = LunexBg,
                        focusedBorderColor = LunexAccent,
                        unfocusedBorderColor = LunexLine,
                        focusedTextColor = LunexText,
                        unfocusedTextColor = LunexText
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newTxt = editInputText.trim()
                        if (newTxt.isNotEmpty()) {
                            repository.editMessage(roomNumber, msg.id, newTxt, occupant.id)
                            editingMessage = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LunexAccent),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Save", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { editingMessage = null },
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LunexLine)
                ) {
                    Text("Cancel", color = LunexMuted)
                }
            }
        )
    }
}

@Composable
fun MessageBubbleItem(
    message: ChatMessage,
    isMine: Boolean,
    currentlyPlayingUri: String?,
    onPlayAudio: (String) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val timeFormatted = remember(message.timestamp) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))
    }

    if (message.type == MessageType.SYSTEM) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "✦ ${message.text}",
                color = LunexMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
        return
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
    ) {
        // Meta info (Sender & Time)
        Row(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${message.senderLabel} · $timeFormatted" + if (message.isEdited) " · edited" else "",
                color = LunexMuted,
                fontSize = 10.sp
            )
        }

        // Message Card
        Box(
            modifier = Modifier
                .widthIn(min = 60.dp, max = 300.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (isMine) LunexMineBubble else LunexOtherBubble)
                .border(
                    1.dp,
                    if (isMine) LunexMineBorder else LunexOtherBorder,
                    RoundedCornerShape(16.dp)
                )
                .padding(12.dp)
        ) {
            Column {
                if (message.isDeleted) {
                    Text(
                        text = "Message deleted",
                        color = LunexMuted,
                        fontSize = 13.sp,
                        fontStyle = FontStyle.Italic
                    )
                } else {
                    when (message.type) {
                        MessageType.TEXT -> {
                            Text(
                                text = message.text ?: "",
                                color = LunexText,
                                fontSize = 14.sp,
                                lineHeight = 19.sp
                            )
                        }
                        MessageType.IMAGE -> {
                            message.mediaUri?.let { uriStr ->
                                AsyncImage(
                                    model = uriStr,
                                    contentDescription = "Sent image",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 240.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                )
                            }
                        }
                        MessageType.AUDIO -> {
                            val isThisPlaying = currentlyPlayingUri == message.mediaUri
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                IconButton(
                                    onClick = { message.mediaUri?.let { onPlayAudio(it) } },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(LunexAccent)
                                ) {
                                    Icon(
                                        imageVector = if (isThisPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = "Play voice note",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (isThisPlaying) "Playing voice note…" else "Voice note",
                                        color = LunexText,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = message.mediaName ?: "audio.m4a",
                                        color = LunexMuted,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                        MessageType.SYSTEM -> Unit
                    }

                    // Edit & Delete actions for user's own active text messages
                    if (isMine && message.type == MessageType.TEXT) {
                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp)
                        ) {
                            Text(
                                text = "Edit",
                                color = LunexMuted,
                                fontSize = 11.sp,
                                modifier = Modifier
                                    .clickable { onEdit() }
                                    .padding(horizontal = 4.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Delete",
                                color = LunexMuted,
                                fontSize = 11.sp,
                                modifier = Modifier
                                    .clickable { onDelete() }
                                    .padding(horizontal = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
