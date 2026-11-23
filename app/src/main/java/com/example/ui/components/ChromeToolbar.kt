package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.BansifyAccountState
import com.example.data.BrowserSettings
import com.example.data.BrowserTab
import com.example.data.FindInPageState
import com.example.data.ToolbarShortcut

/**
 * Pixel-accurate modern Chrome Android toolbar:
 * - On webpage: [Home] [Clean Pill URL Bar] [+ / Shortcut] [Tab Count Box] [3-dot]
 * - On NTP: [Home] ... [Bansify Account Avatar] [Tab Count Box] [3-dot]
 * - Supports both Top and Bottom address bar placement cleanly with system insets.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChromeToolbar(
    activeTab: BrowserTab,
    tabCount: Int,
    settings: BrowserSettings,
    accountState: BansifyAccountState,
    isBookmarked: Boolean,
    findInPage: FindInPageState,
    isBottomPlacement: Boolean,
    overflowMenuExpanded: Boolean,
    onOverflowMenuChange: (Boolean) -> Unit,
    onHomeClick: () -> Unit,
    onAccountClick: () -> Unit,
    onOmniboxClick: () -> Unit,
    onPageInfoClick: () -> Unit,
    onTabSwitcherClick: () -> Unit,
    onNewTabClick: () -> Unit,
    onNewIncognitoTabClick: () -> Unit,
    onCloseActiveTabClick: () -> Unit,
    onShareClick: () -> Unit,
    onToggleBookmarkClick: () -> Unit,
    onFindQueryChange: (String) -> Unit,
    onFindNext: (Boolean) -> Unit,
    onCloseFindInPage: () -> Unit,
    overflowMenuContent: @Composable () -> Unit
) {
    var tabQuickMenuExpanded by remember { mutableStateOf(false) }

    val toolbarBg = if (activeTab.isNewTab) {
        MaterialTheme.colorScheme.background
    } else {
        MaterialTheme.colorScheme.surface
    }

    val insetModifier = if (isBottomPlacement) {
        Modifier.navigationBarsPadding()
    } else {
        Modifier.statusBarsPadding()
    }

    Surface(
        color = toolbarBg,
        tonalElevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = insetModifier) {
            if (isBottomPlacement && !activeTab.isNewTab) {
                ProgressOrDivider(activeTab)
            }

            if (findInPage.isActive) {
                FindInPageBar(
                    state = findInPage,
                    onQueryChange = onFindQueryChange,
                    onFindNext = onFindNext,
                    onClose = onCloseFindInPage
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Home button (shown on both webpage & NTP like Screenshot 4 & 7 when enabled!)
                    if (settings.homepageEnabled) {
                        IconButton(
                            onClick = onHomeClick,
                            modifier = Modifier.testTag("toolbar_home_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Home,
                                contentDescription = stringResource(R.string.cd_home),
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    } else if (activeTab.isNewTab && activeTab.isIncognito) {
                        Box(
                            modifier = Modifier.size(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            IncognitoIcon(
                                size = 22.dp,
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(10.dp))
                    }

                    // Clean Pill Omnibox (matches Screenshot 7: clean domain/path pill!)
                    if (!activeTab.isNewTab) {
                        val displayUrl = remember(activeTab.url) {
                            activeTab.url
                                .removePrefix("https://")
                                .removePrefix("http://")
                                .removePrefix("www.")
                                .trimEnd('/')
                        }

                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clip(RoundedCornerShape(21.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainer)
                                .combinedClickable(
                                    onClick = onOmniboxClick,
                                    onLongClick = onPageInfoClick
                                )
                                .testTag("omnibox_bar")
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (activeTab.isIncognito) {
                                IncognitoIcon(
                                    size = 18.dp,
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            } else if (!activeTab.isSecure) {
                                Icon(
                                    imageVector = Icons.Outlined.WarningAmber,
                                    contentDescription = stringResource(R.string.cd_page_info),
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clickable(onClick = onPageInfoClick)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }

                            Text(
                                text = displayUrl,
                                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.5.sp),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    } else {
                        // On NTP: Home on left, Bansify Account avatar + Tab counter + 3-dot on right
                        if (activeTab.isIncognito) {
                            Spacer(modifier = Modifier.width(4.dp))
                            IncognitoIcon(
                                size = 20.dp,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))

                        if (!activeTab.isIncognito) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .clickable(onClick = onAccountClick)
                                    .testTag("ntp_account_avatar"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (accountState.isSignedIn) {
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(CircleShape)
                                            .background(Color(accountState.avatarColorHex)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = accountState.initialLetter,
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = Icons.Outlined.AccountCircle,
                                        contentDescription = "Bansify Account",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(27.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Toolbar Shortcut icon (Default: '+' New Tab, matching Screenshot 7!)
                    if (!activeTab.isNewTab && settings.toolbarShortcut != ToolbarShortcut.NONE) {
                        when (settings.toolbarShortcut) {
                            ToolbarShortcut.NEW_TAB -> {
                                IconButton(
                                    onClick = onNewTabClick,
                                    modifier = Modifier.testTag("toolbar_shortcut_new_tab")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = stringResource(R.string.new_tab),
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(25.dp)
                                    )
                                }
                            }
                            ToolbarShortcut.SHARE -> {
                                IconButton(
                                    onClick = onShareClick,
                                    modifier = Modifier.testTag("toolbar_shortcut_share")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = stringResource(R.string.share),
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(21.dp)
                                    )
                                }
                            }
                            ToolbarShortcut.BOOKMARK -> {
                                IconButton(
                                    onClick = onToggleBookmarkClick,
                                    modifier = Modifier.testTag("toolbar_shortcut_bookmark")
                                ) {
                                    Icon(
                                        imageVector = if (isBookmarked) Icons.Default.Star else Icons.Outlined.StarBorder,
                                        contentDescription = stringResource(R.string.cd_bookmark),
                                        tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            ToolbarShortcut.NONE -> Unit
                        }
                    }

                    // Tab Switcher Counter Box
                    Box {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .combinedClickable(
                                    onClick = onTabSwitcherClick,
                                    onLongClick = { tabQuickMenuExpanded = true }
                                )
                                .testTag("tab_switcher_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            ChromeTabCountBadge(
                                count = tabCount,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        DropdownMenu(
                            expanded = tabQuickMenuExpanded,
                            onDismissRequest = { tabQuickMenuExpanded = false },
                            shape = RoundedCornerShape(16.dp),
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.new_tab)) },
                                onClick = {
                                    tabQuickMenuExpanded = false
                                    onNewTabClick()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.new_incognito_tab)) },
                                onClick = {
                                    tabQuickMenuExpanded = false
                                    onNewIncognitoTabClick()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Close tab") },
                                onClick = {
                                    tabQuickMenuExpanded = false
                                    onCloseActiveTabClick()
                                }
                            )
                        }
                    }

                    // 3-Dot Overflow Menu Trigger
                    Box {
                        IconButton(
                            onClick = { onOverflowMenuChange(true) },
                            modifier = Modifier
                                .size(44.dp)
                                .testTag("overflow_menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = stringResource(R.string.cd_more_options),
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(23.dp)
                            )
                        }
                        overflowMenuContent()
                    }
                }
            }

            if (!isBottomPlacement) {
                ProgressOrDivider(activeTab)
            }
        }
    }
}

@Composable
private fun ProgressOrDivider(activeTab: BrowserTab) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(2.dp)
    ) {
        if (activeTab.isLoading && activeTab.progress in 1..99 && !activeTab.isNewTab) {
            LinearProgressIndicator(
                progress = { activeTab.progress / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        } else if (!activeTab.isNewTab) {
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
            )
        }
    }
}

@Composable
private fun FindInPageBar(
    state: FindInPageState,
    onQueryChange: (String) -> Unit,
    onFindNext: (Boolean) -> Unit,
    onClose: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(42.dp)
                .clip(RoundedCornerShape(21.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            BasicTextField(
                value = state.query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onFindNext(true) }),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .testTag("find_in_page_input"),
                decorationBox = { inner ->
                    if (state.query.isEmpty()) {
                        Text(
                            text = "Find in page",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    inner()
                }
            )
        }

        AnimatedVisibility(visible = state.query.isNotEmpty()) {
            Text(
                text = "${state.activeMatchOrdinal}/${state.numberOfMatches}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 6.dp)
            )
        }

        IconButton(
            onClick = { onFindNext(false) },
            enabled = state.numberOfMatches > 0
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "Previous match",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        IconButton(
            onClick = { onFindNext(true) },
            enabled = state.numberOfMatches > 0
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Next match",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        IconButton(
            onClick = onClose,
            modifier = Modifier.testTag("find_in_page_close")
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.cd_close),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
