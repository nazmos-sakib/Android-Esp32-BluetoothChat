package com.example.android_esp32_bluetoothchat.domain


typealias BluetoothDeviceDomain = BluetoothDevice

data class BluetoothDevice(
    val name:String?,
    val address:String,
    val type: Int,
    val bondState:Int
)