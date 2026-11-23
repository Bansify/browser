package com.bansify.browser

import com.bansify.browser.browser.UrlUtils
import com.bansify.browser.data.BrowserTab
import com.bansify.browser.data.SearchEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Omnibox input resolution. Robolectric is required because [UrlUtils] uses
 * `android.util.Patterns` and `android.net.Uri`.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class UrlUtilsTest {

    private fun resolve(input: String, httpsFirst: Boolean = true) =
        UrlUtils.resolveInputToUrl(input, SearchEngine.GOOGLE, httpsFirst)

    @Test
    fun `blank input opens the new tab page`() {
        assertEquals(BrowserTab.NEW_TAB_URL, resolve(""))
        assertEquals(BrowserTab.NEW_TAB_URL, resolve("   "))
        assertEquals(BrowserTab.NEW_TAB_URL, resolve("bansify://newtab"))
    }

    @Test
    fun `explicit https is kept as typed`() {
        assertEquals("https://bansify.com/drive", resolve("https://bansify.com/drive"))
    }

    @Test
    fun `http is upgraded when https first is on`() {
        assertEquals("https://example.com/page", resolve("http://example.com/page", httpsFirst = true))
    }

    @Test
    fun `http is kept when https first is off`() {
        assertEquals("http://example.com/page", resolve("http://example.com/page", httpsFirst = false))
    }

    @Test
    fun `bare domain becomes https`() {
        assertEquals("https://bansify.com", resolve("bansify.com"))
    }

    @Test
    fun `localhost and ip addresses stay http`() {
        assertEquals("http://localhost:8080", resolve("localhost:8080"))
        assertTrue(resolve("192.168.1.10").startsWith("http://192.168.1.10"))
    }

    @Test
    fun `plain words become a search`() {
        assertEquals(
            "https://www.google.com/search?q=bansify+browser",
            resolve("bansify browser")
        )
    }

    @Test
    fun `javascript urls are never executed`() {
        val result = resolve("javascript:alert(1)")
        assertTrue(result.startsWith("https://www.google.com/search?q="))
    }

    @Test
    fun `search url is built from the engine template`() {
        assertEquals(
            "https://duckduckgo.com/?q=hello+world",
            UrlUtils.buildSearchUrl("hello world", SearchEngine.DUCKDUCKGO)
        )
    }

    @Test
    fun `file sizes are formatted`() {
        assertEquals("Unknown size", UrlUtils.formatFileSize(0))
        assertEquals("900 B", UrlUtils.formatFileSize(900))
        assertEquals("2 KB", UrlUtils.formatFileSize(2048))
        assertEquals("1.5 MB", UrlUtils.formatFileSize((1.5 * 1024 * 1024).toLong()))
    }

    @Test
    fun `downloads are categorised by mime and extension`() {
        assertEquals("Images", UrlUtils.inferDownloadCategory("photo.png", "image/png"))
        assertEquals("Documents", UrlUtils.inferDownloadCategory("notes.txt", "text/plain"))
        assertEquals("Pages", UrlUtils.inferDownloadCategory("page.html", "text/html"))
        assertEquals("Other", UrlUtils.inferDownloadCategory("archive.zip", "application/zip"))
    }
}
