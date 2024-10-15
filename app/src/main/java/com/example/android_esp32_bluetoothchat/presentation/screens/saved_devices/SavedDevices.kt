package com.example.android_esp32_bluetoothchat.presentation.screens.saved_devices

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
 import com.example.android_esp32_bluetoothchat.domain.BluetoothDeviceDomain

@Composable
fun SavedDevices(
    pairedDevice: List<BluetoothDeviceDomain>,
    onDeviceClick :(BluetoothDeviceDomain)->Unit,
    modifier: Modifier = Modifier
){
    LazyColumn(modifier = modifier) {
        item {
            Text(text = "Paired Devices", fontWeight = FontWeight.Bold, fontSize = 24.sp, modifier = Modifier.padding(16.dp))
        }
        items(pairedDevice){device->
            Text(
                text = device.name.toString(),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDeviceClick(device) }
                    .padding(16.dp)
            )
        }

    }
}