package com.example.android_esp32_bluetoothchat.domain

sealed interface ConnectionResult {
    data object ConnectionEstablished : ConnectionResult
    data class TransferSucceeded(val message: BluetoothMessage): ConnectionResult
    data class Error(val message:String): ConnectionResult
}