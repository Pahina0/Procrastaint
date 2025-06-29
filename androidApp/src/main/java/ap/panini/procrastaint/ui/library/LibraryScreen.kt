package ap.panini.procrastaint.ui.library

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopSearchBar
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ap.panini.procrastaint.ui.calendar.CalendarViewModel
import ap.panini.procrastaint.ui.components.ScreenScaffold
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Destination<RootGraph>
@Composable
fun LibraryScreen(
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel = koinViewModel(),
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value

    ScreenScaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(title = { Text("Library") }, actions = {
                IconButton(onClick = {
                    viewModel.showBottomSheet(true)
                    // show bottom sheet asking for name, color, and label

                    // also make parser parse for #.....
                    // and if it doesn't exist it will create  a new tag
                }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                }
            })
        }
    ) {

        if (state.showBottomSheet) {
            TagBottomSheet(
                state = state,
                updateInfo = { viewModel.onUpdateTag(state.tag.copy(info = it)) },
                updateColor = { /*viewModel.onUpdateTag(state.tag.copy(color = it)) */},
                updateTitle = { viewModel.onUpdateTag(state.tag.copy(title = it)) },
                onDismissRequest = {
                    viewModel.showBottomSheet(false)
                }, onSave = {
                    viewModel.onSave()
                })
        }

    }
}
