package com.example.android_esp32_bluetoothchat.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface BluetoothController {
    val isConnected: StateFlow<Boolean>
    val scannedDevices: StateFlow<List<BluetoothDevice>>
    val pairedDevices: StateFlow<List<BluetoothDevice>>
    val errors:SharedFlow<String>

    fun startDiscovery()
    fun stopDiscovery()

    fun updatePairedDevises()
    fun startBluetoothServer(): Flow<ConnectionResult>
    fun connectToDevice(device:BluetoothDeviceDomain): Flow<ConnectionResult>

    suspend fun trySendMessage(message:String):BluetoothMessage?

    fun closeConnection()

    fun release()

    fun connectToESP(device:BluetoothDeviceDomain): Flow<ConnectionResult>

}