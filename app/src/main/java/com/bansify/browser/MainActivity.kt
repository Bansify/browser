package com.bansify.browser

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.bansify.browser.browser.WebViewPool
import com.bansify.browser.ui.BrowserApp
import com.bansify.browser.viewmodel.BrowserViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: BrowserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Hand the saved WebView state to the pool before the first tab is composed, so a
        // rotation or a process restore keeps scroll position, form input and history.
        WebViewPool.pendingState = savedInstanceState?.getBundle("webview_pool_state")
        enableEdgeToEdge()
        handleIncomingIntent(intent)

        setContent {
            BrowserApp(viewModel = viewModel)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        val poolState = Bundle()
        WebViewPool.active?.saveStateTo(poolState)
        outState.putBundle("webview_pool_state", poolState)
        super.onSaveInstanceState(outState)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_VIEW) {
            val url = intent.dataString
            if (!url.isNullOrBlank()) {
                if (viewModel.settings.value.openExternalLinksInNewTab) {
                    viewModel.createNewTab(url = url, isIncognito = false)
                } else {
                    viewModel.openUrlInActiveTab(url)
                }
            }
        }
    }
}
