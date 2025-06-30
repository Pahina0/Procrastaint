package ap.panini.procrastaint.ui.library

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ap.panini.procrastaint.data.entities.TaskTag
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
    val tags = viewModel.tags.collectAsStateWithLifecycle().value
    var showBottomSheet by remember { mutableStateOf(false) }

    LaunchedEffect(tags) {
        println(tags)
    }

    val bottomSheetState = rememberBottomSheetTagState()

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
                            onClick = {}
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .padding(10.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.Tag,
                            contentDescription = null,
                            tint = it.toRgb().let { c -> Color(c.first, c.second, c.third) }
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                itemVerticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    it.displayTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Text(
                                    "#${it.title}",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }

                            if (it.info.isNotBlank()) {
                                Text(it.info, style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}
