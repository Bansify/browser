package com.bansify.browser.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

private val Context.bansifyAccountDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "bansify_account_store"
)

data class BansifyAccountState(
    val isSignedIn: Boolean = false,
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val avatarColorHex: Long = 0xFF1A73E8L,
    val syncEverything: Boolean = true,
    val syncBookmarks: Boolean = true,
    val syncHistory: Boolean = true,
    val syncOpenTabs: Boolean = true,
    val syncShortcuts: Boolean = true,
    val syncSettings: Boolean = true,
    val syncDownloadsList: Boolean = false,
    val autocompleteSearches: Boolean = true,
    val improveSearchSuggestions: Boolean = true,
    val usageReportsEnabled: Boolean = false,
    val lastSyncedAt: Long = 0L,
    val isSyncing: Boolean = false
) {
    val effectiveBookmarksSync: Boolean get() = isSignedIn && (syncEverything || syncBookmarks)
    val effectiveHistorySync: Boolean get() = isSignedIn && (syncEverything || syncHistory)
    val effectiveTabsSync: Boolean get() = isSignedIn && (syncEverything || syncOpenTabs)
    val effectiveShortcutsSync: Boolean get() = isSignedIn && (syncEverything || syncShortcuts)
    val effectiveSettingsSync: Boolean get() = isSignedIn && (syncEverything || syncSettings)
    val effectiveDownloadsSync: Boolean get() = isSignedIn && (syncEverything || syncDownloadsList)

    val activeSyncCategoriesCount: Int
        get() = listOf(
            effectiveBookmarksSync,
            effectiveHistorySync,
            effectiveTabsSync,
            effectiveShortcutsSync,
            effectiveSettingsSync,
            effectiveDownloadsSync
        ).count { it }

    val initialLetter: String
        get() = displayName.firstOrNull { it.isLetterOrDigit() }?.uppercaseChar()?.toString()
            ?: email.firstOrNull { it.isLetterOrDigit() }?.uppercaseChar()?.toString()
            ?: "B"
}

/**
 * Clean contract for Bansify Account authentication & cloud sync.
 * Replace DemoBansifyAccountProvider with your real Bansify API client later
 * without changing any UI or ViewModel code.
 */
interface BansifyAccountProvider {
    val accountState: Flow<BansifyAccountState>
    suspend fun signIn(displayName: String, email: String, avatarColorHex: Long = 0xFF1A73E8L)
    suspend fun updateProfile(displayName: String, email: String, avatarColorHex: Long)
    suspend fun signOut()
    suspend fun updateSyncPreferences(transform: (BansifyAccountState) -> BansifyAccountState)
    suspend fun performSync(): Long
}

/**
 * Local demo implementation backed by Jetpack DataStore.
 * Preserves sign-in state, profile, granular sync preferences, and last sync timestamp.
 */
class DemoBansifyAccountProvider(
    private val context: Context
) : BansifyAccountProvider {

    private object Keys {
        val IS_SIGNED_IN = booleanPreferencesKey("is_signed_in")
        val USER_ID = stringPreferencesKey("user_id")
        val DISPLAY_NAME = stringPreferencesKey("display_name")
        val EMAIL = stringPreferencesKey("email")
        val AVATAR_COLOR = longPreferencesKey("avatar_color")
        val SYNC_EVERYTHING = booleanPreferencesKey("sync_everything")
        val SYNC_BOOKMARKS = booleanPreferencesKey("sync_bookmarks")
        val SYNC_HISTORY = booleanPreferencesKey("sync_history")
        val SYNC_OPEN_TABS = booleanPreferencesKey("sync_open_tabs")
        val SYNC_SHORTCUTS = booleanPreferencesKey("sync_shortcuts")
        val SYNC_SETTINGS = booleanPreferencesKey("sync_settings")
        val SYNC_DOWNLOADS = booleanPreferencesKey("sync_downloads")
        val AUTOCOMPLETE_SEARCHES = booleanPreferencesKey("autocomplete_searches")
        val IMPROVE_SUGGESTIONS = booleanPreferencesKey("improve_suggestions")
        val USAGE_REPORTS = booleanPreferencesKey("usage_reports")
        val LAST_SYNCED_AT = longPreferencesKey("last_synced_at")
    }

    private fun readState(prefs: Preferences): BansifyAccountState = BansifyAccountState(
        isSignedIn = prefs[Keys.IS_SIGNED_IN] ?: false,
        userId = prefs[Keys.USER_ID] ?: "",
        displayName = prefs[Keys.DISPLAY_NAME] ?: "",
        email = prefs[Keys.EMAIL] ?: "",
        avatarColorHex = prefs[Keys.AVATAR_COLOR] ?: 0xFF1A73E8L,
        syncEverything = prefs[Keys.SYNC_EVERYTHING] ?: true,
        syncBookmarks = prefs[Keys.SYNC_BOOKMARKS] ?: true,
        syncHistory = prefs[Keys.SYNC_HISTORY] ?: true,
        syncOpenTabs = prefs[Keys.SYNC_OPEN_TABS] ?: true,
        syncShortcuts = prefs[Keys.SYNC_SHORTCUTS] ?: true,
        syncSettings = prefs[Keys.SYNC_SETTINGS] ?: true,
        syncDownloadsList = prefs[Keys.SYNC_DOWNLOADS] ?: false,
        autocompleteSearches = prefs[Keys.AUTOCOMPLETE_SEARCHES] ?: true,
        improveSearchSuggestions = prefs[Keys.IMPROVE_SUGGESTIONS] ?: true,
        usageReportsEnabled = prefs[Keys.USAGE_REPORTS] ?: false,
        lastSyncedAt = prefs[Keys.LAST_SYNCED_AT] ?: 0L
    )

    override val accountState: Flow<BansifyAccountState> =
        context.bansifyAccountDataStore.data.map(::readState)

    override suspend fun signIn(displayName: String, email: String, avatarColorHex: Long) {
        val now = System.currentTimeMillis()
        context.bansifyAccountDataStore.edit { prefs ->
            prefs[Keys.IS_SIGNED_IN] = true
            prefs[Keys.USER_ID] = "bansify_${UUID.randomUUID().toString().take(8)}"
            prefs[Keys.DISPLAY_NAME] = displayName.trim().ifBlank { "Bansify User" }
            prefs[Keys.EMAIL] = email.trim().ifBlank { "user@bansify.com" }
            prefs[Keys.AVATAR_COLOR] = avatarColorHex
            prefs[Keys.LAST_SYNCED_AT] = now
        }
    }

    override suspend fun updateProfile(displayName: String, email: String, avatarColorHex: Long) {
        context.bansifyAccountDataStore.edit { prefs ->
            prefs[Keys.DISPLAY_NAME] = displayName.trim().ifBlank { "Bansify User" }
            prefs[Keys.EMAIL] = email.trim().ifBlank { "user@bansify.com" }
            prefs[Keys.AVATAR_COLOR] = avatarColorHex
        }
    }

    override suspend fun signOut() {
        context.bansifyAccountDataStore.edit { prefs ->
            prefs[Keys.IS_SIGNED_IN] = false
            prefs[Keys.USER_ID] = ""
            prefs[Keys.DISPLAY_NAME] = ""
            prefs[Keys.EMAIL] = ""
            prefs[Keys.LAST_SYNCED_AT] = 0L
        }
    }

    override suspend fun updateSyncPreferences(transform: (BansifyAccountState) -> BansifyAccountState) {
        context.bansifyAccountDataStore.edit { prefs ->
            val updated = transform(readState(prefs))
            prefs[Keys.SYNC_EVERYTHING] = updated.syncEverything
            prefs[Keys.SYNC_BOOKMARKS] = updated.syncBookmarks
            prefs[Keys.SYNC_HISTORY] = updated.syncHistory
            prefs[Keys.SYNC_OPEN_TABS] = updated.syncOpenTabs
            prefs[Keys.SYNC_SHORTCUTS] = updated.syncShortcuts
            prefs[Keys.SYNC_SETTINGS] = updated.syncSettings
            prefs[Keys.SYNC_DOWNLOADS] = updated.syncDownloadsList
            prefs[Keys.AUTOCOMPLETE_SEARCHES] = updated.autocompleteSearches
            prefs[Keys.IMPROVE_SUGGESTIONS] = updated.improveSearchSuggestions
            prefs[Keys.USAGE_REPORTS] = updated.usageReportsEnabled
        }
    }

    override suspend fun performSync(): Long {
        // Simulated network round-trip — replace with real Bansify Sync REST call later
        delay(550L)
        val now = System.currentTimeMillis()
        context.bansifyAccountDataStore.edit { prefs ->
            prefs[Keys.LAST_SYNCED_AT] = now
        }
        return now
    }
}
