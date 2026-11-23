package com.bansify.browser.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bansify.browser.R
import com.bansify.browser.data.TimeRange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteBrowsingDataScreen(
    historyCount: Int,
    openNormalTabCount: Int,
    onBack: () -> Unit,
    onConfirmClear: (
        timeRange: TimeRange,
        clearHistory: Boolean,
        clearCookies: Boolean,
        clearCache: Boolean,
        clearSiteData: Boolean,
        closeOpenTabs: Boolean
    ) -> Unit
) {
    BackHandler { onBack() }

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Basic, 1 = Advanced
    var timeRange by remember { mutableStateOf(TimeRange.LAST_24_HOURS) }
    var timeMenuExpanded by remember { mutableStateOf(false) }

    var clearHistory by remember { mutableStateOf(true) }
    var clearCookies by remember { mutableStateOf(true) }
    var clearCache by remember { mutableStateOf(true) }
    var clearSiteData by remember { mutableStateOf(false) }
    var closeOpenTabs by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.delete_browsing_data)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 2.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Button(
                        onClick = {
                            onConfirmClear(
                                timeRange,
                                clearHistory,
                                clearCookies,
                                clearCache,
                                clearSiteData,
                                closeOpenTabs
                            )
                        },
                        enabled = clearHistory || clearCookies || clearCache || clearSiteData || closeOpenTabs,
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.testTag("confirm_delete_browsing_data")
                    ) {
                        Text("Delete data")
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Basic") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Advanced") }
                )
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    // Time Range Picker Row (Exact Chrome Layout)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { timeMenuExpanded = true }
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Time range",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Box {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = timeRange.label,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Select time range",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            DropdownMenu(
                                expanded = timeMenuExpanded,
                                onDismissRequest = { timeMenuExpanded = false }
                            ) {
                                TimeRange.entries.forEach { range ->
                                    DropdownMenuItem(
                                        text = { Text(range.label) },
                                        onClick = {
                                            timeRange = range
                                            timeMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }

                item {
                    ClearDataCheckRow(
                        title = "Browsing history",
                        summary = if (historyCount == 0) "No items" else "$historyCount visited items",
                        checked = clearHistory,
                        onCheckedChange = { clearHistory = it }
                    )
                    ClearDataCheckRow(
                        title = "Cookies and site data",
                        summary = "Signs you out of most sites",
                        checked = clearCookies,
                        onCheckedChange = { clearCookies = it }
                    )
                    ClearDataCheckRow(
                        title = "Cached images and files",
                        summary = "Frees up local WebView storage. Some sites may load more slowly on your next visit.",
                        checked = clearCache,
                        onCheckedChange = { clearCache = it }
                    )
                }

                if (selectedTab == 1) {
                    item {
                        ClearDataCheckRow(
                            title = "Web storage & local databases",
                            summary = "Clears HTML5 localStorage, IndexedDB, and WebSQL data",
                            checked = clearSiteData,
                            onCheckedChange = { clearSiteData = it }
                        )
                        ClearDataCheckRow(
                            title = "Open tabs",
                            summary = "$openNormalTabCount open normal tab(s) will be closed",
                            checked = closeOpenTabs,
                            onCheckedChange = { closeOpenTabs = it }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
private fun ClearDataCheckRow(
    title: String,
    summary: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = summary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
