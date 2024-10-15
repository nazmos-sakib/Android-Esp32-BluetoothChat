package com.example.android_esp32_bluetoothchat.data

import android.annotation.SuppressLint
import android.bluetooth.BluetoothSocket
import com.example.android_esp32_bluetoothchat.domain.BluetoothMessage
import com.example.android_esp32_bluetoothchat.domain.TransferFailedException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.IOException

class BluetoothDataTransferService(
    private val socket: BluetoothSocket
) {
    @SuppressLint("MissingPermission")
    fun listenForIncomingMessages(): Flow<BluetoothMessage> {
        return flow {
            if (!socket.isConnected) {
                socket.close()
                return@flow
            }
            val buffer = ByteArray(1024)
            val stringBuilder = StringBuilder()
            while (true){

                try {
                    val byteCount = socket.inputStream.read(buffer)
                    if (byteCount == -1) {
                        throw IOException("Connection closed by peer")
                    }
                    val receivedString = String(buffer, 0, byteCount)
                    stringBuilder.append(receivedString)

                    // Check if the message is complete (assuming newline as end marker)
                    if (receivedString.contains("\n")) {
                        val completeMessage =
                            stringBuilder.toString().trim()  // Remove any trailing newlines
                        emit(
                            completeMessage.toBluetoothMessage(
                                isFromLocalUser = false,
                                senderName = socket.remoteDevice.name
                            )
                        )

                        // Clear the stringBuilder for the next message
                        stringBuilder.clear()
                    }
                } catch (e: IOException) {
                    throw TransferFailedException()
                }
            }
        }.flowOn(Dispatchers.IO)
    }


    suspend fun sendMessage(bytes:ByteArray):Boolean{
        return withContext(Dispatchers.IO){
            try {
                socket.outputStream.write(bytes)
            } catch (e:IOException){
                e.stackTrace
                return@withContext false
            }
            true
        }
    }
}