package ap.panini.procrastaint.ui.library

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    var showBottomSheet by remember { mutableStateOf(false) }

    val bottomSheetState = rememberBottomSheetTagState()

    ScreenScaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(title = { Text("Library") }, actions = {
                IconButton(onClick = {
                    showBottomSheet = true
                    // show bottom sheet asking for name, color, and label

                    // also make parser parse for #.....
                    // and if it doesn't exist it will create  a new tag
                }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                }
            })
        }
    ) {

        if (showBottomSheet) {
            TagBottomSheet(
                state = bottomSheetState,
                onDismissRequest = {
                    bottomSheetState.randomReset()
                    showBottomSheet = false
                }, onSave = {
                    showBottomSheet = false
                    bottomSheetState.randomReset()
                    viewModel.onSave(it)
                })
        }

    }
}
