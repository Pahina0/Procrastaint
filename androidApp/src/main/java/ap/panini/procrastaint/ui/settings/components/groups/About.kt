package ap.panini.procrastaint.ui.settings.components.groups

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.Composable
import ap.panini.procrastaint.BuildConfig
import ap.panini.procrastaint.ui.settings.components.Section
import ap.panini.procrastaint.ui.settings.components.SettingsItem

@Composable
fun About() {
    Section("About") {
        SettingsItem(Icons.Outlined.Info, "Version", BuildConfig.VERSION_NAME)
    }
}
