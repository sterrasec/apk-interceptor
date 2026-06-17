package com.sterrasec.apkinterceptor

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.sterrasec.apkinterceptor.log.LogEntry
import com.sterrasec.apkinterceptor.log.LogEntryType
import com.sterrasec.apkinterceptor.log.LogRepository

class InterceptActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val uri = intent?.data
        if (uri == null) {
            LogRepository.add(
                LogEntry(
                    type = LogEntryType.ERROR,
                    title = "InterceptActivity launched without URI",
                    detail = "intent.data was null",
                ),
            )
        } else {
            val detail = runCatching {
                buildString {
                    appendLine("Full URI: $uri")
                    appendLine("Parameters:")
                    uri.queryParameterNames.forEach { key ->
                        appendLine("  $key = ${uri.getQueryParameter(key)}")
                    }
                }
            }.getOrElse { error ->
                "Full URI: $uri\nCould not parse parameters: ${error.message}"
            }
            LogRepository.add(
                LogEntry(
                    type = LogEntryType.RECEIVED,
                    title = "Intercepted: ${uri.scheme}://${uri.host.orEmpty()}${uri.path.orEmpty()}",
                    detail = detail,
                ),
            )
        }

        startActivity(
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra(MainActivity.EXTRA_NAVIGATE_TO, "interceptor")
            },
        )
        finish()
    }
}
