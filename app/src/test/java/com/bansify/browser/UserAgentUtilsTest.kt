package com.bansify.browser

import com.bansify.browser.browser.UserAgentUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UserAgentUtilsTest {

    private val webViewUa =
        "Mozilla/5.0 (Linux; Android 13; Pixel 7; wv) AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Version/4.0 Chrome/120.0.6099.144 Mobile Safari/537.36"

    private val chromeUa =
        "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/120.0.6099.144 Mobile Safari/537.36"

    @Test
    fun `webview markers are removed`() {
        assertEquals(chromeUa, UserAgentUtils.normalize(webViewUa))
        assertFalse(UserAgentUtils.looksLikeWebView(UserAgentUtils.normalize(webViewUa)))
    }

    @Test
    fun `normalising twice changes nothing`() {
        val once = UserAgentUtils.normalize(webViewUa)
        assertEquals(once, UserAgentUtils.normalize(once))
    }

    @Test
    fun `a normal chrome ua is left alone`() {
        assertEquals(chromeUa, UserAgentUtils.normalize(chromeUa))
    }

    @Test
    fun `desktop ua keeps the engine version and drops mobile`() {
        val desktop = UserAgentUtils.toDesktop(chromeUa)
        assertTrue(desktop.startsWith("Mozilla/5.0 (X11; Linux x86_64)"))
        assertTrue(desktop.contains("Chrome/120.0.6099.144"))
        assertFalse(desktop.contains("Mobile"))
    }

    @Test
    fun `desktop conversion falls back when the ua has no platform section`() {
        assertEquals("garbage", UserAgentUtils.toDesktop("garbage"))
    }
}
