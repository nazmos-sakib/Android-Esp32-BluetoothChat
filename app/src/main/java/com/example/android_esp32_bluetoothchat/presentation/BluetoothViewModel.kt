package com.example.android_esp32_bluetoothchat.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android_esp32_bluetoothchat.domain.BluetoothController
import com.example.android_esp32_bluetoothchat.domain.BluetoothDeviceDomain
import com.example.android_esp32_bluetoothchat.domain.ConnectionResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BluetoothViewModel @Inject constructor(
    private val bluetoothController: BluetoothController
) : ViewModel() {
    private val _state = MutableStateFlow(BluetoothUiState())
    //    val state: State<CoinListState> = _state
    val state = combine(
        bluetoothController.scannedDevices,
        bluetoothController.pairedDevices,
        _state
    ){ scannedDevices, pairedDevices, state ->
        state.copy(
            scannedDevices = scannedDevices,
            pairedDevices = pairedDevices,
            messages = if(state.isConnected)  state.messages else emptyList()
        )
    }.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),_state.value)

    private var deviceConnectionJob : Job?  = null

    init {
        //init observers
        bluetoothController.isConnected.onEach { isConnected->
            _state.update { it.copy(isConnected = isConnected) }
        }.launchIn(viewModelScope)

        bluetoothController.errors.onEach { error->
            _state.update {  it.copy(  errMessage = error ) }
        }.launchIn(viewModelScope)
    }

    fun connectToDevice(device:BluetoothDeviceDomain){
        _state.update { it.copy(isConnecting = true) }
        //waitForIncomingConnection() //newly added
        deviceConnectionJob = bluetoothController.connectToDevice(device).listen() //call the extension function
    }

    fun connectToESP(device:BluetoothDeviceDomain){
        _state.update { it.copy(isConnecting = true) }
        //waitForIncomingConnection() //newly added
        deviceConnectionJob = bluetoothController.connectToESP(device).listen() //call the extension function
    }

    fun disconnectFromDevice(){
        deviceConnectionJob?.cancel()
        bluetoothController.closeConnection()
        _state.update {  it.copy(  isConnecting = false, isConnected = false ) }
    }

    fun waitForIncomingConnection(){
        _state.update {  it.copy(  isConnecting = true ) }
        deviceConnectionJob = bluetoothController.startBluetoothServer().listen()
    }

    fun sendMessage(message:String){
        viewModelScope.launch {
            val bluetoothMessage = bluetoothController.trySendMessage(message)
            if (bluetoothMessage !=null){
                _state.update { it.copy(
                    messages = it.messages + bluetoothMessage
                ) }
            }

        }
    }

    fun startScan(){
        bluetoothController.startDiscovery()
    }

    fun stopScan(){
        bluetoothController.stopDiscovery()
    }

    fun updatePairedDevice(){
      bluetoothController.updatePairedDevises()
    }

    private fun Flow<ConnectionResult>.listen():Job{
        return onEach { result->
            when(result){
                is ConnectionResult.ConnectionEstablished ->{
                    _state.update {
                        it.copy(
                            isConnected = true,
                            isConnecting = false,
                            errMessage = null
                        )
                    }
                }

                is ConnectionResult.TransferSucceeded ->{
                    _state.update {  it.copy(
                        messages = it.messages + result.message
                    ) }
                }

                is ConnectionResult.Error -> {
                    _state.update {
                        it.copy(
                            isConnected = false,
                            isConnecting = false,
                            errMessage = result.message
                        )
                    }
                }
            }
        }.catch { throwable->
            bluetoothController.closeConnection()
            _state.update {
                it.copy(
                    isConnected = false,
                    isConnecting = false
                )
            }

        }.launchIn(viewModelScope)
    }

    override fun onCleared() {
        super.onCleared()
        bluetoothController.release()
    }



}