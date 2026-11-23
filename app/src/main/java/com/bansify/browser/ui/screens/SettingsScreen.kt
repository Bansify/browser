package com.bansify.browser.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bansify.browser.R
import com.bansify.browser.data.BansifyAccountState
import com.bansify.browser.data.BrowserSettings
import com.bansify.browser.data.SettingsSubScreen
import com.bansify.browser.ui.components.ExpressiveGroupedCard
import com.bansify.browser.ui.components.expressiveCanvasColor
import com.bansify.browser.ui.theme.BrandBlue

private data class SettingsEntry(
    val title: String,
    val subtitle: String? = null,
    val testTag: String,
    val target: SettingsSubScreen,
    val keywords: String = ""
)

/**
 * Pixel-accurate clone of modern Chrome Android Settings (Screenshots 1 & 8):
 * - Top bar with Back arrow, "Settings", and "?" Help icon
 * - Live interactive pill "Search settings" bar
 * - "You and Bansify" 2-card Expressive section (Profile + Bansify services)
 * - "Basics" & "Advanced" Material 3 Expressive segmented card groups
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    subScreen: SettingsSubScreen,
    settings: BrowserSettings,
    accountState: BansifyAccountState,
    bookmarksCount: Int,
    historyCount: Int,
    openTabsCount: Int,
    shortcutsCount: Int,
    downloadsCount: Int,
    onBack: () -> Unit,
    onNavigateSubScreen: (SettingsSubScreen) -> Unit,
    onOpenDeleteBrowsingData: () -> Unit,
    onUpdateSettings: ((BrowserSettings) -> BrowserSettings) -> Unit,
    onSignInBansify: (displayName: String, email: String, avatarColorHex: Long) -> Unit,
    onUpdateBansifyProfile: (displayName: String, email: String, avatarColorHex: Long) -> Unit,
    onSignOutBansify: () -> Unit,
    onUpdateBansifySync: ((BansifyAccountState) -> BansifyAccountState) -> Unit,
    onSyncNow: () -> Unit
) {
    BackHandler { onBack() }

    when (subScreen) {
        SettingsSubScreen.BANSIFY_ACCOUNT -> {
            BansifyAccountScreen(
                accountState = accountState,
                bookmarksCount = bookmarksCount,
                historyCount = historyCount,
                openTabsCount = openTabsCount,
                shortcutsCount = shortcutsCount,
                downloadsCount = downloadsCount,
                onBack = onBack,
                onSignIn = onSignInBansify,
                onUpdateProfile = onUpdateBansifyProfile,
                onSignOut = onSignOutBansify,
                onUpdateSync = onUpdateBansifySync,
                onSyncNow = onSyncNow
            )
            return
        }

        SettingsSubScreen.BANSIFY_SERVICES -> {
            BansifyServicesScreen(
                accountState = accountState,
                onBack = onBack,
                onUpdateSync = onUpdateBansifySync
            )
            return
        }

        SettingsSubScreen.MAIN -> Unit

        else -> {
            SettingsSubScreenContent(
                subScreen = subScreen,
                settings = settings,
                onBack = onBack,
                onNavigateSubScreen = onNavigateSubScreen,
                onOpenDeleteBrowsingData = onOpenDeleteBrowsingData,
                onUpdateSettings = onUpdateSettings
            )
            return
        }
    }

    var searchQuery by remember { mutableStateOf("") }
    val canvasBg = expressiveCanvasColor()

    val basicsItems = remember(settings) {
        listOf(
            SettingsEntry(
                title = "Search engine",
                subtitle = settings.searchEngine.displayName,
                testTag = "settings_search_engine",
                target = SettingsSubScreen.SEARCH_ENGINE,
                keywords = "google bing duckduckgo yahoo brave ecosia"
            ),
            SettingsEntry(
                title = "Address bar",
                subtitle = settings.addressBarPosition.label,
                testTag = "settings_address_bar",
                target = SettingsSubScreen.ADDRESS_BAR,
                keywords = "top bottom toolbar omnibox position"
            ),
            SettingsEntry(
                title = "Privacy and security",
                testTag = "settings_privacy",
                target = SettingsSubScreen.PRIVACY_SECURITY,
                keywords = "safe browsing https cookies delete browsing data do not track incognito"
            ),
            SettingsEntry(
                title = "Safety check",
                testTag = "settings_safety_check",
                target = SettingsSubScreen.SAFETY_CHECK,
                keywords = "security scan protection check"
            ),
            SettingsEntry(
                title = "Default browser",
                testTag = "settings_default_browser",
                target = SettingsSubScreen.DEFAULT_BROWSER,
                keywords = "system apps links"
            ),
            SettingsEntry(
                title = "Autofill and passwords",
                testTag = "settings_autofill",
                target = SettingsSubScreen.AUTOFILL,
                keywords = "forms addresses save"
            )
        )
    }

    val advancedItems = remember(settings) {
        listOf(
            SettingsEntry(
                title = "Tabs and tab groups",
                testTag = "settings_tabs",
                target = SettingsSubScreen.TABS,
                keywords = "inactive auto close groups external links"
            ),
            SettingsEntry(
                title = "Homepage",
                subtitle = if (settings.homepageEnabled) "On" else "Off",
                testTag = "settings_homepage",
                target = SettingsSubScreen.HOMEPAGE,
                keywords = "home button custom url start page"
            ),
            SettingsEntry(
                title = "Notifications",
                testTag = "settings_notifications",
                target = SettingsSubScreen.NOTIFICATIONS,
                keywords = "alerts downloads sites"
            ),
            SettingsEntry(
                title = "Appearance",
                testTag = "settings_appearance",
                target = SettingsSubScreen.APPEARANCE,
                keywords = "theme dark light system toolbar shortcut"
            ),
            SettingsEntry(
                title = "Accessibility",
                testTag = "settings_accessibility",
                target = SettingsSubScreen.ACCESSIBILITY,
                keywords = "text scaling font zoom simplified reading"
            ),
            SettingsEntry(
                title = "Site settings",
                testTag = "settings_site_settings",
                target = SettingsSubScreen.SITE_SETTINGS,
                keywords = "javascript cookies popups images desktop autoplay permissions"
            ),
            SettingsEntry(
                title = "Languages",
                testTag = "settings_languages",
                target = SettingsSubScreen.LANGUAGES,
                keywords = "translate english hindi spanish"
            ),
            SettingsEntry(
                title = "Downloads",
                testTag = "settings_downloads",
                target = SettingsSubScreen.DOWNLOADS_SETTINGS,
                keywords = "storage folder files save"
            ),
            SettingsEntry(
                title = "About Bansify",
                testTag = "settings_about",
                target = SettingsSubScreen.ABOUT,
                keywords = "version webview package android info"
            )
        )
    }

    val query = searchQuery.trim()
    val showYouAndBansify = remember(query, accountState) {
        query.isEmpty() ||
            "you and bansify account sync login bookmarks profile services".contains(query, ignoreCase = true) ||
            accountState.displayName.contains(query, ignoreCase = true) ||
            accountState.email.contains(query, ignoreCase = true)
    }
    val filteredBasics = remember(basicsItems, query) {
        if (query.isEmpty()) basicsItems
        else basicsItems.filter {
            it.title.contains(query, true) ||
                (it.subtitle?.contains(query, true) == true) ||
                it.keywords.contains(query, true)
        }
    }
    val filteredAdvanced = remember(advancedItems, query) {
        if (query.isEmpty()) advancedItems
        else advancedItems.filter {
            it.title.contains(query, true) ||
                (it.subtitle?.contains(query, true) == true) ||
                it.keywords.contains(query, true)
        }
    }

    Scaffold(
        containerColor = canvasBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings),
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("settings_back")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateSubScreen(SettingsSubScreen.ABOUT) }) {
                        Icon(
                            imageVector = Icons.Outlined.HelpOutline,
                            contentDescription = "Help and about Bansify"
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
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // Pill "Search settings" Bar
            item(key = "settings_search_bar") {
                Surface(
                    shape = RoundedCornerShape(26.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .height(50.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 16.sp
                                ),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("settings_search_input"),
                                decorationBox = { inner ->
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text = "Search settings",
                                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    inner()
                                }
                            )
                        }
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear search",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // "You and Bansify" Section (Exact match to Screenshot 8's "You and Google" 2-card group!)
            if (showYouAndBansify) {
                item(key = "header_you_and_bansify") {
                    ExpressiveSettingsSectionTitle("You and Bansify")
                }

                // Card 1: Bansify Account Profile / Sign-in Card
                item(key = "bansify_account_card") {
                    ExpressiveGroupedCard(
                        index = 0,
                        totalCount = 2,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateSubScreen(SettingsSubScreen.BANSIFY_ACCOUNT) }
                                .testTag("settings_bansify_account")
                                .padding(horizontal = 18.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (accountState.isSignedIn) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Color(accountState.avatarColorHex)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = accountState.initialLetter,
                                        color = Color.White,
                                        fontSize = 19.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.AccountCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (accountState.isSignedIn) accountState.displayName else "Sign in to Bansify",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontSize = 16.5.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (accountState.isSignedIn) {
                                        "${accountState.email} • Syncing ${accountState.activeSyncCategoriesCount}/6"
                                    } else {
                                        "Sync bookmarks, history, tabs & settings"
                                    },
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // Card 2: Bansify services (matches Screenshot 8's blue G "Google services" card!)
                item(key = "bansify_services_card") {
                    ExpressiveGroupedCard(
                        index = 1,
                        totalCount = 2,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateSubScreen(SettingsSubScreen.BANSIFY_SERVICES) }
                                .testTag("settings_bansify_services")
                                .padding(horizontal = 18.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(BrandBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "B",
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(18.dp))
                            Text(
                                text = "Bansify services",
                                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Basics Section
            if (filteredBasics.isNotEmpty()) {
                item(key = "header_basics") {
                    if (showYouAndBansify) Spacer(modifier = Modifier.height(14.dp))
                    ExpressiveSettingsSectionTitle("Basics")
                }
                itemsIndexed(filteredBasics, key = { _, item -> item.testTag }) { index, item ->
                    ExpressiveSettingsCardRow(
                        index = index,
                        totalCount = filteredBasics.size,
                        title = item.title,
                        subtitle = item.subtitle,
                        testTag = item.testTag,
                        onClick = { onNavigateSubScreen(item.target) }
                    )
                }
            }

            // Advanced Section
            if (filteredAdvanced.isNotEmpty()) {
                item(key = "header_advanced") {
                    Spacer(modifier = Modifier.height(14.dp))
                    ExpressiveSettingsSectionTitle("Advanced")
                }
                itemsIndexed(filteredAdvanced, key = { _, item -> item.testTag }) { index, item ->
                    ExpressiveSettingsCardRow(
                        index = index,
                        totalCount = filteredAdvanced.size,
                        title = item.title,
                        subtitle = item.subtitle,
                        testTag = item.testTag,
                        onClick = { onNavigateSubScreen(item.target) }
                    )
                }
            }

            if (!showYouAndBansify && filteredBasics.isEmpty() && filteredAdvanced.isEmpty()) {
                item(key = "empty_search") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No settings found for \"$query\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(28.dp)) }
        }
    }
}

@Composable
fun ExpressiveSettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall.copy(
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 10.dp)
    )
}

@Composable
fun ExpressiveSettingsCardRow(
    index: Int,
    totalCount: Int,
    title: String,
    subtitle: String? = null,
    testTag: String = "",
    onClick: () -> Unit
) {
    ExpressiveGroupedCard(
        index = index,
        totalCount = totalCount,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier)
                .padding(horizontal = 20.dp, vertical = 17.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                color = MaterialTheme.colorScheme.onSurface
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
