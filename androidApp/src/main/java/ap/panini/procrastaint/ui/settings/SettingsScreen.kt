package ap.panini.procrastaint.ui.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ap.panini.procrastaint.ui.components.ScreenScaffold
import ap.panini.procrastaint.ui.settings.auth.GoogleAuth
import ap.panini.procrastaint.ui.settings.groups.About
import ap.panini.procrastaint.ui.settings.groups.Sync
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToAboutLibraries: () -> Unit,
    onNavigateToSync: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = koinViewModel(),
    googleAuth: GoogleAuth = koinInject()
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value

    ScreenScaffold(modifier = modifier, topBar = {
        TopAppBar(title = { Text("Settings") })
    }) {
        Box(modifier = Modifier.padding(it)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Sync(
                    onNavigateToSync,
                    state.googleLoggedIn,
                    viewModel::googleLogout,
                    googleAuth::auth
                )

                HorizontalDivider()

                About(onNavigateToAboutLibraries)
            }
        }
    }
}
