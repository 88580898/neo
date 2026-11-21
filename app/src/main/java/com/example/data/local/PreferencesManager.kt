package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "minimal_fx_prefs")

enum class RateProvider(val id: String, val displayLabel: String) {
    AUTO("AUTO", "自动优选"),
    ER_API("ER_API", "Open ER-API"),
    FRANKFURTER("FRANKFURTER", "Frankfurter (欧洲央行)");

    companion object {
        fun fromId(id: String?): RateProvider =
            entries.firstOrNull { it.id == id } ?: AUTO
    }
}

data class UserPreferences(
    val lastSyncMillis: Long = 0L,
    val lastSyncSource: String = "内置基准汇率",
    val pureOfflineMode: Boolean = false,
    val autoUpdateOnLaunch: Boolean = true,
    val preferredProvider: RateProvider = RateProvider.AUTO,
    val activeCurrencyCode: String = "CNY"
)

class PreferencesManager(private val context: Context) {

    private object Keys {
        val LAST_SYNC_MILLIS = longPreferencesKey("last_sync_millis")
        val LAST_SYNC_SOURCE = stringPreferencesKey("last_sync_source")
        val PURE_OFFLINE_MODE = booleanPreferencesKey("pure_offline_mode")
        val AUTO_UPDATE_ON_LAUNCH = booleanPreferencesKey("auto_update_on_launch")
        val PREFERRED_PROVIDER = stringPreferencesKey("preferred_provider")
        val ACTIVE_CURRENCY_CODE = stringPreferencesKey("active_currency_code")
    }

    val preferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        UserPreferences(
            lastSyncMillis = prefs[Keys.LAST_SYNC_MILLIS] ?: 0L,
            lastSyncSource = prefs[Keys.LAST_SYNC_SOURCE] ?: "内置基准汇率",
            pureOfflineMode = prefs[Keys.PURE_OFFLINE_MODE] ?: false,
            autoUpdateOnLaunch = prefs[Keys.AUTO_UPDATE_ON_LAUNCH] ?: true,
            preferredProvider = RateProvider.fromId(prefs[Keys.PREFERRED_PROVIDER]),
            activeCurrencyCode = prefs[Keys.ACTIVE_CURRENCY_CODE] ?: "CNY"
        )
    }

    suspend fun recordSync(timestampMillis: Long, sourceLabel: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LAST_SYNC_MILLIS] = timestampMillis
            prefs[Keys.LAST_SYNC_SOURCE] = sourceLabel
        }
    }

    suspend fun setPureOfflineMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.PURE_OFFLINE_MODE] = enabled
        }
    }

    suspend fun setAutoUpdateOnLaunch(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.AUTO_UPDATE_ON_LAUNCH] = enabled
        }
    }

    suspend fun setPreferredProvider(provider: RateProvider) {
        context.dataStore.edit { prefs ->
            prefs[Keys.PREFERRED_PROVIDER] = provider.id
        }
    }

    suspend fun setActiveCurrencyCode(code: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ACTIVE_CURRENCY_CODE] = code.uppercase()
        }
    }
}
