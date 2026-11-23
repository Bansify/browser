package com.bansify.browser.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bansify.browser.R
import com.bansify.browser.browser.UrlUtils
import com.bansify.browser.data.BrowserTab
import com.bansify.browser.ui.components.ChromeTabCountBadge
import com.bansify.browser.ui.components.IncognitoIcon
import com.bansify.browser.ui.components.SiteAvatar
import com.bansify.browser.ui.theme.IncognitoBackground
import com.bansify.browser.ui.theme.IncognitoOnSurface
import com.bansify.browser.ui.theme.IncognitoOnSurfaceVariant
import com.bansify.browser.ui.theme.IncognitoPrimary
import com.bansify.browser.ui.theme.IncognitoSurfaceContainer
import com.bansify.browser.ui.theme.IncognitoSurfaceContainerHigh

@Composable
fun TabSwitcherScreen(
    tabs: List<BrowserTab>,
    activeTabId: String,
    initialShowIncognito: Boolean,
    onDismiss: () -> Unit,
    onSelectTab: (String) -> Unit,
    onCloseTab: (String) -> Unit,
    onCreateNewTab: (isIncognito: Boolean) -> Unit,
    onCloseAllTabs: (incognitoOnly: Boolean) -> Unit,
    onOpenSettings: () -> Unit
) {
    BackHandler { onDismiss() }

    var showingIncognito by remember(initialShowIncognito) { mutableStateOf(initialShowIncognito) }
    var menuExpanded by remember { mutableStateOf(false) }

    val normalTabs = remember(tabs) { tabs.filter { !it.isIncognito } }
    val incognitoTabs = remember(tabs) { tabs.filter { it.isIncognito } }
    val displayedTabs = if (showingIncognito) incognitoTabs else normalTabs

    val bgColor = if (showingIncognito) IncognitoBackground else MaterialTheme.colorScheme.surfaceContainerLow
    val cardBgColor = if (showingIncognito) IncognitoSurfaceContainer else MaterialTheme.colorScheme.surface
    val textColor = if (showingIncognito) IncognitoOnSurface else MaterialTheme.colorScheme.onSurface
    val mutedColor = if (showingIncognito) IncognitoOnSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant
    val activeBorderColor = if (showingIncognito) IncognitoPrimary else MaterialTheme.colorScheme.primary

    Surface(
        color = bgColor,
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Chrome Tab Switcher Top Bar (+ button | Segmented Normal/Incognito Switcher | 3-Dot Menu)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onCreateNewTab(showingIncognito) },
                    modifier = Modifier.testTag("tab_switcher_add_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.new_tab),
                        tint = textColor,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Centered Chrome Segmented Normal / Incognito Pill
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = if (showingIncognito) IncognitoSurfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainerHighest,
                    modifier = Modifier.height(40.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Normal Tabs Segment
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    if (!showingIncognito) MaterialTheme.colorScheme.surface
                                    else androidx.compose.ui.graphics.Color.Transparent
                                )
                                .clickable { showingIncognito = false }
                                .testTag("tab_switcher_normal_segment")
                                .padding(horizontal = 18.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            ChromeTabCountBadge(
                                count = normalTabs.size,
                                color = if (!showingIncognito) MaterialTheme.colorScheme.primary else mutedColor
                            )
                        }

                        // Incognito Tabs Segment
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    if (showingIncognito) IncognitoBackground
                                    else androidx.compose.ui.graphics.Color.Transparent
                                )
                                .clickable { showingIncognito = true }
                                .testTag("tab_switcher_incognito_segment")
                                .padding(horizontal = 18.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            IncognitoIcon(
                                size = 20.dp,
                                tint = if (showingIncognito) IncognitoOnSurface else mutedColor
                            )
                            if (incognitoTabs.isNotEmpty()) {
                                Text(
                                    text = incognitoTabs.size.toString(),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (showingIncognito) IncognitoOnSurface else mutedColor
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.testTag("tab_switcher_menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.cd_more_options),
                            tint = textColor
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.new_tab)) },
                            onClick = {
                                menuExpanded = false
                                onCreateNewTab(false)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.new_incognito_tab)) },
                            onClick = {
                                menuExpanded = false
                                onCreateNewTab(true)
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    if (showingIncognito) "Close all Incognito tabs" else "Close all tabs"
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onCloseAllTabs(showingIncognito)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.settings)) },
                            onClick = {
                                menuExpanded = false
                                onOpenSettings()
                            }
                        )
                    }
                }
            }

            if (displayedTabs.isEmpty()) {
                // Empty Incognito or Normal Tabs State
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(
                                    if (showingIncognito) IncognitoSurfaceContainerHigh
                                    else MaterialTheme.colorScheme.surfaceContainerHigh
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (showingIncognito) {
                                IncognitoIcon(size = 38.dp, tint = textColor)
                            } else {
                                Icon(
                                    imageVector = Icons.Outlined.Public,
                                    contentDescription = null,
                                    tint = textColor,
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (showingIncognito) "No Incognito tabs" else "No open tabs",
                            style = MaterialTheme.typography.titleMedium,
                            color = textColor
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (showingIncognito) {
                                "Browse privately without saving history or cookies on this device."
                            } else {
                                "Tap + to start a new tab."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = mutedColor,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(onClick = { onCreateNewTab(showingIncognito) }) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (showingIncognito) "New Incognito tab" else "New tab")
                        }
                    }
                }
            } else {
                // Authentic Chrome 2-Column Tab Thumbnail Grid
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 158.dp),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(displayedTabs, key = { it.id }) { tab ->
                        val isActive = tab.id == activeTabId
                        TabGridCard(
                            tab = tab,
                            isActive = isActive,
                            cardBgColor = cardBgColor,
                            textColor = textColor,
                            mutedColor = mutedColor,
                            activeBorderColor = activeBorderColor,
                            onSelect = { onSelectTab(tab.id) },
                            onClose = { onCloseTab(tab.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TabGridCard(
    tab: BrowserTab,
    isActive: Boolean,
    cardBgColor: androidx.compose.ui.graphics.Color,
    textColor: androidx.compose.ui.graphics.Color,
    mutedColor: androidx.compose.ui.graphics.Color,
    activeBorderColor: androidx.compose.ui.graphics.Color,
    onSelect: () -> Unit,
    onClose: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        border = if (isActive) BorderStroke(2.5.dp, activeBorderColor) else BorderStroke(0.5.dp, mutedColor.copy(alpha = 0.25f)),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isActive) 4.dp else 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(216.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onSelect)
            .testTag("tab_card_${tab.id}")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Card Header Bar (Favicon + Title + Close X)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .padding(start = 10.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (tab.isIncognito && tab.isNewTab) {
                    IncognitoIcon(size = 16.dp, tint = textColor)
                } else {
                    SiteAvatar(
                        title = tab.title,
                        url = tab.url,
                        favicon = tab.favicon,
                        size = 20.dp
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = tab.title.ifBlank { if (tab.isIncognito) "New Incognito tab" else "New tab" },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium,
                    color = textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close tab",
                        tint = mutedColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Tab Group Chip (if tab belongs to a group)
            if (!tab.groupName.isNullOrBlank()) {
                val badgeColor = androidx.compose.ui.graphics.Color(tab.groupColor ?: 0xFF8AB4F8L)
                Row(
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .padding(bottom = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(badgeColor.copy(alpha = 0.22f))
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(badgeColor)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = tab.groupName,
                        style = MaterialTheme.typography.labelSmall,
                        color = textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Tab Thumbnail Body
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 4.dp, end = 4.dp, bottom = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (tab.isIncognito) IncognitoBackground
                        else MaterialTheme.colorScheme.surfaceContainerLowest
                    ),
                contentAlignment = Alignment.Center
            ) {
                val thumb = remember(tab.thumbnail) { tab.thumbnail?.asImageBitmap() }
                if (thumb != null && !tab.isNewTab) {
                    Image(
                        bitmap = thumb,
                        contentDescription = tab.title,
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.TopCenter,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (tab.isIncognito) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IncognitoIcon(size = 34.dp, tint = mutedColor)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (tab.isNewTab) "Incognito" else UrlUtils.extractShortDomain(tab.url),
                            style = MaterialTheme.typography.labelSmall,
                            color = mutedColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        SiteAvatar(
                            title = tab.title,
                            url = tab.url,
                            favicon = tab.favicon,
                            size = 40.dp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (tab.isNewTab) "Bansify" else UrlUtils.extractShortDomain(tab.url),
                            style = MaterialTheme.typography.labelSmall,
                            color = mutedColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            }
        }
    }
}
