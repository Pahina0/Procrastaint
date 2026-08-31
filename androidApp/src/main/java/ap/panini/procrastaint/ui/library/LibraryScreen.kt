package ap.panini.procrastaint.ui.library

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ap.panini.procrastaint.ui.components.EmptyPage
import ap.panini.procrastaint.ui.components.ScreenScaffold
import ap.panini.procrastaint.ui.components.TagBottomSheet
import ap.panini.procrastaint.ui.components.rememberBottomSheetTagState
import ap.panini.procrastaint.ui.library.components.TagItem
import ap.panini.procrastaint.ui.navigation.Route
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onNavigateToTag: (Route.Tag) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel = koinViewModel(),
) {
    val tags by viewModel.tags.collectAsStateWithLifecycle()
    var showBottomSheet by remember { mutableStateOf(false) }

    val bottomSheetState = rememberBottomSheetTagState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

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
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                scrollBehavior = scrollBehavior,
                title = { Text("Library") },
                actions = {
                    IconButton(onClick = {
                        bottomSheetState.randomReset()
                        showBottomSheet = true
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "New tag")
                    }
                }
            )
        }
    ) { padding ->
        if (tags.isEmpty()) {
            EmptyPage(
                Icons.Outlined.Tag,
                "No tags yet!\nAdd a #tag to your tasks to organize them.",
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp)
            ) {
                items(items = tags, key = { it.tagId }) { tag ->
                    Card(
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .combinedClickable(
                                onLongClick = {
                                    bottomSheetState.setTag(tag)
                                    showBottomSheet = true
                                },
                                onClick = {
                                    onNavigateToTag(Route.Tag(tag.tagId))
                                }
                            ),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    ) {
                        TagItem(tag = tag)
                    }
                }
            }
        }
    }
}
