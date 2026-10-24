package ap.panini.procrastaint.ui.settings.groups

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ap.panini.procrastaint.ui.settings.components.Section
import ap.panini.procrastaint.ui.settings.components.SettingsItem

@Composable
fun Sync(
    onNavigateToSync: () -> Unit,
    isLoggedInGoogle: Boolean,
    showGoogleCalendar: Boolean,
    onToggleGoogleCalendar: (Boolean) -> Unit,
    logoutGoogle: () -> Unit,
    loginGoogle: (Context) -> Unit,
    hasDeviceCalendarPermission: Boolean,
    showDeviceCalendar: Boolean,
    onToggleDeviceCalendar: (Boolean) -> Unit,
    onRequestDeviceCalendarPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Section("Calendars & Accounts", modifier = modifier) {
        // Device Calendar Setting
        SettingsItem(
            icon = Icons.Outlined.CalendarMonth,
            text = "Device Calendar",
            subtext = when {
                !hasDeviceCalendarPermission -> "Connect to show events from local device calendars"
                showDeviceCalendar -> "Events visible on your agenda"
                else -> "Events hidden from your agenda"
            },
            trailingItem = {
                if (!hasDeviceCalendarPermission) {
                    FilledTonalButton(onClick = onRequestDeviceCalendarPermission) {
                        Text("Connect")
                    }
                } else {
                    Switch(
                        checked = showDeviceCalendar,
                        onCheckedChange = onToggleDeviceCalendar
                    )
                }
            }
        )

        // Google Calendar Setting
        SettingsItem(
            icon = Icons.Outlined.AccountCircle,
            text = "Google Calendar",
            subtext = when {
                !isLoggedInGoogle -> "Sync tasks and view Google Calendar events"
                showGoogleCalendar -> "Events visible on your agenda"
                else -> "Events hidden from your agenda"
            },
            trailingItem = {
                if (!isLoggedInGoogle) {
                    FilledTonalButton(onClick = { loginGoogle(context) }) {
                        Text("Connect")
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Switch(
                            checked = showGoogleCalendar,
                            onCheckedChange = onToggleGoogleCalendar
                        )
                        OutlinedButton(
                            onClick = logoutGoogle,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Logout")
                        }
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
