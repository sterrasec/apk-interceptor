package com.sterrasec.apkinterceptor.ui.interceptor

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.sterrasec.apkinterceptor.BuildConfig
import com.sterrasec.apkinterceptor.log.LogEntry
import com.sterrasec.apkinterceptor.log.LogEntryType
import com.sterrasec.apkinterceptor.log.LogRepository
import com.sterrasec.apkinterceptor.ui.components.LogView

@Composable
fun InterceptorScreen(
    entries: List<LogEntry>,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var testParams by rememberSaveable { mutableStateOf("sample=value") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Interceptor", style = MaterialTheme.typography.titleLarge)
        Text("This build intercepts scheme \"${BuildConfig.INTERCEPT_SCHEME}\".")
        if (BuildConfig.INTERCEPT_SCHEME == "intercept-poc-example") {
            Card {
                Text(
                    text = "This build still uses the harmless dummy scheme. Build with --scheme for an authorized assessment.",
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
        OutlinedTextField(
            value = testParams,
            onValueChange = { testParams = it },
            label = { Text("Test query parameters") },
            modifier = Modifier.fillMaxWidth(),
        )
        Button(
            onClick = {
                val uri = Uri.parse("${BuildConfig.INTERCEPT_SCHEME}://test?$testParams")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                try {
                    context.startActivity(intent)
                    LogRepository.add(
                        LogEntry(
                            type = LogEntryType.SENT,
                            title = "Test deeplink sent",
                            detail = uri.toString(),
                        ),
                    )
                } catch (error: ActivityNotFoundException) {
                    LogRepository.add(
                        LogEntry(
                            type = LogEntryType.ERROR,
                            title = "Test deeplink send failed",
                            detail = error.message ?: error.javaClass.simpleName,
                        ),
                    )
                }
            },
        ) {
            Text("Send Test Deeplink")
        }
        LogView(
            entries = entries,
            filter = { it.type == LogEntryType.RECEIVED },
            onClear = LogRepository::clear,
            modifier = Modifier.weight(1f),
        )
    }
}
