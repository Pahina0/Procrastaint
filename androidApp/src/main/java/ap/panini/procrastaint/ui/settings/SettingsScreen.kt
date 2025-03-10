package ap.panini.procrastaint.ui.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ap.panini.procrastaint.ui.settings.auth.GoogleAuth
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Destination<RootGraph>
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = koinViewModel(),
    googleAuth: GoogleAuth = koinInject()
) {
    val context = LocalContext.current
    val state = viewModel.uiState.collectAsStateWithLifecycle().value

    Scaffold(modifier = modifier) {
        Box(modifier = Modifier.padding(it)) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .fillMaxSize()
            ) {
                Button(
                    onClick = {
                        if (state.googleLoggedIn) {
                            viewModel.googleLogout()
                        } else {

                            googleAuth.auth(context)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {

                    if (state.googleLoggedIn) {
                        Text("Logout of Google")

                    } else {
                        Text("Sign in with Google")

                    }
                }
            }
        }
    }
}
