package com.sterrasec.apkinterceptor

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.sterrasec.apkinterceptor.ui.navigation.AppNavigation
import com.sterrasec.apkinterceptor.ui.navigation.AppTab

class MainActivity : ComponentActivity() {
    private var selectedTab by mutableStateOf(AppTab.SENDER)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        updateSelectedTab(intent)
        setContent {
            var showAuthorizedUseDialog by remember { mutableStateOf(shouldShowAuthorizedUseDialog()) }
            MaterialTheme {
                AppNavigation(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                )
                if (showAuthorizedUseDialog) {
                    AlertDialog(
                        onDismissRequest = {},
                        title = { Text("Authorized use only") },
                        text = {
                            Text(
                                "This tool is for authorized security testing only.\n\n" +
                                    "Use only on applications you own or have explicit permission to test. " +
                                    "Unauthorized interception of other applications' data may violate " +
                                    "applicable laws. The developers assume no liability for misuse.",
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    markAuthorizedUseAccepted()
                                    showAuthorizedUseDialog = false
                                },
                            ) {
                                Text("I understand")
                            }
                        },
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        updateSelectedTab(intent)
    }

    private fun updateSelectedTab(intent: Intent?) {
        val route = intent?.getStringExtra(EXTRA_NAVIGATE_TO) ?: return
        selectedTab = AppTab.entries.firstOrNull { it.route == route } ?: selectedTab
    }

    private fun shouldShowAuthorizedUseDialog(): Boolean {
        val acceptedVersionCode = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .getInt(KEY_ACCEPTED_VERSION_CODE, VERSION_NOT_ACCEPTED)
        return acceptedVersionCode != BuildConfig.VERSION_CODE
    }

    private fun markAuthorizedUseAccepted() {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .edit()
            .putInt(KEY_ACCEPTED_VERSION_CODE, BuildConfig.VERSION_CODE)
            .apply()
    }

    companion object {
        const val EXTRA_NAVIGATE_TO = "navigate_to"
        private const val PREFS_NAME = "authorized_use"
        private const val KEY_ACCEPTED_VERSION_CODE = "accepted_version_code"
        private const val VERSION_NOT_ACCEPTED = -1
    }
}
