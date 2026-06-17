package com.sterrasec.apkinterceptor.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sterrasec.apkinterceptor.log.LogRepository
import com.sterrasec.apkinterceptor.ui.interceptor.InterceptorScreen
import com.sterrasec.apkinterceptor.ui.payload.PayloadScreen
import com.sterrasec.apkinterceptor.ui.sender.SenderScreen

enum class AppTab(val route: String, val label: String) {
    SENDER("sender", "Sender"),
    PAYLOAD("payload", "Payload"),
    INTERCEPTOR("interceptor", "Interceptor"),
}

@Composable
fun AppNavigation(
    selectedTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
) {
    val logs by LogRepository.logs.collectAsStateWithLifecycle()
    Scaffold(
        bottomBar = {
            NavigationBar {
                AppTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { onTabSelected(tab) },
                        icon = { Text(tab.label.take(1)) },
                        label = { Text(tab.label) },
                    )
                }
            }
        },
    ) { innerPadding ->
        when (selectedTab) {
            AppTab.SENDER -> SenderScreen(modifier = Modifier.padding(innerPadding))
            AppTab.PAYLOAD -> PayloadScreen(entries = logs, modifier = Modifier.padding(innerPadding))
            AppTab.INTERCEPTOR -> InterceptorScreen(entries = logs, modifier = Modifier.padding(innerPadding))
        }
    }
}
