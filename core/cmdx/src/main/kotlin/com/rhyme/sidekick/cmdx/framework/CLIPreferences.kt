package com.rhyme.sidekick.cmdx.framework

import com.rhyme.sidekick.cmdx.datastore.core.DataStore
import com.rhyme.sidekick.cmdx.datastore.preferences.Preferences

/**
 * 项目名：SideKickCLI
 * 包名：com.rhyme.sidekick.framework
 * 描述：App 偏好
 *
 * @author 李春俊-Rhyme
 * @since 21:24 2026/4/26 星期日
 * @version 1.0
 */
interface CLIPreferences {
    val dataStore : DataStore<Preferences>
}