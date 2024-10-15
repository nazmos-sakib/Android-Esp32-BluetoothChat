package com.example.android_esp32_bluetoothchat.data

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import com.example.android_esp32_bluetoothchat.domain.BluetoothController
import com.example.android_esp32_bluetoothchat.domain.BluetoothDeviceDomain
import com.example.android_esp32_bluetoothchat.domain.BluetoothMessage
import com.example.android_esp32_bluetoothchat.domain.ConnectionResult
import com.example.android_esp32_bluetoothchat.domain.ERROR_TAG
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.UUID


@SuppressLint("MissingPermission")
class AndroidBluetoothController(
    private val context: Context
): BluetoothController {

    private val bluetoothManager by lazy {
        context.getSystemService(BluetoothManager::class.java)
    }
    private val bluetoothAdapter by lazy {
        bluetoothManager?.adapter
    }

    private var dataTransferService:BluetoothDataTransferService? = null

    private val _isConnected = MutableStateFlow<Boolean>(false)
    override val isConnected: StateFlow<Boolean>
        get() = _isConnected.asStateFlow()

    private val _scannedDevices = MutableStateFlow<List<BluetoothDeviceDomain>>(emptyList())
    override val scannedDevices: StateFlow<List<BluetoothDeviceDomain>>
        get() = _scannedDevices.asStateFlow()


    private val _pairedDevices = MutableStateFlow<List<BluetoothDeviceDomain>>(emptyList())
    override val pairedDevices: StateFlow<List<BluetoothDeviceDomain>>
        get() = _pairedDevices.asStateFlow()


    private val _errors = MutableSharedFlow<String>()
    override val errors: SharedFlow<String>
        get() = _errors.asSharedFlow()


    //broadcast receiver -----------------------------------------------
    //broadcast receiver to get bluetooth device list when search
    private val foundDeviceReceiver = FoundDeviceReceiver{ device ->
        _scannedDevices.update { devices ->
            val newDevice = device.toBluetoothDeviceDomain()
            if (newDevice in devices) devices else devices + newDevice
        }
    }

    //broadcast receiver - when a connection is interrupted with an existed connected device
    private val bluetoothStateReceiver = BluetoothStateReceiver{ isConnected, bluetoothDevice ->
        if (bluetoothAdapter?.bondedDevices?.contains(bluetoothDevice) == true){
            _isConnected.update {isConnected}
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                _errors.emit("Disconnected: can't connect to a non-paired device")
            }
        }
    }

    //broadcast receiver for observing pairing mode
    private val pairingReceiver = PairingReceiver { device ->
        //connectToDevice(device)  // Automatically connect after pairing
    }

    //on connection to a device observe the bonding with the device
    private val bondStateReceiver = BondStateReceiver{
        updatePairedDevises()
        //context.unregisterReceiver(bondStateReceiver)
    }
    //end broadcast receiver -----------------------------------------------

    private var currentServerSocket: BluetoothServerSocket? = null
    private var currentClientSocket: BluetoothSocket? = null

    init {
        updatePairedDevises()
        //register bluetoothStateReceiver (disconnected) receiver
        context.registerReceiver(
            bluetoothStateReceiver,
            //intent_filter take one arguments
            IntentFilter().apply {
                addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
                addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
                addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            }
        )

        //register pairing receiver
        context.registerReceiver(
            pairingReceiver,
            IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        )
    }

    override fun startDiscovery() {
        if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)){
            return
        }
        //register a broadcast receiver to update
        context.registerReceiver(
            foundDeviceReceiver,
            IntentFilter(android.bluetooth.BluetoothDevice.ACTION_FOUND)
        )

        updatePairedDevises()
        bluetoothAdapter?.startDiscovery()
    }

    override fun stopDiscovery() {
        if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)){
            return
        }
        bluetoothAdapter?.cancelDiscovery()
    }

    override fun startBluetoothServer(): Flow<ConnectionResult> {
        return flow {
            if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)){
                throw SecurityException("No BLUETOOTH_CONNECT permission")
            }
            currentServerSocket = bluetoothAdapter?.listenUsingRfcommWithServiceRecord(
                "chat_service",
                UUID.fromString(SERVICE_UUID)
            )

            var shouldLoop = true
            while (shouldLoop){
                currentClientSocket = try {
                    currentServerSocket?.accept()

                } catch (e:IOException){
                    shouldLoop = false
                    null
                }
                emit(ConnectionResult.ConnectionEstablished)
                currentClientSocket?.let {
                    currentServerSocket?.close()
                    val service = BluetoothDataTransferService(it)
                    dataTransferService = service

                    emitAll(
                        service.listenForIncomingMessages()
                            .map {
                                ConnectionResult.TransferSucceeded(it)
                            }
                    )
                }
            }
        }.onCompletion {
            closeConnection()
        }.flowOn(Dispatchers.IO)
    }

    private fun pairDevice(device: BluetoothDevice) {
        // Check if the device is already paired
        if (device.bondState == BluetoothDevice.BOND_BONDED) {
            Toast.makeText(context, "Already paired with ${device.name}", Toast.LENGTH_SHORT).show()
            connectToDevice(device.toBluetoothDeviceDomain())
        } else {
            // Initiate pairing
            device.createBond()
        }
    }

    override fun connectToDevice(device: BluetoothDeviceDomain): Flow<ConnectionResult> {
        return flow {
            if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)){
                throw SecurityException("No BLUETOOTH_CONNECT permission")
            }

            // Register bond broadcast receiver
            // it will automatically unregister
             context.registerReceiver(bondStateReceiver, IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED))


            //get BluetoothDevice from BluetoothDeviceDomain
            val bluetoothDevice = bluetoothAdapter?.getRemoteDevice(device.address)

            if (bluetoothDevice?.bondState != BluetoothDevice.BOND_BONDED) {
                bluetoothDevice?.createBond()
            }

            //new
            currentServerSocket = bluetoothAdapter?.listenUsingRfcommWithServiceRecord(
                "chat_service",
                UUID.fromString(SERVICE_UUID)
            ) //new

            currentClientSocket = bluetoothDevice
            ?.createRfcommSocketToServiceRecord(
                UUID.fromString(SERVICE_UUID)
            )

            stopDiscovery()


            currentClientSocket?.let { clintSocket->
                try {
                    clintSocket.connect()
                    emit(ConnectionResult.ConnectionEstablished)
                    BluetoothDataTransferService(clintSocket).also {
                        dataTransferService = it
                        emitAll(
                            it.listenForIncomingMessages()
                                .map {bluetoothMessage->
                                    ConnectionResult.TransferSucceeded(bluetoothMessage)
                                }
                        )
                    }
                } catch (e:IOException){
                    Log.d(ERROR_TAG, "connectToDevice: ${e.message}")
                    clintSocket.close()
                    currentClientSocket = null
                    emit(ConnectionResult.Error("Connection was interrupted: ${e.message}"))
                }
            }
        }.onCompletion {
            closeConnection()
        }.flowOn(Dispatchers.IO)
    }

    override fun connectToESP(device: BluetoothDeviceDomain): Flow<ConnectionResult> {
        return flow {
            if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)){
                throw SecurityException("No BLUETOOTH_CONNECT permission")
            }
            val clintDevice: BluetoothDevice? = bluetoothAdapter?.getRemoteDevice(device.address)
            val uuid: UUID? = clintDevice?.uuids?.get(0)?.uuid // UUID provided by your ESP32 service
            val clintSocket: BluetoothSocket? = clintDevice?.createRfcommSocketToServiceRecord(uuid)
            // Ensure Bluetooth discovery is stopped before trying to connect
            bluetoothAdapter?.cancelDiscovery()

            clintSocket?.let { socket->
                try {
                    socket.connect()
                    emit(ConnectionResult.ConnectionEstablished)
                    BluetoothDataTransferService(socket).also {
                        dataTransferService = it
                        emitAll(
                            it.listenForIncomingMessages()
                                .map {bluetoothMessage->
                                    ConnectionResult.TransferSucceeded(bluetoothMessage)
                                }
                        )
                    }
                } catch (e:IOException){
                    Log.d(ERROR_TAG, "connectToDevice: ${e.message}")
                    socket.close()
                    currentClientSocket = null
                    emit(ConnectionResult.Error("Connection was interrupted: ${e.message}"))
                }
            }
        }.onCompletion {
            closeConnection()
        }.flowOn(Dispatchers.IO)

    }

    override suspend fun trySendMessage(message: String): BluetoothMessage? {
        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)){ return null}

        if (dataTransferService == null){
            return null
        }

        val bluetoothMessage = BluetoothMessage(
            message = message,
            senderName = bluetoothAdapter?.name?: "Unknown name ",
            isFromLocalUser = true
        )

        //sending the message
        dataTransferService?.sendMessage(bluetoothMessage.toByteArray())

        return bluetoothMessage
    }

    override fun closeConnection() {
        currentServerSocket?.close()
        currentServerSocket = null
        currentClientSocket?.close()
        currentClientSocket = null
    }

    override fun release() {
        context.unregisterReceiver(foundDeviceReceiver)
        context.unregisterReceiver(bluetoothStateReceiver)
        context.unregisterReceiver(pairingReceiver)
        closeConnection()
    }

    override fun updatePairedDevises(){
        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)){
            return
        }
        bluetoothAdapter
            ?.bondedDevices
            ?.map{
                it.toBluetoothDeviceDomain()
            }
            ?.also { devices->
                println("device size: ${devices.size}")
                _pairedDevices.update { devices }
            }
    }

    private fun hasPermission(permission:String):Boolean{
        return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private const val SERVICE_UUID = "6fb9c1f2-243e-4e54-8ea3-6d650c2f2c8f"
    }

}