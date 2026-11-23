package com.bansify.browser.data

import android.graphics.Bitmap
import android.webkit.JsPromptResult
import android.webkit.JsResult
import java.util.UUID

enum class SearchEngine(
    val displayName: String,
    val domain: String,
    val queryUrlTemplate: String
) {
    GOOGLE("Google", "google.com", "https://www.google.com/search?q=%s"),
    DUCKDUCKGO("DuckDuckGo", "duckduckgo.com", "https://duckduckgo.com/?q=%s"),
    BING("Bing", "bing.com", "https://www.bing.com/search?q=%s"),
    YAHOO("Yahoo!", "search.yahoo.com", "https://search.yahoo.com/search?p=%s"),
    BRAVE("Brave Search", "search.brave.com", "https://search.brave.com/search?q=%s"),
    ECOSIA("Ecosia", "ecosia.org", "https://www.ecosia.org/search?q=%s")
}

enum class ThemeMode(val label: String) {
    SYSTEM("System default"),
    LIGHT("Light"),
    DARK("Dark")
}

enum class AddressBarPosition(val label: String, val description: String) {
    TOP("Top", "Show address bar at the top of the screen"),
    BOTTOM("Bottom", "Move address bar to the bottom for easier one-handed reach")
}

enum class SafeBrowsingLevel(val label: String, val summary: String) {
    ENHANCED(
        "Enhanced protection",
        "Faster, proactive protection against dangerous websites, downloads, and extensions"
    ),
    STANDARD(
        "Standard protection",
        "Standard protection against websites, downloads, and extensions known to be dangerous"
    ),
    NONE(
        "No protection (not recommended)",
        "Does not protect you against dangerous websites, downloads, and extensions"
    )
}

enum class ThirdPartyCookiePolicy(val label: String, val summary: String) {
    ALLOW("Allow third-party cookies", "Sites can use cookies to improve your browsing experience"),
    BLOCK_INCOGNITO(
        "Block third-party cookies in Incognito",
        "Features on some sites may break in Incognito mode"
    ),
    BLOCK_ALL(
        "Block third-party cookies",
        "Sites can't use cookies that track you across the web"
    )
}

enum class ToolbarShortcut(val label: String) {
    NEW_TAB("New tab"),
    SHARE("Share"),
    BOOKMARK("Bookmark"),
    NONE("None")
}

enum class TimeRange(val label: String, val durationMillis: Long) {
    LAST_15_MIN("Last 15 minutes", 15L * 60L * 1000L),
    LAST_HOUR("Last hour", 60L * 60L * 1000L),
    LAST_24_HOURS("Last 24 hours", 24L * 60L * 60L * 1000L),
    LAST_7_DAYS("Last 7 days", 7L * 24L * 60L * 60L * 1000L),
    LAST_4_WEEKS("Last 4 weeks", 28L * 24L * 60L * 60L * 1000L),
    ALL_TIME("All time", Long.MAX_VALUE)
}

data class BrowserSettings(
    val searchEngine: SearchEngine = SearchEngine.GOOGLE,
    val addressBarPosition: AddressBarPosition = AddressBarPosition.TOP,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val homepageEnabled: Boolean = true,
    val useCustomHomepage: Boolean = false,
    val customHomepageUrl: String = "https://www.google.com",
    val toolbarShortcut: ToolbarShortcut = ToolbarShortcut.NEW_TAB,
    val openExternalLinksInNewTab: Boolean = true,
    val autoCloseTabsDays: Int = 0,
    val safeBrowsingLevel: SafeBrowsingLevel = SafeBrowsingLevel.STANDARD,
    val httpsFirstEnabled: Boolean = true,
    val doNotTrackEnabled: Boolean = false,
    val thirdPartyCookiePolicy: ThirdPartyCookiePolicy = ThirdPartyCookiePolicy.BLOCK_INCOGNITO,
    val lockIncognitoOnLeave: Boolean = false,
    val saveFormDataEnabled: Boolean = true,
    val autofillAddressesEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val downloadNotificationsEnabled: Boolean = true,
    val preferredLanguage: String = "English (United States)",
    val offerTranslatePages: Boolean = true,
    val javascriptEnabled: Boolean = true,
    val cookiesEnabled: Boolean = true,
    val popupsAllowed: Boolean = false,
    val loadImagesEnabled: Boolean = true,
    val defaultDesktopSite: Boolean = false,
    val mediaAutoplayEnabled: Boolean = true,
    val textZoomPercent: Int = 100,
    val forceEnableZoom: Boolean = false,
    val simplifiedViewEnabled: Boolean = false,
    val askWhereToSaveDownloads: Boolean = false,
    val showRecentCardsOnNtp: Boolean = true
)

data class BrowserTab(
    val id: String = UUID.randomUUID().toString(),
    val url: String = NEW_TAB_URL,
    val title: String = "New tab",
    val isIncognito: Boolean = false,
    val isDesktopSite: Boolean = false,
    val groupName: String? = null,
    val groupColor: Long? = null,
    val progress: Int = 0,
    val isLoading: Boolean = false,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val isSecure: Boolean = false,
    val favicon: Bitmap? = null,
    val thumbnail: Bitmap? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val lastAccessedAt: Long = System.currentTimeMillis()
) {
    val isNewTab: Boolean
        get() = url.isBlank() || url == NEW_TAB_URL

    companion object {
        const val NEW_TAB_URL = "bansify://newtab"
    }
}

data class ClosedTab(
    val title: String,
    val url: String,
    val closedAt: Long = System.currentTimeMillis()
)

data class ReadingModeContent(
    val title: String,
    val domain: String,
    val url: String,
    val paragraphs: List<String>
)

enum class SettingsSubScreen(val title: String) {
    MAIN("Settings"),
    BANSIFY_ACCOUNT("Bansify Account & sync"),
    BANSIFY_SERVICES("Bansify services"),
    SEARCH_ENGINE("Search engine"),
    ADDRESS_BAR("Address bar"),
    PRIVACY_SECURITY("Privacy and security"),
    SAFE_BROWSING("Safe Browsing"),
    THIRD_PARTY_COOKIES("Third-party cookies"),
    SAFETY_CHECK("Safety check"),
    DEFAULT_BROWSER("Default browser"),
    AUTOFILL("Autofill and passwords"),
    TABS("Tabs and tab groups"),
    HOMEPAGE("Homepage"),
    NOTIFICATIONS("Notifications"),
    APPEARANCE("Appearance"),
    TOOLBAR_SHORTCUT("Toolbar shortcut"),
    THEME("Theme"),
    ACCESSIBILITY("Accessibility"),
    SITE_SETTINGS("Site settings"),
    LANGUAGES("Languages"),
    DOWNLOADS_SETTINGS("Downloads"),
    ABOUT("About Bansify")
}

sealed interface BrowserOverlay {
    data object None : BrowserOverlay
    data class TabSwitcher(val showIncognito: Boolean = false) : BrowserOverlay
    data class Omnibox(val initialQuery: String = "") : BrowserOverlay
    data object Bookmarks : BrowserOverlay
    data object History : BrowserOverlay
    data object Downloads : BrowserOverlay
    data object RecentTabs : BrowserOverlay
    data object DeleteBrowsingData : BrowserOverlay
    data class Settings(val subScreen: SettingsSubScreen = SettingsSubScreen.MAIN) : BrowserOverlay
}

sealed interface JsDialogState {
    data class Alert(val url: String, val message: String, val result: JsResult) : JsDialogState
    data class Confirm(val url: String, val message: String, val result: JsResult) : JsDialogState
    data class Prompt(
        val url: String,
        val message: String,
        val defaultValue: String,
        val result: JsPromptResult
    ) : JsDialogState
}

data class FindInPageState(
    val isActive: Boolean = false,
    val query: String = "",
    val activeMatchOrdinal: Int = 0,
    val numberOfMatches: Int = 0
)

data class SnackbarEvent(
    val message: String,
    val actionLabel: String? = null,
    val onAction: (() -> Unit)? = null
)
