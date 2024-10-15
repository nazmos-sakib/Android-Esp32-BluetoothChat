package com.example.android_esp32_bluetoothchat.presentation.screens.terminal

import androidx.compose.runtime.Composable
import com.example.android_esp32_bluetoothchat.presentation.BluetoothUiState
import com.example.bchat.presentation.components.ChatScreen

@Composable
fun Terminal(
    state: BluetoothUiState,
    onDisconnect:()-> Unit,
    onSendMessage:(String)->Unit

){
    ChatScreen(
        state = state,
        onDisconnect =  onDisconnect,
        onSendMessage = onSendMessage
    )

}