package com.bansify.browser.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONObject

/** A capability a website can ask the browser for. */
enum class SitePermission(val label: String, val description: String) {
    CAMERA("Camera", "Use your camera (video calls, scanning, uploads)"),
    MICROPHONE("Microphone", "Use your microphone (calls, voice search)"),
    LOCATION("Location", "Know your location (maps, local results)"),
    NOTIFICATIONS("Notifications", "Send you notifications");
}

/** One remembered decision: this origin may (or may not) use this permission. */
data class SitePermissionDecision(
    val origin: String,
    val permission: SitePermission,
    val granted: Boolean
)

private val Context.sitePermissionDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "site_permissions"
)

/**
 * Remembers "allow once / allow always / block" per site and permission.
 *
 * Stored as one JSON object in DataStore: `{ "example.com": ["CAMERA", "LOCATION"] }`.
 * Only granted permissions are stored, so clearing an entry means "ask again".
 */
class SitePermissionStore(private val context: Context) {

    private companion object {
        val DECISIONS = stringPreferencesKey("decisions")
    }

    val decisions: Flow<List<SitePermissionDecision>> = context.sitePermissionDataStore.data.map { prefs ->
        decode(prefs[DECISIONS])
    }

    suspend fun decisionFor(origin: String, permission: SitePermission): Boolean? {
        val raw = context.sitePermissionDataStore.data.first()[DECISIONS]
        return decode(raw).firstOrNull { it.origin == origin && it.permission == permission }?.granted
    }

    suspend fun remember(origin: String, permission: SitePermission, granted: Boolean) {
        context.sitePermissionDataStore.edit { prefs ->
            val current = decode(prefs[DECISIONS]).toMutableList()
            current.removeAll { it.origin == origin && it.permission == permission }
            if (granted) current.add(SitePermissionDecision(origin, permission, true))
            prefs[DECISIONS] = encode(current)
        }
    }

    suspend fun forget(origin: String) {
        context.sitePermissionDataStore.edit { prefs ->
            val current = decode(prefs[DECISIONS]).filterNot { it.origin == origin }
            prefs[DECISIONS] = encode(current)
        }
    }

    suspend fun forgetAll() {
        context.sitePermissionDataStore.edit { prefs -> prefs.remove(DECISIONS) }
    }

    private fun decode(raw: String?): List<SitePermissionDecision> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching {
            val root = JSONObject(raw)
            buildList {
                root.keys().forEach { origin ->
                    val arr = root.optJSONArray(origin) ?: return@forEach
                    for (i in 0 until arr.length()) {
                        val name = arr.optString(i)
                        SitePermission.entries.firstOrNull { it.name == name }?.let {
                            add(SitePermissionDecision(origin, it, true))
                        }
                    }
                }
            }
        }.getOrDefault(emptyList())
    }

    private fun encode(list: List<SitePermissionDecision>): String {
        val root = JSONObject()
        list.filter { it.granted }
            .groupBy { it.origin }
            .forEach { (origin, perms) ->
                root.put(origin, org.json.JSONArray(perms.map { it.permission.name }))
            }
        return root.toString()
    }
}
