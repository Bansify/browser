package com.bansify.browser.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bansify.browser.R
import com.bansify.browser.browser.UrlUtils
import com.bansify.browser.data.BookmarkEntity
import com.bansify.browser.data.BrowserSettings
import com.bansify.browser.data.HistoryEntity
import com.bansify.browser.data.ShortcutEntity
import com.bansify.browser.data.ThirdPartyCookiePolicy
import com.bansify.browser.ui.components.IncognitoIcon
import com.bansify.browser.ui.components.ShortcutDialog
import com.bansify.browser.ui.components.SiteAvatar
import com.bansify.browser.ui.theme.BrandBlue
import com.bansify.browser.ui.theme.BrandGreen
import com.bansify.browser.ui.theme.BrandRed
import com.bansify.browser.ui.theme.BrandYellow
import com.bansify.browser.ui.theme.LocalIsDarkBrowserTheme
import com.bansify.browser.ui.theme.PlusJakartaSansFontFamily

@Composable
fun NewTabScreen(
    isIncognito: Boolean,
    settings: BrowserSettings,
    shortcuts: List<ShortcutEntity>,
    recentBookmarks: List<BookmarkEntity>,
    recentHistory: List<HistoryEntity>,
    onSearchClick: () -> Unit,
    onVoiceSearchClick: () -> Unit,
    onNewTabClick: () -> Unit,
    onNewIncognitoTabClick: () -> Unit,
    onOpenUrl: (String) -> Unit,
    onAddShortcut: (title: String, url: String) -> Unit,
    onUpdateShortcut: (ShortcutEntity) -> Unit,
    onDeleteShortcut: (Long) -> Unit,
    onToggleIncognitoBlockThirdParty: (Boolean) -> Unit,
    onOpenBookmarks: () -> Unit,
    onOpenHistory: () -> Unit
) {
    if (isIncognito) {
        IncognitoNewTabContent(
            settings = settings,
            onSearchClick = onSearchClick,
            onToggleBlockThirdParty = onToggleIncognitoBlockThirdParty
        )
    } else {
        NormalNewTabContent(
            settings = settings,
            shortcuts = shortcuts,
            recentBookmarks = recentBookmarks,
            recentHistory = recentHistory,
            onSearchClick = onSearchClick,
            onVoiceSearchClick = onVoiceSearchClick,
            onNewTabClick = onNewTabClick,
            onNewIncognitoTabClick = onNewIncognitoTabClick,
            onOpenUrl = onOpenUrl,
            onAddShortcut = onAddShortcut,
            onUpdateShortcut = onUpdateShortcut,
            onDeleteShortcut = onDeleteShortcut,
            onOpenBookmarks = onOpenBookmarks,
            onOpenHistory = onOpenHistory
        )
    }
}

/**
 * Pixel-accurate clone of modern Chrome Android's New Tab Page:
 * - Centered geometric wordmark (crisp white in Dark mode, brand colors in Light mode)
 * - 60.dp rounded pill search bar: [+] Search Google or type URL [Mic]
 * - Side-by-side 2 rounded pill buttons directly below: [Bookmarks] & [Incognito]
 * - Single 24.dp rounded Most Visited shortcuts card with horizontal scrollable circular icons
 * - Rounded 24.dp Continue Browsing & Bookmarks cards
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NormalNewTabContent(
    settings: BrowserSettings,
    shortcuts: List<ShortcutEntity>,
    recentBookmarks: List<BookmarkEntity>,
    recentHistory: List<HistoryEntity>,
    onSearchClick: () -> Unit,
    onVoiceSearchClick: () -> Unit,
    onNewTabClick: () -> Unit,
    onNewIncognitoTabClick: () -> Unit,
    onOpenUrl: (String) -> Unit,
    onAddShortcut: (title: String, url: String) -> Unit,
    onUpdateShortcut: (ShortcutEntity) -> Unit,
    onDeleteShortcut: (Long) -> Unit,
    onOpenBookmarks: () -> Unit,
    onOpenHistory: () -> Unit
) {
    var showAddShortcutDialog by remember { mutableStateOf(false) }
    var editingShortcut by remember { mutableStateOf<ShortcutEntity?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp)
                .navigationBarsPadding(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Centered Geometric Wordmark
            item {
                Spacer(modifier = Modifier.height(22.dp))
                BansifyWordmark()
                Spacer(modifier = Modifier.height(28.dp))
            }

            // Exact Chrome 60.dp Rounded Pill Search Bar ([+] Search Google or type URL [Mic])
            item {
                Surface(
                    shape = RoundedCornerShape(30.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clip(RoundedCornerShape(30.dp))
                        .clickable(onClick = onSearchClick)
                        .testTag("ntp_search_bar")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 8.dp, end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onNewTabClick,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = stringResource(R.string.new_tab),
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Text(
                            text = "Search ${settings.searchEngine.displayName} or type URL",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp)
                        )

                        IconButton(
                            onClick = onVoiceSearchClick,
                            modifier = Modifier
                                .size(44.dp)
                                .testTag("ntp_voice_search")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Mic,
                                contentDescription = "Voice search",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Two Equal-Width Side-by-Side Pill Buttons directly below search bar (exact Chrome layout!)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .clickable(onClick = onOpenBookmarks)
                            .testTag("ntp_bookmarks_pill")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Bookmarks,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(19.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Bookmarks",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .clickable(onClick = onNewIncognitoTabClick)
                            .testTag("ntp_incognito_pill")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IncognitoIcon(
                                size = 20.dp,
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Incognito",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Single 24.dp Rounded Most Visited Shortcuts Card with Horizontal Row (matches Screenshot 4!)
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 18.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(shortcuts, key = { it.id }) { shortcut ->
                            Column(
                                modifier = Modifier
                                    .width(68.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .combinedClickable(
                                        onClick = { onOpenUrl(shortcut.url) },
                                        onLongClick = { editingShortcut = shortcut }
                                    )
                                    .padding(vertical = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    SiteAvatar(
                                        title = shortcut.title,
                                        url = shortcut.url,
                                        accentColor = shortcut.accentColor,
                                        size = 34.dp
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = shortcut.title,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // "+ Add" Shortcut Tile at end of row
                        item(key = "add_shortcut_tile") {
                            Column(
                                modifier = Modifier
                                    .width(68.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { showAddShortcutDialog = true }
                                    .testTag("ntp_add_shortcut")
                                    .padding(vertical = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add shortcut",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Add",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // Recent Bookmarks & Continue Browsing Rounded Cards
            if (settings.showRecentCardsOnNtp && (recentBookmarks.isNotEmpty() || recentHistory.isNotEmpty())) {
                item { Spacer(modifier = Modifier.height(16.dp)) }

                if (recentHistory.isNotEmpty()) {
                    item {
                        NtpRoundedModuleCard(
                            title = "Continue browsing",
                            icon = Icons.Outlined.History,
                            actionText = "History",
                            onActionClick = onOpenHistory
                        ) {
                            val distinctRecent = remember(recentHistory) {
                                recentHistory.distinctBy { it.url }.take(5)
                            }
                            Column(modifier = Modifier.fillMaxWidth()) {
                                distinctRecent.forEachIndexed { idx, entry ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onOpenUrl(entry.url) }
                                            .padding(horizontal = 18.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        SiteAvatar(
                                            title = entry.title,
                                            url = entry.url,
                                            size = 34.dp
                                        )
                                        Spacer(modifier = Modifier.width(14.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = entry.title,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = UrlUtils.extractShortDomain(entry.url),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                    if (idx < distinctRecent.lastIndex) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(start = 66.dp, end = 16.dp),
                                            thickness = 0.5.dp,
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                }

                if (recentBookmarks.isNotEmpty()) {
                    item {
                        NtpRoundedModuleCard(
                            title = "Saved bookmarks",
                            icon = Icons.Outlined.Bookmarks,
                            actionText = "See all",
                            onActionClick = onOpenBookmarks
                        ) {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                items(recentBookmarks.take(8), key = { it.id }) { bm ->
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = MaterialTheme.colorScheme.surfaceContainer,
                                        modifier = Modifier
                                            .width(160.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .clickable { onOpenUrl(bm.url) }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(11.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            SiteAvatar(
                                                title = bm.title,
                                                url = bm.url,
                                                size = 30.dp
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = bm.title,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = UrlUtils.extractShortDomain(bm.url),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }

    if (showAddShortcutDialog) {
        ShortcutDialog(
            onDismiss = { showAddShortcutDialog = false },
            onSave = { title, url ->
                onAddShortcut(title, url)
                showAddShortcutDialog = false
            }
        )
    }

    editingShortcut?.let { shortcut ->
        ShortcutDialog(
            initial = shortcut,
            onDismiss = { editingShortcut = null },
            onSave = { title, url ->
                val normalized = if (url.startsWith("http://") || url.startsWith("https://")) url else "https://$url"
                onUpdateShortcut(shortcut.copy(title = title.ifBlank { shortcut.title }, url = normalized))
                editingShortcut = null
            },
            onDelete = {
                onDeleteShortcut(shortcut.id)
            }
        )
    }
}

@Composable
private fun BansifyWordmark() {
    val isDark = LocalIsDarkBrowserTheme.current
    if (isDark) {
        // Matches Screenshot 4's crisp pure-white geometric wordmark in Chrome Dark mode!
        Text(
            text = "Bansify",
            fontFamily = PlusJakartaSansFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 44.sp,
            letterSpacing = (-1.2).sp,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
    } else {
        val wordmark = buildAnnotatedString {
            withStyle(SpanStyle(color = BrandBlue)) { append("B") }
            withStyle(SpanStyle(color = BrandRed)) { append("a") }
            withStyle(SpanStyle(color = BrandYellow)) { append("n") }
            withStyle(SpanStyle(color = BrandBlue)) { append("s") }
            withStyle(SpanStyle(color = BrandGreen)) { append("i") }
            withStyle(SpanStyle(color = BrandRed)) { append("f") }
            withStyle(SpanStyle(color = BrandYellow)) { append("y") }
        }
        Text(
            text = wordmark,
            fontFamily = PlusJakartaSansFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 44.sp,
            letterSpacing = (-1.2).sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun NtpRoundedModuleCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    actionText: String,
    onActionClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 18.dp, end = 12.dp, top = 14.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = actionText,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(onClick = onActionClick)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            content()
        }
    }
}

@Composable
private fun IncognitoNewTabContent(
    settings: BrowserSettings,
    onSearchClick: () -> Unit,
    onToggleBlockThirdParty: (Boolean) -> Unit
) {
    val blockThirdParty = settings.thirdPartyCookiePolicy != ThirdPartyCookiePolicy.ALLOW

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp)
                .navigationBarsPadding(),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .clickable(onClick = onSearchClick)
                        .testTag("incognito_search_bar")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Public,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Search ${settings.searchEngine.displayName} or type URL",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(28.dp))
            }

            item {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                    contentAlignment = Alignment.Center
                ) {
                    IncognitoIcon(
                        size = 42.dp,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = stringResource(R.string.incognito_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.incognito_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    IncognitoInfoBlock(
                        heading = "Bansify won't save:",
                        bullets = listOf(
                            "Your browsing history",
                            "Cookies and site data",
                            "Information entered in forms"
                        )
                    )
                    IncognitoInfoBlock(
                        heading = "Your activity might still be visible to:",
                        bullets = listOf(
                            "Websites you visit",
                            "Your employer or school",
                            "Your internet service provider"
                        )
                    )
                }
                Spacer(modifier = Modifier.height(22.dp))
            }

            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Block third-party cookies",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "When on, sites can't use cookies that track you across the web. Features on some sites may break.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Switch(
                            checked = blockThirdParty,
                            onCheckedChange = onToggleBlockThirdParty,
                            modifier = Modifier.testTag("incognito_block_cookies_switch")
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun IncognitoInfoBlock(heading: String, bullets: List<String>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = heading,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        bullets.forEach { bullet ->
            Row(
                modifier = Modifier.padding(vertical = 2.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "•",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 10.dp)
                )
                Text(
                    text = bullet,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
