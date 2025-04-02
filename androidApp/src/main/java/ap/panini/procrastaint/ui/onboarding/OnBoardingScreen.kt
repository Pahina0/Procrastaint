package ap.panini.procrastaint.ui.onboarding

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ap.panini.procrastaint.data.repositories.PreferenceRepository
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnBoardingScreen(modifier: Modifier = Modifier) {
    val preferenceRepository: PreferenceRepository = koinInject()

    val landingComplete = preferenceRepository.getBoolean(PreferenceRepository.ON_BOARDING_COMPLETE)
        .collectAsStateWithLifecycle(false).value

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    if (!landingComplete) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            topBar = {
                TopAppBar(title = { Text("Setup") })
            }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
            ) {
                Column(modifier = Modifier.padding(15.dp)) {
                    Card {
                        Column {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                PermissionRequester(
                                    android.Manifest.permission.POST_NOTIFICATIONS,
                                    "Notifications",
                                    "Get notified when tasks are due",
                                    Intent(
                                        Settings.ACTION_APP_NOTIFICATION_SETTINGS
                                    ).apply {
                                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                    }
                                )
                            }
                        }
                    }
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            coroutineScope.launch {
                                preferenceRepository.putBoolean(
                                    PreferenceRepository.ON_BOARDING_COMPLETE,
                                    true
                                )
                            }
                        }
                    ) {
                        Text("Complete")
                    }
                }
            }
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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(15.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(permName)
            Text(
                permReason,
                style = MaterialTheme.typography.labelSmall
            )
        }

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
