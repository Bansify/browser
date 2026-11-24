package com.bansify.browser.browser

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Message
import android.os.Environment
import android.view.View
import android.view.ViewGroup
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.GeolocationPermissions
import android.webkit.PermissionRequest
import android.webkit.SafeBrowsingResponse
import android.webkit.WebResourceResponse
import android.webkit.JsPromptResult
import android.webkit.JsResult
import android.webkit.URLUtil
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.RenderProcessGoneDetail
import android.webkit.SslErrorHandler
import android.webkit.WebResourceError
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.bansify.browser.data.BrowserSettings
import com.bansify.browser.data.BrowserTab
import com.bansify.browser.data.JsDialogState
import com.bansify.browser.data.SafeBrowsingLevel
import com.bansify.browser.data.SitePermission
import com.bansify.browser.data.ThirdPartyCookiePolicy
import java.io.File

/**
 * Keeps live Android WebView instances alive per tab ID so switching tabs, opening Settings,
 * or toggling the Tab Switcher preserves exact scroll position, DOM state, and back/forward history.
 */
class WebViewPool(
    private val context: Context,
    private val callbacks: Callbacks
) {

    private val adBlocker = AdBlocker(context)

    /** Live pool for the current process, so the activity can save WebView state on rotation/kill. */
    companion object {
        @Volatile
        var active: WebViewPool? = null
            private set

        /** State handed back by the activity after a configuration change or process restore. */
        @Volatile
        var pendingState: Bundle? = null
    }

    init {
        active = this
    }

    fun adBlockerInstance(): AdBlocker = adBlocker
    interface Callbacks {
        fun onPageStarted(tabId: String, url: String)
        fun onPageFinished(tabId: String, url: String, title: String, canGoBack: Boolean, canGoForward: Boolean)
        fun onProgressChanged(tabId: String, progress: Int)
        fun onTitleReceived(tabId: String, title: String)
        fun onFaviconReceived(tabId: String, icon: Bitmap?)
        fun onThumbnailCaptured(tabId: String, bitmap: Bitmap)
        fun onDownloadStarted(fileName: String, url: String, mimeType: String, sizeBytes: Long, localPath: String)
        fun onJsDialog(dialog: JsDialogState)
        fun onShowCustomView(view: View, callback: WebChromeClient.CustomViewCallback)
        fun onHideCustomView()
        fun onFileChooserRequested(filePathCallback: ValueCallback<Array<Uri>>, acceptTypes: Array<String>)
        fun onFindResults(activeMatchOrdinal: Int, numberOfMatches: Int)
        fun onOpenNewTabRequest(url: String, isIncognito: Boolean)

        /** A site called window.open(): a WebView is already created and loading for this tab id. */
        fun onPopupTabCreated(tabId: String, isIncognito: Boolean)

        /** Main-frame load failure, including blocked certificate errors. */
        fun onPageError(tabId: String, url: String, message: String, isSslError: Boolean)

        /** The renderer process for this tab died (crash or OOM); the WebView is already gone. */
        fun onRendererGone(tabId: String, didCrash: Boolean)

        /** A site asked for camera/mic/location. Answer through [onResult]. */
        fun onSitePermissionRequest(
            tabId: String,
            origin: String,
            permission: SitePermission,
            onResult: (Boolean) -> Unit
        )

        /** Safe Browsing flagged this page; the caller must answer the response. */
        fun onSafeBrowsingHit(
            tabId: String,
            url: String,
            threatType: Int,
            response: SafeBrowsingResponse
        )

        fun onExternalUrlError(message: String)
    }

    private val webViews = mutableMapOf<String, WebView>()
    private val defaultUserAgents = mutableMapOf<String, String>()

    /** Hosts an incognito tab actually visited, so their cookies can be removed when it closes. */
    private val incognitoHosts = mutableSetOf<String>()
    private var popupCounter = 0
    private var currentSettings: BrowserSettings = BrowserSettings()

    companion object {
        private const val DESKTOP_USER_AGENT =
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36"
    }

    fun getOrCreateWebView(tab: BrowserTab, settings: BrowserSettings): WebView {
        currentSettings = settings
        val existing = webViews[tab.id]
        if (existing != null) {
            applySettingsToWebView(existing, tab, settings)
            return existing
        }

        val webView = createWebView(tab, settings)
        webViews[tab.id] = webView
        if (!tab.isNewTab) {
            loadUrl(tab.id, tab.url, settings)
        }
        return webView
    }

    fun getWebViewIfExists(tabId: String): WebView? = webViews[tabId]

    fun updateSettingsForAll(tabs: List<BrowserTab>, settings: BrowserSettings) {
        currentSettings = settings
        val tabMap = tabs.associateBy { it.id }
        webViews.forEach { (id, wv) ->
            val tab = tabMap[id]
            if (tab != null) {
                applySettingsToWebView(wv, tab, settings)
            }
        }
    }

    fun loadUrl(tabId: String, url: String, settings: BrowserSettings = currentSettings) {
        val webView = webViews[tabId] ?: return
        if (url == BrowserTab.NEW_TAB_URL || url.isBlank()) {
            webView.stopLoading()
            return
        }
        val headers = mutableMapOf<String, String>()
        if (settings.doNotTrackEnabled) {
            headers["DNT"] = "1"
        }
        val langTag = when {
            settings.preferredLanguage.startsWith("Hindi") -> "hi-IN,hi;q=0.9,en;q=0.8"
            settings.preferredLanguage.startsWith("Spanish") -> "es-ES,es;q=0.9,en;q=0.8"
            settings.preferredLanguage.startsWith("French") -> "fr-FR,fr;q=0.9,en;q=0.8"
            settings.preferredLanguage.startsWith("German") -> "de-DE,de;q=0.9,en;q=0.8"
            settings.preferredLanguage.startsWith("Japanese") -> "ja-JP,ja;q=0.9,en;q=0.8"
            else -> "en-US,en;q=0.9"
        }
        headers["Accept-Language"] = langTag
        webView.loadUrl(url, headers)
    }

    fun extractReadableArticle(tabId: String, onResult: (title: String, paragraphs: List<String>) -> Unit) {
        val wv = webViews[tabId]
        if (wv == null) {
            onResult("", emptyList())
            return
        }
        val js = """
            (function() {
                var title = document.title || '';
                var root = document.querySelector('article') || document.querySelector('main') || document.body;
                var nodes = root ? root.querySelectorAll('h1, h2, h3, p, li') : [];
                var out = [];
                for (var i = 0; i < nodes.length && out.length < 80; i++) {
                    var t = (nodes[i].innerText || '').trim().replace(/\s+/g, ' ');
                    if (t.length >= 25) out.push(t);
                }
                if (out.length === 0 && root && root.innerText) {
                    var lines = root.innerText.split('\n');
                    for (var j = 0; j < lines.length && out.length < 60; j++) {
                        var l = lines[j].trim();
                        if (l.length >= 20) out.push(l);
                    }
                }
                return JSON.stringify({ title: title, paragraphs: out });
            })();
        """.trimIndent()
        wv.evaluateJavascript(js) { raw ->
            try {
                if (raw == null || raw == "null") {
                    onResult(wv.title.orEmpty(), emptyList())
                    return@evaluateJavascript
                }
                // Unquote JSON string returned by evaluateJavascript
                val jsonText = org.json.JSONTokener(raw).nextValue() as? String ?: raw
                val obj = org.json.JSONObject(jsonText)
                val title = obj.optString("title", wv.title.orEmpty())
                val arr = obj.optJSONArray("paragraphs")
                val list = mutableListOf<String>()
                if (arr != null) {
                    for (i in 0 until arr.length()) {
                        val p = arr.optString(i).trim()
                        if (p.isNotEmpty()) list.add(p)
                    }
                }
                onResult(title, list)
            } catch (_: Exception) {
                onResult(wv.title.orEmpty(), emptyList())
            }
        }
    }

    fun reload(tabId: String) {
        webViews[tabId]?.reload()
    }

    fun stopLoading(tabId: String) {
        webViews[tabId]?.stopLoading()
    }

    fun goBack(tabId: String): Boolean {
        val wv = webViews[tabId] ?: return false
        return if (wv.canGoBack()) {
            wv.goBack()
            true
        } else {
            false
        }
    }

    fun goForward(tabId: String): Boolean {
        val wv = webViews[tabId] ?: return false
        return if (wv.canGoForward()) {
            wv.goForward()
            true
        } else {
            false
        }
    }

    fun setDesktopSite(tab: BrowserTab, enabled: Boolean) {
        val wv = webViews[tab.id] ?: return
        val defaultUa = defaultUserAgents[tab.id] ?: wv.settings.userAgentString
        wv.settings.userAgentString = if (enabled) DESKTOP_USER_AGENT else defaultUa
        wv.settings.useWideViewPort = true
        wv.settings.loadWithOverviewMode = true
        if (!tab.isNewTab) {
            wv.reload()
        }
    }

    fun findInPage(tabId: String, query: String) {
        val wv = webViews[tabId] ?: return
        if (query.isBlank()) {
            wv.clearMatches()
            callbacks.onFindResults(0, 0)
        } else {
            wv.findAllAsync(query)
        }
    }

    fun findNext(tabId: String, forward: Boolean) {
        webViews[tabId]?.findNext(forward)
    }

    fun clearFindMatches(tabId: String) {
        webViews[tabId]?.clearMatches()
    }

    fun saveCurrentPageArchive(tabId: String, title: String, url: String) {
        val wv = webViews[tabId] ?: return
        val safeName = (title.ifBlank { UrlUtils.extractDisplayHost(url).ifBlank { "page" } })
            .replace(Regex("[^a-zA-Z0-9._-]"), "_")
            .take(40) + ".mht"
        val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir, "saved_pages")
        if (!dir.exists()) dir.mkdirs()
        val destFile = File(dir, safeName)
        wv.saveWebArchive(destFile.absolutePath, false) { savedPath ->
            val actualPath = savedPath ?: destFile.absolutePath
            val size = runCatching { File(actualPath).length() }.getOrDefault(0L)
            callbacks.onDownloadStarted(
                fileName = safeName,
                url = url,
                mimeType = "multipart/related",
                sizeBytes = size,
                localPath = actualPath
            )
        }
    }

    fun captureThumbnail(tabId: String) {
        val wv = webViews[tabId] ?: return
        if (wv.width <= 0 || wv.height <= 0) return
        try {
            val scale = 0.25f
            val targetW = (wv.width * scale).toInt().coerceIn(120, 360)
            val targetH = (wv.height * scale).toInt().coerceIn(160, 480)
            val bitmap = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.RGB_565)
            val canvas = Canvas(bitmap)
            canvas.scale(targetW.toFloat() / wv.width.toFloat(), targetH.toFloat() / wv.height.toFloat())
            wv.draw(canvas)
            callbacks.onThumbnailCaptured(tabId, bitmap)
        } catch (_: Exception) {
            // Skip thumbnail if WebView is not currently laid out
        }
    }

    fun clearAllCaches() {
        webViews.values.forEach { it.clearCache(true) }
    }

    fun destroyTab(tabId: String) {
        val wv = webViews.remove(tabId) ?: return
        defaultUserAgents.remove(tabId)
        (wv.parent as? ViewGroup)?.removeView(wv)
        wv.stopLoading()
        wv.loadUrl("about:blank")
        wv.clearHistory()
        wv.removeAllViews()
        wv.destroy()
    }

    fun destroyAll() {
        webViews.keys.toList().forEach { destroyTab(it) }
        if (active === this) active = null
    }

    /** Writes every live WebView's navigation state into [outState] for a later restore. */
    fun saveStateTo(outState: Bundle) {
        webViews.forEach { (tabId, webView) ->
            runCatching {
                val state = Bundle()
                webView.saveState(state)
                outState.putBundle("wv_state_$tabId", state)
            }
        }
    }

    /**
     * Builds the WebView a window.open() call will load into. It is registered in the pool
     * under [popupId] so the tab the UI adds afterwards adopts this exact instance.
     */
    private fun createPopupWebView(
        popupId: String,
        parent: BrowserTab,
        settings: BrowserSettings
    ): WebView {
        val popupTab = BrowserTab(
            id = popupId,
            url = BrowserTab.NEW_TAB_URL,
            title = "New tab",
            isIncognito = parent.isIncognito,
            isDesktopSite = parent.isDesktopSite,
            isSecure = true
        )
        val webView = createWebView(popupTab, settings)
        webViews[popupId] = webView
        return webView
    }

    /**
     * Called when the last incognito tab closes. WebView has a single process-wide cookie jar,
     * so isolation is done by refusing cookies while browsing privately (see
     * [applySettingsToWebView]) and then removing whatever the visited hosts still hold.
     */
    fun clearIncognitoData() {
        val cookieManager = CookieManager.getInstance()
        val hosts = incognitoHosts.toList()
        incognitoHosts.clear()
        hosts.forEach { host ->
            listOf("https://$host/", "http://$host/").forEach { url ->
                val cookies = cookieManager.getCookie(url).orEmpty()
                if (cookies.isBlank()) return@forEach
                cookies.split(';').forEach { pair ->
                    val name = pair.substringBefore('=', "").trim()
                    if (name.isNotEmpty()) {
                        cookieManager.setCookie(url, "$name=; Max-Age=0; Path=/")
                        cookieManager.setCookie(url, "$name=; Max-Age=0; Path=/; Secure")
                    }
                }
            }
        }
        cookieManager.flush()
        webViews.values.firstOrNull()?.let { wv ->
            wv.clearCache(true)
            wv.clearFormData()
            wv.clearHistory()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun createWebView(tab: BrowserTab, settings: BrowserSettings): WebView {
        val webView = WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            isFocusable = true
            isFocusableInTouchMode = true
        }

        defaultUserAgents[tab.id] = webView.settings.userAgentString
        applySettingsToWebView(webView, tab, settings)

        webView.setFindListener { activeMatchOrdinal, numberOfMatches, _ ->
            callbacks.onFindResults(
                activeMatchOrdinal = if (numberOfMatches > 0) activeMatchOrdinal + 1 else 0,
                numberOfMatches = numberOfMatches
            )
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {
                if (request == null) return null
                if (!currentSettings.adBlockEnabled) return null
                val url = request.url?.toString() ?: return null
                return if (adBlocker.shouldBlock(url, request.isForMainFrame)) {
                    adBlocker.emptyResponse()
                } else {
                    null
                }
            }

            override fun onSafeBrowsingHit(
                view: WebView?,
                request: WebResourceRequest?,
                threatType: Int,
                callback: SafeBrowsingResponse?
            ) {
                if (callback == null) return
                callbacks.onSafeBrowsingHit(
                    tabId = tab.id,
                    url = request?.url?.toString().orEmpty(),
                    threatType = threatType,
                    response = callback
                )
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val uri = request?.url ?: return false
                val scheme = uri.scheme?.lowercase() ?: return false
                if (scheme == "http" || scheme == "https") {
                    if (scheme == "http" && currentSettings.httpsFirstEnabled && request.isForMainFrame) {
                        val upgraded = uri.buildUpon().scheme("https").build().toString()
                        loadUrl(tab.id, upgraded, currentSettings)
                        return true
                    }
                    return false
                }
                if (scheme == "about" || scheme == "data" || scheme == "blob") {
                    return false
                }
                return handleExternalScheme(uri)
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                val actualUrl = url ?: return
                if (actualUrl == "about:blank") return
                if (tab.isIncognito) {
                    runCatching { Uri.parse(actualUrl).host }.getOrNull()
                        ?.takeIf { it.isNotBlank() }
                        ?.let { incognitoHosts.add(it) }
                }
                callbacks.onPageStarted(tab.id, actualUrl)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                val actualUrl = url ?: return
                if (actualUrl == "about:blank") return
                val wv = view ?: return
                if (currentSettings.forceEnableZoom) {
                    injectForceZoom(wv)
                }
                callbacks.onPageFinished(
                    tabId = tab.id,
                    url = actualUrl,
                    title = wv.title?.takeIf { it.isNotBlank() } ?: UrlUtils.extractDisplayHost(actualUrl),
                    canGoBack = wv.canGoBack(),
                    canGoForward = wv.canGoForward()
                )
                wv.postDelayed({ captureThumbnail(tab.id) }, 250L)
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                if (request?.isForMainFrame != true) return
                val description = error?.description?.toString().orEmpty()
                    .ifBlank { "The page could not be loaded" }
                callbacks.onPageError(
                    tabId = tab.id,
                    url = request.url?.toString().orEmpty(),
                    message = description,
                    isSslError = false
                )
            }

            override fun onReceivedSslError(
                view: WebView?,
                handler: SslErrorHandler?,
                error: SslError?
            ) {
                // A browser must never silently continue past a bad certificate.
                handler?.cancel()
                val host = runCatching { Uri.parse(error?.url.orEmpty()).host }.getOrNull()
                callbacks.onPageError(
                    tabId = tab.id,
                    url = error?.url.orEmpty(),
                    message = "The certificate for ${host ?: "this site"} is not trusted, so the connection was blocked.",
                    isSslError = true
                )
            }

            override fun onRenderProcessGone(
                view: WebView?,
                detail: RenderProcessGoneDetail?
            ): Boolean {
                // Returning true tells the framework we handled it: the app keeps running
                // instead of being killed along with the dead renderer.
                val didCrash = detail?.didCrash() ?: true
                webViews.remove(tab.id)
                defaultUserAgents.remove(tab.id)
                runCatching {
                    (view?.parent as? ViewGroup)?.removeView(view)
                    view?.stopLoading()
                    view?.destroy()
                }
                callbacks.onRendererGone(tab.id, didCrash)
                return true
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                callbacks.onProgressChanged(tab.id, newProgress)
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
                if (!title.isNullOrBlank()) {
                    callbacks.onTitleReceived(tab.id, title)
                }
            }

            override fun onReceivedIcon(view: WebView?, icon: Bitmap?) {
                callbacks.onFaviconReceived(tab.id, icon)
            }

            override fun onJsAlert(
                view: WebView?,
                url: String?,
                message: String?,
                result: JsResult?
            ): Boolean {
                if (result == null) return false
                callbacks.onJsDialog(JsDialogState.Alert(url.orEmpty(), message.orEmpty(), result))
                return true
            }

            override fun onJsConfirm(
                view: WebView?,
                url: String?,
                message: String?,
                result: JsResult?
            ): Boolean {
                if (result == null) return false
                callbacks.onJsDialog(JsDialogState.Confirm(url.orEmpty(), message.orEmpty(), result))
                return true
            }

            override fun onJsPrompt(
                view: WebView?,
                url: String?,
                message: String?,
                defaultValue: String?,
                result: JsPromptResult?
            ): Boolean {
                if (result == null) return false
                callbacks.onJsDialog(
                    JsDialogState.Prompt(
                        url.orEmpty(),
                        message.orEmpty(),
                        defaultValue.orEmpty(),
                        result
                    )
                )
                return true
            }

            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                if (view != null && callback != null) {
                    callbacks.onShowCustomView(view, callback)
                }
            }

            override fun onHideCustomView() {
                callbacks.onHideCustomView()
            }

            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                if (filePathCallback == null) return false
                val acceptTypes = fileChooserParams?.acceptTypes ?: arrayOf("*/*")
                callbacks.onFileChooserRequested(filePathCallback, acceptTypes)
                return true
            }

            override fun onPermissionRequest(request: PermissionRequest?) {
                val req = request ?: return
                val origin = runCatching { Uri.parse(req.origin.toString()).host }
                    .getOrNull()
                    .orEmpty()
                val wanted = buildList {
                    if (req.resources.contains(PermissionRequest.RESOURCE_VIDEO_CAPTURE)) {
                        add(SitePermission.CAMERA)
                    }
                    if (req.resources.contains(PermissionRequest.RESOURCE_AUDIO_CAPTURE)) {
                        add(SitePermission.MICROPHONE)
                    }
                }
                if (wanted.isEmpty()) {
                    req.deny()
                    return
                }
                val granted = mutableSetOf<String>()
                var outstanding = wanted.size
                wanted.forEach { permission ->
                    callbacks.onSitePermissionRequest(tab.id, origin, permission) { allowed ->
                        synchronized(granted) {
                            if (allowed) {
                                when (permission) {
                                    SitePermission.CAMERA -> granted.add(PermissionRequest.RESOURCE_VIDEO_CAPTURE)
                                    SitePermission.MICROPHONE -> granted.add(PermissionRequest.RESOURCE_AUDIO_CAPTURE)
                                    else -> Unit
                                }
                            }
                            outstanding -= 1
                            if (outstanding == 0) {
                                if (granted.isEmpty()) {
                                    req.deny()
                                } else {
                                    req.grant(granted.toTypedArray())
                                }
                            }
                        }
                    }
                }
            }

            override fun onGeolocationPermissionsShowPrompt(
                origin: String?,
                callback: GeolocationPermissions.Callback?
            ) {
                if (origin == null || callback == null) return
                val host = runCatching { Uri.parse(origin).host }.getOrNull().orEmpty()
                callbacks.onSitePermissionRequest(tab.id, host, SitePermission.LOCATION) { allowed ->
                    callback.invoke(origin, allowed, true)
                }
            }

            override fun onCreateWindow(
                view: WebView?,
                isDialog: Boolean,
                isUserGesture: Boolean,
                resultMsg: Message?
            ): Boolean {
                if (resultMsg == null) return false
                val transport = resultMsg.obj as? WebView.WebViewTransport ?: return false
                val popupId = "popup_${System.currentTimeMillis()}_${popupCounter++}"
                val popup = createPopupWebView(popupId, tab, currentSettings)
                transport.webView = popup
                resultMsg.sendToTarget()
                callbacks.onPopupTabCreated(popupId, tab.isIncognito)
                return true
            }
        }

        webView.setDownloadListener { url, userAgent, contentDisposition, mimeType, contentLength ->
            val fileName = URLUtil.guessFileName(url, contentDisposition, mimeType)
            val resolvedMime = mimeType.ifBlank { "application/octet-stream" }
            var localPath = ""
            if (url.startsWith("http://", ignoreCase = true) ||
                url.startsWith("https://", ignoreCase = true)
            ) {
                runCatching {
                    val request = DownloadManager.Request(Uri.parse(url)).apply {
                        setMimeType(resolvedMime)
                        val cookies = CookieManager.getInstance().getCookie(url)
                        if (!cookies.isNullOrBlank()) addRequestHeader("cookie", cookies)
                        addRequestHeader("User-Agent", userAgent)
                        setTitle(fileName)
                        setDescription("Downloading with Bansify Browser")
                        setNotificationVisibility(
                            DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                        )
                        setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                    }
                    val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                    dm.enqueue(request)
                    localPath = File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                        fileName
                    ).absolutePath
                }
            }
            callbacks.onDownloadStarted(
                fileName = fileName,
                url = url,
                mimeType = resolvedMime,
                sizeBytes = contentLength.coerceAtLeast(0L),
                localPath = localPath
            )
        }

        pendingState?.getBundle("wv_state_$tab.id")?.let { saved ->
            runCatching { webView.restoreState(saved) }
        }

        return webView
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun applySettingsToWebView(
        webView: WebView,
        tab: BrowserTab,
        settings: BrowserSettings
    ) {
        val ws = webView.settings
        ws.javaScriptEnabled = settings.javascriptEnabled
        ws.domStorageEnabled = !tab.isIncognito
        ws.databaseEnabled = !tab.isIncognito
        ws.loadsImagesAutomatically = settings.loadImagesEnabled
        ws.javaScriptCanOpenWindowsAutomatically = settings.popupsAllowed
        ws.mediaPlaybackRequiresUserGesture = !settings.mediaAutoplayEnabled
        ws.textZoom = settings.textZoomPercent.coerceIn(50, 200)
        ws.useWideViewPort = true
        ws.loadWithOverviewMode = true
        ws.builtInZoomControls = true
        ws.displayZoomControls = false
        ws.setSupportZoom(true)
        ws.allowFileAccess = false
        ws.allowContentAccess = true
        ws.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        ws.cacheMode = if (tab.isIncognito) WebSettings.LOAD_NO_CACHE else WebSettings.LOAD_DEFAULT
        // window.open() / target=_blank land in onCreateWindow below instead of a dead end.
        ws.setSupportMultipleWindows(true)
        // Websites ask for geolocation through onGeolocationPermissionsShowPrompt; the user
        // decides, never the page.
        ws.setGeolocationEnabled(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ws.safeBrowsingEnabled = settings.safeBrowsingLevel != SafeBrowsingLevel.NONE
        }

        val defaultUa = defaultUserAgents[tab.id] ?: ws.userAgentString
        val wantDesktop = tab.isDesktopSite || settings.defaultDesktopSite
        val desiredUa = if (wantDesktop) DESKTOP_USER_AGENT else defaultUa
        if (ws.userAgentString != desiredUa) {
            ws.userAgentString = desiredUa
        }

        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(settings.cookiesEnabled && !tab.isIncognito)
        val allowThirdParty = when {
            !settings.cookiesEnabled -> false
            settings.thirdPartyCookiePolicy == ThirdPartyCookiePolicy.BLOCK_ALL -> false
            settings.thirdPartyCookiePolicy == ThirdPartyCookiePolicy.BLOCK_INCOGNITO && tab.isIncognito -> false
            else -> true
        }
        cookieManager.setAcceptThirdPartyCookies(webView, allowThirdParty)
    }

    private fun injectForceZoom(webView: WebView) {
        val script = """
            (function() {
                var meta = document.querySelector('meta[name=viewport]');
                if (meta) {
                    meta.setAttribute('content', 'width=device-width, initial-scale=1.0, maximum-scale=5.0, user-scalable=yes');
                }
            })();
        """.trimIndent()
        webView.evaluateJavascript(script, null)
    }

    private fun handleExternalScheme(uri: Uri): Boolean {
        return try {
            val intent = if (uri.scheme.equals("intent", ignoreCase = true)) {
                Intent.parseUri(uri.toString(), Intent.URI_INTENT_SCHEME)
            } else {
                Intent(Intent.ACTION_VIEW, uri)
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            true
        } catch (_: ActivityNotFoundException) {
            callbacks.onExternalUrlError("No app found to open ${uri.scheme} link")
            true
        } catch (_: Exception) {
            true
        }
    }
}
