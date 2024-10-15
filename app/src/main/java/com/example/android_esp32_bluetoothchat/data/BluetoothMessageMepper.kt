package com.example.android_esp32_bluetoothchat.data

import com.example.android_esp32_bluetoothchat.domain.BluetoothMessage


fun String.toBluetoothMessage(isFromLocalUser:Boolean,senderName:String): BluetoothMessage {
//    val name = substringBeforeLast("#")
//    val message = substringAfter("#")
    return BluetoothMessage(
        message = this,
        senderName = senderName,
        isFromLocalUser = isFromLocalUser
    )
}

fun BluetoothMessage.toByteArray():ByteArray{
    return "$senderName#$message\n".encodeToByteArray()
}