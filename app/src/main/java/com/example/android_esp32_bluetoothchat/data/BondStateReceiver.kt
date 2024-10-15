package com.example.android_esp32_bluetoothchat.data

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BondStateReceiver (
    private val onFinish:() -> Unit
): BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action
        if (action == BluetoothDevice.ACTION_BOND_STATE_CHANGED) {
            val state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE)
            val prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.BOND_NONE)
            if (state == BluetoothDevice.BOND_BONDED && prevState != BluetoothDevice.BOND_BONDED) {
                // Bonding is complete
                println("bonded")
                onFinish() // Now you can update the paired devices
                context?.unregisterReceiver(this) // Unregister the receiver once done
            }
        }
    }
}