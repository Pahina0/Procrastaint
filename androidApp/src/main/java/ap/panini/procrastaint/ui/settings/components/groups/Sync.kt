package ap.panini.procrastaint.ui.settings.components.groups

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Autorenew
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import ap.panini.procrastaint.ui.settings.components.Section
import ap.panini.procrastaint.ui.settings.components.SettingsItem
import com.ramcosta.composedestinations.generated.destinations.SyncScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Composable
fun Sync(
    navigator: DestinationsNavigator,
    isLoggedInGoogle: Boolean,
    logoutGoogle: () -> Unit,
    loginGoogle: (Context) -> Unit
) {
    val context = LocalContext.current

    Section("Sync") {

        SettingsItem(
            Icons.Outlined.Autorenew,
            "Google",
            "Sync to Google calendar",
            { navigator.navigate(SyncScreenDestination) }) {

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