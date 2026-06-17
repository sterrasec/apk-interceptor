package com.sterrasec.apkinterceptor.ui.payload

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.sterrasec.apkinterceptor.log.LogEntry
import com.sterrasec.apkinterceptor.log.LogEntryType
import com.sterrasec.apkinterceptor.log.LogRepository
import com.sterrasec.apkinterceptor.provider.PayloadProvider
import com.sterrasec.apkinterceptor.ui.components.LogView
import com.sterrasec.apkinterceptor.webview.SelfTestWebView
import java.io.File

@Composable
fun PayloadScreen(
    entries: List<LogEntry>,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val contentUri = remember(context.packageName) { PayloadProvider.contentUri(context.packageName) }
    var html by rememberSaveable { mutableStateOf("<html><body><h1>Local self-test payload</h1></body></html>") }
    var javascript by rememberSaveable { mutableStateOf("") }
    var bridgeName by rememberSaveable { mutableStateOf("") }
    var loadToken by rememberSaveable { mutableIntStateOf(0) }
    var hasSavedPayload by remember {
        mutableStateOf(
            File(context.filesDir, "${PayloadProvider.PAYLOADS_DIR}/${PayloadProvider.PAYLOAD_FILE}").isFile,
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Payload", style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(
            value = html,
            onValueChange = { html = it },
            label = { Text("HTML") },
            textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            modifier = Modifier.fillMaxWidth(),
            minLines = 5,
        )
        OutlinedTextField(
            value = javascript,
            onValueChange = { javascript = it },
            label = { Text("JavaScript evaluated after page load") },
            textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            modifier = Modifier.fillMaxWidth(),
            minLines = 4,
        )
        OutlinedTextField(
            value = bridgeName,
            onValueChange = { bridgeName = it },
            label = { Text("Bridge object name") },
            modifier = Modifier.fillMaxWidth(),
        )
        Text("Generated content URI", style = MaterialTheme.typography.titleSmall)
        SelectionContainer {
            Text(contentUri.toString())
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    try {
                        val dir = File(context.filesDir, PayloadProvider.PAYLOADS_DIR)
                        check(dir.exists() || dir.mkdirs()) { "Could not create payload directory" }
                        File(dir, PayloadProvider.PAYLOAD_FILE).writeText(html)
                        hasSavedPayload = true
                        LogRepository.add(
                            LogEntry(
                                type = LogEntryType.SENT,
                                title = "Payload saved",
                                detail = contentUri.toString(),
                            ),
                        )
                    } catch (error: Exception) {
                        LogRepository.add(
                            LogEntry(
                                type = LogEntryType.ERROR,
                                title = "Payload save failed",
                                detail = error.message ?: error.javaClass.simpleName,
                            ),
                        )
                    }
                },
            ) {
                Text("Save Payload")
            }
            Button(
                enabled = hasSavedPayload && bridgeName.isNotBlank(),
                onClick = { loadToken += 1 },
            ) {
                Text("Run Self-Test")
            }
        }
        if (bridgeName.isBlank()) {
            Text("Enter a bridge object name to enable the local self-test.")
        }
        if (loadToken > 0) {
            SelfTestWebView(
                contentUri = contentUri.toString(),
                bridgeName = bridgeName,
                javascript = javascript,
                loadToken = loadToken,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
            )
        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
            ) {
                Text(
                    text = "Self-test WebView appears here after you save a payload and tap Run Self-Test.",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        LogView(
            entries = entries,
            filter = { it.type == LogEntryType.BRIDGE_RESULT || it.type == LogEntryType.ERROR },
            onClear = LogRepository::clear,
            modifier = Modifier.height(320.dp),
        )
    }
}
