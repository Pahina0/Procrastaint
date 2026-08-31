package ap.panini.procrastaint.ui.settings.groups

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import ap.panini.procrastaint.ui.settings.components.Section
import ap.panini.procrastaint.ui.settings.components.SettingsItem

@Composable
fun Sync(
    onNavigateToSync: () -> Unit,
    isLoggedInGoogle: Boolean,
    logoutGoogle: () -> Unit,
    loginGoogle: (Context) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Section("Calendar Sync", modifier = modifier) {
        SettingsItem(
            icon = Icons.Outlined.AccountCircle,
            text = "Google Calendar",
            subtext = if (isLoggedInGoogle) "Connected and syncing" else "Not connected",
            trailingItem = {
                if (isLoggedInGoogle) {
                    OutlinedButton(
                        onClick = logoutGoogle,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Disconnect")
                    }
                } else {
                    FilledTonalButton(
                        onClick = { loginGoogle(context) }
                    ) {
                        Text("Connect")
                    }
                }
            }
        )

        if (isLoggedInGoogle) {
            SettingsItem(
                icon = Icons.Outlined.Sync,
                text = "Sync Queue",
                subtext = "View pending calendar sync items",
                onClick = onNavigateToSync,
                trailingItem = {
                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            )
        }
    }
}
