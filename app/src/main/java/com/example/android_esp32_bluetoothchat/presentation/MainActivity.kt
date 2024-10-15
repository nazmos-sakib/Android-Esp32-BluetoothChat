package com.example.android_esp32_bluetoothchat.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.android_esp32_bluetoothchat.domain.BluetoothMessage
import com.example.android_esp32_bluetoothchat.domain.ROUTE_PAIR_NEW_DEVICE
import com.example.android_esp32_bluetoothchat.domain.ROUTE_TERMINAL
import com.example.android_esp32_bluetoothchat.domain.getNavigationItems
import com.example.android_esp32_bluetoothchat.domain.rememberAppState
import com.example.android_esp32_bluetoothchat.domain.routsGraph
import com.example.android_esp32_bluetoothchat.presentation.components.AppDrawer
import com.example.android_esp32_bluetoothchat.presentation.components.TopBar
import com.example.android_esp32_bluetoothchat.presentation.ui.theme.AndroidEsp32BluetoothChatTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var discoverableLauncher: ActivityResultLauncher<Intent>

    private val bluetoothManager by lazy {
        applicationContext.getSystemService(BluetoothManager::class.java)
    }
    private val bluetoothAdapter by lazy {
        bluetoothManager?.adapter
    }
    private val isBluetoothEnabled:Boolean
        get() = bluetoothAdapter?.isEnabled == true

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        //enable bluetooth discovery
        discoverableLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Bluetooth discoverability denied", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Device is now discoverable for ${result.resultCode} seconds", Toast.LENGTH_SHORT).show()
            }
        }



        //check if the device bluetooth is enabled
        val enabledBluetoothLauncher = registerForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ){ /*not needed */}

        //checkBluetooth permission
        val permissionLauncher = registerForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { perms->
            //1st- check bluetooth can be enable
            val canEnableBluetooth = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                perms[Manifest.permission.BLUETOOTH_CONNECT] == true
            } else {
                true
            }

            // 2nd - if the bluetooth is available but not enabled
            if (canEnableBluetooth && !isBluetoothEnabled){
                enabledBluetoothLauncher.launch(
                    Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                )
            }
        }


        //check permission and if not permitted then ask for permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }

        enableDiscoverable()

        setContent {
            AndroidEsp32BluetoothChatTheme {
                val viewModel:BluetoothViewModel = hiltViewModel()
                val state by viewModel.state.collectAsState()
                var appDrawerSelectedRoute by remember {
                    mutableStateOf(ROUTE_TERMINAL)
                }

                LaunchedEffect(key1 = state.errMessage) {
                    state.errMessage?.let {message->
                        Toast.makeText(applicationContext,"err:$message", Toast.LENGTH_SHORT).show()
                    }
                }
                val appState = rememberAppState()
                val navBackStackEntry by appState.navController.currentBackStackEntryAsState()
                // Extract the current route or destination
                val currentRoute = navBackStackEntry?.destination?.route

                //observe changes in the route
                LaunchedEffect(currentRoute) {
                    currentRoute?.let {route->
                        // Do something whenever the route changes
                        //println("Route changed: $route")
                        //if current route is to discover near by devices the then enable bluetooth discovery mode.
                        appDrawerSelectedRoute = route
                        if (route== ROUTE_PAIR_NEW_DEVICE) viewModel.startScan() else viewModel.stopScan()

                    }
                }
                //constantly observing if the connection with
                // a bluetooth device is active or not
                LaunchedEffect(key1 = state.isConnected) {
                    if (state.isConnected  ) {
                        Toast.makeText(applicationContext,"You are connected",Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(applicationContext,"disconnected",Toast.LENGTH_SHORT).show()
                    }
                }

                //always active server socket
                LaunchedEffect(true) {
                    //viewModel.waitForIncomingConnection()
                }

                var topAppBarTitle by remember {
                    mutableStateOf("Terminal")
                }


                Scaffold { innerPadding->
                    AppDrawer(
                        modifier = Modifier.padding(innerPadding),
                        topBar = { onNavClick->
                            TopBar(title = topAppBarTitle, isConnected = state.isConnected, onNavigationIconClick = onNavClick)
                        },
                        currentIndex = appDrawerSelectedRoute,
                        onClickNavigate = {route,title->
                            appState.navigate(route)
                            topAppBarTitle = title
                            //appDrawerSelectedRoute = route
                        }
                    ) {
                        NavHost(
                            navController = appState.navController,
                            startDestination = ROUTE_TERMINAL,
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            routsGraph(
                                appState = appState,
                                uiState = state,
                                viewModel = viewModel
                            )
                        }
                    }
                }

            }
        }
    }

    private fun enableDiscoverable() {
        Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
            putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300) // 300 seconds (5 minutes)
        }.also {discoverableIntent->
            discoverableLauncher.launch(discoverableIntent)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
