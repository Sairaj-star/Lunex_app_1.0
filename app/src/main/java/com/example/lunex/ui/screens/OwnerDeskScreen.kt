package com.example.lunex.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lunex.data.model.HotelRoom
import com.example.lunex.data.model.RoomStatus
import com.example.lunex.data.repository.HotelRepository
import com.example.lunex.ui.theme.LunexAccent
import com.example.lunex.ui.theme.LunexAvailable
import com.example.lunex.ui.theme.LunexBg
import com.example.lunex.ui.theme.LunexDangerBg
import com.example.lunex.ui.theme.LunexDangerBorder
import com.example.lunex.ui.theme.LunexDangerText
import com.example.lunex.ui.theme.LunexFull
import com.example.lunex.ui.theme.LunexLine
import com.example.lunex.ui.theme.LunexMuted
import com.example.lunex.ui.theme.LunexOccupied
import com.example.lunex.ui.theme.LunexPanel
import com.example.lunex.ui.theme.LunexPanelElevated
import com.example.lunex.ui.theme.LunexText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OwnerDeskScreen(
    repository: HotelRepository,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val roomsMap by repository.rooms.collectAsState()

    var isAuthenticated by remember { mutableStateOf(false) }

    // Login Form State
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf<String?>(null) }

    // Expanded room details state
    val expandedRooms = remember { mutableStateMapOf<Int, Boolean>() }
    var roomToReset by remember { mutableStateOf<Int?>(null) }

    val defaultEmail = "deshmukhvidyut771@gmail.com"
    val defaultPassword = "Zeus74474@#"

    if (!isAuthenticated) {
        // Owner Login Screen
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LunexBg)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = LunexText
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "PRIVATE DESK",
                        color = LunexAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Owner access",
                        color = LunexText,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = LunexPanel),
                border = androidx.compose.foundation.BorderStroke(1.dp, LunexLine)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = LunexAccent,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Authenticate Desk Access",
                            color = LunexText,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = {
                            emailInput = it
                            loginError = null
                        },
                        label = { Text("Owner Email") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = LunexBg,
                            unfocusedContainerColor = LunexBg,
                            focusedBorderColor = LunexAccent,
                            unfocusedBorderColor = LunexLine,
                            focusedTextColor = LunexText,
                            unfocusedTextColor = LunexText
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = {
                            passwordInput = it
                            loginError = null
                        },
                        label = { Text("Password") },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle password visibility",
                                    tint = LunexMuted
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = LunexBg,
                            unfocusedContainerColor = LunexBg,
                            focusedBorderColor = LunexAccent,
                            unfocusedBorderColor = LunexLine,
                            focusedTextColor = LunexText,
                            unfocusedTextColor = LunexText
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Helper button to prefill defaults
                    OutlinedButton(
                        onClick = {
                            emailInput = defaultEmail
                            passwordInput = defaultPassword
                            loginError = null
                        },
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LunexLine),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Fill configured owner credentials", color = LunexAccent, fontSize = 12.sp)
                    }

                    if (loginError != null) {
                        Text(
                            text = loginError ?: "",
                            color = LunexFull,
                            fontSize = 13.sp
                        )
                    }

                    Button(
                        onClick = {
                            val emailClean = emailInput.trim().lowercase()
                            val passClean = passwordInput.trim()
                            if (emailClean == defaultEmail.lowercase() && passClean == defaultPassword) {
                                isAuthenticated = true
                                loginError = null
                            } else {
                                loginError = "Invalid owner credentials."
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LunexAccent),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("owner_login_button")
                    ) {
                        Text("Enter Owner Desk", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    } else {
        // Authenticated Owner Desk Screen
        val allRooms = roomsMap.values.toList()
        val availableCount = allRooms.count { it.status == RoomStatus.AVAILABLE }
        val occupiedCount = allRooms.count { it.status == RoomStatus.OCCUPIED }
        val fullCount = allRooms.count { it.status == RoomStatus.FULL }
        val bookedRooms = allRooms.filter { it.booked }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LunexBg)
        ) {
            // Desk Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LunexPanel)
                    .border(1.dp, LunexLine)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
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
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "OWNER DESK",
                        color = LunexText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = {
                        isAuthenticated = false
                        passwordInput = ""
                    },
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LunexLine),
                    modifier = Modifier.testTag("owner_logout_button")
                ) {
                    Text("Log out", color = LunexMuted, fontSize = 12.sp)
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Statistics Cards
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            title = "Available",
                            count = availableCount,
                            color = LunexAvailable,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Occupied",
                            count = occupiedCount,
                            color = LunexOccupied,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Full",
                            count = fullCount,
                            color = LunexFull,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "ACTIVE BOOKED ROOMS (${bookedRooms.size})",
                        color = LunexAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }

                if (bookedRooms.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(LunexPanel)
                                .border(1.dp, LunexLine, RoundedCornerShape(18.dp))
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No rooms are currently booked.\nHotel is completely quiet.",
                                color = LunexMuted,
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(bookedRooms, key = { it.number }) { room ->
                        val isExpanded = expandedRooms[room.number] ?: false

                        OwnerRoomCard(
                            room = room,
                            isExpanded = isExpanded,
                            onToggle = { expandedRooms[room.number] = !isExpanded },
                            onCopyKey = { key ->
                                clipboardManager.setText(AnnotatedString(key))
                                Toast.makeText(context, "Key copied", Toast.LENGTH_SHORT).show()
                            },
                            onReset = { roomToReset = room.number }
                        )
                    }
                }
            }
        }
    }

    // Reset confirmation dialog
    roomToReset?.let { roomNum ->
        AlertDialog(
            onDismissRequest = { roomToReset = null },
            containerColor = LunexPanel,
            shape = RoundedCornerShape(22.dp),
            title = {
                Text(
                    text = "Close Room #$roomNum?",
                    color = LunexText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "This will immediately evict all current guests, clear all room messages and issued keys, and return Room #$roomNum to Available status.",
                    color = LunexMuted,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        repository.ownerResetRoom(roomNum)
                        roomToReset = null
                        Toast.makeText(context, "Room #$roomNum reset.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LunexAccent),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("owner_reset_room_button")
                ) {
                    Text("Close / Reset Room", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { roomToReset = null },
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LunexLine)
                ) {
                    Text("Cancel", color = LunexText)
                }
            }
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = LunexPanel),
        border = androidx.compose.foundation.BorderStroke(1.dp, LunexLine)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "$count",
                color = color,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                color = LunexMuted,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun OwnerRoomCard(
    room: HotelRoom,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onCopyKey: (String) -> Unit,
    onReset: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = LunexPanel),
        border = androidx.compose.foundation.BorderStroke(1.dp, LunexLine)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Room ${room.number}",
                        color = LunexText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${room.booking?.name ?: "Guest"} · ${room.occupants.size}/${room.capacity} occupants",
                        color = LunexMuted,
                        fontSize = 12.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(
                                when (room.status) {
                                    RoomStatus.AVAILABLE -> LunexAvailable.copy(alpha = 0.2f)
                                    RoomStatus.FULL -> LunexFull.copy(alpha = 0.2f)
                                    RoomStatus.OCCUPIED -> LunexOccupied.copy(alpha = 0.2f)
                                }
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = room.status.name.lowercase(),
                            color = when (room.status) {
                                RoomStatus.AVAILABLE -> LunexAvailable
                                RoomStatus.FULL -> LunexFull
                                RoomStatus.OCCUPIED -> LunexText
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = LunexMuted
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Booking Details Section
                    SectionBox(title = "BOOKING DETAILS") {
                        Text("Guest Name: ${room.booking?.name ?: "Unknown"}", color = LunexText, fontSize = 13.sp)
                        Text("Mobile: ${room.booking?.phone ?: "Unknown"}", color = LunexText, fontSize = 13.sp)
                        Text("Capacity: ${room.capacity} persons", color = LunexText, fontSize = 13.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Room Key: ", color = LunexText, fontSize = 13.sp)
                            Text(
                                text = room.key ?: "None",
                                color = LunexAccent,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            room.key?.let { k ->
                                IconButton(onClick = { onCopyKey(k) }, modifier = Modifier.size(28.dp)) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy Key", tint = LunexMuted, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }

                    // Occupants Section
                    SectionBox(title = "CURRENT OCCUPANTS (${room.occupants.size})") {
                        if (room.occupants.isEmpty()) {
                            Text("No occupants currently inside", color = LunexMuted, fontSize = 12.sp)
                        } else {
                            room.occupants.forEach { occ ->
                                val joinedTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(occ.joinedAt))
                                Text("• ${occ.label} (joined at $joinedTime)", color = LunexText, fontSize = 12.sp)
                            }
                        }
                    }

                    // Audit Log Section
                    SectionBox(title = "ENTRY / EXIT / ACTION AUDIT (${room.audit.size})") {
                        if (room.audit.isEmpty()) {
                            Text("No audit log events", color = LunexMuted, fontSize = 12.sp)
                        } else {
                            room.audit.takeLast(10).forEach { a ->
                                val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(a.timestamp))
                                Text("[$time] ${a.type}: ${a.details}", color = LunexMuted, fontSize = 11.sp)
                            }
                        }
                    }

                    // Messages Trail Section
                    SectionBox(title = "MESSAGES ACTIVITY (${room.messages.size})") {
                        if (room.messages.isEmpty()) {
                            Text("No messages sent yet", color = LunexMuted, fontSize = 12.sp)
                        } else {
                            room.messages.takeLast(8).forEach { m ->
                                val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(m.timestamp))
                                val content = if (m.isDeleted) "[deleted]" else m.text ?: m.mediaName ?: ""
                                Text("[$time] ${m.senderLabel}: $content", color = LunexText, fontSize = 11.sp)
                            }
                        }
                    }

                    // Reset Button
                    Button(
                        onClick = onReset,
                        colors = ButtonDefaults.buttonColors(containerColor = LunexDangerBg),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LunexDangerBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Close / Reset Room", color = LunexDangerText, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionBox(
    title: String,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0D0912))
            .border(1.dp, LunexLine, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                color = LunexAccent,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
            content()
        }
    }
}
