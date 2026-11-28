package com.bansify.browser.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val url: String,
    val folder: String = "Mobile bookmarks",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val url: String,
    val visitedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fileName: String,
    val url: String,
    val mimeType: String = "application/octet-stream",
    val sizeBytes: Long = 0L,
    val category: String = "Other", // Pages, Images, Documents, Other
    val status: String = "Completed", // Downloading, Completed, Failed
    val downloadedAt: Long = System.currentTimeMillis(),
    val localPath: String = "",
    /** DownloadManager id, so progress, cancel and retry can act on the real download. */
    val managerId: Long = -1L
)

@Entity(tableName = "shortcuts")
data class ShortcutEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val url: String,
    val accentColor: Long = 0xFF1A73E8,
    val sortOrder: Int = 0
)

@Entity(tableName = "saved_tabs")
data class SavedTabEntity(
    @PrimaryKey val tabId: String,
    val url: String,
    val title: String,
    val isDesktopSite: Boolean = false,
    val lastAccessedAt: Long = System.currentTimeMillis()
)
