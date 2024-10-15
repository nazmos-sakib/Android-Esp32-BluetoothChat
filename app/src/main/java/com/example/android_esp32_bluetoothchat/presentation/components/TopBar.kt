package com.example.android_esp32_bluetoothchat.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.android_esp32_bluetoothchat.R
 import com.example.android_esp32_bluetoothchat.presentation.ui.theme.AndroidEsp32BluetoothChatTheme
import com.example.android_esp32_bluetoothchat.presentation.ui.theme.PurpleGrey40
import com.example.android_esp32_bluetoothchat.presentation.ui.theme.PurpleGrey80


@Composable
fun TopBar(
    title:String,
    isConnected:Boolean,
    onNavigationIconClick:()->Unit = {},
    modifier: Modifier = Modifier
){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PurpleGrey40)
            .padding(horizontal = 5.dp, vertical = 10.dp),
        horizontalArrangement  = Arrangement.SpaceEvenly,
        verticalAlignment  = Alignment.CenterVertically,
    ) {
        Icon(imageVector = Icons.Default.Menu, contentDescription = "App drawer", tint = Color.White, modifier = Modifier.clickable { onNavigationIconClick() })
        Text(
            text = title,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 20.dp),
            color = Color.White,
            fontSize = 20.sp)
        Icon(painter = painterResource(id = if (isConnected) R.drawable.bluetooth_connected_24 else R.drawable.bluetooth_not_connected), contentDescription = "App drawer", tint = Color.White)
        Spacer(modifier = Modifier.width(5.dp))
        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "App drawer", tint = Color.White)


    }
}

@Preview
@Composable
fun TopBarPreview(){
    AndroidEsp32BluetoothChatTheme {
        TopBar(title = "Terminal",isConnected = false)
    }
}