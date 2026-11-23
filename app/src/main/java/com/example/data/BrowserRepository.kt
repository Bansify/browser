package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class BrowserRepository(
    private val dao: BrowserDao,
    private val preferences: BrowserPreferences
) {
    val settings: Flow<BrowserSettings> = preferences.settingsFlow
    val bookmarks: Flow<List<BookmarkEntity>> = dao.getAllBookmarks()
    val history: Flow<List<HistoryEntity>> = dao.getAllHistory()
    val downloads: Flow<List<DownloadEntity>> = dao.getAllDownloads()
    val shortcuts: Flow<List<ShortcutEntity>> = dao.getAllShortcuts()

    suspend fun ensureDefaultShortcuts() {
        val alreadySeeded = preferences.shortcutsSeededFlow.first()
        if (!alreadySeeded && dao.getShortcutCount() == 0) {
            dao.insertShortcuts(
                listOf(
                    ShortcutEntity(
                        title = "Wikipedia",
                        url = "https://www.wikipedia.org",
                        accentColor = 0xFF3366CC,
                        sortOrder = 0
                    ),
                    ShortcutEntity(
                        title = "YouTube",
                        url = "https://m.youtube.com",
                        accentColor = 0xFFEA4335,
                        sortOrder = 1
                    ),
                    ShortcutEntity(
                        title = "GitHub",
                        url = "https://github.com",
                        accentColor = 0xFF24292F,
                        sortOrder = 2
                    ),
                    ShortcutEntity(
                        title = "Reddit",
                        url = "https://www.reddit.com",
                        accentColor = 0xFFFF4500,
                        sortOrder = 3
                    ),
                    ShortcutEntity(
                        title = "News",
                        url = "https://news.google.com",
                        accentColor = 0xFF1A73E8,
                        sortOrder = 4
                    ),
                    ShortcutEntity(
                        title = "MDN Docs",
                        url = "https://developer.mozilla.org",
                        accentColor = 0xFF0060DF,
                        sortOrder = 5
                    ),
                    ShortcutEntity(
                        title = "Weather",
                        url = "https://www.weather.com",
                        accentColor = 0xFF0B8043,
                        sortOrder = 6
                    )
                )
            )
            preferences.markShortcutsSeeded()
        }
    }

    suspend fun updateSettings(transform: (BrowserSettings) -> BrowserSettings) {
        preferences.updateSettings(transform)
    }

    // Bookmarks
    suspend fun getBookmarkByUrl(url: String): BookmarkEntity? = dao.getBookmarkByUrl(url)

    suspend fun addBookmark(title: String, url: String, folder: String = "Mobile bookmarks"): Long {
        return dao.insertBookmark(
            BookmarkEntity(title = title.ifBlank { url }, url = url, folder = folder)
        )
    }

    suspend fun updateBookmark(bookmark: BookmarkEntity) = dao.updateBookmark(bookmark)

    suspend fun deleteBookmark(id: Long) = dao.deleteBookmarkById(id)

    suspend fun deleteBookmarkByUrl(url: String) = dao.deleteBookmarkByUrl(url)

    // History
    suspend fun recordHistory(title: String, url: String) {
        if (url.isBlank() || url == BrowserTab.NEW_TAB_URL || url == "about:blank") return
        dao.insertHistory(
            HistoryEntity(
                title = title.ifBlank { url },
                url = url,
                visitedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteHistoryItem(id: Long) = dao.deleteHistoryById(id)

    suspend fun clearHistory(timeRange: TimeRange) {
        if (timeRange == TimeRange.ALL_TIME) {
            dao.clearAllHistory()
        } else {
            val cutoff = System.currentTimeMillis() - timeRange.durationMillis
            dao.deleteHistorySince(cutoff)
        }
    }

    // Downloads
    suspend fun insertDownload(download: DownloadEntity): Long = dao.insertDownload(download)

    suspend fun updateDownloadStatus(id: Long, status: String, sizeBytes: Long, localPath: String) {
        dao.updateDownloadStatus(id, status, sizeBytes, localPath)
    }

    suspend fun deleteDownload(id: Long) = dao.deleteDownloadById(id)

    suspend fun clearAllDownloads() = dao.clearAllDownloads()

    // Shortcuts
    suspend fun addShortcut(title: String, url: String, accentColor: Long) {
        val nextOrder = dao.getShortcutCount()
        dao.insertShortcut(
            ShortcutEntity(
                title = title,
                url = url,
                accentColor = accentColor,
                sortOrder = nextOrder
            )
        )
    }

    suspend fun updateShortcut(shortcut: ShortcutEntity) = dao.updateShortcut(shortcut)

    suspend fun deleteShortcut(id: Long) = dao.deleteShortcutById(id)

    // Saved Tabs
    suspend fun loadSavedTabs(): List<SavedTabEntity> = dao.getSavedTabsOnce()

    suspend fun persistOpenTabs(tabs: List<BrowserTab>) {
        val normalTabs = tabs.filter { !it.isIncognito }.map {
            SavedTabEntity(
                tabId = it.id,
                url = it.url,
                title = it.title,
                isDesktopSite = it.isDesktopSite,
                lastAccessedAt = it.lastAccessedAt
            )
        }
        dao.clearSavedTabs()
        if (normalTabs.isNotEmpty()) {
            dao.saveTabs(normalTabs)
        }
    }
}
