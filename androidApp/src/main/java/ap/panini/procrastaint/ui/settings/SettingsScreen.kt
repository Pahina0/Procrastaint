package ap.panini.procrastaint.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ap.panini.procrastaint.ui.components.ScreenScaffold
import ap.panini.procrastaint.ui.settings.auth.GoogleAuth
import ap.panini.procrastaint.ui.settings.components.Section
import ap.panini.procrastaint.ui.settings.components.SettingsItem
import ap.panini.procrastaint.ui.settings.groups.About
import ap.panini.procrastaint.ui.settings.groups.Sync
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToAboutLibraries: () -> Unit,
    onNavigateToSync: () -> Unit,
    onNavigateToParserSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = koinViewModel(),
    googleAuth: GoogleAuth = koinInject()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val scrollState = rememberScrollState()

    ScreenScaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                scrollBehavior = scrollBehavior,
                title = { Text("Settings") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(bottom = 24.dp)
        ) {
            Section("General") {
                SettingsItem(
                    icon = Icons.Outlined.Tune,
                    text = "Date & Time",
                    subtext = "Configure time and date formats, keywords, and display",
                    onClick = onNavigateToParserSettings,
                    trailingItem = {
                        Icon(
                            imageVector = Icons.Outlined.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            Sync(
                onNavigateToSync = onNavigateToSync,
                isLoggedInGoogle = state.googleLoggedIn,
                logoutGoogle = viewModel::googleLogout,
                loginGoogle = googleAuth::auth
            )

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            About(onNavigateToAboutLibraries = onNavigateToAboutLibraries)
        }
    }
}
