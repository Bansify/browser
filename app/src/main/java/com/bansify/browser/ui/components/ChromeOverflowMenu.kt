package com.bansify.browser.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.AddBox
import androidx.compose.material.icons.outlined.AddToHomeScreen
import androidx.compose.material.icons.outlined.ChromeReaderMode
import androidx.compose.material.icons.outlined.DashboardCustomize
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.DesktopWindows
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.DownloadDone
import androidx.compose.material.icons.outlined.FindInPage
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bansify.browser.R
import com.bansify.browser.data.BrowserTab
import com.bansify.browser.ui.theme.ChromeDarkMenuCircle
import com.bansify.browser.ui.theme.ChromeDarkMenuSurface
import com.bansify.browser.ui.theme.LocalIsDarkBrowserTheme

/**
 * Pixel-accurate Chrome Android 3-Dot Overflow Menu:
 * - Rendered in the root overlay layer aligned flush to TopEnd (or BottomEnd for bottom toolbar)
 *   with tight 6.dp right edge margin and sleek 256.dp width.
 * - Bounded automatically by available window height (status bar to navigation bar),
 *   so verticalScroll() reaches every bottom item (Desktop site, Settings, Help and feedback) cleanly.
 */
@Composable
fun ChromeOverflowMenu(
    expanded: Boolean,
    activeTab: BrowserTab,
    isBookmarked: Boolean,
    isBottomPlacement: Boolean = false,
    onDismiss: () -> Unit,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onToggleBookmark: () -> Unit,
    onDownloadPage: () -> Unit,
    onReloadOrStop: () -> Unit,
    onNewTab: () -> Unit,
    onNewIncognitoTab: () -> Unit,
    onAddTabToNewGroup: () -> Unit,
    onHistory: () -> Unit,
    onDeleteBrowsingData: () -> Unit,
    onSiteControls: () -> Unit,
    onDownloads: () -> Unit,
    onBookmarks: () -> Unit,
    onRecentTabs: () -> Unit,
    onShare: () -> Unit,
    onFindInPage: () -> Unit,
    onTranslate: () -> Unit,
    onReadingMode: () -> Unit,
    onAddToHomeScreen: () -> Unit,
    onToggleDesktopSite: () -> Unit,
    onSettings: () -> Unit,
    onHelpAndFeedback: () -> Unit
) {
    BackHandler(enabled = expanded) {
        onDismiss()
    }

    val isWebPage = !activeTab.isNewTab
    val isDark = LocalIsDarkBrowserTheme.current
    val menuBg = if (isDark) ChromeDarkMenuSurface else MaterialTheme.colorScheme.surfaceContainerLow
    val circleBg = if (isDark) ChromeDarkMenuCircle else MaterialTheme.colorScheme.surfaceContainerHigh
    val circleDisabledBg = if (isDark) Color(0xFF3E4044) else MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.65f)
    val dividerColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
    val scrollState = rememberScrollState()

    LaunchedEffect(expanded) {
        if (expanded) {
            scrollState.scrollTo(0)
        }
    }

    AnimatedVisibility(
        visible = expanded,
        enter = fadeIn(tween(130)),
        exit = fadeOut(tween(110))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.16f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(
                    top = if (isBottomPlacement) 12.dp else 6.dp,
                    end = 6.dp,
                    bottom = if (isBottomPlacement) 6.dp else 12.dp
                ),
            contentAlignment = if (isBottomPlacement) Alignment.BottomEnd else Alignment.TopEnd
        ) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = menuBg,
                shadowElevation = 16.dp,
                tonalElevation = 4.dp,
                modifier = Modifier
                    .width(256.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
                    .testTag("chrome_overflow_menu")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .padding(top = 8.dp, bottom = 10.dp)
                ) {
                    // Exact Chrome Top 5 Circular Buttons Strip (Back, Forward, Star, Download, Reload)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MenuCircleAction(
                            icon = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back),
                            enabled = activeTab.canGoBack || isWebPage,
                            activeBg = circleBg,
                            disabledBg = circleDisabledBg,
                            testTag = "menu_back",
                            onClick = {
                                onDismiss()
                                onBack()
                            }
                        )

                        MenuCircleAction(
                            icon = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = stringResource(R.string.cd_forward),
                            enabled = activeTab.canGoForward,
                            activeBg = circleBg,
                            disabledBg = circleDisabledBg,
                            testTag = "menu_forward",
                            onClick = {
                                onDismiss()
                                onForward()
                            }
                        )

                        MenuCircleAction(
                            icon = if (isBookmarked) Icons.Default.Star else Icons.Outlined.StarBorder,
                            contentDescription = stringResource(R.string.cd_bookmark),
                            enabled = isWebPage,
                            activeBg = circleBg,
                            disabledBg = circleDisabledBg,
                            iconTint = if (isBookmarked && isWebPage) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            testTag = "menu_bookmark_star",
                            onClick = {
                                onDismiss()
                                onToggleBookmark()
                            }
                        )

                        MenuCircleAction(
                            icon = Icons.Outlined.Download,
                            contentDescription = stringResource(R.string.cd_download_page),
                            enabled = isWebPage,
                            activeBg = circleBg,
                            disabledBg = circleDisabledBg,
                            testTag = "menu_download_page",
                            onClick = {
                                onDismiss()
                                onDownloadPage()
                            }
                        )

                        MenuCircleAction(
                            icon = if (activeTab.isLoading) Icons.Default.Close else Icons.Default.Refresh,
                            contentDescription = stringResource(if (activeTab.isLoading) R.string.cd_stop else R.string.cd_reload),
                            enabled = isWebPage,
                            activeBg = circleBg,
                            disabledBg = circleDisabledBg,
                            testTag = "menu_reload",
                            onClick = {
                                onDismiss()
                                onReloadOrStop()
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Section 1: Tabs & Groups
                    ChromeMenuRow(
                        icon = Icons.Outlined.AddBox,
                        label = "New tab",
                        testTag = "menu_new_tab",
                        onClick = {
                            onDismiss()
                            onNewTab()
                        }
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .clickable {
                                onDismiss()
                                onNewIncognitoTab()
                            }
                            .testTag("menu_new_incognito_tab")
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IncognitoIcon(
                            size = 20.dp,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(15.dp))
                        Text(
                            text = "New Incognito tab",
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    ChromeMenuRow(
                        icon = Icons.Outlined.DashboardCustomize,
                        label = "Add tab to new group",
                        testTag = "menu_add_tab_group",
                        onClick = {
                            onDismiss()
                            onAddTabToNewGroup()
                        }
                    )

                    MenuSectionDivider(dividerColor)

                    // Section 2: History, Clear Data, Site Controls
                    ChromeMenuRow(
                        icon = Icons.Outlined.History,
                        label = "History",
                        testTag = "menu_history",
                        onClick = {
                            onDismiss()
                            onHistory()
                        }
                    )

                    ChromeMenuRow(
                        icon = Icons.Outlined.DeleteOutline,
                        label = "Delete browsing data",
                        testTag = "menu_delete_browsing_data",
                        onClick = {
                            onDismiss()
                            onDeleteBrowsingData()
                        }
                    )

                    if (isWebPage) {
                        ChromeMenuRow(
                            icon = Icons.Outlined.Tune,
                            label = "Site controls",
                            testTag = "menu_site_controls",
                            onClick = {
                                onDismiss()
                                onSiteControls()
                            }
                        )
                    }

                    MenuSectionDivider(dividerColor)

                    // Section 3: Downloads, Bookmarks, Recent tabs
                    ChromeMenuRow(
                        icon = Icons.Outlined.DownloadDone,
                        label = "Downloads",
                        testTag = "menu_downloads",
                        onClick = {
                            onDismiss()
                            onDownloads()
                        }
                    )

                    ChromeMenuRow(
                        icon = Icons.Default.Star,
                        label = "Bookmarks",
                        testTag = "menu_bookmarks",
                        onClick = {
                            onDismiss()
                            onBookmarks()
                        }
                    )

                    ChromeMenuRow(
                        icon = Icons.Outlined.Devices,
                        label = "Recent tabs",
                        testTag = "menu_recent_tabs",
                        onClick = {
                            onDismiss()
                            onRecentTabs()
                        }
                    )

                    if (isWebPage) {
                        MenuSectionDivider(dividerColor)

                        // Section 4: Page Actions
                        ChromeMenuRow(
                            icon = Icons.Outlined.Share,
                            label = "Share...",
                            testTag = "menu_share",
                            onClick = {
                                onDismiss()
                                onShare()
                            }
                        )

                        ChromeMenuRow(
                            icon = Icons.Outlined.FindInPage,
                            label = "Find in page",
                            testTag = "menu_find_in_page",
                            onClick = {
                                onDismiss()
                                onFindInPage()
                            }
                        )

                        ChromeMenuRow(
                            icon = Icons.Outlined.Translate,
                            label = "Translate...",
                            testTag = "menu_translate",
                            onClick = {
                                onDismiss()
                                onTranslate()
                            }
                        )

                        ChromeMenuRow(
                            icon = Icons.Outlined.ChromeReaderMode,
                            label = "Show Reading mode",
                            testTag = "menu_reading_mode",
                            onClick = {
                                onDismiss()
                                onReadingMode()
                            }
                        )

                        ChromeMenuRow(
                            icon = Icons.Outlined.AddToHomeScreen,
                            label = "Install and create shortcut",
                            testTag = "menu_add_to_home_screen",
                            onClick = {
                                onDismiss()
                                onAddToHomeScreen()
                            }
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .clickable {
                                    onDismiss()
                                    onToggleDesktopSite()
                                }
                                .testTag("menu_desktop_site")
                                .padding(start = 16.dp, end = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.DesktopWindows,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(15.dp))
                            Text(
                                text = "Desktop site",
                                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Checkbox(
                                checked = activeTab.isDesktopSite,
                                onCheckedChange = {
                                    onDismiss()
                                    onToggleDesktopSite()
                                }
                            )
                        }
                    }

                    MenuSectionDivider(dividerColor)

                    // Section 5: Settings & Help
                    ChromeMenuRow(
                        icon = Icons.Outlined.Settings,
                        label = "Settings",
                        testTag = "menu_settings",
                        onClick = {
                            onDismiss()
                            onSettings()
                        }
                    )

                    ChromeMenuRow(
                        icon = Icons.Outlined.HelpOutline,
                        label = "Help and feedback",
                        testTag = "menu_help_about",
                        onClick = {
                            onDismiss()
                            onHelpAndFeedback()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MenuCircleAction(
    icon: ImageVector,
    contentDescription: String,
    enabled: Boolean,
    activeBg: Color,
    disabledBg: Color,
    iconTint: Color = MaterialTheme.colorScheme.onSurface,
    testTag: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(if (enabled) activeBg else disabledBg)
            .clickable(enabled = enabled, onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (enabled) iconTint else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun MenuSectionDivider(color: Color) {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
        thickness = 0.6.dp,
        color = color
    )
}

@Composable
private fun ChromeMenuRow(
    icon: ImageVector,
    label: String,
    testTag: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .clickable(onClick = onClick)
            .testTag(testTag)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(15.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
