package com.bansify.browser.browser

/**
 * User-agent handling for a real browser built on Android WebView.
 *
 * WebView's stock UA carries two tokens that bot checks (Cloudflare Turnstile, reCAPTCHA,
 * hCaptcha) treat as "this is an embedded app view, not a browser":
 *
 *   `...; wv) AppleWebKit/... Version/4.0 Chrome/120.0.0.0 Mobile Safari/537.36`
 *          ^^                          ^^^^^^^^^^^^
 *
 * Stripping them makes the browser present itself the way Chrome on Android does, which is
 * exactly what it is: a real browser the user drives by hand. Everything else (engine
 * version, platform, mobile token) is kept, so the UA still matches the engine that is
 * actually rendering the page — an inconsistent UA is itself a bot signal.
 */
object UserAgentUtils {

    /**
     * Removes the WebView-only markers while keeping the real engine version.
     * Safe to call twice; returns the input unchanged if it carries neither marker.
     */
    fun normalize(rawUserAgent: String): String {
        var ua = rawUserAgent
        // `; wv` sits inside the platform parentheses: "(Linux; Android 13; Pixel 7; wv)".
        ua = ua.replace("; wv)", ")")
        ua = ua.replace("; wv;", ";")
        ua = ua.replace(" wv)", ")")
        // Legacy Chromium token that only WebView emits.
        ua = ua.replace("Version/4.0 ", "")
        return ua.trim()
    }

    /**
     * Builds a desktop UA from the device's own UA so the engine version always matches.
     * Returns [mobileUserAgent] unchanged when it cannot find the platform section.
     */
    fun toDesktop(mobileUserAgent: String): String {
        val versionPart = mobileUserAgent.substringAfter("(KHTML, like Gecko)", "")
            .trim()
            .replace(" Mobile", "")
        if (versionPart.isBlank()) return mobileUserAgent
        return "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) $versionPart"
    }

    /** True when the UA still looks like an embedded WebView. */
    fun looksLikeWebView(userAgent: String): Boolean =
        userAgent.contains("; wv)") || userAgent.contains("Version/4.0")
}
