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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.DownloadForOffline
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.InsertDriveFile
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bansify.browser.R
import com.bansify.browser.browser.UrlUtils
import com.bansify.browser.data.DownloadEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsScreen(
    downloads: List<DownloadEntity>,
    onClose: () -> Unit,
    onOpenDownloadUrl: (String) -> Unit,
    onShareDownload: (DownloadEntity) -> Unit,
    onDeleteDownload: (Long) -> Unit,
    onOpenDownloadsSettings: () -> Unit,
    downloadProgress: Map<Long, Float> = emptyMap(),
    onOpenFile: (Long) -> Unit = {},
    onCancelDownload: (Long) -> Unit = {},
    onRetryDownload: (Long) -> Unit = {}
) {
    BackHandler { onClose() }

    var searchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Pages", "Images", "Documents", "Other")

    val filtered = remember(downloads, searchQuery, selectedCategory) {
        downloads.filter { dl ->
            val matchesCat = selectedCategory == "All" || dl.category == selectedCategory
            val matchesQuery = searchQuery.isBlank() ||
                dl.fileName.contains(searchQuery, ignoreCase = true) ||
                dl.url.contains(searchQuery, ignoreCase = true)
            matchesCat && matchesQuery
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (searchActive) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search downloads") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(stringResource(R.string.downloads))
                    }
                },
                actions = {
                    IconButton(onClick = {
                        searchActive = !searchActive
                        if (!searchActive) searchQuery = ""
                    }) {
                        Icon(
                            imageVector = if (searchActive) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = stringResource(R.string.cd_search)
                        )
                    }
                    IconButton(onClick = onOpenDownloadsSettings) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Download settings"
                        )
                    }
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.cd_close)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category) }
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            if (filtered.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.DownloadForOffline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No downloads yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Files and saved web pages you download appear here.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(filtered, key = { it.id }) { item ->
                        var itemMenu by remember { mutableStateOf(false) }
                        val icon = when (item.category) {
                            "Pages" -> Icons.Outlined.Language
                            "Images" -> Icons.Outlined.Image
                            "Documents" -> Icons.Outlined.Description
                            else -> Icons.Outlined.InsertDriveFile
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (item.status == "Completed" && item.localPath.isNotBlank()) {
                                        onOpenFile(item.id)
                                    } else {
                                        onOpenDownloadUrl(item.url)
                                    }
                                }
                                .padding(start = 16.dp, end = 6.dp, top = 12.dp, bottom = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.fileName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = buildString {
                                        append(UrlUtils.formatFileSize(item.sizeBytes))
                                        append(" • ")
                                        append(UrlUtils.extractShortDomain(item.url))
                                        append(" • ")
                                        append(
                                            when (item.status) {
                                                "Downloading" -> {
                                                    val pct = ((downloadProgress[item.id] ?: 0f) * 100).toInt()
                                                    "Downloading $pct%"
                                                }
                                                "Failed" -> "Failed"
                                                else -> UrlUtils.formatShortDate(item.downloadedAt)
                                            }
                                        )
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (item.status == "Failed") {
                                        MaterialTheme.colorScheme.error
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (item.status == "Downloading") {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    LinearProgressIndicator(
                                        progress = { downloadProgress[item.id] ?: 0f },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                            Box {
                                IconButton(onClick = { itemMenu = true }) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "Download options",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                DropdownMenu(
                                    expanded = itemMenu,
                                    onDismissRequest = { itemMenu = false }
                                ) {
                                    if (item.status == "Completed" && item.localPath.isNotBlank()) {
                                        DropdownMenuItem(
                                            text = { Text("Open file") },
                                            onClick = {
                                                itemMenu = false
                                                onOpenFile(item.id)
                                            }
                                        )
                                    }
                                    if (item.status == "Downloading") {
                                        DropdownMenuItem(
                                            text = { Text("Cancel download", color = MaterialTheme.colorScheme.error) },
                                            onClick = {
                                                itemMenu = false
                                                onCancelDownload(item.id)
                                            }
                                        )
                                    }
                                    if (item.status == "Failed") {
                                        DropdownMenuItem(
                                            text = { Text("Retry") },
                                            onClick = {
                                                itemMenu = false
                                                onRetryDownload(item.id)
                                            }
                                        )
                                    }
                                    DropdownMenuItem(
                                        text = { Text("Open source page") },
                                        onClick = {
                                            itemMenu = false
                                            onOpenDownloadUrl(item.url)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Share") },
                                        onClick = {
                                            itemMenu = false
                                            onShareDownload(item)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                                        onClick = {
                                            itemMenu = false
                                            onDeleteDownload(item.id)
                                        }
                                    )
                                }
                            }
                        }
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 72.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }
    }
}
