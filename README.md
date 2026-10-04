# 🔴 Lunex — Android Private Chat Hotel

Lunex is a hotel-style private real-time chat platform with exactly **500 numbered rooms**, rewritten as a modern native Android application built with Kotlin, Jetpack Compose, and Material Design 3.

## Features Preserved & Ported
- **Reception & Role Desk**: Guest / Customer entrance and Owner management desk.
- **500 Numbered Rooms**: Full hotel grid across floors with live available, occupied, and full statuses.
- **Strict Check-in & Booking**: Validation for guest name (letters and spaces only), 10-digit mobile number, and guest capacity (1–50 guests).
- **9-Character Cryptographic Keys**: Keys generated from `ABCDEFGHJKLMNPQRSTUVWXYZ23456789` with one-tap copy and share.
- **Key-Only Entry**: Invitees join with room number and 9-character key; anonymous guest labels assigned automatically.
- **Full Chat Experience**:
  - Real-time text messaging with anti-spam rate limiting.
  - Edit and delete own messages.
  - Image attachments via Android Photo Picker (`PickVisualMedia`).
  - Voice notes recorder with live timer and in-app audio playback.
  - System notices on guest arrivals and departures.
- **Auto-Clear & Reset on Exit**: When all occupants leave a room, the room is automatically reset, keys and messages cleared, and marked available for the next guest.
- **Owner Desk Management**:
  - Secure authentication matching `.env` configuration.
  - Real-time hotel statistics (Available, Occupied, and Full rooms).
  - Detailed room records: guest details, phone, capacity, issued key, active occupants, audit logs, and message trails.
  - Owner room closure and manual reset action.
- **Dark Luxury Aesthetic**: Custom dark burgundy and crimson palette matching the original Lunex hotel theme, edge-to-edge support, and adaptive launcher icon.

## Tech Stack
- **Language**: Kotlin 2.2+
- **UI Toolkit**: Jetpack Compose & Material 3
- **Architecture**: MVVM with Repository Pattern and Kotlin StateFlow
- **Image Loading**: Coil Compose
- **Build System**: Gradle 9+ & Android Gradle Plugin 9.1.1
