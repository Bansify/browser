package com.bansify.browser.viewmodel

import android.app.Application
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File
import android.graphics.Bitmap
import android.webkit.CookieManager
import android.webkit.SafeBrowsingResponse
import android.webkit.WebStorage
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bansify.browser.browser.UrlUtils
import com.bansify.browser.data.BansifyAccountProvider
import com.bansify.browser.data.BansifyAccountState
import com.bansify.browser.data.BookmarkEntity
import com.bansify.browser.data.BrowserDatabase
import com.bansify.browser.data.BrowserOverlay
import com.bansify.browser.data.BrowserPreferences
import com.bansify.browser.data.BrowserRepository
import com.bansify.browser.data.BrowserSettings
import com.bansify.browser.data.BrowserTab
import com.bansify.browser.data.ClosedTab
import com.bansify.browser.data.DemoBansifyAccountProvider
import com.bansify.browser.data.DownloadEntity
import com.bansify.browser.data.FindInPageState
import com.bansify.browser.data.HistoryEntity
import com.bansify.browser.data.JsDialogState
import com.bansify.browser.data.ReadingModeContent
import com.bansify.browser.data.SettingsSubScreen
import com.bansify.browser.data.SitePermission
import com.bansify.browser.data.SitePermissionDecision
import com.bansify.browser.data.SitePermissionStore
import com.bansify.browser.data.ShortcutEntity
import com.bansify.browser.data.SnackbarEvent
import com.bansify.browser.data.ThirdPartyCookiePolicy
import com.bansify.browser.data.TimeRange
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.URLEncoder

class BrowserViewModel(application: Application) : AndroidViewModel(application) {

    private val database = BrowserDatabase.getInstance(application)
    private val preferences = BrowserPreferences(application)
    private val repository = BrowserRepository(database.browserDao(), preferences)
    private val accountProvider: BansifyAccountProvider = DemoBansifyAccountProvider(application)

    private val _isSyncing = MutableStateFlow(false)

    val accountState: StateFlow<BansifyAccountState> = combine(
        accountProvider.accountState,
        _isSyncing
    ) { state, syncing ->
        state.copy(isSyncing = syncing)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = BansifyAccountState()
    )

    val settings: StateFlow<BrowserSettings> = repository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = BrowserSettings()
    )

    val bookmarks: StateFlow<List<BookmarkEntity>> = repository.bookmarks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val history: StateFlow<List<HistoryEntity>> = repository.history.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val downloads: StateFlow<List<DownloadEntity>> = repository.downloads.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val shortcuts: StateFlow<List<ShortcutEntity>> = repository.shortcuts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val initialTab = BrowserTab()
    private val _tabs = MutableStateFlow(listOf(initialTab))
    val tabs: StateFlow<List<BrowserTab>> = _tabs.asStateFlow()

    private val _activeTabId = MutableStateFlow(initialTab.id)
    val activeTabId: StateFlow<String> = _activeTabId.asStateFlow()

    val activeTab: StateFlow<BrowserTab> = combine(_tabs, _activeTabId) { list, id ->
        list.firstOrNull { it.id == id } ?: list.firstOrNull() ?: BrowserTab()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = initialTab
    )

    private val _recentlyClosedTabs = MutableStateFlow<List<ClosedTab>>(emptyList())
    val recentlyClosedTabs: StateFlow<List<ClosedTab>> = _recentlyClosedTabs.asStateFlow()

    private val _recentSearches = MutableStateFlow<List<String>>(emptyList())
    val recentSearches: StateFlow<List<String>> = _recentSearches.asStateFlow()

    private val _overlay = MutableStateFlow<BrowserOverlay>(BrowserOverlay.None)
    val overlay: StateFlow<BrowserOverlay> = _overlay.asStateFlow()

    private val _findInPage = MutableStateFlow(FindInPageState())
    val findInPage: StateFlow<FindInPageState> = _findInPage.asStateFlow()

    private val _jsDialog = MutableStateFlow<JsDialogState?>(null)
    val jsDialog: StateFlow<JsDialogState?> = _jsDialog.asStateFlow()

    private val _showPageInfoSheet = MutableStateFlow(false)
    val showPageInfoSheet: StateFlow<Boolean> = _showPageInfoSheet.asStateFlow()

    private val _showTranslateSheet = MutableStateFlow(false)
    val showTranslateSheet: StateFlow<Boolean> = _showTranslateSheet.asStateFlow()

    private val _readingModeContent = MutableStateFlow<ReadingModeContent?>(null)
    val readingModeContent: StateFlow<ReadingModeContent?> = _readingModeContent.asStateFlow()

    private val _editingBookmark = MutableStateFlow<BookmarkEntity?>(null)
    val editingBookmark: StateFlow<BookmarkEntity?> = _editingBookmark.asStateFlow()

    private val _snackbarEvents = MutableSharedFlow<SnackbarEvent>(extraBufferCapacity = 8)
    val snackbarEvents = _snackbarEvents.asSharedFlow()

    private val _destroyedTabIds = MutableSharedFlow<String>(extraBufferCapacity = 16)
    val destroyedTabIds = _destroyedTabIds.asSharedFlow()

    private val _loadUrlCommands = MutableSharedFlow<Pair<String, String>>(extraBufferCapacity = 8)
    val loadUrlCommands = _loadUrlCommands.asSharedFlow()

    private val _pageError = MutableStateFlow<PageError?>(null)
    val pageError: StateFlow<PageError?> = _pageError.asStateFlow()

    private val sitePermissionStore = SitePermissionStore(getApplication())

    /** Remembered per-site decisions, newest first. */
    val sitePermissionDecisions: StateFlow<List<SitePermissionDecision>> =
        sitePermissionStore.decisions.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _pendingPermission = MutableStateFlow<PendingSitePermission?>(null)
    val pendingPermission: StateFlow<PendingSitePermission?> = _pendingPermission.asStateFlow()

    private val _safeBrowsingWarning = MutableStateFlow<SafeBrowsingWarning?>(null)
    val safeBrowsingWarning: StateFlow<SafeBrowsingWarning?> = _safeBrowsingWarning.asStateFlow()

    private var groupCounter = 1
    private val groupColors = listOf(
        0xFF8AB4F8L,
        0xFFF28B82L,
        0xFFFDD663L,
        0xFF81C995L,
        0xFFC58AF9L,
        0xFF78D9ECL
    )

    init {
        watchDownloads()
        viewModelScope.launch {
            repository.ensureDefaultShortcuts()
            val saved = repository.loadSavedTabs()
            if (saved.isNotEmpty()) {
                val restored = saved.map {
                    BrowserTab(
                        id = it.tabId,
                        url = it.url,
                        title = it.title,
                        isIncognito = false,
                        isDesktopSite = it.isDesktopSite,
                        isSecure = it.url.startsWith("https://", ignoreCase = true),
                        lastAccessedAt = it.lastAccessedAt
                    )
                }
                _tabs.value = restored
                _activeTabId.value = restored.last().id
            }
        }
    }

    // --- Navigation & Tab Operations ---

    fun openUrlInActiveTab(rawInput: String) {
        val trimmed = rawInput.trim()
        val currentSettings = settings.value
        val tab = activeTab.value

        // Record query in recent searches if it's a search query (not a direct URL) in non-Incognito mode
        if (!tab.isIncognito &&
            trimmed.isNotEmpty() &&
            trimmed != BrowserTab.NEW_TAB_URL &&
            !trimmed.startsWith("http://", ignoreCase = true) &&
            !trimmed.startsWith("https://", ignoreCase = true) &&
            !trimmed.contains('.')
        ) {
            _recentSearches.update { prev ->
                (listOf(trimmed) + prev.filterNot { it.equals(trimmed, ignoreCase = true) }).take(15)
            }
        }

        val resolved = UrlUtils.resolveInputToUrl(
            rawInput = trimmed,
            searchEngine = currentSettings.searchEngine,
            httpsFirst = currentSettings.httpsFirstEnabled
        )
        updateTab(tab.id) {
            it.copy(
                url = resolved,
                title = if (resolved == BrowserTab.NEW_TAB_URL) "New tab" else UrlUtils.extractDisplayHost(resolved),
                isSecure = resolved.startsWith("https://", ignoreCase = true),
                isLoading = resolved != BrowserTab.NEW_TAB_URL,
                progress = if (resolved != BrowserTab.NEW_TAB_URL) 10 else 0,
                lastAccessedAt = System.currentTimeMillis()
            )
        }
        _overlay.value = BrowserOverlay.None
        _findInPage.value = FindInPageState()
        _readingModeContent.value = null
        if (resolved != BrowserTab.NEW_TAB_URL) {
            _loadUrlCommands.tryEmit(tab.id to resolved)
        }
        persistNormalTabs()
    }

    fun navigateHome() {
        val currentSettings = settings.value
        val target = if (currentSettings.useCustomHomepage && currentSettings.customHomepageUrl.isNotBlank()) {
            currentSettings.customHomepageUrl
        } else {
            BrowserTab.NEW_TAB_URL
        }
        openUrlInActiveTab(target)
    }

    // --- Site permissions -------------------------------------------------

    /**
     * A page asked for camera, microphone or location. A remembered decision answers straight
     * away; otherwise the user gets a dialog. If prompts are switched off everything is denied.
     */
    fun onSitePermissionRequest(
        tabId: String,
        origin: String,
        permission: SitePermission,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val remembered = sitePermissionStore.decisionFor(origin, permission)
            when {
                remembered == true -> onResult(true)
                remembered == false -> onResult(false)
                !settings.value.sitePermissionPromptsEnabled -> onResult(false)
                else -> _pendingPermission.value = PendingSitePermission(
                    tabId = tabId,
                    origin = origin,
                    permission = permission,
                    onResult = onResult
                )
            }
        }
    }

    fun answerPendingPermission(grant: Boolean, remember: Boolean) {
        val pending = _pendingPermission.value ?: return
        _pendingPermission.value = null
        if (remember) {
            viewModelScope.launch {
                sitePermissionStore.remember(pending.origin, pending.permission, grant)
            }
        }
        pending.onResult(grant)
    }

    fun forgetSitePermission(origin: String) {
        viewModelScope.launch { sitePermissionStore.forget(origin) }
    }

    fun forgetAllSitePermissions() {
        viewModelScope.launch { sitePermissionStore.forgetAll() }
    }

    // --- Safe Browsing -----------------------------------------------------

    fun onSafeBrowsingHit(
        tabId: String,
        url: String,
        threatType: Int,
        response: SafeBrowsingResponse
    ) {
        _safeBrowsingWarning.value = SafeBrowsingWarning(tabId, url, threatType, response)
    }

    /** [proceed] = the user chose to load the flagged page anyway. */
    fun resolveSafeBrowsing(proceed: Boolean) {
        val warning = _safeBrowsingWarning.value ?: return
        _safeBrowsingWarning.value = null
        runCatching {
            if (proceed) warning.response.proceed(true) else warning.response.backToSafety(true)
        }
    }

    /**
     * A site called window.open(). The WebView already exists in the pool under [tabId] and is
     * loading the popup, so this only adds the tab entry the UI needs to show it.
     */
    fun adoptPopupTab(tabId: String, isIncognito: Boolean) {
        if (_tabs.value.any { it.id == tabId }) {
            _activeTabId.value = tabId
            return
        }
        val popupTab = BrowserTab(
            id = tabId,
            url = BrowserTab.NEW_TAB_URL,
            title = "New tab",
            isIncognito = isIncognito,
            isSecure = true,
            isLoading = true,
            progress = 5
        )
        _tabs.update { it + popupTab }
        _activeTabId.value = tabId
        _overlay.value = BrowserOverlay.None
    }

    /** Main-frame failure (network or blocked certificate) for the active tab. */
    fun onPageError(tabId: String, url: String, message: String, isSslError: Boolean) {
        _tabs.update { tabs ->
            tabs.map { if (it.id == tabId) it.copy(isLoading = false, progress = 100) else it }
        }
        _pageError.value = PageError(
            tabId = tabId,
            url = url,
            message = message,
            isSslError = isSslError
        )
    }

    fun dismissPageError() {
        _pageError.value = null
    }

    /** The renderer for this tab died; the WebView is already destroyed, so drop the tab. */
    fun onRendererGone(tabId: String, didCrash: Boolean) {
        if (_pageError.value?.tabId == tabId) _pageError.value = null
        closeTab(tabId, allowUndo = false)
        showSnackbar(
            message = if (didCrash) "This tab stopped responding and was closed" else "This tab ran out of memory and was closed"
        )
    }

    fun createNewTab(url: String = BrowserTab.NEW_TAB_URL, isIncognito: Boolean = false) {
        val currentSettings = settings.value
        val resolved = if (url == BrowserTab.NEW_TAB_URL) {
            BrowserTab.NEW_TAB_URL
        } else {
            UrlUtils.resolveInputToUrl(url, currentSettings.searchEngine, currentSettings.httpsFirstEnabled)
        }
        val newTab = BrowserTab(
            url = resolved,
            title = if (resolved == BrowserTab.NEW_TAB_URL) {
                if (isIncognito) "New Incognito tab" else "New tab"
            } else {
                UrlUtils.extractDisplayHost(resolved)
            },
            isIncognito = isIncognito,
            isDesktopSite = currentSettings.defaultDesktopSite,
            isSecure = resolved.startsWith("https://", ignoreCase = true),
            isLoading = resolved != BrowserTab.NEW_TAB_URL,
            progress = if (resolved != BrowserTab.NEW_TAB_URL) 10 else 0
        )
        _tabs.update { it + newTab }
        _activeTabId.value = newTab.id
        _overlay.value = BrowserOverlay.None
        _findInPage.value = FindInPageState()
        _readingModeContent.value = null
        if (resolved != BrowserTab.NEW_TAB_URL) {
            _loadUrlCommands.tryEmit(newTab.id to resolved)
        }
        persistNormalTabs()
    }

    fun addActiveTabToNewGroup() {
        val tab = activeTab.value
        val color = groupColors[(groupCounter - 1) % groupColors.size]
        val groupLabel = "Group $groupCounter"
        groupCounter++
        updateTab(tab.id) {
            it.copy(groupName = groupLabel, groupColor = color)
        }
        showSnackbar(
            message = "Added to $groupLabel",
            actionLabel = "View tabs",
            onAction = { openOverlay(BrowserOverlay.TabSwitcher(showIncognito = tab.isIncognito)) }
        )
    }

    fun removeTabFromGroup(tabId: String) {
        updateTab(tabId) { it.copy(groupName = null, groupColor = null) }
    }

    fun selectTab(tabId: String) {
        if (_tabs.value.any { it.id == tabId }) {
            _activeTabId.value = tabId
            updateTab(tabId) { it.copy(lastAccessedAt = System.currentTimeMillis()) }
            _overlay.value = BrowserOverlay.None
            _findInPage.value = FindInPageState()
            _readingModeContent.value = null
            persistNormalTabs()
        }
    }

    fun closeTab(tabId: String, allowUndo: Boolean = true) {
        val currentList = _tabs.value
        val target = currentList.firstOrNull { it.id == tabId } ?: return
        val remaining = currentList.filterNot { it.id == tabId }

        if (!target.isIncognito && !target.isNewTab) {
            _recentlyClosedTabs.update { list ->
                (listOf(ClosedTab(title = target.title, url = target.url)) + list).take(25)
            }
        }

        _destroyedTabIds.tryEmit(tabId)

        if (remaining.isEmpty()) {
            val replacement = BrowserTab(isIncognito = false)
            _tabs.value = listOf(replacement)
            _activeTabId.value = replacement.id
        } else {
            _tabs.value = remaining
            if (_activeTabId.value == tabId) {
                val sameMode = remaining.lastOrNull { it.isIncognito == target.isIncognito }
                _activeTabId.value = (sameMode ?: remaining.last()).id
            }
        }

        if (allowUndo && !target.isNewTab && !target.isIncognito) {
            showSnackbar(
                message = "Closed ${target.title.take(28)}",
                actionLabel = "Undo",
                onAction = { createNewTab(url = target.url, isIncognito = false) }
            )
        }
        persistNormalTabs()
    }

    fun closeAllTabs(incognitoOnly: Boolean) {
        val currentList = _tabs.value
        val toClose = currentList.filter { it.isIncognito == incognitoOnly }
        toClose.forEach { tab ->
            if (!tab.isIncognito && !tab.isNewTab) {
                _recentlyClosedTabs.update { list ->
                    (listOf(ClosedTab(title = tab.title, url = tab.url)) + list).take(25)
                }
            }
            _destroyedTabIds.tryEmit(tab.id)
        }

        val remaining = currentList.filter { it.isIncognito != incognitoOnly }
        if (remaining.isEmpty()) {
            val fresh = BrowserTab(isIncognito = false)
            _tabs.value = listOf(fresh)
            _activeTabId.value = fresh.id
            _overlay.value = BrowserOverlay.None
        } else {
            _tabs.value = remaining
            if (remaining.none { it.id == _activeTabId.value }) {
                _activeTabId.value = remaining.last().id
            }
        }

        showSnackbar(if (incognitoOnly) "Closed all Incognito tabs" else "Closed all tabs")
        persistNormalTabs()
    }

    fun toggleDesktopSiteForActiveTab(): Boolean {
        val tab = activeTab.value
        val newValue = !tab.isDesktopSite
        updateTab(tab.id) { it.copy(isDesktopSite = newValue) }
        persistNormalTabs()
        return newValue
    }

    // --- Translate & Reading Mode ---

    fun setTranslateSheetVisible(visible: Boolean) {
        _showTranslateSheet.value = visible
    }

    fun translateActivePage(targetLangCode: String, targetLangName: String) {
        val tab = activeTab.value
        _showTranslateSheet.value = false
        if (tab.isNewTab) return
        val encoded = URLEncoder.encode(tab.url, "UTF-8")
        val translateUrl = "https://translate.google.com/translate?sl=auto&tl=$targetLangCode&u=$encoded"
        openUrlInActiveTab(translateUrl)
        showSnackbar("Translating page to $targetLangName")
    }

    fun showReadingMode(content: ReadingModeContent) {
        _readingModeContent.value = content
    }

    fun closeReadingMode() {
        _readingModeContent.value = null
    }

    // --- WebView Callback Handlers ---

    fun onWebPageStarted(tabId: String, url: String) {
        updateTab(tabId) {
            it.copy(
                url = url,
                isSecure = url.startsWith("https://", ignoreCase = true),
                isLoading = true,
                progress = 15
            )
        }
    }

    fun onWebPageFinished(
        tabId: String,
        url: String,
        title: String,
        canGoBack: Boolean,
        canGoForward: Boolean
    ) {
        val tab = _tabs.value.firstOrNull { it.id == tabId }
        updateTab(tabId) {
            it.copy(
                url = url,
                title = title.ifBlank { UrlUtils.extractDisplayHost(url) },
                isSecure = url.startsWith("https://", ignoreCase = true),
                isLoading = false,
                progress = 100,
                canGoBack = canGoBack,
                canGoForward = canGoForward
            )
        }
        if (tab != null && !tab.isIncognito) {
            viewModelScope.launch {
                repository.recordHistory(title, url)
            }
            persistNormalTabs()
        }
    }

    fun onWebProgressChanged(tabId: String, progress: Int) {
        updateTab(tabId) {
            it.copy(
                progress = progress,
                isLoading = progress in 1..99
            )
        }
    }

    fun onWebTitleReceived(tabId: String, title: String) {
        updateTab(tabId) { it.copy(title = title) }
    }

    fun onWebFaviconReceived(tabId: String, icon: Bitmap?) {
        if (icon != null) {
            updateTab(tabId) { it.copy(favicon = icon) }
        }
    }

    fun onThumbnailCaptured(tabId: String, bitmap: Bitmap) {
        updateTab(tabId) { it.copy(thumbnail = bitmap) }
    }

    fun onDownloadRecorded(
        fileName: String,
        url: String,
        mimeType: String,
        sizeBytes: Long,
        localPath: String,
        managerId: Long = -1L
    ) {
        viewModelScope.launch {
            repository.insertDownload(
                DownloadEntity(
                    fileName = fileName,
                    url = url,
                    mimeType = mimeType,
                    sizeBytes = sizeBytes,
                    category = UrlUtils.inferDownloadCategory(fileName, mimeType),
                    status = if (managerId >= 0) "Downloading" else "Completed",
                    localPath = localPath,
                    managerId = managerId
                )
            )
            if (managerId >= 0) {
                showSnackbar(
                    message = "Downloading $fileName",
                    actionLabel = "View",
                    onAction = { openOverlay(BrowserOverlay.Downloads) }
                )
            } else {
                showSnackbar(
                    message = "Saved $fileName",
                    actionLabel = "View",
                    onAction = { openOverlay(BrowserOverlay.Downloads) }
                )
            }
        }
    }

    // --- Downloads: live progress, cancel, retry, open, share ----------------

    private val _downloadProgress = MutableStateFlow<Map<Long, Float>>(emptyMap())
    val downloadProgress: StateFlow<Map<Long, Float>> = _downloadProgress.asStateFlow()

    private val downloadManager: DownloadManager?
        get() = getApplication<Application>()
            .getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager

    /**
     * Polls DownloadManager while anything is downloading, so the Downloads screen shows a real
     * percentage and a finished download flips to "Completed" on its own.
     */
    private fun watchDownloads() {
        viewModelScope.launch {
            val finishedStatuses = mutableMapOf<Long, String>()
            while (true) {
                kotlinx.coroutines.delay(1500)
                val active = runCatching { repository.getActiveDownloads() }.getOrDefault(emptyList())
                if (active.isEmpty()) {
                    if (_downloadProgress.value.isNotEmpty()) _downloadProgress.value = emptyMap()
                    continue
                }
                val dm = downloadManager ?: continue
                val progress = mutableMapOf<Long, Float>()
                active.forEach { row ->
                    runCatching {
                        dm.query(DownloadManager.Query().setFilterById(row.managerId)).use { cursor ->
                            if (cursor == null || !cursor.moveToFirst()) return@use
                            val total = cursor.getLong(
                                cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                            )
                            val done = cursor.getLong(
                                cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                            )
                            val statusCode = cursor.getInt(
                                cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)
                            )
                            progress[row.id] =
                                if (total > 0) (done.toFloat() / total).coerceIn(0f, 1f) else 0f
                            when (statusCode) {
                                DownloadManager.STATUS_SUCCESSFUL -> {
                                    if (finishedStatuses[row.id] != "Completed") {
                                        finishedStatuses[row.id] = "Completed"
                                        viewModelScope.launch {
                                            repository.updateDownloadStatus(
                                                row.id,
                                                "Completed",
                                                if (total > 0) total else done,
                                                row.localPath
                                            )
                                            showSnackbar("Downloaded ${row.fileName}")
                                        }
                                    }
                                }
                                DownloadManager.STATUS_FAILED -> {
                                    if (finishedStatuses[row.id] != "Failed") {
                                        finishedStatuses[row.id] = "Failed"
                                        viewModelScope.launch {
                                            repository.updateDownloadStatus(row.id, "Failed", done, row.localPath)
                                            showSnackbar("Download failed: ${row.fileName}")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                _downloadProgress.value = progress
            }
        }
    }

    fun cancelDownload(id: Long) {
        viewModelScope.launch {
            val row = repository.getDownloadById(id) ?: return@launch
            if (row.managerId >= 0) runCatching { downloadManager?.remove(row.managerId) }
            repository.deleteDownload(id)
            _downloadProgress.update { it - id }
            showSnackbar("Download cancelled")
        }
    }

    fun retryDownload(id: Long) {
        viewModelScope.launch {
            val row = repository.getDownloadById(id) ?: return@launch
            val dm = downloadManager ?: return@launch
            val newId = runCatching {
                dm.enqueue(
                    DownloadManager.Request(Uri.parse(row.url)).apply {
                        setMimeType(row.mimeType)
                        setTitle(row.fileName)
                        setDescription("Downloading with Bansify Browser")
                        setNotificationVisibility(
                            DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                        )
                        setDestinationInExternalPublicDir(
                            Environment.DIRECTORY_DOWNLOADS,
                            row.fileName
                        )
                    }
                )
            }.getOrNull()
            if (newId == null) {
                showSnackbar("Could not restart this download")
                return@launch
            }
            repository.updateDownloadManagerId(id, newId, "Downloading")
            showSnackbar("Retrying ${row.fileName}")
        }
    }

    fun openDownloadedFile(id: Long) {
        viewModelScope.launch {
            val row = repository.getDownloadById(id) ?: return@launch
            val file = File(row.localPath)
            if (row.localPath.isBlank() || !file.exists()) {
                showSnackbar("This file is no longer on the device")
                return@launch
            }
            val app = getApplication<Application>()
            val uri = runCatching {
                FileProvider.getUriForFile(app, "${app.packageName}.fileprovider", file)
            }.getOrNull()
            if (uri == null) {
                showSnackbar("Cannot open this file")
                return@launch
            }
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, row.mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            runCatching { app.startActivity(intent) }
                .onFailure { showSnackbar("No app can open this file") }
        }
    }

    fun shareDownloadedFile(id: Long) {
        viewModelScope.launch {
            val row = repository.getDownloadById(id) ?: return@launch
            val app = getApplication<Application>()
            val file = File(row.localPath)
            val uri = if (row.localPath.isNotBlank() && file.exists()) {
                runCatching {
                    FileProvider.getUriForFile(app, "${app.packageName}.fileprovider", file)
                }.getOrNull()
            } else {
                null
            }
            val intent = if (uri != null) {
                Intent(Intent.ACTION_SEND).apply {
                    type = row.mimeType
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_TEXT, row.fileName)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            } else {
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, row.url)
                }
            }
            runCatching {
                app.startActivity(
                    Intent.createChooser(intent, "Share").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }.onFailure { showSnackbar("Nothing to share with") }
        }
    }

    // --- Overlays & UI States ---

    fun openOverlay(overlay: BrowserOverlay) {
        _overlay.value = overlay
    }

    fun closeOverlay() {
        _overlay.value = BrowserOverlay.None
    }

    fun navigateSettingsSubScreen(subScreen: SettingsSubScreen) {
        _overlay.value = BrowserOverlay.Settings(subScreen)
    }

    fun navigateSettingsBack() {
        val current = _overlay.value
        if (current is BrowserOverlay.Settings) {
            when (current.subScreen) {
                SettingsSubScreen.MAIN -> _overlay.value = BrowserOverlay.None
                SettingsSubScreen.SAFE_BROWSING,
                SettingsSubScreen.THIRD_PARTY_COOKIES -> {
                    _overlay.value = BrowserOverlay.Settings(SettingsSubScreen.PRIVACY_SECURITY)
                }
                SettingsSubScreen.THEME,
                SettingsSubScreen.TOOLBAR_SHORTCUT -> {
                    _overlay.value = BrowserOverlay.Settings(SettingsSubScreen.APPEARANCE)
                }
                else -> _overlay.value = BrowserOverlay.Settings(SettingsSubScreen.MAIN)
            }
        } else {
            _overlay.value = BrowserOverlay.None
        }
    }

    fun setPageInfoVisible(visible: Boolean) {
        _showPageInfoSheet.value = visible
    }

    fun setFindInPageActive(active: Boolean) {
        _findInPage.value = if (active) FindInPageState(isActive = true) else FindInPageState()
    }

    fun updateFindQuery(query: String) {
        _findInPage.update { it.copy(query = query) }
    }

    fun updateFindResults(activeOrdinal: Int, total: Int) {
        _findInPage.update { it.copy(activeMatchOrdinal = activeOrdinal, numberOfMatches = total) }
    }

    fun showJsDialog(dialog: JsDialogState?) {
        _jsDialog.value = dialog
    }

    fun showSnackbar(message: String, actionLabel: String? = null, onAction: (() -> Unit)? = null) {
        _snackbarEvents.tryEmit(SnackbarEvent(message, actionLabel, onAction))
    }

    // --- Bookmarks ---

    fun toggleBookmarkForActiveTab() {
        val tab = activeTab.value
        if (tab.isNewTab) return
        viewModelScope.launch {
            val existing = repository.getBookmarkByUrl(tab.url)
            if (existing != null) {
                repository.deleteBookmark(existing.id)
                showSnackbar("Bookmark removed")
            } else {
                val id = repository.addBookmark(tab.title, tab.url)
                val inserted = BookmarkEntity(id = id, title = tab.title, url = tab.url)
                showSnackbar(
                    message = "Bookmarked",
                    actionLabel = "Edit",
                    onAction = { _editingBookmark.value = inserted }
                )
            }
        }
    }

    fun startEditingBookmark(bookmark: BookmarkEntity?) {
        _editingBookmark.value = bookmark
    }

    fun saveBookmark(id: Long, title: String, url: String, folder: String) {
        viewModelScope.launch {
            if (id == 0L) {
                repository.addBookmark(title, url, folder)
                showSnackbar("Bookmark added")
            } else {
                repository.updateBookmark(BookmarkEntity(id = id, title = title, url = url, folder = folder))
                showSnackbar("Bookmark updated")
            }
            _editingBookmark.value = null
        }
    }

    fun deleteBookmark(id: Long) {
        viewModelScope.launch {
            repository.deleteBookmark(id)
            showSnackbar("Bookmark deleted")
        }
    }

    // --- History & Downloads & Shortcuts ---

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch {
            repository.deleteHistoryItem(id)
        }
    }

    fun deleteDownload(id: Long) {
        viewModelScope.launch {
            repository.deleteDownload(id)
            showSnackbar("Download removed")
        }
    }

    fun addShortcut(title: String, url: String) {
        val normalized = if (url.startsWith("http://") || url.startsWith("https://")) url else "https://$url"
        viewModelScope.launch {
            repository.addShortcut(
                title = title.ifBlank { UrlUtils.extractDisplayHost(normalized) },
                url = normalized,
                accentColor = UrlUtils.colorForDomain(normalized)
            )
            showSnackbar("Shortcut added")
        }
    }

    fun updateShortcut(shortcut: ShortcutEntity) {
        viewModelScope.launch {
            repository.updateShortcut(shortcut)
            showSnackbar("Shortcut updated")
        }
    }

    fun deleteShortcut(id: Long) {
        viewModelScope.launch {
            repository.deleteShortcut(id)
            showSnackbar("Shortcut removed")
        }
    }

    // --- Settings & Clear Browsing Data ---

    fun updateSettings(transform: (BrowserSettings) -> BrowserSettings) {
        viewModelScope.launch {
            repository.updateSettings(transform)
        }
    }

    fun toggleIncognitoBlockThirdPartyCookies(block: Boolean) {
        updateSettings {
            it.copy(
                thirdPartyCookiePolicy = if (block) {
                    ThirdPartyCookiePolicy.BLOCK_INCOGNITO
                } else {
                    ThirdPartyCookiePolicy.ALLOW
                }
            )
        }
    }

    fun clearBrowsingData(
        timeRange: TimeRange,
        clearHistory: Boolean,
        clearCookies: Boolean,
        clearCache: Boolean,
        clearSiteData: Boolean,
        closeOpenTabs: Boolean,
        onClearWebViewCache: () -> Unit
    ) {
        viewModelScope.launch {
            if (clearHistory) {
                repository.clearHistory(timeRange)
                _recentSearches.value = emptyList()
            }
            if (clearCookies) {
                CookieManager.getInstance().removeAllCookies(null)
                CookieManager.getInstance().flush()
            }
            if (clearCache) {
                onClearWebViewCache()
            }
            if (clearSiteData) {
                WebStorage.getInstance().deleteAllData()
            }
            if (closeOpenTabs) {
                closeAllTabs(incognitoOnly = false)
            }
            _overlay.value = BrowserOverlay.None
            showSnackbar("Cleared Bansify browsing data")
        }
    }

    fun clearSiteDataForActiveSite(onClearWebViewCache: () -> Unit) {
        CookieManager.getInstance().removeAllCookies(null)
        CookieManager.getInstance().flush()
        WebStorage.getInstance().deleteAllData()
        onClearWebViewCache()
        _showPageInfoSheet.value = false
        showSnackbar("Cookies and site data cleared")
    }

    // --- Bansify Account & Sync ---

    fun signInBansifyAccount(displayName: String, email: String, avatarColorHex: Long = 0xFF1A73E8L) {
        viewModelScope.launch {
            accountProvider.signIn(displayName, email, avatarColorHex)
            showSnackbar("Signed in to Bansify Account (Demo)")
        }
    }

    fun updateBansifyProfile(displayName: String, email: String, avatarColorHex: Long) {
        viewModelScope.launch {
            accountProvider.updateProfile(displayName, email, avatarColorHex)
            showSnackbar("Bansify profile updated")
        }
    }

    fun signOutBansifyAccount() {
        viewModelScope.launch {
            accountProvider.signOut()
            showSnackbar("Signed out of Bansify Account")
        }
    }

    fun updateBansifySync(transform: (BansifyAccountState) -> BansifyAccountState) {
        viewModelScope.launch {
            accountProvider.updateSyncPreferences(transform)
        }
    }

    fun triggerManualSync() {
        if (!accountState.value.isSignedIn || _isSyncing.value) return
        viewModelScope.launch {
            _isSyncing.value = true
            try {
                accountProvider.performSync()
                val activeCount = accountState.value.activeSyncCategoriesCount
                showSnackbar("Bansify sync complete ($activeCount categories synced)")
            } finally {
                _isSyncing.value = false
            }
        }
    }

    private fun updateTab(tabId: String, transform: (BrowserTab) -> BrowserTab) {
        _tabs.update { list ->
            list.map { if (it.id == tabId) transform(it) else it }
        }
    }

    private fun persistNormalTabs() {
        val current = _tabs.value
        viewModelScope.launch {
            repository.persistOpenTabs(current)
        }
    }
}

/** A main-frame load failure shown over the page instead of a blank screen. */
data class PageError(
    val tabId: String,
    val url: String,
    val message: String,
    val isSslError: Boolean
)

/** A permission request waiting for the user's answer. */
data class PendingSitePermission(
    val tabId: String,
    val origin: String,
    val permission: SitePermission,
    val onResult: (Boolean) -> Unit
)

/** Safe Browsing flagged a page; the user decides whether to continue. */
data class SafeBrowsingWarning(
    val tabId: String,
    val url: String,
    val threatType: Int,
    val response: SafeBrowsingResponse
) {
    val label: String
        get() = when (threatType) {
            2 -> "Phishing page"
            3 -> "Page hosting unwanted software"
            4 -> "Page with harmful software"
            5 -> "Unwanted software"
            else -> "Dangerous page"
        }
}
