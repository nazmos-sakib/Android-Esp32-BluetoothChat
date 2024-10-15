package com.example.android_esp32_bluetoothchat.domain

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.android_esp32_bluetoothchat.presentation.BluetoothUiState
import com.example.android_esp32_bluetoothchat.presentation.BluetoothViewModel
import com.example.android_esp32_bluetoothchat.presentation.screens.terminal.Terminal
import com.example.android_esp32_bluetoothchat.presentation.screens.info.Info
import com.example.android_esp32_bluetoothchat.presentation.screens.pair_new_devices.PairNewDevice
import com.example.android_esp32_bluetoothchat.presentation.screens.saved_devices.SavedDevices
import com.example.android_esp32_bluetoothchat.presentation.screens.settings.Settings


@Composable
fun rememberAppState(navController: NavHostController = rememberNavController()) =
    remember(navController) {
        AppState(navController)
    }


//extended function of navHost
fun NavGraphBuilder.routsGraph(
    appState:  AppState,
    uiState: BluetoothUiState,
    viewModel: BluetoothViewModel
) {
    when{
        uiState.isConnected ->{
            appState.navigate(ROUTE_TERMINAL)
        }
    }
    composable("fdsg") {
        //SplashScreen(openAndPopUp = { route, popUp -> appState.navigateAndPopUp(route, popUp) })
        //SplashScreen(openAndPopUp = { route, popUp -> appState.clearAndNavigate(route) })
    }


    composable(ROUTE_TERMINAL){
        //HomeScreen(onClickGotoBluetoothScreen = { route -> appState.navigate (route) })
        Terminal(
            state = uiState,
            onDisconnect = viewModel::disconnectFromDevice,
            onSendMessage = viewModel::sendMessage
        )
    }
    composable(ROUTE_PAIR_NEW_DEVICE){
        //HomeScreen(onClickGotoBluetoothScreen = { route -> appState.navigate (route) })
        PairNewDevice(
            scannedDevices = uiState.scannedDevices,
            onDeviceClick = viewModel::connectToDevice
        )
    }
    composable(ROUTE_SAVED_DEVICES){
        //HomeScreen(onClickGotoBluetoothScreen = { route -> appState.navigate (route) })
        //viewModel.updatePairedDevice()
        SavedDevices(
            pairedDevice = uiState.pairedDevices,
            onDeviceClick = viewModel::connectToESP
        )
    }
    composable(ROUTE_SETTINGS){
        //HomeScreen(onClickGotoBluetoothScreen = { route -> appState.navigate (route) })
        Settings()
    }
    composable(ROUTE_INFO){
        //HomeScreen(onClickGotoBluetoothScreen = { route -> appState.navigate (route) })
        Info()
    }
}