package com.example.lunex.data.repository

import android.content.Context
import com.example.lunex.data.model.AuditEntry
import com.example.lunex.data.model.BookingInfo
import com.example.lunex.data.model.ChatMessage
import com.example.lunex.data.model.HotelRoom
import com.example.lunex.data.model.MessageType
import com.example.lunex.data.model.Occupant
import com.example.lunex.data.model.RoomStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.security.SecureRandom
import java.util.UUID

class HotelRepository private constructor(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val random = SecureRandom()
    private val keyChars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    private val persistenceFile = File(context.filesDir, "lunex_rooms.json")

    private val _rooms = MutableStateFlow<Map<Int, HotelRoom>>(emptyMap())
    val rooms: StateFlow<Map<Int, HotelRoom>> = _rooms.asStateFlow()

    init {
        loadRooms()
    }

    private fun generateKey(): String {
        val sb = StringBuilder(9)
        for (i in 0 until 9) {
            sb.append(keyChars[random.nextInt(keyChars.length)])
        }
        return sb.toString()
    }

    private fun sanitizeKey(raw: String?): String {
        return (raw ?: "").replace("\\s+".toRegex(), "").trim().uppercase()
    }

    private fun loadRooms() {
        val initialMap = mutableMapOf<Int, HotelRoom>()
        for (i in 1..500) {
            initialMap[i] = HotelRoom(number = i)
        }

        if (persistenceFile.exists()) {
            try {
                val jsonStr = persistenceFile.readText()
                val jsonObject = JSONObject(jsonStr)
                val roomsObj = jsonObject.optJSONObject("rooms")
                if (roomsObj != null) {
                    val keys = roomsObj.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        val num = key.toIntOrNull() ?: continue
                        if (num in 1..500) {
                            val rObj = roomsObj.getJSONObject(key)
                            val booked = rObj.optBoolean("booked", false)
                            val capacity = rObj.optInt("capacity", 0)
                            val roomKey = rObj.optString("key", "").takeIf { it.isNotEmpty() }

                            var bookingInfo: BookingInfo? = null
                            val bObj = rObj.optJSONObject("booking")
                            if (bObj != null) {
                                bookingInfo = BookingInfo(
                                    name = bObj.optString("name", "Guest"),
                                    phone = bObj.optString("phone", ""),
                                    capacity = bObj.optInt("capacity", capacity),
                                    bookedAt = bObj.optLong("bookedAt", System.currentTimeMillis())
                                )
                            }

                            val occupants = mutableListOf<Occupant>()
                            val occArr = rObj.optJSONArray("occupants")
                            if (occArr != null) {
                                for (j in 0 until occArr.length()) {
                                    val o = occArr.getJSONObject(j)
                                    occupants.add(
                                        Occupant(
                                            id = o.optString("id", UUID.randomUUID().toString()),
                                            label = o.optString("label", "Guest"),
                                            joinedAt = o.optLong("joinedAt", System.currentTimeMillis())
                                        )
                                    )
                                }
                            }

                            val messages = mutableListOf<ChatMessage>()
                            val msgArr = rObj.optJSONArray("messages")
                            if (msgArr != null) {
                                for (j in 0 until msgArr.length()) {
                                    val m = msgArr.getJSONObject(j)
                                    messages.add(
                                        ChatMessage(
                                            id = m.optString("id", UUID.randomUUID().toString()),
                                            roomNumber = num,
                                            senderId = m.optString("senderId").takeIf { it.isNotEmpty() },
                                            senderLabel = m.optString("senderLabel", "Guest"),
                                            type = runCatching { MessageType.valueOf(m.optString("type", "TEXT")) }.getOrDefault(MessageType.TEXT),
                                            text = m.optString("text").takeIf { it.isNotEmpty() },
                                            mediaUri = m.optString("mediaUri").takeIf { it.isNotEmpty() },
                                            mediaName = m.optString("mediaName").takeIf { it.isNotEmpty() },
                                            timestamp = m.optLong("timestamp", System.currentTimeMillis()),
                                            isEdited = m.optBoolean("isEdited", false),
                                            isDeleted = m.optBoolean("isDeleted", false)
                                        )
                                    )
                                }
                            }

                            val audits = mutableListOf<AuditEntry>()
                            val audArr = rObj.optJSONArray("audit")
                            if (audArr != null) {
                                for (j in 0 until audArr.length()) {
                                    val a = audArr.getJSONObject(j)
                                    audits.add(
                                        AuditEntry(
                                            id = a.optString("id", UUID.randomUUID().toString()),
                                            type = a.optString("type", "audit"),
                                            timestamp = a.optLong("timestamp", System.currentTimeMillis()),
                                            details = a.optString("details", "")
                                        )
                                    )
                                }
                            }

                            initialMap[num] = HotelRoom(
                                number = num,
                                booked = booked,
                                booking = bookingInfo,
                                key = roomKey,
                                capacity = capacity,
                                occupants = occupants,
                                messages = messages,
                                audit = audits
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        _rooms.value = initialMap
    }

    private fun saveRooms() {
        val current = _rooms.value
        scope.launch {
            try {
                val rootObj = JSONObject()
                val roomsObj = JSONObject()
                current.values.filter { it.booked || it.messages.isNotEmpty() || it.audit.isNotEmpty() }.forEach { room ->
                    val rObj = JSONObject()
                    rObj.put("number", room.number)
                    rObj.put("booked", room.booked)
                    rObj.put("capacity", room.capacity)
                    rObj.put("key", room.key ?: "")

                    room.booking?.let { b ->
                        val bObj = JSONObject()
                        bObj.put("name", b.name)
                        bObj.put("phone", b.phone)
                        bObj.put("capacity", b.capacity)
                        bObj.put("bookedAt", b.bookedAt)
                        rObj.put("booking", bObj)
                    }

                    val occArr = JSONArray()
                    room.occupants.forEach { occ ->
                        val o = JSONObject()
                        o.put("id", occ.id)
                        o.put("label", occ.label)
                        o.put("joinedAt", occ.joinedAt)
                        occArr.put(o)
                    }
                    rObj.put("occupants", occArr)

                    val msgArr = JSONArray()
                    room.messages.takeLast(500).forEach { msg ->
                        val m = JSONObject()
                        m.put("id", msg.id)
                        m.put("senderId", msg.senderId ?: "")
                        m.put("senderLabel", msg.senderLabel)
                        m.put("type", msg.type.name)
                        m.put("text", msg.text ?: "")
                        m.put("mediaUri", msg.mediaUri ?: "")
                        m.put("mediaName", msg.mediaName ?: "")
                        m.put("timestamp", msg.timestamp)
                        m.put("isEdited", msg.isEdited)
                        m.put("isDeleted", msg.isDeleted)
                        msgArr.put(m)
                    }
                    rObj.put("messages", msgArr)

                    val audArr = JSONArray()
                    room.audit.takeLast(300).forEach { aud ->
                        val a = JSONObject()
                        a.put("id", aud.id)
                        a.put("type", aud.type)
                        a.put("timestamp", aud.timestamp)
                        a.put("details", aud.details)
                        audArr.put(a)
                    }
                    rObj.put("audit", audArr)

                    roomsObj.put(room.number.toString(), rObj)
                }
                rootObj.put("rooms", roomsObj)

                val tmpFile = File(context.filesDir, "lunex_rooms.json.tmp")
                tmpFile.writeText(rootObj.toString())
                tmpFile.renameTo(persistenceFile)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @Synchronized
    fun bookRoom(roomNumber: Int, rawName: String, rawPhone: String, capacity: Int): Result<String> {
        if (roomNumber !in 1..500) {
            return Result.failure(IllegalArgumentException("Invalid room number."))
        }
        val room = _rooms.value[roomNumber] ?: return Result.failure(IllegalArgumentException("Room not found."))
        if (room.booked) {
            return Result.failure(IllegalStateException("Room #$roomNumber is already private & booked."))
        }

        val name = rawName.trim().replace("\\s+".toRegex(), " ")
        if (!name.matches(Regex("^[A-Za-z ]+$")) || name.length < 2 || name.length > 60) {
            return Result.failure(IllegalArgumentException("Name must contain only letters and spaces (2-60 characters)."))
        }

        val phone = rawPhone.trim()
        if (!phone.matches(Regex("^\\d{10}$"))) {
            return Result.failure(IllegalArgumentException("Enter a valid 10-digit mobile number."))
        }

        val targetCapacity = capacity.coerceIn(1, 50)
        val key = generateKey()
        val booking = BookingInfo(name = name, phone = phone, capacity = targetCapacity)
        val auditEntry = AuditEntry(
            id = UUID.randomUUID().toString(),
            type = "room_booked",
            details = "Booked by $name ($phone) for $targetCapacity guests. Key: $key"
        )

        val updatedRoom = room.copy(
            booked = true,
            capacity = targetCapacity,
            key = key,
            booking = booking,
            occupants = emptyList(),
            messages = emptyList(),
            audit = listOf(auditEntry)
        )

        _rooms.value = _rooms.value + (roomNumber to updatedRoom)
        saveRooms()
        return Result.success(key)
    }

    @Synchronized
    fun joinRoom(roomNumber: Int, key: String, nickname: String? = null): Result<Pair<HotelRoom, Occupant>> {
        if (roomNumber !in 1..500) {
            return Result.failure(IllegalArgumentException("Invalid room number."))
        }
        val room = _rooms.value[roomNumber] ?: return Result.failure(IllegalArgumentException("Room not found."))

        val cleanInputKey = sanitizeKey(key)
        val cleanRoomKey = sanitizeKey(room.key)

        if (!room.booked || cleanRoomKey.isEmpty()) {
            return Result.failure(IllegalArgumentException("Room #$roomNumber is not currently booked."))
        }
        if (cleanInputKey != cleanRoomKey) {
            return Result.failure(IllegalArgumentException("Invalid room key for Room #$roomNumber. Please verify the 9-character key."))
        }

        // All users who have the key are granted access.
        // Automatically expand capacity if needed to welcome invited guests.
        val newCapacity = maxOf(room.capacity, room.occupants.size + 1)

        val occupantId = "guest_" + UUID.randomUUID().toString().take(8)
        val occupantLabel = if (!nickname.isNullOrBlank()) {
            nickname.trim()
        } else {
            "Guest ${room.occupants.size + 1}"
        }
        val occupant = Occupant(id = occupantId, label = occupantLabel)

        val audit = AuditEntry(
            id = UUID.randomUUID().toString(),
            type = "entered",
            details = "$occupantLabel entered the room using the key."
        )

        val systemMsg = ChatMessage(
            id = "sys_" + UUID.randomUUID().toString().take(8),
            roomNumber = roomNumber,
            senderId = null,
            senderLabel = "Lunex Reception",
            type = MessageType.SYSTEM,
            text = "$occupantLabel joined the chat."
        )

        val updatedRoom = room.copy(
            capacity = newCapacity,
            occupants = room.occupants + occupant,
            messages = room.messages + systemMsg,
            audit = room.audit + audit
        )

        _rooms.value = _rooms.value + (roomNumber to updatedRoom)
        saveRooms()
        return Result.success(Pair(updatedRoom, occupant))
    }

    @Synchronized
    fun sendMessage(roomNumber: Int, senderId: String, senderLabel: String, text: String): Result<ChatMessage> {
        val room = _rooms.value[roomNumber] ?: return Result.failure(IllegalArgumentException("Room not found."))
        val value = text.trim()
        if (value.isEmpty() || value.length > 4000) {
            return Result.failure(IllegalArgumentException("Message cannot be empty or exceed 4000 characters."))
        }

        val recent = room.messages.filter {
            it.senderId == senderId && (System.currentTimeMillis() - it.timestamp) < 3000
        }
        if (recent.size >= 8) {
            return Result.failure(IllegalStateException("Please slow down for a moment."))
        }

        val msg = ChatMessage(
            id = "msg_" + UUID.randomUUID().toString().take(10),
            roomNumber = roomNumber,
            senderId = senderId,
            senderLabel = senderLabel,
            type = MessageType.TEXT,
            text = value
        )

        val audit = AuditEntry(
            id = UUID.randomUUID().toString(),
            type = "message_sent",
            details = "$senderLabel: $value"
        )

        val updatedRoom = room.copy(
            messages = room.messages + msg,
            audit = room.audit + audit
        )

        _rooms.value = _rooms.value + (roomNumber to updatedRoom)
        saveRooms()
        return Result.success(msg)
    }

    @Synchronized
    fun sendMediaMessage(
        roomNumber: Int,
        senderId: String,
        senderLabel: String,
        type: MessageType,
        uri: String,
        name: String
    ): Result<ChatMessage> {
        val room = _rooms.value[roomNumber] ?: return Result.failure(IllegalArgumentException("Room not found."))
        val msg = ChatMessage(
            id = "media_" + UUID.randomUUID().toString().take(10),
            roomNumber = roomNumber,
            senderId = senderId,
            senderLabel = senderLabel,
            type = type,
            mediaUri = uri,
            mediaName = name
        )

        val audit = AuditEntry(
            id = UUID.randomUUID().toString(),
            type = "media_sent",
            details = "$senderLabel sent ${type.name.lowercase()}: $name"
        )

        val updatedRoom = room.copy(
            messages = room.messages + msg,
            audit = room.audit + audit
        )

        _rooms.value = _rooms.value + (roomNumber to updatedRoom)
        saveRooms()
        return Result.success(msg)
    }

    @Synchronized
    fun editMessage(roomNumber: Int, messageId: String, newText: String, senderId: String): Result<Unit> {
        val room = _rooms.value[roomNumber] ?: return Result.failure(IllegalArgumentException("Room not found."))
        val msgIndex = room.messages.indexOfFirst { it.id == messageId }
        if (msgIndex == -1) return Result.failure(IllegalArgumentException("Message not found."))
        val oldMsg = room.messages[msgIndex]
        if (oldMsg.senderId != senderId) return Result.failure(IllegalStateException("Unauthorized."))

        val updatedList = room.messages.toMutableList()
        updatedList[msgIndex] = oldMsg.copy(
            text = newText.trim(),
            isEdited = true
        )

        val audit = AuditEntry(
            id = UUID.randomUUID().toString(),
            type = "message_edited",
            details = "${oldMsg.senderLabel} edited message: '$newText'"
        )

        val updatedRoom = room.copy(
            messages = updatedList,
            audit = room.audit + audit
        )

        _rooms.value = _rooms.value + (roomNumber to updatedRoom)
        saveRooms()
        return Result.success(Unit)
    }

    @Synchronized
    fun deleteMessage(roomNumber: Int, messageId: String, senderId: String): Result<Unit> {
        val room = _rooms.value[roomNumber] ?: return Result.failure(IllegalArgumentException("Room not found."))
        val msgIndex = room.messages.indexOfFirst { it.id == messageId }
        if (msgIndex == -1) return Result.failure(IllegalArgumentException("Message not found."))
        val oldMsg = room.messages[msgIndex]
        if (oldMsg.senderId != senderId) return Result.failure(IllegalStateException("Unauthorized."))

        val updatedList = room.messages.toMutableList()
        updatedList[msgIndex] = oldMsg.copy(
            text = null,
            mediaUri = null,
            mediaName = null,
            isDeleted = true
        )

        val audit = AuditEntry(
            id = UUID.randomUUID().toString(),
            type = "message_deleted",
            details = "${oldMsg.senderLabel} deleted a message."
        )

        val updatedRoom = room.copy(
            messages = updatedList,
            audit = room.audit + audit
        )

        _rooms.value = _rooms.value + (roomNumber to updatedRoom)
        saveRooms()
        return Result.success(Unit)
    }

    @Synchronized
    fun leaveRoomChat(roomNumber: Int, occupantId: String): Result<Unit> {
        val room = _rooms.value[roomNumber] ?: return Result.failure(IllegalArgumentException("Room not found."))
        val leaving = room.occupants.find { it.id == occupantId }
        val remainingOccupants = room.occupants.filter { it.id != occupantId }

        val audit = AuditEntry(
            id = UUID.randomUUID().toString(),
            type = "stepped_out",
            details = "${leaving?.label ?: "A guest"} stepped out to Reception."
        )

        val sysMsg = ChatMessage(
            id = "sys_" + UUID.randomUUID().toString().take(8),
            roomNumber = roomNumber,
            senderId = null,
            senderLabel = "Lunex Reception",
            type = MessageType.SYSTEM,
            text = "${leaving?.label ?: "A guest"} stepped out of the room."
        )

        // CRITICAL: Room REMAINS BOOKED, PRIVATE, AND OCCUPIED! Key and messages remain active!
        val updatedRoom = room.copy(
            occupants = remainingOccupants,
            messages = room.messages + sysMsg,
            audit = room.audit + audit
        )
        _rooms.value = _rooms.value + (roomNumber to updatedRoom)
        saveRooms()
        return Result.success(Unit)
    }

    @Synchronized
    fun checkoutAndCloseRoom(roomNumber: Int): Result<Unit> {
        val room = _rooms.value[roomNumber] ?: return Result.failure(IllegalArgumentException("Room not found."))
        val resetRoom = HotelRoom(
            number = roomNumber,
            booked = false,
            booking = null,
            key = null,
            capacity = 0,
            occupants = emptyList(),
            messages = emptyList(),
            audit = listOf(
                AuditEntry(
                    id = UUID.randomUUID().toString(),
                    type = "checked_out",
                    details = "Guest checked out. Room #$roomNumber has been cleared and is now available."
                )
            )
        )
        _rooms.value = _rooms.value + (roomNumber to resetRoom)
        saveRooms()
        return Result.success(Unit)
    }

    @Synchronized
    fun ownerResetRoom(roomNumber: Int): Result<Unit> {
        if (roomNumber !in 1..500) return Result.failure(IllegalArgumentException("Invalid room."))
        val resetRoom = HotelRoom(
            number = roomNumber,
            booked = false,
            booking = null,
            key = null,
            capacity = 0,
            occupants = emptyList(),
            messages = emptyList(),
            audit = listOf(
                AuditEntry(
                    id = UUID.randomUUID().toString(),
                    type = "room_closed",
                    details = "Room #$roomNumber was closed and reset by the Owner."
                )
            )
        )
        _rooms.value = _rooms.value + (roomNumber to resetRoom)
        saveRooms()
        return Result.success(Unit)
    }

    companion object {
        @Volatile
        private var instance: HotelRepository? = null

        fun getInstance(context: Context): HotelRepository {
            return instance ?: synchronized(this) {
                instance ?: HotelRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}
