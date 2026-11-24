package com.bansify.browser.ui

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.speech.RecognizerIntent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bansify.browser.MainActivity
import com.bansify.browser.R
import com.bansify.browser.browser.UrlUtils
import com.bansify.browser.browser.WebViewPool
import com.bansify.browser.viewmodel.PageError
import com.bansify.browser.data.AddressBarPosition
import com.bansify.browser.data.BookmarkEntity
import com.bansify.browser.data.BrowserOverlay
import com.bansify.browser.data.BrowserTab
import com.bansify.browser.data.JsDialogState
import com.bansify.browser.data.ReadingModeContent
import com.bansify.browser.data.SettingsSubScreen
import com.bansify.browser.ui.components.BookmarkEditDialog
import com.bansify.browser.ui.components.ChromeOverflowMenu
import com.bansify.browser.ui.components.ChromeToolbar
import com.bansify.browser.ui.components.PageInfoBottomSheet
import com.bansify.browser.ui.components.ReadingModeOverlay
import com.bansify.browser.ui.components.TranslatePageBottomSheet
import com.bansify.browser.ui.components.WebViewJsDialog
import com.bansify.browser.ui.screens.BookmarksScreen
import com.bansify.browser.ui.screens.DeleteBrowsingDataScreen
import com.bansify.browser.ui.screens.DownloadsScreen
import com.bansify.browser.ui.screens.HistoryScreen
import com.bansify.browser.ui.screens.NewTabScreen
import com.bansify.browser.ui.screens.OmniboxOverlay
import com.bansify.browser.ui.screens.RecentTabsScreen
import com.bansify.browser.ui.screens.SettingsScreen
import com.bansify.browser.ui.screens.TabSwitcherScreen
import com.bansify.browser.ui.theme.BansifyBrowserTheme
import com.bansify.browser.ui.theme.LocalIsDarkBrowserTheme
import com.bansify.browser.viewmodel.BrowserViewModel
import java.util.Locale

@Composable
fun BrowserApp(viewModel: BrowserViewModel) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val accountState by viewModel.accountState.collectAsStateWithLifecycle()
    val tabs by viewModel.tabs.collectAsStateWithLifecycle()
    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
    val pageError by viewModel.pageError.collectAsStateWithLifecycle()
    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    val downloads by viewModel.downloads.collectAsStateWithLifecycle()
    val shortcuts by viewModel.shortcuts.collectAsStateWithLifecycle()
    val recentlyClosed by viewModel.recentlyClosedTabs.collectAsStateWithLifecycle()
    val recentSearches by viewModel.recentSearches.collectAsStateWithLifecycle()
    val overlay by viewModel.overlay.collectAsStateWithLifecycle()
    val findInPage by viewModel.findInPage.collectAsStateWithLifecycle()
    val jsDialog by viewModel.jsDialog.collectAsStateWithLifecycle()
    val showPageInfo by viewModel.showPageInfoSheet.collectAsStateWithLifecycle()
    val showTranslate by viewModel.showTranslateSheet.collectAsStateWithLifecycle()
    val readingMode by viewModel.readingModeContent.collectAsStateWithLifecycle()
    val editingBookmark by viewModel.editingBookmark.collectAsStateWithLifecycle()

    var overflowMenuExpanded by remember { mutableStateOf(false) }
    var customFullscreenView by remember { mutableStateOf<View?>(null) }
    var customFullscreenCallback by remember { mutableStateOf<WebChromeClient.CustomViewCallback?>(null) }
    var pendingFileChooserCallback by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }

    val fileChooserLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        pendingFileChooserCallback?.onReceiveValue(uris.toTypedArray())
        pendingFileChooserCallback = null
    }

    val voiceSearchLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
            if (!spoken.isNullOrBlank()) {
                viewModel.openUrlInActiveTab(spoken)
            }
        }
    }

    fun launchVoiceSearch() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to search with ${settings.searchEngine.displayName}")
        }
        try {
            voiceSearchLauncher.launch(intent)
        } catch (_: ActivityNotFoundException) {
            viewModel.showSnackbar("Voice search is not available on this device")
        }
    }

    val webViewPool = remember {
        WebViewPool(
            context = context,
            callbacks = object : WebViewPool.Callbacks {
                override fun onPageStarted(tabId: String, url: String) {
                    viewModel.onWebPageStarted(tabId, url)
                }

                override fun onPageFinished(
                    tabId: String,
                    url: String,
                    title: String,
                    canGoBack: Boolean,
                    canGoForward: Boolean
                ) {
                    viewModel.onWebPageFinished(tabId, url, title, canGoBack, canGoForward)
                }

                override fun onProgressChanged(tabId: String, progress: Int) {
                    viewModel.onWebProgressChanged(tabId, progress)
                }

                override fun onTitleReceived(tabId: String, title: String) {
                    viewModel.onWebTitleReceived(tabId, title)
                }

                override fun onFaviconReceived(tabId: String, icon: Bitmap?) {
                    viewModel.onWebFaviconReceived(tabId, icon)
                }

                override fun onThumbnailCaptured(tabId: String, bitmap: Bitmap) {
                    viewModel.onThumbnailCaptured(tabId, bitmap)
                }

                override fun onDownloadStarted(
                    fileName: String,
                    url: String,
                    mimeType: String,
                    sizeBytes: Long,
                    localPath: String
                ) {
                    viewModel.onDownloadRecorded(fileName, url, mimeType, sizeBytes, localPath)
                }

                override fun onJsDialog(dialog: JsDialogState) {
                    viewModel.showJsDialog(dialog)
                }

                override fun onShowCustomView(view: View, callback: WebChromeClient.CustomViewCallback) {
                    customFullscreenView = view
                    customFullscreenCallback = callback
                }

                override fun onHideCustomView() {
                    customFullscreenView = null
                    customFullscreenCallback?.onCustomViewHidden()
                    customFullscreenCallback = null
                }

                override fun onFileChooserRequested(
                    filePathCallback: ValueCallback<Array<Uri>>,
                    acceptTypes: Array<String>
                ) {
                    pendingFileChooserCallback?.onReceiveValue(null)
                    pendingFileChooserCallback = filePathCallback
                    val mime = acceptTypes.firstOrNull { it.isNotBlank() } ?: "*/*"
                    fileChooserLauncher.launch(mime)
                }

                override fun onFindResults(activeMatchOrdinal: Int, numberOfMatches: Int) {
                    viewModel.updateFindResults(activeMatchOrdinal, numberOfMatches)
                }

                override fun onOpenNewTabRequest(url: String, isIncognito: Boolean) {
                    viewModel.createNewTab(url = url, isIncognito = isIncognito)
                }

                override fun onPopupTabCreated(tabId: String, isIncognito: Boolean) {
                    viewModel.adoptPopupTab(tabId, isIncognito)
                }

                override fun onPageError(
                    tabId: String,
                    url: String,
                    message: String,
                    isSslError: Boolean
                ) {
                    viewModel.onPageError(tabId, url, message, isSslError)
                }

                override fun onRendererGone(tabId: String, didCrash: Boolean) {
                    viewModel.onRendererGone(tabId, didCrash)
                }

                override fun onExternalUrlError(message: String) {
                    viewModel.showSnackbar(message)
                }
            }
        )
    }

    DisposableEffect(Unit) {
        onDispose { webViewPool.destroyAll() }
    }

    LaunchedEffect(settings) {
        webViewPool.updateSettingsForAll(tabs, settings)
    }

    // Incognito tabs must not show up in the recents thumbnail or a screenshot.
    val hasIncognitoTab = tabs.any { it.isIncognito }
    DisposableEffect(hasIncognitoTab) {
        val window = (context as? Activity)?.window
        if (hasIncognitoTab) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
        onDispose { window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE) }
    }

    // WebView keeps one cookie jar for the whole process, so the incognito session is wiped
    // when its last tab closes.
    var hadIncognitoTab by remember { mutableStateOf(false) }
    LaunchedEffect(hasIncognitoTab) {
        if (hadIncognitoTab && !hasIncognitoTab) {
            webViewPool.clearIncognitoData()
        }
        hadIncognitoTab = hasIncognitoTab
    }

    LaunchedEffect(Unit) {
        viewModel.destroyedTabIds.collect { tabId ->
            webViewPool.destroyTab(tabId)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadUrlCommands.collect { (tabId, url) ->
            val tab = viewModel.tabs.value.firstOrNull { it.id == tabId }
            if (tab != null) {
                webViewPool.getOrCreateWebView(tab, viewModel.settings.value)
                webViewPool.loadUrl(tabId, url, viewModel.settings.value)
            }
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        viewModel.snackbarEvents.collect { event ->
            snackbarHostState.currentSnackbarData?.dismiss()
            val result = snackbarHostState.showSnackbar(
                message = event.message,
                actionLabel = event.actionLabel,
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                event.onAction?.invoke()
            }
        }
    }

    val isBookmarked = remember(bookmarks, activeTab.url) {
        !activeTab.isNewTab && bookmarks.any { it.url == activeTab.url }
    }

    val tabCountForCurrentMode = remember(tabs, activeTab.isIncognito) {
        tabs.count { it.isIncognito == activeTab.isIncognito }
    }

    fun shareText(title: String, text: String) {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(sendIntent, "Share via"))
    }

    fun copyToClipboard(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("URL", text))
        viewModel.showSnackbar("Link copied")
    }

    fun pinCurrentPageToHomeScreen() {
        if (activeTab.isNewTab) return
        if (ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
            val shortcutIntent = Intent(context, MainActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                data = Uri.parse(activeTab.url)
            }
            val icon = activeTab.favicon?.let { IconCompat.createWithBitmap(it) }
                ?: IconCompat.createWithResource(context, R.mipmap.ic_launcher)
            val shortcutInfo = ShortcutInfoCompat.Builder(context, "bansify_${activeTab.url.hashCode()}")
                .setShortLabel(activeTab.title.ifBlank { UrlUtils.extractDisplayHost(activeTab.url) }.take(24))
                .setIcon(icon)
                .setIntent(shortcutIntent)
                .build()
            ShortcutManagerCompat.requestPinShortcut(context, shortcutInfo, null)
            viewModel.showSnackbar("Shortcut requested for Home screen")
        } else {
            viewModel.showSnackbar("Home screen shortcuts are not supported by this launcher")
        }
    }

    fun navigateBackInCurrentTab() {
        if (!webViewPool.goBack(activeTab.id)) {
            viewModel.openUrlInActiveTab(BrowserTab.NEW_TAB_URL)
        }
    }

    fun openReadingModeForCurrentTab() {
        if (activeTab.isNewTab) return
        webViewPool.extractReadableArticle(activeTab.id) { title, paragraphs ->
            viewModel.showReadingMode(
                ReadingModeContent(
                    title = title.ifBlank { activeTab.title },
                    domain = UrlUtils.extractDisplayHost(activeTab.url),
                    url = activeTab.url,
                    paragraphs = paragraphs
                )
            )
        }
    }

    BackHandler(enabled = customFullscreenView != null) {
        customFullscreenCallback?.onCustomViewHidden()
        customFullscreenView = null
        customFullscreenCallback = null
    }

    BackHandler(enabled = customFullscreenView == null && readingMode == null && findInPage.isActive && overlay == BrowserOverlay.None) {
        webViewPool.clearFindMatches(activeTab.id)
        viewModel.setFindInPageActive(false)
    }

    BackHandler(enabled = customFullscreenView == null && readingMode == null && !findInPage.isActive && overlay == BrowserOverlay.None && !activeTab.isNewTab) {
        navigateBackInCurrentTab()
    }

    val useIncognitoTheme = activeTab.isIncognito &&
        (overlay == BrowserOverlay.None || overlay is BrowserOverlay.Omnibox)

    val isBottomAddressBar = settings.addressBarPosition == AddressBarPosition.BOTTOM && !activeTab.isNewTab

    BansifyBrowserTheme(
        themeMode = settings.themeMode,
        isIncognito = useIncognitoTheme
    ) {
        val isDarkNow = LocalIsDarkBrowserTheme.current

        val toolbarComposable: @Composable () -> Unit = {
            ChromeToolbar(
                activeTab = activeTab,
                tabCount = tabCountForCurrentMode,
                settings = settings,
                accountState = accountState,
                isBookmarked = isBookmarked,
                findInPage = findInPage,
                isBottomPlacement = isBottomAddressBar,
                overflowMenuExpanded = overflowMenuExpanded,
                onOverflowMenuChange = { overflowMenuExpanded = it },
                onHomeClick = { viewModel.navigateHome() },
                onAccountClick = {
                    viewModel.openOverlay(BrowserOverlay.Settings(SettingsSubScreen.BANSIFY_ACCOUNT))
                },
                onOmniboxClick = { viewModel.openOverlay(BrowserOverlay.Omnibox()) },
                onPageInfoClick = { viewModel.setPageInfoVisible(true) },
                onTabSwitcherClick = {
                    if (!activeTab.isNewTab) {
                        webViewPool.captureThumbnail(activeTab.id)
                    }
                    viewModel.openOverlay(BrowserOverlay.TabSwitcher(showIncognito = activeTab.isIncognito))
                },
                onNewTabClick = { viewModel.createNewTab(isIncognito = false) },
                onNewIncognitoTabClick = { viewModel.createNewTab(isIncognito = true) },
                onCloseActiveTabClick = { viewModel.closeTab(activeTab.id) },
                onShareClick = {
                    if (!activeTab.isNewTab) shareText(activeTab.title, activeTab.url)
                },
                onToggleBookmarkClick = { viewModel.toggleBookmarkForActiveTab() },
                onFindQueryChange = { query ->
                    viewModel.updateFindQuery(query)
                    webViewPool.findInPage(activeTab.id, query)
                },
                onFindNext = { forward -> webViewPool.findNext(activeTab.id, forward) },
                onCloseFindInPage = {
                    webViewPool.clearFindMatches(activeTab.id)
                    viewModel.setFindInPageActive(false)
                },
                overflowMenuContent = {}
            )
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                snackbarHost = {
                    SnackbarHost(
                        hostState = snackbarHostState,
                        modifier = Modifier.navigationBarsPadding()
                    )
                },
                topBar = {
                    if (!isBottomAddressBar) toolbarComposable()
                },
                bottomBar = {
                    if (isBottomAddressBar) toolbarComposable()
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .then(if (isBottomAddressBar) Modifier.statusBarsPadding() else Modifier)
                        .imePadding()
                ) {
                    if (activeTab.isNewTab) {
                        NewTabScreen(
                            isIncognito = activeTab.isIncognito,
                            settings = settings,
                            shortcuts = shortcuts,
                            recentBookmarks = bookmarks,
                            recentHistory = history,
                            onSearchClick = { viewModel.openOverlay(BrowserOverlay.Omnibox()) },
                            onVoiceSearchClick = { launchVoiceSearch() },
                            onNewTabClick = { viewModel.createNewTab(isIncognito = false) },
                            onNewIncognitoTabClick = { viewModel.createNewTab(isIncognito = true) },
                            onOpenUrl = { url -> viewModel.openUrlInActiveTab(url) },
                            onAddShortcut = { title, url -> viewModel.addShortcut(title, url) },
                            onUpdateShortcut = { shortcut -> viewModel.updateShortcut(shortcut) },
                            onDeleteShortcut = { id -> viewModel.deleteShortcut(id) },
                            onToggleIncognitoBlockThirdParty = { block ->
                                viewModel.toggleIncognitoBlockThirdPartyCookies(block)
                            },
                            onOpenBookmarks = { viewModel.openOverlay(BrowserOverlay.Bookmarks) },
                            onOpenHistory = { viewModel.openOverlay(BrowserOverlay.History) }
                        )
                    } else {
                        AndroidView(
                            factory = { ctx ->
                                FrameLayout(ctx).apply {
                                    layoutParams = ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT
                                    )
                                }
                            },
                            update = { container ->
                                val targetWebView = webViewPool.getOrCreateWebView(activeTab, settings)
                                if (container.childCount != 1 || container.getChildAt(0) !== targetWebView) {
                                    container.removeAllViews()
                                    (targetWebView.parent as? ViewGroup)?.removeView(targetWebView)
                                    container.addView(targetWebView)
                                }
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .then(if (!isBottomAddressBar) Modifier.navigationBarsPadding() else Modifier)
                        )
                    }

                    val currentError = pageError
                    if (currentError != null && currentError.tabId == activeTab.id) {
                        PageErrorOverlay(
                            error = currentError,
                            onRetry = {
                                viewModel.dismissPageError()
                                webViewPool.reload(currentError.tabId)
                            },
                            onClose = {
                                viewModel.dismissPageError()
                                viewModel.closeTab(currentError.tabId)
                            }
                        )
                    }
                }
            }

            // Root-aligned Chrome 3-Dot Overflow Menu (flush right edge + full vertical scrolling)
            ChromeOverflowMenu(
                expanded = overflowMenuExpanded,
                activeTab = activeTab,
                isBookmarked = isBookmarked,
                isBottomPlacement = isBottomAddressBar,
                onDismiss = { overflowMenuExpanded = false },
                onBack = { navigateBackInCurrentTab() },
                onForward = { webViewPool.goForward(activeTab.id) },
                onToggleBookmark = { viewModel.toggleBookmarkForActiveTab() },
                onDownloadPage = {
                    webViewPool.saveCurrentPageArchive(
                        tabId = activeTab.id,
                        title = activeTab.title,
                        url = activeTab.url
                    )
                },
                onReloadOrStop = {
                    if (activeTab.isLoading) {
                        webViewPool.stopLoading(activeTab.id)
                    } else {
                        webViewPool.reload(activeTab.id)
                    }
                },
                onNewTab = { viewModel.createNewTab(isIncognito = false) },
                onNewIncognitoTab = { viewModel.createNewTab(isIncognito = true) },
                onAddTabToNewGroup = { viewModel.addActiveTabToNewGroup() },
                onHistory = { viewModel.openOverlay(BrowserOverlay.History) },
                onDeleteBrowsingData = { viewModel.openOverlay(BrowserOverlay.DeleteBrowsingData) },
                onSiteControls = { viewModel.setPageInfoVisible(true) },
                onDownloads = { viewModel.openOverlay(BrowserOverlay.Downloads) },
                onBookmarks = { viewModel.openOverlay(BrowserOverlay.Bookmarks) },
                onRecentTabs = { viewModel.openOverlay(BrowserOverlay.RecentTabs) },
                onShare = {
                    if (!activeTab.isNewTab) shareText(activeTab.title, activeTab.url)
                },
                onFindInPage = { viewModel.setFindInPageActive(true) },
                onTranslate = { viewModel.setTranslateSheetVisible(true) },
                onReadingMode = { openReadingModeForCurrentTab() },
                onAddToHomeScreen = { pinCurrentPageToHomeScreen() },
                onToggleDesktopSite = {
                    val enabled = viewModel.toggleDesktopSiteForActiveTab()
                    webViewPool.setDesktopSite(activeTab, enabled)
                    viewModel.showSnackbar(if (enabled) "Desktop site enabled" else "Mobile site enabled")
                },
                onSettings = {
                    viewModel.openOverlay(BrowserOverlay.Settings(SettingsSubScreen.MAIN))
                },
                onHelpAndFeedback = {
                    viewModel.openOverlay(BrowserOverlay.Settings(SettingsSubScreen.ABOUT))
                }
            )

            // Fullscreen Chrome Overlays
            when (val currentOverlay = overlay) {
                BrowserOverlay.None -> Unit

                is BrowserOverlay.Omnibox -> {
                    OmniboxOverlay(
                        activeTab = activeTab,
                        openTabs = tabs,
                        bookmarks = bookmarks,
                        history = history,
                        shortcuts = shortcuts,
                        recentSearches = recentSearches,
                        settings = settings,
                        onDismiss = { viewModel.closeOverlay() },
                        onSubmitUrl = { input -> viewModel.openUrlInActiveTab(input) },
                        onVoiceSearchClick = { launchVoiceSearch() },
                        onNewTabClick = { viewModel.createNewTab(isIncognito = false) },
                        onSwitchToTab = { tabId -> viewModel.selectTab(tabId) },
                        onShareCurrentUrl = {
                            if (!activeTab.isNewTab) shareText(activeTab.title, activeTab.url)
                        },
                        onCopyCurrentUrl = {
                            if (!activeTab.isNewTab) copyToClipboard(activeTab.url)
                        }
                    )
                }

                is BrowserOverlay.TabSwitcher -> {
                    TabSwitcherScreen(
                        tabs = tabs,
                        activeTabId = activeTab.id,
                        initialShowIncognito = currentOverlay.showIncognito,
                        onDismiss = { viewModel.closeOverlay() },
                        onSelectTab = { tabId -> viewModel.selectTab(tabId) },
                        onCloseTab = { tabId -> viewModel.closeTab(tabId) },
                        onCreateNewTab = { incognito -> viewModel.createNewTab(isIncognito = incognito) },
                        onCloseAllTabs = { incognitoOnly -> viewModel.closeAllTabs(incognitoOnly) },
                        onOpenSettings = {
                            viewModel.openOverlay(BrowserOverlay.Settings(SettingsSubScreen.MAIN))
                        }
                    )
                }

                BrowserOverlay.Bookmarks -> {
                    BookmarksScreen(
                        bookmarks = bookmarks,
                        accountState = accountState,
                        onClose = { viewModel.closeOverlay() },
                        onOpenUrl = { url -> viewModel.openUrlInActiveTab(url) },
                        onOpenInNewTab = { url, incog -> viewModel.createNewTab(url = url, isIncognito = incog) },
                        onAddBookmark = {
                            viewModel.startEditingBookmark(
                                BookmarkEntity(
                                    id = 0L,
                                    title = if (activeTab.isNewTab) "" else activeTab.title,
                                    url = if (activeTab.isNewTab) "https://" else activeTab.url
                                )
                            )
                        },
                        onEditBookmark = { bm -> viewModel.startEditingBookmark(bm) },
                        onDeleteBookmark = { id -> viewModel.deleteBookmark(id) },
                        onCopyUrl = { url -> copyToClipboard(url) },
                        onOpenSyncSettings = {
                            viewModel.openOverlay(BrowserOverlay.Settings(SettingsSubScreen.BANSIFY_ACCOUNT))
                        }
                    )
                }

                BrowserOverlay.History -> {
                    HistoryScreen(
                        history = history,
                        onClose = { viewModel.closeOverlay() },
                        onOpenUrl = { url -> viewModel.openUrlInActiveTab(url) },
                        onDeleteItem = { id -> viewModel.deleteHistoryItem(id) },
                        onOpenDeleteBrowsingData = { viewModel.openOverlay(BrowserOverlay.DeleteBrowsingData) }
                    )
                }

                BrowserOverlay.Downloads -> {
                    DownloadsScreen(
                        downloads = downloads,
                        onClose = { viewModel.closeOverlay() },
                        onOpenDownloadUrl = { url -> viewModel.openUrlInActiveTab(url) },
                        onShareDownload = { dl -> shareText(dl.fileName, dl.url) },
                        onDeleteDownload = { id -> viewModel.deleteDownload(id) },
                        onOpenDownloadsSettings = {
                            viewModel.openOverlay(BrowserOverlay.Settings(SettingsSubScreen.DOWNLOADS_SETTINGS))
                        }
                    )
                }

                BrowserOverlay.RecentTabs -> {
                    RecentTabsScreen(
                        recentlyClosedTabs = recentlyClosed,
                        onClose = { viewModel.closeOverlay() },
                        onReopenTab = { url -> viewModel.createNewTab(url = url, isIncognito = false) },
                        onOpenFullHistory = { viewModel.openOverlay(BrowserOverlay.History) }
                    )
                }

                BrowserOverlay.DeleteBrowsingData -> {
                    DeleteBrowsingDataScreen(
                        historyCount = history.size,
                        openNormalTabCount = tabs.count { !it.isIncognito },
                        onBack = { viewModel.closeOverlay() },
                        onConfirmClear = { range, clearHist, clearCookies, clearCache, clearSiteData, closeTabs ->
                            viewModel.clearBrowsingData(
                                timeRange = range,
                                clearHistory = clearHist,
                                clearCookies = clearCookies,
                                clearCache = clearCache,
                                clearSiteData = clearSiteData,
                                closeOpenTabs = closeTabs,
                                onClearWebViewCache = { webViewPool.clearAllCaches() }
                            )
                        }
                    )
                }

                is BrowserOverlay.Settings -> {
                    SettingsScreen(
                        subScreen = currentOverlay.subScreen,
                        settings = settings,
                        accountState = accountState,
                        bookmarksCount = bookmarks.size,
                        historyCount = history.size,
                        openTabsCount = tabs.count { !it.isIncognito },
                        shortcutsCount = shortcuts.size,
                        downloadsCount = downloads.size,
                        onBack = { viewModel.navigateSettingsBack() },
                        onNavigateSubScreen = { sub -> viewModel.navigateSettingsSubScreen(sub) },
                        onOpenDeleteBrowsingData = { viewModel.openOverlay(BrowserOverlay.DeleteBrowsingData) },
                        onUpdateSettings = { transform -> viewModel.updateSettings(transform) },
                        onSignInBansify = { name, email, color ->
                            viewModel.signInBansifyAccount(name, email, color)
                        },
                        onUpdateBansifyProfile = { name, email, color ->
                            viewModel.updateBansifyProfile(name, email, color)
                        },
                        onSignOutBansify = { viewModel.signOutBansifyAccount() },
                        onUpdateBansifySync = { transform -> viewModel.updateBansifySync(transform) },
                        onSyncNow = { viewModel.triggerManualSync() }
                    )
                }
            }

            // Reading Mode Distraction-Free Overlay
            readingMode?.let { article ->
                ReadingModeOverlay(
                    content = article,
                    initialDark = isDarkNow,
                    onClose = { viewModel.closeReadingMode() }
                )
            }

            // Fullscreen WebView Video Container
            customFullscreenView?.let { fsView ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                ) {
                    AndroidView(
                        factory = { ctx ->
                            FrameLayout(ctx).apply {
                                (fsView.parent as? ViewGroup)?.removeView(fsView)
                                addView(fsView)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Chrome Page Info / Site Controls Bottom Sheet
            if (showPageInfo && !activeTab.isNewTab) {
                PageInfoBottomSheet(
                    tab = activeTab,
                    cookiesAllowed = settings.cookiesEnabled,
                    onDismiss = { viewModel.setPageInfoVisible(false) },
                    onClearSiteData = {
                        viewModel.clearSiteDataForActiveSite { webViewPool.clearAllCaches() }
                    },
                    onOpenSiteSettings = {
                        viewModel.openOverlay(BrowserOverlay.Settings(SettingsSubScreen.SITE_SETTINGS))
                    },
                    onShareUrl = { shareText(activeTab.title, activeTab.url) }
                )
            }

            // Chrome Translate Page Bottom Sheet
            if (showTranslate && !activeTab.isNewTab) {
                TranslatePageBottomSheet(
                    currentUrl = activeTab.url,
                    onDismiss = { viewModel.setTranslateSheetVisible(false) },
                    onSelectLanguage = { code, name ->
                        viewModel.translateActivePage(code, name)
                    }
                )
            }

            // Bookmark Add / Edit Dialog
            editingBookmark?.let { bm ->
                BookmarkEditDialog(
                    bookmark = bm,
                    onDismiss = { viewModel.startEditingBookmark(null) },
                    onSave = { id, title, url, folder ->
                        viewModel.saveBookmark(id, title, url, folder)
                    },
                    onDelete = { id -> viewModel.deleteBookmark(id) }
                )
            }

            // WebView JavaScript Alert / Confirm / Prompt Dialogs
            jsDialog?.let { dialog ->
                WebViewJsDialog(
                    dialog = dialog,
                    onDismiss = { viewModel.showJsDialog(null) }
                )
            }
        }
    }
}

/**
 * Shown over a page that failed to load, so a network or certificate failure never leaves a
 * blank screen behind the toolbar.
 */
@Composable
private fun PageErrorOverlay(
    error: PageError,
    onRetry: () -> Unit,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(24.dp)
                .widthIn(max = 420.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (error.isSslError) "Connection not secure" else "This page could not load",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = error.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Start
                )
                if (error.url.isNotBlank()) {
                    Text(
                        text = error.url,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(onClick = onRetry) {
                        Text("Try again")
                    }
                    TextButton(onClick = onClose) {
                        Text("Close tab")
                    }
                }
            }
        }
    }
}
