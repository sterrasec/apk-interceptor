package com.sterrasec.apkinterceptor.ui.sender

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.sterrasec.apkinterceptor.log.LogEntry
import com.sterrasec.apkinterceptor.log.LogEntryType
import com.sterrasec.apkinterceptor.log.LogRepository
import com.sterrasec.apkinterceptor.provider.PayloadProvider

private enum class TargetMode { IMPLICIT, EXPLICIT }

@Composable
fun SenderScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var targetMode by rememberSaveable { mutableStateOf(TargetMode.IMPLICIT) }
    var uri by rememberSaveable { mutableStateOf("") }
    var packageName by rememberSaveable { mutableStateOf("") }
    var activityClass by rememberSaveable { mutableStateOf("") }
    var attachContentUri by rememberSaveable { mutableStateOf(false) }
    var grantReadPermission by rememberSaveable { mutableStateOf(true) }
    val contentUri = remember(context.packageName) { PayloadProvider.contentUri(context.packageName) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Card {
            Text(
                text = "WARNING: Send Intents only to apps you own or have explicit authorization to test. " +
                    "Do not use this feature against apps without permission.",
                modifier = Modifier.padding(12.dp),
                color = MaterialTheme.colorScheme.error,
            )
        }

        Text("Target mode", style = MaterialTheme.typography.titleMedium)
        TargetMode.entries.forEach { mode ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = targetMode == mode,
                    onClick = {
                        targetMode = mode
                        // content:// is delivered as Intent data, which would overwrite the
                        // implicit deeplink URI. Keep it for Explicit Activity mode only.
                        if (mode == TargetMode.IMPLICIT) attachContentUri = false
                    },
                )
                Text(if (mode == TargetMode.IMPLICIT) "Implicit Deeplink" else "Explicit Activity")
            }
        }

        if (targetMode == TargetMode.IMPLICIT) {
            OutlinedTextField(
                value = uri,
                onValueChange = { uri = it },
                label = { Text("URI") },
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            OutlinedTextField(
                value = packageName,
                onValueChange = { packageName = it },
                label = { Text("Package name") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = activityClass,
                onValueChange = { activityClass = it },
                label = { Text("Activity class") },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        // content:// is attached as Intent data, so it is only offered in Explicit
        // Activity mode where it does not collide with an implicit deeplink URI.
        if (targetMode == TargetMode.EXPLICIT) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = attachContentUri,
                    onCheckedChange = {
                        attachContentUri = it
                        if (it) grantReadPermission = true
                    },
                )
                Text("Attach content:// URI", modifier = Modifier.padding(start = 8.dp))
            }
            if (attachContentUri) {
                Text(contentUri.toString(), style = MaterialTheme.typography.bodySmall)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = grantReadPermission,
                        onCheckedChange = { grantReadPermission = it },
                    )
                    Text("FLAG_GRANT_READ_URI_PERMISSION")
                }
            }
        }

        Button(
            onClick = {
                val intent = when (targetMode) {
                    TargetMode.IMPLICIT -> Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                    TargetMode.EXPLICIT -> Intent().setClassName(packageName, activityClass)
                }
                if (attachContentUri) {
                    intent.data = contentUri
                    if (grantReadPermission) {
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                }
                try {
                    context.startActivity(intent)
                    LogRepository.add(
                        LogEntry(
                            type = LogEntryType.SENT,
                            title = "Intent sent",
                            detail = intent.toUri(Intent.URI_INTENT_SCHEME),
                        ),
                    )
                } catch (error: ActivityNotFoundException) {
                    logError(error)
                } catch (error: SecurityException) {
                    logError(error)
                }
            },
        ) {
            Text("Send Intent")
        }
    }
}

private fun logError(error: Exception) {
    LogRepository.add(
        LogEntry(
            type = LogEntryType.ERROR,
            title = "Intent send failed",
            detail = error.message ?: error.javaClass.simpleName,
        ),
    )
}
