package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.browserDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "bansify_browser_settings"
)

class BrowserPreferences(private val context: Context) {

    private object Keys {
        val SEARCH_ENGINE = stringPreferencesKey("search_engine")
        val ADDRESS_BAR_POSITION = stringPreferencesKey("address_bar_position")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val HOMEPAGE_ENABLED = booleanPreferencesKey("homepage_enabled")
        val USE_CUSTOM_HOMEPAGE = booleanPreferencesKey("use_custom_homepage")
        val CUSTOM_HOMEPAGE_URL = stringPreferencesKey("custom_homepage_url")
        val TOOLBAR_SHORTCUT = stringPreferencesKey("toolbar_shortcut")
        val OPEN_EXTERNAL_NEW_TAB = booleanPreferencesKey("open_external_new_tab")
        val AUTO_CLOSE_TABS_DAYS = intPreferencesKey("auto_close_tabs_days")
        val SAFE_BROWSING = stringPreferencesKey("safe_browsing")
        val HTTPS_FIRST = booleanPreferencesKey("https_first")
        val DO_NOT_TRACK = booleanPreferencesKey("do_not_track")
        val THIRD_PARTY_COOKIES = stringPreferencesKey("third_party_cookies")
        val LOCK_INCOGNITO_ON_LEAVE = booleanPreferencesKey("lock_incognito_on_leave")
        val SAVE_FORM_DATA = booleanPreferencesKey("save_form_data")
        val AUTOFILL_ADDRESSES = booleanPreferencesKey("autofill_addresses")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val DOWNLOAD_NOTIFICATIONS = booleanPreferencesKey("download_notifications")
        val PREFERRED_LANGUAGE = stringPreferencesKey("preferred_language")
        val OFFER_TRANSLATE = booleanPreferencesKey("offer_translate")
        val JAVASCRIPT_ENABLED = booleanPreferencesKey("javascript_enabled")
        val COOKIES_ENABLED = booleanPreferencesKey("cookies_enabled")
        val POPUPS_ALLOWED = booleanPreferencesKey("popups_allowed")
        val LOAD_IMAGES = booleanPreferencesKey("load_images")
        val DEFAULT_DESKTOP_SITE = booleanPreferencesKey("default_desktop_site")
        val MEDIA_AUTOPLAY = booleanPreferencesKey("media_autoplay")
        val TEXT_ZOOM_PERCENT = intPreferencesKey("text_zoom_percent")
        val FORCE_ENABLE_ZOOM = booleanPreferencesKey("force_enable_zoom")
        val SIMPLIFIED_VIEW = booleanPreferencesKey("simplified_view")
        val ASK_WHERE_TO_SAVE = booleanPreferencesKey("ask_where_to_save")
        val SHOW_RECENT_CARDS = booleanPreferencesKey("show_recent_cards")
        val SHORTCUTS_SEEDED = booleanPreferencesKey("shortcuts_seeded")
    }

    private fun readSettings(prefs: Preferences): BrowserSettings = BrowserSettings(
        searchEngine = prefs[Keys.SEARCH_ENGINE]
            ?.let { runCatching { SearchEngine.valueOf(it) }.getOrNull() }
            ?: SearchEngine.GOOGLE,
        addressBarPosition = prefs[Keys.ADDRESS_BAR_POSITION]
            ?.let { runCatching { AddressBarPosition.valueOf(it) }.getOrNull() }
            ?: AddressBarPosition.TOP,
        themeMode = prefs[Keys.THEME_MODE]
            ?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
            ?: ThemeMode.SYSTEM,
        homepageEnabled = prefs[Keys.HOMEPAGE_ENABLED] ?: true,
        useCustomHomepage = prefs[Keys.USE_CUSTOM_HOMEPAGE] ?: false,
        customHomepageUrl = prefs[Keys.CUSTOM_HOMEPAGE_URL] ?: "https://www.google.com",
        toolbarShortcut = prefs[Keys.TOOLBAR_SHORTCUT]
            ?.let { runCatching { ToolbarShortcut.valueOf(it) }.getOrNull() }
            ?: ToolbarShortcut.NEW_TAB,
        openExternalLinksInNewTab = prefs[Keys.OPEN_EXTERNAL_NEW_TAB] ?: true,
        autoCloseTabsDays = prefs[Keys.AUTO_CLOSE_TABS_DAYS] ?: 0,
        safeBrowsingLevel = prefs[Keys.SAFE_BROWSING]
            ?.let { runCatching { SafeBrowsingLevel.valueOf(it) }.getOrNull() }
            ?: SafeBrowsingLevel.STANDARD,
        httpsFirstEnabled = prefs[Keys.HTTPS_FIRST] ?: true,
        doNotTrackEnabled = prefs[Keys.DO_NOT_TRACK] ?: false,
        thirdPartyCookiePolicy = prefs[Keys.THIRD_PARTY_COOKIES]
            ?.let { runCatching { ThirdPartyCookiePolicy.valueOf(it) }.getOrNull() }
            ?: ThirdPartyCookiePolicy.BLOCK_INCOGNITO,
        lockIncognitoOnLeave = prefs[Keys.LOCK_INCOGNITO_ON_LEAVE] ?: false,
        saveFormDataEnabled = prefs[Keys.SAVE_FORM_DATA] ?: true,
        autofillAddressesEnabled = prefs[Keys.AUTOFILL_ADDRESSES] ?: true,
        notificationsEnabled = prefs[Keys.NOTIFICATIONS_ENABLED] ?: true,
        downloadNotificationsEnabled = prefs[Keys.DOWNLOAD_NOTIFICATIONS] ?: true,
        preferredLanguage = prefs[Keys.PREFERRED_LANGUAGE] ?: "English (United States)",
        offerTranslatePages = prefs[Keys.OFFER_TRANSLATE] ?: true,
        javascriptEnabled = prefs[Keys.JAVASCRIPT_ENABLED] ?: true,
        cookiesEnabled = prefs[Keys.COOKIES_ENABLED] ?: true,
        popupsAllowed = prefs[Keys.POPUPS_ALLOWED] ?: false,
        loadImagesEnabled = prefs[Keys.LOAD_IMAGES] ?: true,
        defaultDesktopSite = prefs[Keys.DEFAULT_DESKTOP_SITE] ?: false,
        mediaAutoplayEnabled = prefs[Keys.MEDIA_AUTOPLAY] ?: true,
        textZoomPercent = prefs[Keys.TEXT_ZOOM_PERCENT] ?: 100,
        forceEnableZoom = prefs[Keys.FORCE_ENABLE_ZOOM] ?: false,
        simplifiedViewEnabled = prefs[Keys.SIMPLIFIED_VIEW] ?: false,
        askWhereToSaveDownloads = prefs[Keys.ASK_WHERE_TO_SAVE] ?: false,
        showRecentCardsOnNtp = prefs[Keys.SHOW_RECENT_CARDS] ?: true
    )

    val settingsFlow: Flow<BrowserSettings> = context.browserDataStore.data.map(::readSettings)

    val shortcutsSeededFlow: Flow<Boolean> = context.browserDataStore.data.map { prefs ->
        prefs[Keys.SHORTCUTS_SEEDED] ?: false
    }

    suspend fun markShortcutsSeeded() {
        context.browserDataStore.edit { it[Keys.SHORTCUTS_SEEDED] = true }
    }

    suspend fun updateSettings(transform: (BrowserSettings) -> BrowserSettings) {
        context.browserDataStore.edit { prefs ->
            val updated = transform(readSettings(prefs))
            prefs[Keys.SEARCH_ENGINE] = updated.searchEngine.name
            prefs[Keys.ADDRESS_BAR_POSITION] = updated.addressBarPosition.name
            prefs[Keys.THEME_MODE] = updated.themeMode.name
            prefs[Keys.HOMEPAGE_ENABLED] = updated.homepageEnabled
            prefs[Keys.USE_CUSTOM_HOMEPAGE] = updated.useCustomHomepage
            prefs[Keys.CUSTOM_HOMEPAGE_URL] = updated.customHomepageUrl
            prefs[Keys.TOOLBAR_SHORTCUT] = updated.toolbarShortcut.name
            prefs[Keys.OPEN_EXTERNAL_NEW_TAB] = updated.openExternalLinksInNewTab
            prefs[Keys.AUTO_CLOSE_TABS_DAYS] = updated.autoCloseTabsDays
            prefs[Keys.SAFE_BROWSING] = updated.safeBrowsingLevel.name
            prefs[Keys.HTTPS_FIRST] = updated.httpsFirstEnabled
            prefs[Keys.DO_NOT_TRACK] = updated.doNotTrackEnabled
            prefs[Keys.THIRD_PARTY_COOKIES] = updated.thirdPartyCookiePolicy.name
            prefs[Keys.LOCK_INCOGNITO_ON_LEAVE] = updated.lockIncognitoOnLeave
            prefs[Keys.SAVE_FORM_DATA] = updated.saveFormDataEnabled
            prefs[Keys.AUTOFILL_ADDRESSES] = updated.autofillAddressesEnabled
            prefs[Keys.NOTIFICATIONS_ENABLED] = updated.notificationsEnabled
            prefs[Keys.DOWNLOAD_NOTIFICATIONS] = updated.downloadNotificationsEnabled
            prefs[Keys.PREFERRED_LANGUAGE] = updated.preferredLanguage
            prefs[Keys.OFFER_TRANSLATE] = updated.offerTranslatePages
            prefs[Keys.JAVASCRIPT_ENABLED] = updated.javascriptEnabled
            prefs[Keys.COOKIES_ENABLED] = updated.cookiesEnabled
            prefs[Keys.POPUPS_ALLOWED] = updated.popupsAllowed
            prefs[Keys.LOAD_IMAGES] = updated.loadImagesEnabled
            prefs[Keys.DEFAULT_DESKTOP_SITE] = updated.defaultDesktopSite
            prefs[Keys.MEDIA_AUTOPLAY] = updated.mediaAutoplayEnabled
            prefs[Keys.TEXT_ZOOM_PERCENT] = updated.textZoomPercent
            prefs[Keys.FORCE_ENABLE_ZOOM] = updated.forceEnableZoom
            prefs[Keys.SIMPLIFIED_VIEW] = updated.simplifiedViewEnabled
            prefs[Keys.ASK_WHERE_TO_SAVE] = updated.askWhereToSaveDownloads
            prefs[Keys.SHOW_RECENT_CARDS] = updated.showRecentCardsOnNtp
        }
    }
}
