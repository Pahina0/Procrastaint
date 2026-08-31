package ap.panini.procrastaint.ui.settings.groups

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Attribution
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Spellcheck
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import ap.panini.procrastaint.BuildConfig
import ap.panini.procrastaint.ui.settings.components.Section
import ap.panini.procrastaint.ui.settings.components.SettingsItem

@Composable
fun About(
    onNavigateToAboutLibraries: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current

    Section("About", modifier = modifier) {
        SettingsItem(
            icon = Icons.Outlined.Info,
            text = "Version",
            subtext = BuildConfig.VERSION_NAME
        )

        SettingsItem(
            icon = Icons.Outlined.Code,
            text = "Source code",
            subtext = "Help contribute on GitHub",
            onClick = {
                uriHandler.openUri("https://github.com/Pahina0/Procrastaint")
            },
            trailingItem = {
                Icon(
                    imageVector = Icons.Outlined.OpenInNew,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp)
                )
            }
        )

        SettingsItem(
            icon = Icons.Outlined.Spellcheck,
            text = "Text parser source code",
            subtext = "kwhen natural language date parser",
            onClick = {
                uriHandler.openUri("https://github.com/Pahina0/kwhen")
            },
            trailingItem = {
                Icon(
                    imageVector = Icons.Outlined.OpenInNew,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp)
                )
            }
        )

        SettingsItem(
            icon = Icons.Outlined.BugReport,
            text = "Report an issue",
            subtext = "Submit a bug report or feature request",
            onClick = {
                uriHandler.openUri("https://github.com/Pahina0/Procrastaint/issues/new")
            },
            trailingItem = {
                Icon(
                    imageVector = Icons.Outlined.OpenInNew,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp)
                )
            }
        )

        SettingsItem(
            icon = Icons.Outlined.Attribution,
            text = "Open source libraries",
            subtext = "Licenses of third-party dependencies",
            onClick = onNavigateToAboutLibraries,
            trailingItem = {
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
        )
    }
}
