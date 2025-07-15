package ap.panini.procrastaint.ui.settings.groups

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AlternateEmail
import androidx.compose.material.icons.outlined.Attribution
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Spellcheck
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalUriHandler
import ap.panini.procrastaint.BuildConfig
import ap.panini.procrastaint.ui.settings.components.Section
import ap.panini.procrastaint.ui.settings.components.SettingsItem

@Composable
fun About(
    onNavigateToAboutLibraries: () -> Unit
) {
    val uriHandler = LocalUriHandler.current

    Section("About") {
        SettingsItem(Icons.Outlined.Info, "Version", subtext = BuildConfig.VERSION_NAME)
        SettingsItem(
            Icons.Outlined.Code,
            "Source code",
            subtext = "Help contribute",
            onClick = {
                uriHandler.openUri("https://github.com/Pahina0/Procrastaint")
            }
        )

        SettingsItem(
            Icons.Outlined.Spellcheck,
            "Text parser source code",
            subtext = "See how times are pulled from your tasks",
            onClick = {
                uriHandler.openUri("https://github.com/Pahina0/kwhen")
            }
        )

        SettingsItem(Icons.Outlined.BugReport, "Issues", subtext = "Report a bug", onClick = {
            uriHandler.openUri("https://github.com/Pahina0/Procrastaint/issues/new")
        })

        SettingsItem(Icons.Outlined.Attribution, "Open source libraries", onClick = {
            onNavigateToAboutLibraries()
        })

        SettingsItem(
            Icons.Outlined.AlternateEmail,
            "Username: pahina",
            subtext = "Add me on Discord! (I don't have a server)"
        )
    }
}
