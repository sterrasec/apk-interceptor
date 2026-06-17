package com.sterrasec.apkinterceptor.webview

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.sterrasec.apkinterceptor.BuildConfig
import com.sterrasec.apkinterceptor.bridge.JsBridge
import com.sterrasec.apkinterceptor.log.LogEntry
import com.sterrasec.apkinterceptor.log.LogEntryType
import com.sterrasec.apkinterceptor.log.LogRepository

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun SelfTestWebView(
    contentUri: String,
    bridgeName: String,
    javascript: String,
    loadToken: Int,
    modifier: Modifier = Modifier,
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            ManagedSelfTestWebView(context).apply {
                settings.javaScriptEnabled = true
                settings.allowContentAccess = true
                WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG)
                webChromeClient = object : WebChromeClient() {
                    override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                        LogRepository.add(
                            LogEntry(
                                type = LogEntryType.BRIDGE_RESULT,
                                title = "console.log",
                                detail = consoleMessage.message(),
                            ),
                        )
                        return true
                    }
                }
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String) {
                        super.onPageFinished(view, url)
                        if (javascriptToEvaluate.isNotBlank()) {
                            view.evaluateJavascript(javascriptToEvaluate) { result ->
                                LogRepository.add(
                                    LogEntry(
                                        type = LogEntryType.BRIDGE_RESULT,
                                        title = "evaluateJavascript result",
                                        detail = result,
                                    ),
                                )
                            }
                        }
                    }
                }
            }
        },
        update = { webView ->
            webView.javascriptToEvaluate = javascript
            if (loadToken > 0 && webView.loadedToken != loadToken) {
                webView.loadedToken = loadToken
                webView.activeBridgeName?.let(webView::removeJavascriptInterface)
                if (bridgeName.isNotBlank()) {
                    webView.addJavascriptInterface(JsBridge(LogRepository), bridgeName)
                    webView.activeBridgeName = bridgeName
                }
                webView.loadUrl(contentUri)
            }
        },
        onRelease = { webView ->
            webView.activeBridgeName?.let(webView::removeJavascriptInterface)
            webView.destroy()
        },
    )
}

private class ManagedSelfTestWebView(context: Context) : WebView(context) {
    var activeBridgeName: String? = null
    var javascriptToEvaluate: String = ""
    var loadedToken: Int = 0
}
