package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.browser.UrlUtils
import com.example.data.BansifyAccountState
import com.example.ui.components.ExpressiveGroupedCard
import com.example.ui.components.expressiveCanvasColor
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.LocalIsDarkBrowserTheme
import com.example.ui.theme.SecureGreen
import com.example.ui.theme.SecureGreenDark

private val BansifyAvatarColors = listOf(
    0xFF1A73E8L,
    0xFF188038L,
    0xFF8430CEL,
    0xFFD93025L,
    0xFFE37400L,
    0xFF007B83L
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BansifyAccountScreen(
    accountState: BansifyAccountState,
    bookmarksCount: Int,
    historyCount: Int,
    openTabsCount: Int,
    shortcutsCount: Int,
    downloadsCount: Int,
    onBack: () -> Unit,
    onSignIn: (displayName: String, email: String, avatarColorHex: Long) -> Unit,
    onUpdateProfile: (displayName: String, email: String, avatarColorHex: Long) -> Unit,
    onSignOut: () -> Unit,
    onUpdateSync: ((BansifyAccountState) -> BansifyAccountState) -> Unit,
    onSyncNow: () -> Unit
) {
    BackHandler { onBack() }

    val canvasBg = expressiveCanvasColor()
    var showEditProfileDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = canvasBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(if (accountState.isSignedIn) "Bansify Account & sync" else "Sign in to Bansify")
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("bansify_account_back")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
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
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            if (!accountState.isSignedIn) {
                item(key = "sign_in_card") {
                    BansifySignInCard(onSignIn = onSignIn)
                }
            } else {
                // Hero Signed-In Profile + Sync Now Card
                item(key = "signed_in_hero") {
                    val syncedColor = if (LocalIsDarkBrowserTheme.current) SecureGreenDark else SecureGreen
                    ExpressiveGroupedCard(index = 0, totalCount = 1) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(Color(accountState.avatarColorHex)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = accountState.initialLetter,
                                        color = Color.White,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = accountState.displayName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = accountState.email,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (accountState.activeSyncCategoriesCount > 0) Icons.Outlined.CloudDone else Icons.Outlined.CloudOff,
                                            contentDescription = null,
                                            tint = if (accountState.activeSyncCategoriesCount > 0) syncedColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        val syncText = when {
                                            accountState.isSyncing -> "Syncing with Bansify…"
                                            accountState.activeSyncCategoriesCount == 0 -> "Sync paused (0 of 6 categories)"
                                            accountState.lastSyncedAt > 0L ->
                                                "Synced ${accountState.activeSyncCategoriesCount}/6 • ${UrlUtils.formatShortTime(accountState.lastSyncedAt)}"
                                            else -> "Sync active (${accountState.activeSyncCategoriesCount}/6)"
                                        }
                                        Text(
                                            text = syncText,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (accountState.activeSyncCategoriesCount > 0) syncedColor else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                IconButton(onClick = { showEditProfileDialog = true }) {
                                    Icon(
                                        imageVector = Icons.Outlined.Edit,
                                        contentDescription = "Edit profile",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                FilledTonalButton(
                                    onClick = onSyncNow,
                                    enabled = !accountState.isSyncing,
                                    shape = RoundedCornerShape(22.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("bansify_sync_now_button")
                                ) {
                                    if (accountState.isSyncing) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Outlined.Sync,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(if (accountState.isSyncing) "Syncing..." else "Sync now")
                                }

                                OutlinedButton(
                                    onClick = onSignOut,
                                    shape = RoundedCornerShape(22.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("bansify_sign_out_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Outlined.Logout,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Sign out")
                                }
                            }
                        }
                    }
                }

                // Manage what syncs (kya kya sync ho raha hai, kya nahi)
                item(key = "sync_header") {
                    Spacer(modifier = Modifier.height(14.dp))
                    ExpressiveSettingsSectionTitle("Manage what you sync")
                }

                item(key = "sync_master") {
                    SyncSwitchRow(
                        index = 0,
                        totalCount = 7,
                        title = "Sync everything",
                        subtitle = if (accountState.syncEverything) {
                            "All 6 Bansify categories are syncing automatically"
                        } else {
                            "Custom sync • Choose individual categories below"
                        },
                        statusBadge = if (accountState.syncEverything) "All ON" else "${accountState.activeSyncCategoriesCount}/6 ON",
                        isActive = accountState.syncEverything,
                        checked = accountState.syncEverything,
                        enabled = true,
                        onCheckedChange = { enabled ->
                            onUpdateSync { it.copy(syncEverything = enabled) }
                        }
                    )
                }

                item(key = "sync_bookmarks") {
                    val active = accountState.effectiveBookmarksSync
                    SyncSwitchRow(
                        index = 1,
                        totalCount = 7,
                        title = "Bookmarks",
                        subtitle = "$bookmarksCount saved bookmark(s) & folders",
                        statusBadge = if (active) "Syncing" else "Not syncing",
                        isActive = active,
                        checked = active,
                        enabled = !accountState.syncEverything,
                        onCheckedChange = { enabled ->
                            onUpdateSync { it.copy(syncBookmarks = enabled) }
                        }
                    )
                }

                item(key = "sync_history") {
                    val active = accountState.effectiveHistorySync
                    SyncSwitchRow(
                        index = 2,
                        totalCount = 7,
                        title = "History",
                        subtitle = "$historyCount visited page(s) across normal tabs",
                        statusBadge = if (active) "Syncing" else "Not syncing",
                        isActive = active,
                        checked = active,
                        enabled = !accountState.syncEverything,
                        onCheckedChange = { enabled ->
                            onUpdateSync { it.copy(syncHistory = enabled) }
                        }
                    )
                }

                item(key = "sync_tabs") {
                    val active = accountState.effectiveTabsSync
                    SyncSwitchRow(
                        index = 3,
                        totalCount = 7,
                        title = "Open tabs",
                        subtitle = "$openTabsCount open normal tab(s) & tab groups",
                        statusBadge = if (active) "Syncing" else "Not syncing",
                        isActive = active,
                        checked = active,
                        enabled = !accountState.syncEverything,
                        onCheckedChange = { enabled ->
                            onUpdateSync { it.copy(syncOpenTabs = enabled) }
                        }
                    )
                }

                item(key = "sync_shortcuts") {
                    val active = accountState.effectiveShortcutsSync
                    SyncSwitchRow(
                        index = 4,
                        totalCount = 7,
                        title = "Home shortcuts",
                        subtitle = "$shortcutsCount quick shortcut(s) on New Tab page",
                        statusBadge = if (active) "Syncing" else "Not syncing",
                        isActive = active,
                        checked = active,
                        enabled = !accountState.syncEverything,
                        onCheckedChange = { enabled ->
                            onUpdateSync { it.copy(syncShortcuts = enabled) }
                        }
                    )
                }

                item(key = "sync_settings") {
                    val active = accountState.effectiveSettingsSync
                    SyncSwitchRow(
                        index = 5,
                        totalCount = 7,
                        title = "Settings & preferences",
                        subtitle = "Search engine, theme, address bar & site settings",
                        statusBadge = if (active) "Syncing" else "Not syncing",
                        isActive = active,
                        checked = active,
                        enabled = !accountState.syncEverything,
                        onCheckedChange = { enabled ->
                            onUpdateSync { it.copy(syncSettings = enabled) }
                        }
                    )
                }

                item(key = "sync_downloads") {
                    val active = accountState.effectiveDownloadsSync
                    SyncSwitchRow(
                        index = 6,
                        totalCount = 7,
                        title = "Downloads history",
                        subtitle = "$downloadsCount downloaded file record(s)",
                        statusBadge = if (active) "Syncing" else "Not syncing",
                        isActive = active,
                        checked = active,
                        enabled = !accountState.syncEverything,
                        onCheckedChange = { enabled ->
                            onUpdateSync { it.copy(syncDownloadsList = enabled) }
                        }
                    )
                }

                // Developer / Architecture note card
                item(key = "sync_arch_note") {
                    Spacer(modifier = Modifier.height(14.dp))
                    ExpressiveGroupedCard(index = 0, totalCount = 1) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "Demo Bansify Account Architecture",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Local demo account & sync state is persisted via BansifyAccountProvider. Swap DemoBansifyAccountProvider with your real Bansify API client later to enable live multi-device cloud sync.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(28.dp)) }
        }
    }

    if (showEditProfileDialog) {
        EditBansifyProfileDialog(
            accountState = accountState,
            onDismiss = { showEditProfileDialog = false },
            onSave = { name, email, colorHex ->
                onUpdateProfile(name, email, colorHex)
                showEditProfileDialog = false
            }
        )
    }
}

@Composable
private fun BansifySignInCard(
    onSignIn: (displayName: String, email: String, avatarColorHex: Long) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var selectedColor by remember { mutableLongStateOf(BansifyAvatarColors.first()) }

    ExpressiveGroupedCard(index = 0, totalCount = 1) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(selectedColor)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name.firstOrNull { it.isLetterOrDigit() }?.uppercaseChar()?.toString() ?: "B",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Sign in to Bansify",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Get your bookmarks, history, open tabs, and settings synced with your Bansify Account.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
            ) {
                Text(
                    text = "Demo Account Structure • Ready for Real Bansify API",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full name") },
                placeholder = { Text("e.g. Alex Bansify") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("bansify_signin_name")
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Bansify email") },
                placeholder = { Text("e.g. alex@bansify.com") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("bansify_signin_email")
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
            ) {
                BansifyAvatarColors.forEach { hex ->
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Color(hex))
                            .then(
                                if (selectedColor == hex) {
                                    Modifier.border(2.5.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                } else {
                                    Modifier
                                }
                            )
                            .clickable { selectedColor = hex }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    val resolvedName = name.trim().ifBlank { "Bansify User" }
                    val resolvedEmail = email.trim().ifBlank { "user@bansify.com" }
                    onSignIn(resolvedName, resolvedEmail, selectedColor)
                },
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("bansify_signin_submit")
            ) {
                Text("Sign in with Bansify")
            }

            Spacer(modifier = Modifier.height(10.dp))

            FilledTonalButton(
                onClick = {
                    onSignIn("Bansify Demo", "demo@bansify.com", BrandBlue.value.toLong().let { 0xFF1A73E8L })
                },
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("bansify_quick_demo_signin")
            ) {
                Text("Quick Demo Login (demo@bansify.com)")
            }
        }
    }
}

@Composable
private fun SyncSwitchRow(
    index: Int,
    totalCount: Int,
    title: String,
    subtitle: String,
    statusBadge: String,
    isActive: Boolean,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val activeGreen = if (LocalIsDarkBrowserTheme.current) SecureGreenDark else SecureGreen
    ExpressiveGroupedCard(index = index, totalCount = totalCount) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled) { onCheckedChange(!checked) }
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isActive) {
                            activeGreen.copy(alpha = 0.16f)
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerHighest
                        }
                    ) {
                        Text(
                            text = statusBadge,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isActive) activeGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BansifyServicesScreen(
    accountState: BansifyAccountState,
    onBack: () -> Unit,
    onUpdateSync: ((BansifyAccountState) -> BansifyAccountState) -> Unit
) {
    BackHandler { onBack() }
    val canvasBg = expressiveCanvasColor()

    Scaffold(
        containerColor = canvasBg,
        topBar = {
            TopAppBar(
                title = { Text("Bansify services") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
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
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            item {
                ExpressiveSettingsSectionTitle("Bansify cloud & search services")
            }
            item {
                BansifyServiceSwitchCard(
                    index = 0,
                    totalCount = 3,
                    title = "Autocomplete searches and URLs",
                    subtitle = "Sends typed queries in the address bar to match sites and suggestions faster",
                    checked = accountState.autocompleteSearches,
                    onCheckedChange = { v -> onUpdateSync { it.copy(autocompleteSearches = v) } }
                )
            }
            item {
                BansifyServiceSwitchCard(
                    index = 1,
                    totalCount = 3,
                    title = "Improve search suggestions",
                    subtitle = "Prioritize your recent searches, bookmarks, and open tabs in the Omnibox",
                    checked = accountState.improveSearchSuggestions,
                    onCheckedChange = { v -> onUpdateSync { it.copy(improveSearchSuggestions = v) } }
                )
            }
            item {
                BansifyServiceSwitchCard(
                    index = 2,
                    totalCount = 3,
                    title = "Help improve Bansify's features",
                    subtitle = "Share anonymous diagnostic info once real Bansify backend is connected",
                    checked = accountState.usageReportsEnabled,
                    onCheckedChange = { v -> onUpdateSync { it.copy(usageReportsEnabled = v) } }
                )
            }
        }
    }
}

@Composable
private fun BansifyServiceSwitchCard(
    index: Int,
    totalCount: Int,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ExpressiveGroupedCard(index = index, totalCount = totalCount) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCheckedChange(!checked) }
                .padding(horizontal = 20.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
private fun EditBansifyProfileDialog(
    accountState: BansifyAccountState,
    onDismiss: () -> Unit,
    onSave: (displayName: String, email: String, avatarColorHex: Long) -> Unit
) {
    var name by remember(accountState) { mutableStateOf(accountState.displayName) }
    var email by remember(accountState) { mutableStateOf(accountState.email) }
    var colorHex by remember(accountState) { mutableLongStateOf(accountState.avatarColorHex) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Bansify profile") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Bansify email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BansifyAvatarColors.forEach { hex ->
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(hex))
                                .then(
                                    if (colorHex == hex) {
                                        Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                    } else {
                                        Modifier
                                    }
                                )
                                .clickable { colorHex = hex }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(name, email, colorHex) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
