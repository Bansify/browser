package com.bansify.browser

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Smoke test for the app shell: the resources the launcher and the manifest depend on must
 * exist and carry the Bansify identity. The template shipped with the AI Studio export
 * asserted the placeholder name "My Application", which failed as soon as the app was
 * branded — keep these values in step with strings.xml and build.gradle.kts.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `app name comes from resources`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertEquals("Bansify Browser", context.getString(R.string.app_name))
    }

    @Test
    fun `application id is the Bansify package`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertEquals("com.bansify.browser", context.packageName)
    }
}
