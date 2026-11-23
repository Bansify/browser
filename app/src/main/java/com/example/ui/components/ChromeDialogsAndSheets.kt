package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cookie
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.browser.UrlUtils
import com.example.data.BookmarkEntity
import com.example.data.BrowserTab
import com.example.data.JsDialogState
import com.example.data.ShortcutEntity
import com.example.ui.theme.SecureGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageInfoBottomSheet(
    tab: BrowserTab,
    cookiesAllowed: Boolean,
    onDismiss: () -> Unit,
    onClearSiteData: () -> Unit,
    onOpenSiteSettings: () -> Unit,
    onShareUrl: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 20.dp)
        ) {
            // URL Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Text(
                    text = UrlUtils.extractDisplayHost(tab.url).ifBlank { tab.title },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = tab.url,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // Connection security row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (tab.isSecure) Icons.Outlined.Lock else Icons.Outlined.WarningAmber,
                    contentDescription = null,
                    tint = if (tab.isSecure) SecureGreen else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = if (tab.isSecure) "Connection is secure" else "Connection is not secure",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (tab.isSecure) {
                            "Your information (for example, passwords or credit card numbers) is private when sent to this site."
                        } else {
                            "You should not enter sensitive information on this site (for example, passwords or cards)."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Cookies & site data
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClearSiteData)
                    .testTag("page_info_clear_cookies")
                    .padding(horizontal = 24.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Cookie,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Cookies and site data",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (cookiesAllowed && !tab.isIncognito) "Cookies allowed • Tap to clear" else "Restricted in this tab • Tap to clear",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = "Clear site data",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Site Settings
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onDismiss()
                        onOpenSiteSettings()
                    }
                    .testTag("page_info_site_settings")
                    .padding(horizontal = 24.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Site settings",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Share
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onDismiss()
                        onShareUrl()
                    }
                    .padding(horizontal = 24.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Share,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Share link",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun BookmarkEditDialog(
    bookmark: BookmarkEntity,
    onDismiss: () -> Unit,
    onSave: (id: Long, title: String, url: String, folder: String) -> Unit,
    onDelete: ((Long) -> Unit)? = null
) {
    var title by remember(bookmark) { mutableStateOf(bookmark.title) }
    var url by remember(bookmark) { mutableStateOf(bookmark.url) }
    var folder by remember(bookmark) { mutableStateOf(bookmark.folder) }
    val folders = listOf("Mobile bookmarks", "Favorites", "Reading list")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = if (bookmark.id == 0L) "Add bookmark" else "Edit bookmark")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("URL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Folder",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    folders.forEach { option ->
                        FilterChip(
                            selected = folder == option,
                            onClick = { folder = option },
                            label = {
                                Text(
                                    text = option.replace(" bookmarks", ""),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (url.isNotBlank()) {
                        val normalized = if (url.startsWith("http://") || url.startsWith("https://")) url else "https://$url"
                        onSave(bookmark.id, title.ifBlank { normalized }, normalized, folder)
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Row {
                if (bookmark.id != 0L && onDelete != null) {
                    TextButton(onClick = {
                        onDelete(bookmark.id)
                        onDismiss()
                    }) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}

@Composable
fun ShortcutDialog(
    initial: ShortcutEntity? = null,
    onDismiss: () -> Unit,
    onSave: (title: String, url: String) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var title by remember(initial) { mutableStateOf(initial?.title.orEmpty()) }
    var url by remember(initial) { mutableStateOf(initial?.url.orEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Add shortcut" else "Edit shortcut") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("URL") },
                    placeholder = { Text("https://example.com") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (url.isNotBlank()) {
                        onSave(title.trim(), url.trim())
                    }
                }
            ) {
                Text("Done")
            }
        },
        dismissButton = {
            Row {
                if (initial != null && onDelete != null) {
                    TextButton(onClick = {
                        onDelete()
                        onDismiss()
                    }) {
                        Text("Remove", color = MaterialTheme.colorScheme.error)
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}

@Composable
fun WebViewJsDialog(
    dialog: JsDialogState,
    onDismiss: () -> Unit
) {
    when (dialog) {
        is JsDialogState.Alert -> {
            AlertDialog(
                onDismissRequest = {
                    dialog.result.cancel()
                    onDismiss()
                },
                title = { Text("${UrlUtils.extractDisplayHost(dialog.url).ifBlank { "Page" }} says") },
                text = { Text(dialog.message) },
                confirmButton = {
                    TextButton(onClick = {
                        dialog.result.confirm()
                        onDismiss()
                    }) {
                        Text("OK")
                    }
                }
            )
        }

        is JsDialogState.Confirm -> {
            AlertDialog(
                onDismissRequest = {
                    dialog.result.cancel()
                    onDismiss()
                },
                title = { Text("${UrlUtils.extractDisplayHost(dialog.url).ifBlank { "Page" }} says") },
                text = { Text(dialog.message) },
                confirmButton = {
                    TextButton(onClick = {
                        dialog.result.confirm()
                        onDismiss()
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        dialog.result.cancel()
                        onDismiss()
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }

        is JsDialogState.Prompt -> {
            var input by remember(dialog) { mutableStateOf(dialog.defaultValue) }
            AlertDialog(
                onDismissRequest = {
                    dialog.result.cancel()
                    onDismiss()
                },
                title = { Text("${UrlUtils.extractDisplayHost(dialog.url).ifBlank { "Page" }} says") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(dialog.message)
                        OutlinedTextField(
                            value = input,
                            onValueChange = { input = it },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        dialog.result.confirm(input)
                        onDismiss()
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        dialog.result.cancel()
                        onDismiss()
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
