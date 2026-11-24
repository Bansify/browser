package com.bansify.browser.ui.screens

import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bansify.browser.R
import com.bansify.browser.data.AddressBarPosition
import com.bansify.browser.data.BrowserSettings
import com.bansify.browser.data.SafeBrowsingLevel
import com.bansify.browser.data.SearchEngine
import com.bansify.browser.data.SettingsSubScreen
import com.bansify.browser.data.SitePermissionDecision
import com.bansify.browser.data.ThemeMode
import com.bansify.browser.data.ThirdPartyCookiePolicy
import com.bansify.browser.data.ToolbarShortcut
import com.bansify.browser.ui.components.ExpressiveGroupedCard
import com.bansify.browser.ui.components.expressiveCanvasColor
import com.bansify.browser.ui.theme.SecureGreen
import com.bansify.browser.ui.theme.SecureGreenDark
import com.bansify.browser.ui.theme.LocalIsDarkBrowserTheme
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSubScreenContent(
    subScreen: SettingsSubScreen,
    settings: BrowserSettings,
    onBack: () -> Unit,
    onNavigateSubScreen: (SettingsSubScreen) -> Unit,
    onOpenDeleteBrowsingData: () -> Unit,
    onUpdateSettings: ((BrowserSettings) -> BrowserSettings) -> Unit,
    adBlockedCount: Long = 0,
    sitePermissionDecisions: List<SitePermissionDecision> = emptyList(),
    onResetAdBlockCount: () -> Unit = {},
    onForgetSitePermission: (String) -> Unit = {},
    onForgetAllSitePermissions: () -> Unit = {}
) {
    val canvasBg = expressiveCanvasColor()
    val context = LocalContext.current

    Scaffold(
        containerColor = canvasBg,
        topBar = {
            TopAppBar(
                title = { Text(subScreen.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = canvasBg)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(canvasBg)
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            when (subScreen) {
                SettingsSubScreen.SEARCH_ENGINE -> {
                    val engines = SearchEngine.entries
                    items(engines.size) { idx ->
                        val engine = engines[idx]
                        ExpressiveRadioCard(
                            index = idx,
                            totalCount = engines.size,
                            title = engine.displayName,
                            subtitle = engine.domain,
                            selected = settings.searchEngine == engine,
                            onSelect = { onUpdateSettings { it.copy(searchEngine = engine) } }
                        )
                    }
                }

                SettingsSubScreen.ADDRESS_BAR -> {
                    item {
                        ExpressiveSettingsSectionTitle("Choose address bar position")
                    }
                    val positions = AddressBarPosition.entries
                    items(positions.size) { idx ->
                        val pos = positions[idx]
                        ExpressiveGroupedCard(index = idx, totalCount = positions.size) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onUpdateSettings { it.copy(addressBarPosition = pos) } }
                                    .padding(horizontal = 16.dp, vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = settings.addressBarPosition == pos,
                                    onClick = { onUpdateSettings { it.copy(addressBarPosition = pos) } }
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = pos.label,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = pos.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                AddressBarMiniPreview(isBottom = pos == AddressBarPosition.BOTTOM)
                            }
                        }
                    }
                }

                SettingsSubScreen.SAFETY_CHECK -> {
                    item {
                        var hasRunCheck by remember { mutableStateOf(true) }
                        val okColor = if (LocalIsDarkBrowserTheme.current) SecureGreenDark else SecureGreen
                        val safeBrowsingOk = settings.safeBrowsingLevel != SafeBrowsingLevel.NONE

                        ExpressiveGroupedCard(index = 0, totalCount = 1) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Bansify can help keep you safe from dangerous sites, insecure connections, and tracking cookies.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Button(
                                    onClick = { hasRunCheck = true },
                                    shape = RoundedCornerShape(24.dp)
                                ) {
                                    Text("Check now")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (hasRunCheck) {
                            SafetyCheckItemCard(
                                index = 0,
                                totalCount = 3,
                                title = "Safe Browsing",
                                subtitle = settings.safeBrowsingLevel.label,
                                isSafe = safeBrowsingOk,
                                safeColor = okColor,
                                onClick = { onNavigateSubScreen(SettingsSubScreen.SAFE_BROWSING) }
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            SafetyCheckItemCard(
                                index = 1,
                                totalCount = 3,
                                title = "Always use secure connections",
                                subtitle = if (settings.httpsFirstEnabled) "HTTPS-First protection is active" else "Tap to enable HTTPS-First mode",
                                isSafe = settings.httpsFirstEnabled,
                                safeColor = okColor,
                                onClick = { onNavigateSubScreen(SettingsSubScreen.PRIVACY_SECURITY) }
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            SafetyCheckItemCard(
                                index = 2,
                                totalCount = 3,
                                title = "Third-party cookies",
                                subtitle = settings.thirdPartyCookiePolicy.label,
                                isSafe = settings.thirdPartyCookiePolicy != ThirdPartyCookiePolicy.ALLOW,
                                safeColor = okColor,
                                onClick = { onNavigateSubScreen(SettingsSubScreen.THIRD_PARTY_COOKIES) }
                            )
                        }
                    }
                }

                SettingsSubScreen.DEFAULT_BROWSER -> {
                    item {
                        ExpressiveGroupedCard(index = 0, totalCount = 1) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Text(
                                    text = "Make Bansify Browser your default",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Open web links from messages, email, and other apps directly in Bansify Browser.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Button(
                                    onClick = {
                                        runCatching {
                                            val intent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS).apply {
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            }
                                            context.startActivity(intent)
                                        }
                                    },
                                    shape = RoundedCornerShape(24.dp)
                                ) {
                                    Text("Open Default Apps settings")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(Icons.Outlined.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }

                SettingsSubScreen.AUTOFILL -> {
                    item {
                        ExpressiveSwitchCard(
                            index = 0,
                            totalCount = 2,
                            title = "Save and fill form data",
                            subtitle = "Store basic non-sensitive form suggestions locally in WebView",
                            checked = settings.saveFormDataEnabled,
                            onCheckedChange = { v -> onUpdateSettings { it.copy(saveFormDataEnabled = v) } }
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        ExpressiveSwitchCard(
                            index = 1,
                            totalCount = 2,
                            title = "Autofill addresses and more",
                            subtitle = "Use Android Autofill service when filling web forms",
                            checked = settings.autofillAddressesEnabled,
                            onCheckedChange = { v -> onUpdateSettings { it.copy(autofillAddressesEnabled = v) } }
                        )
                    }
                }

                SettingsSubScreen.APPEARANCE -> {
                    item {
                        ExpressiveSettingsCardRow(
                            index = 0,
                            totalCount = 3,
                            title = "Theme",
                            subtitle = settings.themeMode.label,
                            onClick = { onNavigateSubScreen(SettingsSubScreen.THEME) }
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        ExpressiveSettingsCardRow(
                            index = 1,
                            totalCount = 3,
                            title = "Toolbar shortcut",
                            subtitle = settings.toolbarShortcut.label,
                            onClick = { onNavigateSubScreen(SettingsSubScreen.TOOLBAR_SHORTCUT) }
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        ExpressiveSwitchCard(
                            index = 2,
                            totalCount = 3,
                            title = "Show Continue browsing & Bookmarks on New Tab",
                            subtitle = "Display recent cards underneath shortcuts on the Home page",
                            checked = settings.showRecentCardsOnNtp,
                            onCheckedChange = { v -> onUpdateSettings { it.copy(showRecentCardsOnNtp = v) } }
                        )
                    }
                }

                SettingsSubScreen.THEME -> {
                    val modes = ThemeMode.entries
                    items(modes.size) { idx ->
                        val mode = modes[idx]
                        ExpressiveRadioCard(
                            index = idx,
                            totalCount = modes.size,
                            title = mode.label,
                            subtitle = when (mode) {
                                ThemeMode.SYSTEM -> "Follows your Android device theme"
                                ThemeMode.LIGHT -> "Always use light Chrome appearance"
                                ThemeMode.DARK -> "Always use dark Chrome appearance"
                            },
                            selected = settings.themeMode == mode,
                            onSelect = { onUpdateSettings { it.copy(themeMode = mode) } }
                        )
                    }
                }

                SettingsSubScreen.TOOLBAR_SHORTCUT -> {
                    val options = ToolbarShortcut.entries
                    items(options.size) { idx ->
                        val shortcut = options[idx]
                        ExpressiveRadioCard(
                            index = idx,
                            totalCount = options.size,
                            title = shortcut.label,
                            subtitle = when (shortcut) {
                                ToolbarShortcut.NEW_TAB -> "Show '+' New tab button next to address bar"
                                ToolbarShortcut.SHARE -> "Quickly share the current page"
                                ToolbarShortcut.BOOKMARK -> "Star or unstar the current page"
                                ToolbarShortcut.NONE -> "Hide extra toolbar shortcut button"
                            },
                            selected = settings.toolbarShortcut == shortcut,
                            onSelect = { onUpdateSettings { it.copy(toolbarShortcut = shortcut) } }
                        )
                    }
                }

                SettingsSubScreen.HOMEPAGE -> {
                    item {
                        ExpressiveSwitchCard(
                            index = 0,
                            totalCount = 1,
                            title = "On",
                            subtitle = "Show the Home button in the browser toolbar",
                            checked = settings.homepageEnabled,
                            onCheckedChange = { enabled ->
                                onUpdateSettings { it.copy(homepageEnabled = enabled) }
                            }
                        )
                        if (settings.homepageEnabled) {
                            Spacer(modifier = Modifier.height(14.dp))
                            ExpressiveSettingsSectionTitle("Open this page")
                            ExpressiveRadioCard(
                                index = 0,
                                totalCount = 2,
                                title = "Bansify's homepage",
                                subtitle = "Shows the New Tab page with search and shortcuts",
                                selected = !settings.useCustomHomepage,
                                onSelect = { onUpdateSettings { it.copy(useCustomHomepage = false) } }
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            ExpressiveRadioCard(
                                index = 1,
                                totalCount = 2,
                                title = "Enter custom web address",
                                subtitle = null,
                                selected = settings.useCustomHomepage,
                                onSelect = { onUpdateSettings { it.copy(useCustomHomepage = true) } }
                            )
                            if (settings.useCustomHomepage) {
                                var urlDraft by remember(settings.customHomepageUrl) {
                                    mutableStateOf(settings.customHomepageUrl)
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedTextField(
                                    value = urlDraft,
                                    onValueChange = { value ->
                                        urlDraft = value
                                        onUpdateSettings { it.copy(customHomepageUrl = value) }
                                    },
                                    label = { Text("Homepage URL") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                SettingsSubScreen.TABS -> {
                    item {
                        ExpressiveSettingsSectionTitle("Tab behavior")
                        ExpressiveSwitchCard(
                            index = 0,
                            totalCount = 1,
                            title = "Open external links in a new tab",
                            subtitle = "Links opened from other apps open in a fresh Bansify tab",
                            checked = settings.openExternalLinksInNewTab,
                            onCheckedChange = { v ->
                                onUpdateSettings { it.copy(openExternalLinksInNewTab = v) }
                            }
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        ExpressiveSettingsSectionTitle("Auto-close inactive tabs")
                        val options = listOf(0 to "Never", 7 to "After 7 days", 14 to "After 14 days", 21 to "After 21 days")
                        options.forEachIndexed { idx, (days, label) ->
                            ExpressiveRadioCard(
                                index = idx,
                                totalCount = options.size,
                                title = label,
                                subtitle = null,
                                selected = settings.autoCloseTabsDays == days,
                                onSelect = { onUpdateSettings { it.copy(autoCloseTabsDays = days) } }
                            )
                            if (idx < options.lastIndex) Spacer(modifier = Modifier.height(2.dp))
                        }
                    }
                }

                SettingsSubScreen.NOTIFICATIONS -> {
                    item {
                        ExpressiveSwitchCard(
                            index = 0,
                            totalCount = 2,
                            title = "Show notifications",
                            subtitle = "Allow browser status and download notifications",
                            checked = settings.notificationsEnabled,
                            onCheckedChange = { v -> onUpdateSettings { it.copy(notificationsEnabled = v) } }
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        ExpressiveSwitchCard(
                            index = 1,
                            totalCount = 2,
                            title = "Completed downloads",
                            subtitle = "Notify when downloaded files finish saving",
                            checked = settings.downloadNotificationsEnabled,
                            onCheckedChange = { v -> onUpdateSettings { it.copy(downloadNotificationsEnabled = v) } }
                        )
                    }
                }

                SettingsSubScreen.LANGUAGES -> {
                    item {
                        ExpressiveSettingsSectionTitle("Preferred language")
                        val languages = listOf(
                            "English (United States)",
                            "Hindi (India)",
                            "Spanish (Spain)",
                            "French (France)",
                            "German (Germany)",
                            "Japanese (Japan)"
                        )
                        languages.forEachIndexed { idx, lang ->
                            ExpressiveRadioCard(
                                index = idx,
                                totalCount = languages.size,
                                title = lang,
                                subtitle = if (settings.preferredLanguage == lang) "Active Accept-Language header" else null,
                                selected = settings.preferredLanguage == lang,
                                onSelect = { onUpdateSettings { it.copy(preferredLanguage = lang) } }
                            )
                            if (idx < languages.lastIndex) Spacer(modifier = Modifier.height(2.dp))
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        ExpressiveSettingsSectionTitle("Translation")
                        ExpressiveSwitchCard(
                            index = 0,
                            totalCount = 1,
                            title = "Offer to translate pages",
                            subtitle = "Allow quick page translation from the 3-dot menu",
                            checked = settings.offerTranslatePages,
                            onCheckedChange = { v -> onUpdateSettings { it.copy(offerTranslatePages = v) } }
                        )
                    }
                }

                SettingsSubScreen.BLOCKING_PERMISSIONS -> {
                    item {
                        ExpressiveSettingsSectionTitle("Ad and tracker blocking")
                        ExpressiveSwitchCard(
                            index = 0,
                            totalCount = 2,
                            title = "Block ads and trackers",
                            subtitle = if (settings.adBlockEnabled) {
                                "On — $adBlockedCount requests blocked since launch"
                            } else {
                                "Off — every request the page makes is loaded"
                            },
                            checked = settings.adBlockEnabled,
                            onCheckedChange = { v -> onUpdateSettings { it.copy(adBlockEnabled = v) } }
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        ExpressiveSwitchCard(
                            index = 1,
                            totalCount = 2,
                            title = "Ask before sites use camera, mic or location",
                            subtitle = if (settings.sitePermissionPromptsEnabled) {
                                "Each site must ask, and you decide"
                            } else {
                                "Every site permission request is denied"
                            },
                            checked = settings.sitePermissionPromptsEnabled,
                            onCheckedChange = { v -> onUpdateSettings { it.copy(sitePermissionPromptsEnabled = v) } }
                        )
                        if (adBlockedCount > 0) {
                            TextButton(onClick = onResetAdBlockCount) {
                                Text("Reset blocked counter")
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(14.dp))
                        ExpressiveSettingsSectionTitle("Sites you allowed")
                        Column(modifier = Modifier.fillMaxWidth()) {
                            if (sitePermissionDecisions.isEmpty()) {
                                Text(
                                    text = "No site has been allowed a permission yet.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                                )
                            } else {
                                sitePermissionDecisions.forEach { decision ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 20.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = decision.permission.label,
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                            Text(
                                                text = decision.origin,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        TextButton(onClick = { onForgetSitePermission(decision.origin) }) {
                                            Text("Remove")
                                        }
                                    }
                                }
                                TextButton(
                                    onClick = onForgetAllSitePermissions,
                                    modifier = Modifier.padding(start = 12.dp)
                                ) {
                                    Text("Clear all")
                                }
                            }
                        }
                    }
                }

                SettingsSubScreen.PRIVACY_SECURITY -> {
                    item {
                        ExpressiveSettingsCardRow(
                            index = 0,
                            totalCount = 6,
                            title = "Delete browsing data",
                            subtitle = "Clear history, cookies, cache, and site data",
                            onClick = onOpenDeleteBrowsingData
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        ExpressiveSettingsCardRow(
                            index = 1,
                            totalCount = 6,
                            title = "Safe Browsing",
                            subtitle = settings.safeBrowsingLevel.label,
                            onClick = { onNavigateSubScreen(SettingsSubScreen.SAFE_BROWSING) }
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        ExpressiveSwitchCard(
                            index = 2,
                            totalCount = 6,
                            title = "Always use secure connections",
                            subtitle = "Upgrade navigations to HTTPS automatically",
                            checked = settings.httpsFirstEnabled,
                            onCheckedChange = { v -> onUpdateSettings { it.copy(httpsFirstEnabled = v) } }
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        ExpressiveSwitchCard(
                            index = 3,
                            totalCount = 6,
                            title = "Send a \"Do Not Track\" request",
                            subtitle = "Includes a DNT: 1 header with browsing traffic",
                            checked = settings.doNotTrackEnabled,
                            onCheckedChange = { v -> onUpdateSettings { it.copy(doNotTrackEnabled = v) } }
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        ExpressiveSettingsCardRow(
                            index = 4,
                            totalCount = 6,
                            title = "Third-party cookies",
                            subtitle = settings.thirdPartyCookiePolicy.label,
                            onClick = { onNavigateSubScreen(SettingsSubScreen.THIRD_PARTY_COOKIES) }
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        ExpressiveSwitchCard(
                            index = 5,
                            totalCount = 6,
                            title = "Lock Incognito tabs when you leave Bansify",
                            subtitle = "Adds privacy protection for open Incognito sessions",
                            checked = settings.lockIncognitoOnLeave,
                            onCheckedChange = { v -> onUpdateSettings { it.copy(lockIncognitoOnLeave = v) } }
                        )
                    }
                }

                SettingsSubScreen.SAFE_BROWSING -> {
                    val levels = SafeBrowsingLevel.entries
                    items(levels.size) { idx ->
                        val level = levels[idx]
                        ExpressiveRadioCard(
                            index = idx,
                            totalCount = levels.size,
                            title = level.label,
                            subtitle = level.summary,
                            selected = settings.safeBrowsingLevel == level,
                            onSelect = { onUpdateSettings { it.copy(safeBrowsingLevel = level) } }
                        )
                    }
                }

                SettingsSubScreen.THIRD_PARTY_COOKIES -> {
                    val policies = ThirdPartyCookiePolicy.entries
                    items(policies.size) { idx ->
                        val policy = policies[idx]
                        ExpressiveRadioCard(
                            index = idx,
                            totalCount = policies.size,
                            title = policy.label,
                            subtitle = policy.summary,
                            selected = settings.thirdPartyCookiePolicy == policy,
                            onSelect = { onUpdateSettings { it.copy(thirdPartyCookiePolicy = policy) } }
                        )
                    }
                }

                SettingsSubScreen.SITE_SETTINGS -> {
                    item {
                        ExpressiveSettingsSectionTitle("Content & Permissions")
                        ExpressiveSwitchCard(
                            index = 0,
                            totalCount = 6,
                            title = "JavaScript",
                            subtitle = if (settings.javascriptEnabled) "Allowed (recommended)" else "Blocked",
                            checked = settings.javascriptEnabled,
                            onCheckedChange = { v -> onUpdateSettings { it.copy(javascriptEnabled = v) } }
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        ExpressiveSwitchCard(
                            index = 1,
                            totalCount = 6,
                            title = "Cookies",
                            subtitle = if (settings.cookiesEnabled) "Sites can save and read cookie data" else "All cookies blocked",
                            checked = settings.cookiesEnabled,
                            onCheckedChange = { v -> onUpdateSettings { it.copy(cookiesEnabled = v) } }
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        ExpressiveSwitchCard(
                            index = 2,
                            totalCount = 6,
                            title = "Pop-ups and redirects",
                            subtitle = if (settings.popupsAllowed) "Allowed" else "Blocked (recommended)",
                            checked = settings.popupsAllowed,
                            onCheckedChange = { v -> onUpdateSettings { it.copy(popupsAllowed = v) } }
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        ExpressiveSwitchCard(
                            index = 3,
                            totalCount = 6,
                            title = "Images",
                            subtitle = if (settings.loadImagesEnabled) "Show all images (recommended)" else "Do not load images (saves data)",
                            checked = settings.loadImagesEnabled,
                            onCheckedChange = { v -> onUpdateSettings { it.copy(loadImagesEnabled = v) } }
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        ExpressiveSwitchCard(
                            index = 4,
                            totalCount = 6,
                            title = "Desktop site",
                            subtitle = if (settings.defaultDesktopSite) "Always request desktop view for new tabs" else "Request mobile view by default",
                            checked = settings.defaultDesktopSite,
                            onCheckedChange = { v -> onUpdateSettings { it.copy(defaultDesktopSite = v) } }
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        ExpressiveSwitchCard(
                            index = 5,
                            totalCount = 6,
                            title = "Media autoplay",
                            subtitle = if (settings.mediaAutoplayEnabled) "Allowed" else "Require tap before media plays",
                            checked = settings.mediaAutoplayEnabled,
                            onCheckedChange = { v -> onUpdateSettings { it.copy(mediaAutoplayEnabled = v) } }
                        )
                    }
                }

                SettingsSubScreen.ACCESSIBILITY -> {
                    item {
                        ExpressiveGroupedCard(index = 0, totalCount = 3) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Text scaling",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${settings.textZoomPercent}%",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Slider(
                                    value = settings.textZoomPercent.toFloat(),
                                    onValueChange = { v ->
                                        val snapped = (v / 5f).roundToInt() * 5
                                        onUpdateSettings { it.copy(textZoomPercent = snapped.coerceIn(50, 200)) }
                                    },
                                    valueRange = 50f..200f,
                                    steps = 29
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Drag the slider until you can read this comfortably. Text on web pages scales immediately.",
                                    fontSize = (14f * (settings.textZoomPercent / 100f)).sp,
                                    lineHeight = (20f * (settings.textZoomPercent / 100f)).sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        ExpressiveSwitchCard(
                            index = 1,
                            totalCount = 3,
                            title = "Force enable zoom",
                            subtitle = "Override a website's request to prevent zooming in",
                            checked = settings.forceEnableZoom,
                            onCheckedChange = { v -> onUpdateSettings { it.copy(forceEnableZoom = v) } }
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        ExpressiveSwitchCard(
                            index = 2,
                            totalCount = 3,
                            title = "Simplified view for web pages",
                            subtitle = "Prefer clean reading typography whenever supported",
                            checked = settings.simplifiedViewEnabled,
                            onCheckedChange = { v -> onUpdateSettings { it.copy(simplifiedViewEnabled = v) } }
                        )
                    }
                }

                SettingsSubScreen.DOWNLOADS_SETTINGS -> {
                    item {
                        ExpressiveSettingsCardRow(
                            index = 0,
                            totalCount = 2,
                            title = "Download location",
                            subtitle = "/storage/emulated/0/Download",
                            onClick = {}
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        ExpressiveSwitchCard(
                            index = 1,
                            totalCount = 2,
                            title = "Ask where to save files",
                            subtitle = "Prompt confirmation before saving downloads",
                            checked = settings.askWhereToSaveDownloads,
                            onCheckedChange = { v -> onUpdateSettings { it.copy(askWhereToSaveDownloads = v) } }
                        )
                    }
                }

                SettingsSubScreen.ABOUT -> {
                    item {
                        val webViewPkg = remember {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                WebView.getCurrentWebViewPackage()
                            } else {
                                null
                            }
                        }
                        val aboutItems = listOf(
                            "Application version" to "Bansify Browser 1.0.0",
                            "Package name" to "com.bansify.browser",
                            "WebView engine" to (webViewPkg?.let { "${it.packageName} (${it.versionName})" }
                                ?: "Android System WebView"),
                            "Operating system" to "Android ${Build.VERSION.RELEASE}; ${Build.MODEL}",
                            "Account & sync storage" to "Demo Bansify Account sync structure ready — sign in from Settings > You and Bansify to manage what syncs."
                        )
                        aboutItems.forEachIndexed { idx, (label, value) ->
                            ExpressiveSettingsCardRow(
                                index = idx,
                                totalCount = aboutItems.size,
                                title = label,
                                subtitle = value,
                                onClick = {}
                            )
                            if (idx < aboutItems.lastIndex) Spacer(modifier = Modifier.height(2.dp))
                        }
                    }
                }

                SettingsSubScreen.MAIN,
                SettingsSubScreen.BANSIFY_ACCOUNT,
                SettingsSubScreen.BANSIFY_SERVICES -> Unit
            }

            item { Spacer(modifier = Modifier.height(28.dp)) }
        }
    }
}

@Composable
private fun AddressBarMiniPreview(isBottom: Boolean) {
    Box(
        modifier = Modifier
            .width(38.dp)
            .height(54.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .align(if (isBottom) Alignment.BottomCenter else Alignment.TopCenter)
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.primary)
        )
    }
}

@Composable
private fun SafetyCheckItemCard(
    index: Int,
    totalCount: Int,
    title: String,
    subtitle: String,
    isSafe: Boolean,
    safeColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    ExpressiveGroupedCard(index = index, totalCount = totalCount) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isSafe) Icons.Outlined.CheckCircle else Icons.Outlined.WarningAmber,
                contentDescription = null,
                tint = if (isSafe) safeColor else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ExpressiveRadioCard(
    index: Int,
    totalCount: Int,
    title: String,
    subtitle: String?,
    selected: Boolean,
    onSelect: () -> Unit
) {
    ExpressiveGroupedCard(index = index, totalCount = totalCount) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onSelect)
                .padding(horizontal = 16.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = selected, onClick = onSelect)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpressiveSwitchCard(
    index: Int,
    totalCount: Int,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ExpressiveGroupedCard(index = index, totalCount = totalCount) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCheckedChange(!checked) }
                .padding(horizontal = 20.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}
