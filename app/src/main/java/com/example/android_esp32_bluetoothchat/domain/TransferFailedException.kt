package com.example.android_esp32_bluetoothchat.domain

import java.io.IOException

class TransferFailedException:IOException("Reading Incoming Data failed")