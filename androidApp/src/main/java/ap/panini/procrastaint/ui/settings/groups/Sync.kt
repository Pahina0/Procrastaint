package ap.panini.procrastaint.ui.settings.groups

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Autorenew
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import ap.panini.procrastaint.ui.settings.components.Section
import ap.panini.procrastaint.ui.settings.components.SettingsItem
import com.ramcosta.composedestinations.generated.destinations.SyncScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Sync(
    navigator: DestinationsNavigator,
    isLoggedInGoogle: Boolean,
    logoutGoogle: () -> Unit,
    loginGoogle: (Context) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Section("Sync", modifier = modifier) {
        SettingsItem(
            Icons.Outlined.Autorenew,
            "Google",
            subtext = "Sync to Google calendar",
            onClick = { navigator.navigate(SyncScreenDestination) }
        ) {
            Button(
                onClick = {
                    if (isLoggedInGoogle) {
                        logoutGoogle()
                    } else {
                        loginGoogle(context)
                    }
                },
            ) {
                if (isLoggedInGoogle) {
                    Text("Logout")
                } else {
                    Text("Login")
                }
            }
        }
    }
}
