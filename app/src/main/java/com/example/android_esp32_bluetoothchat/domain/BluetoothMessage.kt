package com.example.android_esp32_bluetoothchat.domain


data class BluetoothMessage(
    val message:String,
    val senderName:String,
    val isFromLocalUser:Boolean
)
