package com.example.android_esp32_bluetoothchat.domain

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.example.android_esp32_bluetoothchat.R

data class NavigationItems(
    val title: String,
    val route:String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val badgeCount: Int? = null
)


@Composable
fun getNavigationItems():List<NavigationItems> = listOf(
    NavigationItems(
        title = "Terminal",
        route = ROUTE_TERMINAL,
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    ),
    NavigationItems(
        title = "Pair New Device",
        route = ROUTE_PAIR_NEW_DEVICE,
        selectedIcon = Icons.Filled.Add,
        unselectedIcon = Icons.Outlined.Add
    ),
    NavigationItems(
        title = "Saved Devices",
        route = ROUTE_SAVED_DEVICES,
        selectedIcon = ImageVector.vectorResource(R.drawable.phone_phone_24),
        unselectedIcon = ImageVector.vectorResource(R.drawable.outline_phone_phone_24),
    ),
    NavigationItems(
        title = "Settings",
        route = ROUTE_SETTINGS,
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    ),

    NavigationItems(
        title = "Info",
        route = ROUTE_INFO,
        selectedIcon = Icons.Filled.Info,
        unselectedIcon = Icons.Outlined.Info
    )
)