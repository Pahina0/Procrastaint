package ap.panini.procrastaint.ui.library

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.Card
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ap.panini.procrastaint.ui.components.EmptyPage
import ap.panini.procrastaint.ui.components.ScreenScaffold
import ap.panini.procrastaint.ui.components.TagBottomSheet
import ap.panini.procrastaint.ui.components.rememberBottomSheetTagState
import ap.panini.procrastaint.ui.library.components.TagItem
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.TagScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Destination<RootGraph>
@Composable
fun LibraryScreen(
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel = koinViewModel(),
    navigator: DestinationsNavigator
) {
    val tags by viewModel.tags.collectAsStateWithLifecycle()
    var showBottomSheet by remember { mutableStateOf(false) }

    val bottomSheetState = rememberBottomSheetTagState()

    if (showBottomSheet) {
        TagBottomSheet(
            state = bottomSheetState,
            onDismissRequest = {
                bottomSheetState.randomReset()
                showBottomSheet = false
            },
            isValidTag = viewModel::isValidTag,
            onSave = {
                showBottomSheet = false
                bottomSheetState.randomReset()
                viewModel.onSave(it)
            }
        )
    }

    ScreenScaffold(
        modifier = modifier.fillMaxSize(),
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
    ) { padding ->

        if (tags.isEmpty()) {
            EmptyPage(
                Icons.Outlined.Tag,
                "No tags\nWrite a #tag in your next task to get started!"
            )
        }


        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            items(items = tags) {
                Card(
                    modifier = Modifier
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .combinedClickable(
                            onLongClick = {
                                showBottomSheet = true
                                bottomSheetState.setTag(it)
                            },
                            onClick = {
                                navigator.navigate(TagScreenDestination(it.tagId))
                            }
                        )
                ) {
                    TagItem(
                        tag = it,
                    )
                }
            }
        }
    }
}
