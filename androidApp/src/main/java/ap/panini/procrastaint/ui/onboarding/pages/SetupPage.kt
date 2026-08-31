package ap.panini.procrastaint.ui.onboarding.pages

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ap.panini.procrastaint.data.repositories.calendars.CalendarRepository
import ap.panini.procrastaint.data.repositories.calendars.GoogleCalendarRepository
import ap.panini.procrastaint.ui.onboarding.components.OnBoardingItem
import ap.panini.procrastaint.ui.settings.auth.GoogleAuth
import ap.panini.procrastaint.ui.settings.auth.OAuth
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import org.koin.compose.koinInject

@Composable
fun SetupPage(
    modifier: Modifier = Modifier,
    googleAuth: GoogleAuth = koinInject(),
    googleCalendarRepository: GoogleCalendarRepository = koinInject(),
) {
    val context = LocalContext.current

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            SectionDivider("Permissions")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                PermissionRequester(
                    Manifest.permission.POST_NOTIFICATIONS,
                    "Notifications",
                    "Get notified when tasks are due",
                    Intent(
                        Settings.ACTION_APP_NOTIFICATION_SETTINGS
                    ).apply {
                        putExtra(
                            Settings.EXTRA_APP_PACKAGE,
                            context.packageName
                        )
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            PermissionRequester(
                Manifest.permission.READ_CALENDAR,
                "Device Calendar",
                "View on-device calendar events on your timeline",
                null
            )

            Spacer(modifier = Modifier.height(16.dp))

            SectionDivider("Services")

            CalendarSyncRequester(
                "Google Calendar",
                googleCalendarRepository,
                googleAuth
            )
        }
    }
}

@Composable
private fun ColumnScope.SectionDivider(sectionTitle: String) {
    Text(
        text = sectionTitle,
        modifier = Modifier
            .align(Alignment.Start)
            .padding(vertical = 8.dp),
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary
    )
    HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))
}

@Composable
private fun CalendarSyncRequester(
    serviceName: String,
    calendarRepo: CalendarRepository,
    auth: OAuth
) {
    val context = LocalContext.current
    val googleLoggedIn by calendarRepo.isLoggedIn().collectAsStateWithLifecycle(initialValue = false)

    OnBoardingItem(serviceName, "Sync tasks with $serviceName") {
        Button(
            onClick = { auth.auth(context) },
            enabled = !googleLoggedIn,
            shape = MaterialTheme.shapes.small
        ) {
            Text(
                if (!googleLoggedIn) "Connect" else "Connected"
            )
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun PermissionRequester(
    permission: String,
    permName: String,
    permReason: String,
    settingsIntent: Intent?
) {
    var permissionAlreadyRequested by rememberSaveable(key = permission) {
        mutableStateOf(false)
    }

    val permState = rememberPermissionState(permission) {
        permissionAlreadyRequested = true
    }

    val context = LocalContext.current

    OnBoardingItem(
        text = permName,
        subText = permReason
    ) {
        Button(
            onClick = {
                if ((!permissionAlreadyRequested && !permState.status.shouldShowRationale) ||
                    permState.status.shouldShowRationale
                ) {
                    permState.launchPermissionRequest()
                } else if (settingsIntent != null) {
                    context.startActivity(settingsIntent)
                }
            },
            enabled = !permState.status.isGranted,
            shape = MaterialTheme.shapes.small
        ) {
            if (permState.status.isGranted) {
                Text("Granted")
            } else {
                Text("Grant")
            }
        }
    }
}
