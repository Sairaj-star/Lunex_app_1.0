package com.example.lunex.data.model

enum class RoomStatus {
    AVAILABLE,
    OCCUPIED
}

enum class MessageType {
    TEXT,
    SYSTEM,
    IMAGE,
    AUDIO
}

data class Occupant(
    val id: String,
    val label: String,
    val joinedAt: Long = System.currentTimeMillis()
)

data class BookingInfo(
    val name: String,
    val phone: String,
    val capacity: Int,
    val bookedAt: Long = System.currentTimeMillis()
)

data class ChatMessage(
    val id: String,
    val roomNumber: Int,
    val senderId: String?,
    val senderLabel: String,
    val type: MessageType,
    val text: String? = null,
    val mediaUri: String? = null,
    val mediaName: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isEdited: Boolean = false,
    val isDeleted: Boolean = false
)

data class AuditEntry(
    val id: String,
    val type: String,
    val timestamp: Long = System.currentTimeMillis(),
    val details: String
)

data class HotelRoom(
    val number: Int,
    val booked: Boolean = false,
    val booking: BookingInfo? = null,
    val key: String? = null,
    val capacity: Int = 0,
    val occupants: List<Occupant> = emptyList(),
    val messages: List<ChatMessage> = emptyList(),
    val audit: List<AuditEntry> = emptyList()
) {
    val status: RoomStatus
        get() = if (booked) RoomStatus.OCCUPIED else RoomStatus.AVAILABLE

    val isPrivate: Boolean
        get() = booked
}
