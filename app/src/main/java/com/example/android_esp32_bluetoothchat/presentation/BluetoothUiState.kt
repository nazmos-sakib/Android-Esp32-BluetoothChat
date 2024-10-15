package com.example.android_esp32_bluetoothchat.presentation

import com.example.android_esp32_bluetoothchat.domain.BluetoothDeviceDomain
import com.example.android_esp32_bluetoothchat.domain.BluetoothMessage

data class BluetoothUiState(
    val scannedDevices: List<BluetoothDeviceDomain> = emptyList(),
    val pairedDevices: List<BluetoothDeviceDomain> = emptyList(),
    val isConnected:Boolean = false,
    val isConnecting:Boolean = false,
    val errMessage:String? = null,
    val messages:List<BluetoothMessage> = emptyList()
)
