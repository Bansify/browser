package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.BansifyAccountState
import com.example.data.BookmarkEntity
import com.example.ui.components.SiteAvatar
import com.example.ui.theme.LocalIsDarkBrowserTheme
import com.example.ui.theme.SecureGreen
import com.example.ui.theme.SecureGreenDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksScreen(
    bookmarks: List<BookmarkEntity>,
    accountState: BansifyAccountState,
    onClose: () -> Unit,
    onOpenUrl: (String) -> Unit,
    onOpenInNewTab: (String, Boolean) -> Unit,
    onAddBookmark: () -> Unit,
    onEditBookmark: (BookmarkEntity) -> Unit,
    onDeleteBookmark: (Long) -> Unit,
    onCopyUrl: (String) -> Unit,
    onOpenSyncSettings: () -> Unit
) {
    BackHandler { onClose() }

    var searchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFolder by remember { mutableStateOf("All") }
    val folders = listOf("All", "Mobile bookmarks", "Favorites", "Reading list")

    val filtered = remember(bookmarks, searchQuery, selectedFolder) {
        bookmarks.filter { bm ->
            val matchesFolder = selectedFolder == "All" || bm.folder == selectedFolder
            val matchesQuery = searchQuery.isBlank() ||
                bm.title.contains(searchQuery, ignoreCase = true) ||
                bm.url.contains(searchQuery, ignoreCase = true)
            matchesFolder && matchesQuery
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
                            placeholder = { Text("Search bookmarks") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(stringResource(R.string.bookmarks))
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddBookmark,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add bookmark")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Subtle Bansify Account Bookmarks Sync strip
            val syncActive = accountState.effectiveBookmarksSync
            val greenColor = if (LocalIsDarkBrowserTheme.current) SecureGreenDark else SecureGreen
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenSyncSettings)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (syncActive) Icons.Outlined.CloudDone else Icons.Outlined.CloudOff,
                        contentDescription = null,
                        tint = if (syncActive) greenColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = when {
                            !accountState.isSignedIn -> "Sign in to Bansify to sync bookmarks"
                            syncActive -> "Synced with ${accountState.email}"
                            else -> "Bookmarks sync is off • Tap to manage"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = if (syncActive) greenColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(folders) { folder ->
                    FilterChip(
                        selected = selectedFolder == folder,
                        onClick = { selectedFolder = folder },
                        label = { Text(folder) }
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
                            imageVector = Icons.Outlined.BookmarkBorder,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "No matching bookmarks" else "No bookmarks yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap the star icon in the 3-dot menu to save pages here.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(filtered, key = { it.id }) { bm ->
                        var itemMenuExpanded by remember { mutableStateOf(false) }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenUrl(bm.url) }
                                .padding(start = 16.dp, end = 4.dp, top = 10.dp, bottom = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SiteAvatar(title = bm.title, url = bm.url, size = 38.dp)
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = bm.title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = bm.url,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Box {
                                IconButton(onClick = { itemMenuExpanded = true }) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "Bookmark options",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                DropdownMenu(
                                    expanded = itemMenuExpanded,
                                    onDismissRequest = { itemMenuExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Open in new tab") },
                                        onClick = {
                                            itemMenuExpanded = false
                                            onOpenInNewTab(bm.url, false)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Open in Incognito tab") },
                                        onClick = {
                                            itemMenuExpanded = false
                                            onOpenInNewTab(bm.url, true)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Edit") },
                                        onClick = {
                                            itemMenuExpanded = false
                                            onEditBookmark(bm)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Copy link") },
                                        onClick = {
                                            itemMenuExpanded = false
                                            onCopyUrl(bm.url)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                                        onClick = {
                                            itemMenuExpanded = false
                                            onDeleteBookmark(bm.id)
                                        }
                                    )
                                }
                            }
                        }
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 68.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }
    }
}
