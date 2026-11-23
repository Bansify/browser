package com.example.ui.screens

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NorthWest
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Tab
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.browser.UrlUtils
import com.example.data.BookmarkEntity
import com.example.data.BrowserSettings
import com.example.data.BrowserTab
import com.example.data.HistoryEntity
import com.example.data.ShortcutEntity
import com.example.ui.components.ExpressiveGroupedCard
import com.example.ui.components.SiteAvatar
import com.example.ui.components.expressiveCanvasColor
import com.example.ui.theme.ChromeLinkBlueDark
import com.example.ui.theme.ChromeLinkBlueLight
import com.example.ui.theme.LocalIsDarkBrowserTheme

private sealed interface OmniboxItem {
    val key: String

    data class RecentSearch(val query: String) : OmniboxItem {
        override val key = "search_$query"
    }

    data class SiteMatch(
        val id: String,
        val title: String,
        val url: String,
        val displayUrl: String,
        val accentColor: Long? = null
    ) : OmniboxItem {
        override val key = "site_$id"
    }

    data class OpenTabMatch(val tab: BrowserTab) : OmniboxItem {
        override val key = "tab_${tab.id}"
    }

    data class DirectSearch(val query: String, val isUrl: Boolean) : OmniboxItem {
        override val key = "direct_$query"
    }
}

/**
 * Pixel-accurate clone of modern Chrome Android Omnibox Overlay (Screenshots 5 & 6):
 * - Top pill search bar with [+] / matching site favicon, inline highlighted autocomplete suffix,
 *   and [Mic] (when empty) or [X] (when typing)
 * - Material 3 Expressive segmented card suggestion list (20dp outer / 4dp inner corners, 2dp gap)
 * - Bolded query match highlights, Chrome-blue URLs, and NorthWest insert arrows
 */
@Composable
fun OmniboxOverlay(
    activeTab: BrowserTab,
    openTabs: List<BrowserTab>,
    bookmarks: List<BookmarkEntity>,
    history: List<HistoryEntity>,
    shortcuts: List<ShortcutEntity>,
    recentSearches: List<String>,
    settings: BrowserSettings,
    onDismiss: () -> Unit,
    onSubmitUrl: (String) -> Unit,
    onVoiceSearchClick: () -> Unit,
    onNewTabClick: () -> Unit,
    onSwitchToTab: (String) -> Unit,
    onShareCurrentUrl: () -> Unit,
    onCopyCurrentUrl: () -> Unit
) {
    BackHandler { onDismiss() }

    val initialText = if (activeTab.isNewTab) "" else activeTab.url
    var fieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = initialText,
                selection = TextRange(0, initialText.length)
            )
        )
    }

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    val query = fieldValue.text.trim()
    val isDark = LocalIsDarkBrowserTheme.current
    val linkBlue = if (isDark) ChromeLinkBlueDark else ChromeLinkBlueLight

    // Build unified site candidates from shortcuts, bookmarks, and history for rich inline autocomplete
    val allSiteCandidates = remember(shortcuts, bookmarks, history) {
        buildList {
            shortcuts.forEach {
                add(
                    OmniboxItem.SiteMatch(
                        id = "sc_${it.id}",
                        title = it.title,
                        url = it.url,
                        displayUrl = it.url.removePrefix("https://").removePrefix("http://").removePrefix("www.").trimEnd('/'),
                        accentColor = it.accentColor
                    )
                )
            }
            bookmarks.forEach {
                add(
                    OmniboxItem.SiteMatch(
                        id = "bm_${it.id}",
                        title = it.title,
                        url = it.url,
                        displayUrl = it.url.removePrefix("https://").removePrefix("http://").removePrefix("www.").trimEnd('/')
                    )
                )
            }
            history.distinctBy { it.url }.forEach {
                add(
                    OmniboxItem.SiteMatch(
                        id = "hi_${it.id}",
                        title = it.title,
                        url = it.url,
                        displayUrl = it.url.removePrefix("https://").removePrefix("http://").removePrefix("www.").trimEnd('/')
                    )
                )
            }
        }.distinctBy { it.displayUrl.lowercase() }
    }

    // Inline Chrome-style domain autocomplete (e.g., typing "g" suggests "github.com" inline!)
    val inlineAutocomplete = remember(query, allSiteCandidates) {
        if (query.isBlank() || query.contains(' ') || query.startsWith("http")) {
            null
        } else {
            allSiteCandidates.firstOrNull {
                it.displayUrl.startsWith(query, ignoreCase = true) && it.displayUrl.length > query.length
            }
        }
    }

    val suggestions = remember(query, openTabs, activeTab.id, recentSearches, allSiteCandidates, inlineAutocomplete) {
        buildList<OmniboxItem> {
            if (query.isEmpty()) {
                recentSearches.take(6).forEach { add(OmniboxItem.RecentSearch(it)) }
                allSiteCandidates.take(6).forEach { add(it) }
            } else {
                inlineAutocomplete?.let { add(it) }

                openTabs.filter {
                    it.id != activeTab.id && !it.isNewTab &&
                        (it.title.contains(query, ignoreCase = true) || it.url.contains(query, ignoreCase = true))
                }.take(2).forEach { add(OmniboxItem.OpenTabMatch(it)) }

                recentSearches.filter { it.contains(query, ignoreCase = true) }
                    .take(3)
                    .forEach { add(OmniboxItem.RecentSearch(it)) }

                allSiteCandidates.filter {
                    it.key != inlineAutocomplete?.key &&
                        (it.title.contains(query, ignoreCase = true) || it.displayUrl.contains(query, ignoreCase = true))
                }.take(6).forEach { add(it) }

                val isLikelyUrl = query.contains('.') && !query.contains(' ')
                add(OmniboxItem.DirectSearch(query = query, isUrl = isLikelyUrl))
            }
        }
    }

    Surface(
        color = expressiveCanvasColor(),
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Rounded Pill Omnibox Bar (Matches Screenshots 5 & 6!)
            Surface(
                shape = RoundedCornerShape(26.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp)
                    .height(50.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 6.dp, end = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (inlineAutocomplete != null) {
                        Box(
                            modifier = Modifier.size(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            SiteAvatar(
                                title = inlineAutocomplete.title,
                                url = inlineAutocomplete.url,
                                accentColor = inlineAutocomplete.accentColor,
                                size = 26.dp
                            )
                        }
                    } else {
                        IconButton(
                            onClick = onNewTabClick,
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("omnibox_new_tab_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = stringResource(R.string.new_tab),
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        // Render inline highlighted autocomplete suffix behind cursor just like Screenshot 6!
                        if (inlineAutocomplete != null && fieldValue.selection.collapsed && fieldValue.selection.end == fieldValue.text.length) {
                            val suffix = inlineAutocomplete.displayUrl.substring(query.length)
                            Text(
                                text = buildAnnotatedString {
                                    withStyle(SpanStyle(color = Color.Transparent)) {
                                        append(fieldValue.text)
                                    }
                                    withStyle(
                                        SpanStyle(
                                            color = MaterialTheme.colorScheme.onSurface,
                                            background = MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)
                                        )
                                    ) {
                                        append(suffix)
                                    }
                                },
                                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.5.sp),
                                maxLines = 1,
                                overflow = TextOverflow.Clip
                            )
                        }

                        BasicTextField(
                            value = fieldValue,
                            onValueChange = { fieldValue = it },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 16.5.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Uri,
                                imeAction = ImeAction.Go
                            ),
                            keyboardActions = KeyboardActions(
                                onGo = {
                                    val target = inlineAutocomplete?.url ?: query
                                    if (target.isNotBlank()) onSubmitUrl(target)
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester)
                                .testTag("omnibox_text_input"),
                            decorationBox = { inner ->
                                if (fieldValue.text.isEmpty()) {
                                    Text(
                                        text = "Search ${settings.searchEngine.displayName} or type URL",
                                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.5.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                inner()
                            }
                        )
                    }

                    if (fieldValue.text.isNotEmpty()) {
                        IconButton(
                            onClick = { fieldValue = TextFieldValue("") },
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("omnibox_clear_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.cd_clear),
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(21.dp)
                            )
                        }
                    } else {
                        IconButton(
                            onClick = onVoiceSearchClick,
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("omnibox_voice_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Mic,
                                contentDescription = "Voice search",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(21.dp)
                            )
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Current Page Quick Share / Copy / Edit Card (when editing an open webpage)
                if (!activeTab.isNewTab) {
                    item(key = "current_page_card") {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SiteAvatar(
                                    title = activeTab.title,
                                    url = activeTab.url,
                                    favicon = activeTab.favicon,
                                    size = 30.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = activeTab.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = activeTab.url,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = linkBlue,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                IconButton(onClick = onShareCurrentUrl, modifier = Modifier.size(36.dp)) {
                                    Icon(
                                        imageVector = Icons.Outlined.Share,
                                        contentDescription = "Share",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                                IconButton(onClick = onCopyCurrentUrl, modifier = Modifier.size(36.dp)) {
                                    Icon(
                                        imageVector = Icons.Outlined.ContentCopy,
                                        contentDescription = "Copy",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        fieldValue = TextFieldValue(
                                            text = activeTab.url,
                                            selection = TextRange(activeTab.url.length)
                                        )
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Edit,
                                        contentDescription = "Edit URL",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Material 3 Expressive Segmented Suggestion Cards (Exact Screenshots 5 & 6!)
                itemsIndexed(suggestions, key = { _, item -> item.key }) { index, item ->
                    ExpressiveGroupedCard(
                        index = index,
                        totalCount = suggestions.size,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        when (item) {
                            is OmniboxItem.RecentSearch -> {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onSubmitUrl(item.query) }
                                        .padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.History,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(
                                        text = highlightMatch(item.query, query),
                                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.5.sp),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = {
                                            fieldValue = TextFieldValue(item.query, TextRange(item.query.length))
                                        },
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.NorthWest,
                                            contentDescription = "Insert query",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }

                            is OmniboxItem.SiteMatch -> {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onSubmitUrl(item.url) }
                                        .padding(start = 14.dp, end = 8.dp, top = 11.dp, bottom = 11.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    SiteAvatar(
                                        title = item.title,
                                        url = item.url,
                                        accentColor = item.accentColor,
                                        size = 28.dp
                                    )
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = highlightMatch(item.title, query),
                                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.5.sp),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = highlightMatch(item.displayUrl, query),
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.5.sp),
                                            color = linkBlue,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            fieldValue = TextFieldValue(item.displayUrl, TextRange(item.displayUrl.length))
                                        },
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.NorthWest,
                                            contentDescription = "Insert URL",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }

                            is OmniboxItem.OpenTabMatch -> {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onSwitchToTab(item.tab.id) }
                                        .padding(start = 16.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Tab,
                                        contentDescription = null,
                                        tint = linkBlue,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.tab.title,
                                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "Switch to this tab • ${UrlUtils.extractShortDomain(item.tab.url)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = linkBlue,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            is OmniboxItem.DirectSearch -> {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onSubmitUrl(item.query) }
                                        .testTag("omnibox_submit_row")
                                        .padding(start = 16.dp, end = 12.dp, top = 13.dp, bottom = 13.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.query,
                                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.5.sp),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = if (item.isUrl) "Open website" else "${settings.searchEngine.displayName} Search",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

private fun highlightMatch(text: String, query: String): AnnotatedString {
    if (query.isBlank()) return AnnotatedString(text)
    val startIndex = text.indexOf(query, ignoreCase = true)
    if (startIndex < 0) return AnnotatedString(text)
    val endIndex = (startIndex + query.length).coerceAtMost(text.length)
    return buildAnnotatedString {
        append(text.substring(0, startIndex))
        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
            append(text.substring(startIndex, endIndex))
        }
        append(text.substring(endIndex))
    }
}
