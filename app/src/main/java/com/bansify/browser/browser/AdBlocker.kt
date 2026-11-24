package com.bansify.browser.browser

import android.content.Context
import android.webkit.WebResourceResponse
import com.bansify.browser.R
import java.io.ByteArrayInputStream
import java.util.concurrent.atomic.AtomicLong

/**
 * Host-based ad and tracker blocker.
 *
 * The list ships with the app (`res/raw/adblock_hosts.txt`) so blocking works offline and
 * nothing about the pages you open leaves the device. It is a curated starter list of
 * well-known ad, analytics and tracker hosts — not a full EasyList replacement — and every
 * entry is matched on the host name only, so a CDN that also serves site assets is never
 * touched.
 *
 * A request is blocked when its host equals an entry or is a subdomain of one. Main-frame
 * requests are never blocked: the page the user asked for always loads.
 */
class AdBlocker(private val context: Context) {

    private var hosts: Set<String> = emptySet()

    companion object {
        private val blocked = AtomicLong(0)

        /** Requests blocked since the app started, across every tab. */
        fun blockedCount(): Long = blocked.get()

        fun resetBlockedCount() {
            blocked.set(0)
        }
    }

    private fun ensureLoaded(): Set<String> {
        val cached = hosts
        if (cached.isNotEmpty()) return cached
        val loaded = runCatching {
            context.resources.openRawResource(R.raw.adblock_hosts).bufferedReader().useLines { lines ->
                lines.map { it.trim().lowercase() }
                    .filter { it.isNotEmpty() && !it.startsWith("#") }
                    .toSet()
            }
        }.getOrDefault(emptySet())
        hosts = loaded
        return loaded
    }

    fun isBlockedHost(host: String?): Boolean {
        if (host.isNullOrBlank()) return false
        val list = ensureLoaded()
        if (list.isEmpty()) return false
        var candidate = host.lowercase().trimEnd('.')
        while (true) {
            if (list.contains(candidate)) return true
            val dot = candidate.indexOf('.')
            if (dot < 0 || dot == candidate.length - 1) return false
            candidate = candidate.substring(dot + 1)
        }
    }

    fun shouldBlock(url: String, isForMainFrame: Boolean): Boolean {
        if (isForMainFrame) return false
        val host = runCatching { android.net.Uri.parse(url).host }.getOrNull() ?: return false
        val blockedNow = isBlockedHost(host)
        if (blockedNow) blocked.incrementAndGet()
        return blockedNow
    }

    /** Empty 200 that stops the request without an error dialog on the page. */
    fun emptyResponse(): WebResourceResponse =
        WebResourceResponse("text/plain", "utf-8", ByteArrayInputStream(ByteArray(0)))
}
