package com.example.android_esp32_bluetoothchat.presentation.components

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.android_esp32_bluetoothchat.domain.getNavigationItems
import kotlinx.coroutines.launch



@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AppDrawer(
    modifier: Modifier = Modifier,
    onClickNavigate:(String,String)->Unit,
    currentIndex: String,
    topBar: @Composable (onNavButtonClick:()->Unit) -> Unit = {},
    content: @Composable () -> Unit
) {

    ///List of Navigation Items that will be clicked
    val items = getNavigationItems()


    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        modifier = modifier,
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(16.dp)) //space (margin) from top
                items.forEachIndexed { _, item ->
                    NavigationDrawerItem(
                        label = { Text(text = item.title) },
                        selected = item.route == currentIndex,
                        onClick = {
                            //  navController.navigate(item.route)

                            onClickNavigate(item.route,item.title)
                            //selectedItemIndex = item.route
                            scope.launch {
                                drawerState.close()
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (item.route == currentIndex) {
                                    item.selectedIcon
                                } else item.unselectedIcon,
                                contentDescription = item.title
                            )
                        },
                        modifier = Modifier
                            .padding(NavigationDrawerItemDefaults.ItemPadding) //padding between items
                    )
                }

            }
        },
        gesturesEnabled = true
    ) {
        Scaffold(
            topBar = {
                topBar {
                    scope.launch {
                        drawerState.apply {
                            if (isClosed) open() else close()
                        }
                    }
                }
            }
        ) {_->
            Box(
                Modifier
                    .fillMaxSize()
                    .padding()
            ) {
                content()
            }
        }
    }

}


@Preview
@Composable
fun AppDrawerPreview( ){
    AppDrawer(
        topBar = {
            TopBar(title = "Terminal", isConnected = false)
        },
        currentIndex = "",
        onClickNavigate = {_,_->}
    ) {
        Text(text = "sfgasdfafs", color = Color.Blue)
    }
}
