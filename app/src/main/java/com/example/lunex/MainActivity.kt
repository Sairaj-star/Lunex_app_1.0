package com.example.lunex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.lunex.data.model.Occupant
import com.example.lunex.data.repository.HotelRepository
import com.example.lunex.ui.screens.OwnerDeskScreen
import com.example.lunex.ui.screens.ReceptionRoomsScreen
import com.example.lunex.ui.screens.RoomChatScreen
import com.example.lunex.ui.screens.WelcomeScreen
import com.example.lunex.ui.theme.LunexBg
import com.example.lunex.ui.theme.LunexTheme

sealed class Screen {
    data object Welcome : Screen()
    data object Reception : Screen()
    data class Chat(val roomNumber: Int, val roomKey: String, val occupant: Occupant) : Screen()
    data object Owner : Screen()
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = HotelRepository.getInstance(applicationContext)

        setContent {
            LunexTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = LunexBg
                ) {
                    Box(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
                        LunexApp(repository = repository)
                    }
                }
            }
        }
    }
}

@Composable
fun LunexApp(repository: HotelRepository) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Welcome) }

    when (val screen = currentScreen) {
        is Screen.Welcome -> {
            WelcomeScreen(
                onSelectCustomer = { currentScreen = Screen.Reception },
                onSelectOwner = { currentScreen = Screen.Owner }
            )
        }
        is Screen.Reception -> {
            ReceptionRoomsScreen(
                repository = repository,
                onBack = { currentScreen = Screen.Welcome },
                onEnterRoom = { roomNum, key, occ ->
                    currentScreen = Screen.Chat(roomNum, key, occ)
                }
            )
        }
        is Screen.Chat -> {
            RoomChatScreen(
                roomNumber = screen.roomNumber,
                roomKey = screen.roomKey,
                occupant = screen.occupant,
                repository = repository,
                onExit = {
                    currentScreen = Screen.Reception
                }
            )
        }
        is Screen.Owner -> {
            OwnerDeskScreen(
                repository = repository,
                onBack = { currentScreen = Screen.Welcome }
            )
        }
    }
}
