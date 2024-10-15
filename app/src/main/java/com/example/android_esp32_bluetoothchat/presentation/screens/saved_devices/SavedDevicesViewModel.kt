package com.example.android_esp32_bluetoothchat.presentation.screens.saved_devices

import androidx.lifecycle.viewModelScope
import com.example.android_esp32_bluetoothchat.domain.BluetoothController
import com.example.android_esp32_bluetoothchat.presentation.AppViewModel
import com.example.android_esp32_bluetoothchat.presentation.BluetoothUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class SavedDevicesViewModel  @Inject constructor(
    private val bluetoothController: BluetoothController
) : AppViewModel(){
    private val _state = MutableStateFlow(BluetoothUiState())
    //    val state: State<CoinListState> = _state
    val state = combine(
        bluetoothController.pairedDevices,
        _state
    ){  pairedDevices, state ->
        state.copy(
            pairedDevices = pairedDevices,
            messages = if(state.isConnected)  state.messages else emptyList()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000),_state.value)

    private var deviceConnectionJob : Job?  = null

    init {
        //init observers
        bluetoothController.errors.onEach { error->
            _state.update {  it.copy(  errMessage = error ) }
        }.launchIn(viewModelScope)
    }



}