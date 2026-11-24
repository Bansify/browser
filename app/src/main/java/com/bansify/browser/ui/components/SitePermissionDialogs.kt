package com.bansify.browser.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.bansify.browser.data.SitePermission
import com.bansify.browser.viewmodel.PendingSitePermission
import com.bansify.browser.viewmodel.SafeBrowsingWarning

/**
 * "example.com wants to use your camera" — the browser asks, never the page.
 *
 * Two buttons plus a remember switch: Allow once, Allow always, Block. Remembering stores the
 * decision per origin so the dialog does not come back on every visit.
 */
@Composable
fun SitePermissionDialog(
    pending: PendingSitePermission,
    onAnswer: (grant: Boolean, remember: Boolean) -> Unit
) {
    val permission = pending.permission
    AlertDialog(
        onDismissRequest = { onAnswer(false, false) },
        title = { Text("${pending.origin.ifBlank { "This site" }} wants to use your ${permission.label.lowercase()}") },
        text = {
            Text(
                "${permission.description}.\n\nOnly allow this if you trust the site."
            )
        },
        confirmButton = {
            TextButton(onClick = { onAnswer(true, true) }) { Text("Allow") }
        },
        dismissButton = {
            TextButton(onClick = { onAnswer(false, true) }) { Text("Block") }
        }
    )
}

/**
 * Safe Browsing flagged the page. "Back to safety" is the default; continuing is possible but
 * never automatic.
 */
@Composable
fun SafeBrowsingDialog(
    warning: SafeBrowsingWarning,
    onResolve: (proceed: Boolean) -> Unit
) {
    AlertDialog(
        onDismissRequest = { onResolve(false) },
        title = { Text(warning.label) },
        text = {
            Text(
                "Safe Browsing flagged this page. It may try to trick you into installing " +
                    "software or sharing personal information.\n\n${warning.url}"
            )
        },
        confirmButton = {
            TextButton(onClick = { onResolve(false) }) { Text("Back to safety") }
        },
        dismissButton = {
            TextButton(onClick = { onResolve(true) }) { Text("Continue anyway") }
        }
    )
}
