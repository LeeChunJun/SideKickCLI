package com.rhyme.sidekick.andropilot.data.local.preferences

import com.rhyme.sidekick.cmdx.datastore.core.DataStore
import com.rhyme.sidekick.cmdx.datastore.preferences.Preferences
import com.rhyme.sidekick.cmdx.datastore.preferences.PreferencesSerializer
import com.rhyme.sidekick.cmdx.datastore.preferences.booleanPreferencesKey
import com.rhyme.sidekick.cmdx.datastore.core.dataStore
import com.rhyme.sidekick.cmdx.datastore.preferences.edit
import com.rhyme.sidekick.cmdx.datastore.preferences.stringPreferencesKey
import com.rhyme.sidekick.cmdx.framework.CLIPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 项目名：SideKickCLI
 * 包名：com.rhyme.sidekick.andropilot.data.local.preferences
 * 描述：App 偏好实现类
 *
 * @author 李春俊-Rhyme
 * @since 21:26 2026/4/26 星期日
 * @version 1.0
 */
class CLIPreferencesImpl: CLIPreferences {

    override val dataStore: DataStore<Preferences> by dataStore<Preferences>(name = "app_preferences.pb", serializer = { PreferencesSerializer })

    companion object {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val API_KEY = stringPreferencesKey("api_key")
    }

    val darkMode: Flow<Boolean> = dataStore.data.map { it[DARK_MODE] ?: false }
    val apiKey: Flow<String?> = dataStore.data.map { it[API_KEY] }

    suspend fun setDarkMode(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[DARK_MODE] = enabled }
    }

    suspend fun setApiKey(key: String?) {
        dataStore.edit { preferences ->
            if (key != null) {
                preferences[API_KEY] = key
            } else {
                preferences.remove(API_KEY)
            }
        }
    }
}