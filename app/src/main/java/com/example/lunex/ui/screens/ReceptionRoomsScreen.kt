package com.example.lunex.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lunex.data.model.HotelRoom
import com.example.lunex.data.model.Occupant
import com.example.lunex.data.model.RoomStatus
import com.example.lunex.data.repository.HotelRepository
import com.example.lunex.ui.theme.LunexAccent
import com.example.lunex.ui.theme.LunexAvailable
import com.example.lunex.ui.theme.LunexBg
import com.example.lunex.ui.theme.LunexFull
import com.example.lunex.ui.theme.LunexLine
import com.example.lunex.ui.theme.LunexMineBorder
import com.example.lunex.ui.theme.LunexMuted
import com.example.lunex.ui.theme.LunexOccupied
import com.example.lunex.ui.theme.LunexPanel
import com.example.lunex.ui.theme.LunexPanelElevated
import com.example.lunex.ui.theme.LunexText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceptionRoomsScreen(
    repository: HotelRepository,
    onBack: () -> Unit,
    onEnterRoom: (roomNumber: Int, key: String, occupant: Occupant) -> Unit
) {
    BackHandler(onBack = onBack)

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val roomsMap by repository.rooms.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedRangeIndex by remember { mutableIntStateOf(0) }
    var selectedStatusFilter by remember { mutableStateOf<RoomStatus?>(null) }

    // Dialog states
    var bookingRoomNumber by remember { mutableStateOf<Int?>(null) }
    var issuedKeyData by remember { mutableStateOf<Pair<Int, String>?>(null) }
    var showJoinDialog by remember { mutableStateOf(false) }

    val ranges = listOf(
        1..100 to "1–100",
        101..200 to "101–200",
        201..300 to "201–300",
        301..400 to "301–400",
        401..500 to "401–500"
    )

    val currentRange = ranges[selectedRangeIndex].first

    val displayedRooms = remember(roomsMap, searchQuery, selectedRangeIndex, selectedStatusFilter) {
        val queryNum = searchQuery.trim().toIntOrNull()
        if (queryNum != null) {
            listOfNotNull(roomsMap[queryNum])
        } else {
            val list = currentRange.mapNotNull { roomsMap[it] }
            if (selectedStatusFilter != null) {
                list.filter { it.status == selectedStatusFilter }
            } else {
                list
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LunexBg)
    ) {
        // Reception Toolbar
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
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Welcome",
                        tint = LunexText
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "RECEPTION",
                        color = LunexAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Choose a room",
                        color = LunexText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            OutlinedButton(
                onClick = { showJoinDialog = true },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = LunexText),
                border = androidx.compose.foundation.BorderStroke(1.dp, LunexLine),
                modifier = Modifier.testTag("have_key_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Key,
                    contentDescription = null,
                    tint = LunexAccent,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("I have a key", fontSize = 13.sp)
            }
        }

        // Search & Jump Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Jump to room (1-500)...", color = LunexMuted, fontSize = 13.sp) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = LunexMuted)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = LunexMuted)
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = LunexPanelElevated,
                    unfocusedContainerColor = LunexPanel,
                    focusedBorderColor = LunexAccent,
                    unfocusedBorderColor = LunexLine,
                    focusedTextColor = LunexText,
                    unfocusedTextColor = LunexText
                )
            )
        }

        // Room Ranges Floor Tabs
        if (searchQuery.isEmpty()) {
            ScrollableTabRow(
                selectedTabIndex = selectedRangeIndex,
                containerColor = LunexBg,
                contentColor = LunexAccent,
                edgePadding = 16.dp,
                divider = {}
            ) {
                ranges.forEachIndexed { index, pair ->
                    Tab(
                        selected = selectedRangeIndex == index,
                        onClick = { selectedRangeIndex = index },
                        text = {
                            Text(
                                text = "Floor ${index + 1} (${pair.second})",
                                fontSize = 12.sp,
                                fontWeight = if (selectedRangeIndex == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedRangeIndex == index) LunexAccent else LunexMuted
                            )
                        }
                    )
                }
            }

            // Filter status chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedStatusFilter == null,
                    onClick = { selectedStatusFilter = null },
                    label = { Text("All", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = LunexAccent,
                        selectedLabelColor = Color.White,
                        containerColor = LunexPanel,
                        labelColor = LunexMuted
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedStatusFilter == null,
                        borderColor = LunexLine,
                        selectedBorderColor = LunexAccent
                    )
                )
                FilterChip(
                    selected = selectedStatusFilter == RoomStatus.AVAILABLE,
                    onClick = { selectedStatusFilter = RoomStatus.AVAILABLE },
                    label = { Text("Available", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = LunexAvailable.copy(alpha = 0.2f),
                        selectedLabelColor = LunexAvailable,
                        containerColor = LunexPanel,
                        labelColor = LunexMuted
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedStatusFilter == RoomStatus.AVAILABLE,
                        borderColor = LunexLine,
                        selectedBorderColor = LunexAvailable
                    )
                )
                FilterChip(
                    selected = selectedStatusFilter == RoomStatus.OCCUPIED,
                    onClick = { selectedStatusFilter = RoomStatus.OCCUPIED },
                    label = { Text("Occupied", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = LunexMineBorder,
                        selectedLabelColor = LunexText,
                        containerColor = LunexPanel,
                        labelColor = LunexMuted
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedStatusFilter == RoomStatus.OCCUPIED,
                        borderColor = LunexLine,
                        selectedBorderColor = LunexAccent
                    )
                )
            }
        }

        // Grid of Rooms
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(displayedRooms, key = { it.number }) { room ->
                RoomCardItem(
                    room = room,
                    onClick = {
                        when (room.status) {
                            RoomStatus.AVAILABLE -> {
                                bookingRoomNumber = room.number
                            }
                            RoomStatus.OCCUPIED -> {
                                // Has space to join with key
                                showJoinDialog = true
                            }
                            RoomStatus.FULL -> {
                                Toast.makeText(context, "Room ${room.number} is full.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
            }
        }
    }

    // Check-in Booking Dialog
    bookingRoomNumber?.let { roomNum ->
        BookingDialog(
            roomNumber = roomNum,
            onDismiss = { bookingRoomNumber = null },
            onSubmit = { name, phone, capacity ->
                val result = repository.bookRoom(roomNum, name, phone, capacity)
                result.fold(
                    onSuccess = { key ->
                        bookingRoomNumber = null
                        issuedKeyData = Pair(roomNum, key)
                    },
                    onFailure = { error ->
                        Toast.makeText(context, error.message ?: "Booking failed.", Toast.LENGTH_LONG).show()
                    }
                )
            }
        )
    }

    // Key Issued Dialog
    issuedKeyData?.let { (roomNum, key) ->
        KeyIssuedDialog(
            roomNumber = roomNum,
            key = key,
            onCopy = {
                clipboardManager.setText(AnnotatedString(key))
                Toast.makeText(context, "Key copied to clipboard", Toast.LENGTH_SHORT).show()
            },
            onEnter = {
                val joinResult = repository.joinRoom(roomNum, key)
                joinResult.fold(
                    onSuccess = { (_, occupant) ->
                        issuedKeyData = null
                        onEnterRoom(roomNum, key, occupant)
                    },
                    onFailure = { err ->
                        Toast.makeText(context, err.message ?: "Could not enter room.", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        )
    }

    // Join with Key Dialog
    if (showJoinDialog) {
        JoinRoomDialog(
            onDismiss = { showJoinDialog = false },
            onSubmit = { roomNum, key ->
                val result = repository.joinRoom(roomNum, key)
                result.fold(
                    onSuccess = { (_, occupant) ->
                        showJoinDialog = false
                        onEnterRoom(roomNum, key, occupant)
                    },
                    onFailure = { error ->
                        Toast.makeText(context, error.message ?: "Could not unlock room.", Toast.LENGTH_LONG).show()
                    }
                )
            }
        )
    }
}

@Composable
private fun RoomCardItem(
    room: HotelRoom,
    onClick: () -> Unit
) {
    val isAvailable = room.status == RoomStatus.AVAILABLE
    val isFull = room.status == RoomStatus.FULL

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(enabled = !isFull, onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isAvailable) LunexPanel else LunexPanelElevated
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isAvailable) LunexLine else LunexLine.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Room ${room.number}",
                    color = if (isFull) LunexMuted else LunexText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            when (room.status) {
                                RoomStatus.AVAILABLE -> LunexAvailable
                                RoomStatus.OCCUPIED -> LunexOccupied
                                RoomStatus.FULL -> LunexFull
                            }
                        )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = when (room.status) {
                    RoomStatus.AVAILABLE -> "Available"
                    RoomStatus.FULL -> "Full"
                    RoomStatus.OCCUPIED -> "${room.occupants.size}/${room.capacity} occupied"
                },
                color = when (room.status) {
                    RoomStatus.AVAILABLE -> LunexAvailable
                    RoomStatus.FULL -> LunexFull
                    RoomStatus.OCCUPIED -> LunexOccupied
                },
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun BookingDialog(
    roomNumber: Int,
    onDismiss: () -> Unit,
    onSubmit: (name: String, phone: String, capacity: Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var capacity by remember { mutableIntStateOf(2) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = LunexPanel,
        shape = RoundedCornerShape(24.dp),
        title = {
            Column {
                Text(
                    text = "ROOM $roomNumber",
                    color = LunexAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Check-in details",
                    color = LunexText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorMessage = null
                    },
                    label = { Text("Your name (letters & spaces)") },
                    singleLine = true,
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
                    value = phone,
                    onValueChange = {
                        if (it.length <= 10 && it.all { c -> c.isDigit() }) {
                            phone = it
                            errorMessage = null
                        }
                    },
                    label = { Text("Mobile number (10 digits)") },
                    placeholder = { Text("9876543210") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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

                Column {
                    Text(
                        text = "How many people are taking this room?",
                        color = LunexText,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        IconButton(
                            onClick = { if (capacity > 1) capacity-- },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(LunexBg)
                                .border(1.dp, LunexLine, RoundedCornerShape(8.dp))
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = LunexText)
                        }

                        Text(
                            text = "$capacity guests",
                            color = LunexAccent,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        IconButton(
                            onClick = { if (capacity < 50) capacity++ },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(LunexBg)
                                .border(1.dp, LunexLine, RoundedCornerShape(8.dp))
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase", tint = LunexText)
                        }
                    }
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = LunexFull,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cleanName = name.trim()
                    if (!cleanName.matches(Regex("^[A-Za-z ]+$")) || cleanName.length < 2 || cleanName.length > 60) {
                        errorMessage = "Name must contain only letters and spaces (2-60 characters)."
                        return@Button
                    }
                    if (phone.length != 10) {
                        errorMessage = "Enter a valid 10-digit mobile number."
                        return@Button
                    }
                    onSubmit(cleanName, phone, capacity)
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LunexAccent),
                modifier = Modifier.testTag("book_room_submit_button")
            ) {
                Text("Book Room & Get Key", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = LunexMuted),
                border = androidx.compose.foundation.BorderStroke(1.dp, LunexLine)
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun KeyIssuedDialog(
    roomNumber: Int,
    key: String,
    onCopy: () -> Unit,
    onEnter: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {},
        containerColor = LunexPanel,
        shape = RoundedCornerShape(24.dp),
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "ROOM $roomNumber KEY ISSUED",
                    color = LunexAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Your 9-character key",
                    color = LunexText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Key Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(LunexBg)
                        .border(1.dp, LunexAccent.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                        .padding(18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = key,
                        color = LunexText,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 4.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onCopy,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LunexLine)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Key",
                        tint = LunexAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy Key", color = LunexText, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Share this key with the people you want to invite. They only need the key to enter.",
                    color = LunexMuted,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onEnter,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LunexAccent),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("enter_my_room_button")
            ) {
                Text("Enter My Room", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun JoinRoomDialog(
    onDismiss: () -> Unit,
    onSubmit: (roomNumber: Int, key: String) -> Unit
) {
    var roomText by remember { mutableStateOf("") }
    var keyText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = LunexPanel,
        shape = RoundedCornerShape(24.dp),
        title = {
            Column {
                Text(
                    text = "INVITATION",
                    color = LunexAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Enter your room key",
                    color = LunexText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedTextField(
                    value = roomText,
                    onValueChange = {
                        roomText = it
                        errorMessage = null
                    },
                    label = { Text("Room number (1-500)") },
                    placeholder = { Text("Example: 17") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                    value = keyText,
                    onValueChange = {
                        if (it.length <= 9) {
                            keyText = it.uppercase()
                            errorMessage = null
                        }
                    },
                    label = { Text("9-character key") },
                    placeholder = { Text("XXXXXXXXX") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
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

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = LunexFull,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val num = roomText.trim().toIntOrNull()
                    val key = keyText.trim().uppercase()
                    if (num == null || num !in 1..500 || key.length != 9) {
                        errorMessage = "Enter a valid room number (1-500) and 9-character key."
                        return@Button
                    }
                    onSubmit(num, key)
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LunexAccent),
                modifier = Modifier.testTag("unlock_room_button")
            ) {
                Text("Unlock Room", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = LunexMuted),
                border = androidx.compose.foundation.BorderStroke(1.dp, LunexLine)
            ) {
                Text("Cancel")
            }
        }
    )
}
