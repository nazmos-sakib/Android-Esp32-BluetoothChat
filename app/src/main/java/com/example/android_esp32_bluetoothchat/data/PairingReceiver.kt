package com.example.android_esp32_bluetoothchat.data

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import com.example.android_esp32_bluetoothchat.domain.BluetoothDeviceDomain

class PairingReceiver(
    private val onDevicePaired: (BluetoothDeviceDomain) -> Unit
) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                val device: BluetoothDevice? = getBluetoothDeviceFromIntent(intent)
                val bondState =
                    intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR)
                when (bondState) {
                    BluetoothDevice.BOND_BONDED -> {
                        // Device is successfully paired
                        Toast.makeText(context, "Paired with ${device?.toBluetoothDeviceDomain()?.name}", Toast.LENGTH_SHORT)
                            .show()
                        device?.let { onDevicePaired(it.toBluetoothDeviceDomain()) }  // Connect to the device after pairing
                    }

                    BluetoothDevice.BOND_BONDING -> {
                        // Pairing in progress
                        Toast.makeText(context, "Pairing with ${device?.toBluetoothDeviceDomain()?.name}", Toast.LENGTH_SHORT)
                            .show()
                    }

                    BluetoothDevice.BOND_NONE -> {
                        // Pairing failed or unpaired
                        Toast.makeText(
                            context,
                            "Pairing failed with ${device?.toBluetoothDeviceDomain()?.name}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun getBluetoothDeviceFromIntent(intent: Intent):BluetoothDevice?{
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(
                BluetoothDevice.EXTRA_DEVICE,
                BluetoothDevice::class.java
            )
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(BluetoothDevice.EXTRA_NAME)
        }
    }
}
