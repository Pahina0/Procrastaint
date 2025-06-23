package ap.panini.procrastaint.ui.settings.components.groups

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.outlined.Attribution
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalUriHandler
import ap.panini.procrastaint.BuildConfig
import ap.panini.procrastaint.ui.settings.components.Section
import ap.panini.procrastaint.ui.settings.components.SettingsItem
import com.ramcosta.composedestinations.generated.destinations.AboutLibrariesScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Composable
fun About(navigator: DestinationsNavigator) {
    val uriHandler = LocalUriHandler.current

    Section("About") {
        SettingsItem(Icons.Outlined.Info, "Version", subtext = BuildConfig.VERSION_NAME)
        SettingsItem(
            Icons.Outlined.Code,
            "Source code",
            subtext = "Help by contributing",
            onClick = {
                uriHandler.openUri("https://github.com/Pahina0/Procrastaint/issues/new")
            }
        )

        SettingsItem(Icons.Outlined.BugReport, "Issues", subtext = "Report a bug", onClick = {
            uriHandler.openUri("https://github.com/Pahina0/Procrastaint")
        })

        SettingsItem(Icons.Outlined.Attribution, "Open source libraries", onClick = {
            navigator.navigate(AboutLibrariesScreenDestination())
        })
    }
}
