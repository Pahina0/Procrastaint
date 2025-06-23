package ap.panini.procrastaint.ui.onboarding

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ap.panini.procrastaint.data.repositories.PreferenceRepository
import ap.panini.procrastaint.data.repositories.calendars.CalendarRepository
import ap.panini.procrastaint.data.repositories.calendars.GoogleCalendarRepository
import ap.panini.procrastaint.ui.onboarding.components.OnBoardingItem
import ap.panini.procrastaint.ui.onboarding.components.OnBoardingScaffold
import ap.panini.procrastaint.ui.settings.auth.GoogleAuth
import ap.panini.procrastaint.ui.settings.auth.OAuth
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.compose.koinInject

@Composable
fun OnBoardingScreen(
    modifier: Modifier = Modifier,
    googleAuth: GoogleAuth = koinInject(),
    googleCalendarRepository: GoogleCalendarRepository = koinInject()
) {
    val preferenceRepository: PreferenceRepository = koinInject()


    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    OnBoardingScaffold(
        "Setup", modifier = modifier,
        completeText = "Complete",
        onComplete = {
            coroutineScope.launch {
                preferenceRepository.putBoolean(
                    PreferenceRepository.ON_BOARDING_COMPLETE,
                    true
                )
            }
        }) {
        Card {
            Column {
                SectionDivider("Permissions")
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    PermissionRequester(
                        android.Manifest.permission.POST_NOTIFICATIONS,
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
                }

                Spacer(modifier = Modifier.padding(10.dp))

                SectionDivider("Services")

                CalendarSyncRequester(
                    "Google",
                    googleCalendarRepository,
                    googleAuth
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.SectionDivider(sectionTitle: String) {
    Text(
        sectionTitle,
        modifier = Modifier
            .align(Alignment.CenterHorizontally)
            .padding(top = 10.dp),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurface
    )
    HorizontalDivider(modifier = Modifier.padding(top = 10.dp))
}

@Composable
private fun CalendarSyncRequester(
    serviceName: String,
    calendarRepo: CalendarRepository,
    auth: OAuth
) {
    val context = LocalContext.current
    val googleLoggedIn =
        calendarRepo.isLoggedIn().collectAsStateWithLifecycle(
            runBlocking {
                calendarRepo.isLoggedIn().first()
            }
        ).value

    OnBoardingItem(serviceName, "Sync tasks with $serviceName calendar") {
        Button(
            onClick = { auth.auth(context) },
            enabled = !googleLoggedIn
        ) {
            Text(
                if (!googleLoggedIn) "Enable" else "Enabled"
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
            enabled = !permState.status.isGranted
        ) {
            if (permState.status.isGranted) {
                Text("Granted")
            } else {
                Text("Grant")
            }
        }

    }
}
