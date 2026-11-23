package com.bansify.browser.browser

import android.net.Uri
import android.util.Patterns
import com.bansify.browser.data.BrowserTab
import com.bansify.browser.data.SearchEngine
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object UrlUtils {

    fun resolveInputToUrl(
        rawInput: String,
        searchEngine: SearchEngine,
        httpsFirst: Boolean
    ): String {
        val trimmed = rawInput.trim()
        if (trimmed.isEmpty()) return BrowserTab.NEW_TAB_URL

        // Disallow javascript: injection from Omnibox for safety
        if (trimmed.startsWith("javascript:", ignoreCase = true)) {
            return buildSearchUrl(trimmed, searchEngine)
        }

        if (trimmed.equals("about:blank", ignoreCase = true) ||
            trimmed.equals(BrowserTab.NEW_TAB_URL, ignoreCase = true)
        ) {
            return BrowserTab.NEW_TAB_URL
        }

        // Explicit scheme
        if (trimmed.startsWith("http://", ignoreCase = true)) {
            return if (httpsFirst) {
                "https://" + trimmed.removePrefix("http://").removePrefix("HTTP://")
            } else {
                trimmed
            }
        }
        if (trimmed.startsWith("https://", ignoreCase = true)) {
            return trimmed
        }

        // No whitespace and looks like a domain/host or localhost/IP
        val hasSpaces = trimmed.any { it.isWhitespace() }
        if (!hasSpaces) {
            val isLocalhost = trimmed.startsWith("localhost", ignoreCase = true) ||
                trimmed.matches(Regex("^\\d{1,3}(\\.\\d{1,3}){3}(:\\d+)?(/.*)?$"))
            if (isLocalhost) {
                return "http://$trimmed"
            }

            if (Patterns.WEB_URL.matcher(trimmed).matches() && trimmed.contains('.')) {
                return "https://$trimmed"
            }
        }

        return buildSearchUrl(trimmed, searchEngine)
    }

    fun buildSearchUrl(query: String, searchEngine: SearchEngine): String {
        val encoded = URLEncoder.encode(query.trim(), "UTF-8")
        return searchEngine.queryUrlTemplate.replace("%s", encoded)
    }

    fun extractDisplayHost(url: String): String {
        if (url.isBlank() || url == BrowserTab.NEW_TAB_URL) return ""
        return try {
            val uri = Uri.parse(url)
            val host = uri.host ?: return url
            host.removePrefix("www.").removePrefix("m.")
        } catch (_: Exception) {
            url
        }
    }

    fun extractShortDomain(url: String): String {
        val host = extractDisplayHost(url)
        return host.ifBlank { url }
    }

    fun extractFirstLetter(title: String, url: String): String {
        val host = extractDisplayHost(url)
        val candidate = host.firstOrNull { it.isLetterOrDigit() }
            ?: title.firstOrNull { it.isLetterOrDigit() }
            ?: 'B'
        return candidate.uppercaseChar().toString()
    }

    fun inferDownloadCategory(fileName: String, mimeType: String): String {
        val lowerName = fileName.lowercase(Locale.US)
        val lowerMime = mimeType.lowercase(Locale.US)
        return when {
            lowerMime.startsWith("image/") ||
                lowerName.endsWith(".png") || lowerName.endsWith(".jpg") ||
                lowerName.endsWith(".jpeg") || lowerName.endsWith(".webp") ||
                lowerName.endsWith(".gif") -> "Images"

            lowerMime.contains("html") || lowerMime.contains("mht") ||
                lowerName.endsWith(".mht") || lowerName.endsWith(".html") -> "Pages"

            lowerMime.contains("pdf") || lowerMime.contains("document") ||
                lowerMime.contains("text/") || lowerName.endsWith(".pdf") ||
                lowerName.endsWith(".doc") || lowerName.endsWith(".docx") ||
                lowerName.endsWith(".txt") || lowerName.endsWith(".csv") -> "Documents"

            else -> "Other"
        }
    }

    fun formatFileSize(bytes: Long): String {
        if (bytes <= 0L) return "Unknown size"
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0
        return when {
            gb >= 1.0 -> String.format(Locale.US, "%.2f GB", gb)
            mb >= 1.0 -> String.format(Locale.US, "%.1f MB", mb)
            kb >= 1.0 -> String.format(Locale.US, "%.0f KB", kb)
            else -> "$bytes B"
        }
    }

    fun formatHistorySectionHeader(timestamp: Long): String {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply { timeInMillis = timestamp }
        val sameYear = now.get(Calendar.YEAR) == target.get(Calendar.YEAR)
        val dayDiff = now.get(Calendar.DAY_OF_YEAR) - target.get(Calendar.DAY_OF_YEAR)

        return when {
            sameYear && dayDiff == 0 -> "Today"
            sameYear && dayDiff == 1 -> "Yesterday"
            else -> SimpleDateFormat("EEEE, MMM d, yyyy", Locale.getDefault()).format(Date(timestamp))
        }
    }

    fun formatShortTime(timestamp: Long): String {
        return SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestamp))
    }

    fun formatShortDate(timestamp: Long): String {
        return SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(timestamp))
    }

    fun colorForDomain(url: String): Long {
        val palette = listOf(
            0xFF1A73E8L,
            0xFF188038L,
            0xFFD93025L,
            0xFFE37400L,
            0xFF8430CEL,
            0xFF007B83L,
            0xFFC5221FL,
            0xFF3F51B5L
        )
        val host = extractDisplayHost(url)
        val index = kotlin.math.abs(host.hashCode()) % palette.size
        return palette[index]
    }
}
