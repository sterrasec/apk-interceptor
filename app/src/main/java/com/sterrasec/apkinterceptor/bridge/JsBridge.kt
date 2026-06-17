package com.sterrasec.apkinterceptor.bridge

import android.webkit.JavascriptInterface
import com.sterrasec.apkinterceptor.log.LogEntry
import com.sterrasec.apkinterceptor.log.LogEntryType
import com.sterrasec.apkinterceptor.log.LogRepository

class JsBridge(private val logRepository: LogRepository) {
    @JavascriptInterface
    fun logResult(message: String) {
        logRepository.add(
            LogEntry(
                type = LogEntryType.BRIDGE_RESULT,
                title = "Bridge callback: logResult",
                detail = message,
            ),
        )
    }

    @JavascriptInterface
    fun getInfo(): String = "apk-interceptor self-test bridge"
}
