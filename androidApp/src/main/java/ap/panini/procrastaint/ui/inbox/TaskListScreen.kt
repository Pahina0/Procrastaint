package ap.panini.procrastaint.ui.inbox

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import ap.panini.procrastaint.data.model.Task
import ap.panini.procrastaint.data.repositories.PreferenceRepository
import ap.panini.procrastaint.ui.theme.SlightlyDeemphasizedAlpha

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun UpcomingScreen(
    upcomingState: TaskListScreenModel.State,
    onCompleteTask: (Task) -> Unit,
    options: TaskListScreenModel.Options,
    changeFilterOptions: (key: String, to: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    var showDisplayOptions by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Upcoming") },
                scrollBehavior = scrollBehavior,
                actions = {
                    IconButton(onClick = { showDisplayOptions = true }) {
                        Icon(
                            imageVector = Icons.Outlined.FilterList,
                            contentDescription = "Change how upcoming items are displayed"
                        )
                    }
                }
            )
        },
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(
                items = upcomingState.tasks,
                key = { it.id }
            ) {
                TaskItem(
                    task = it,
                    modifier = Modifier.animateItemPlacement(),
                    check = onCompleteTask
                )
            }
        }

        if (showDisplayOptions) {
            DisplayOptionsSheet(
                options = options,
                onDismissRequest = { showDisplayOptions = false },
                changeFilterOptions = changeFilterOptions
            )
        }
    }
}

@Composable
fun TaskItem(task: Task, modifier: Modifier = Modifier, check: (Task) -> Unit) {
    var completed by remember {
        mutableStateOf(task.completed)
    }

    Row(
        modifier = modifier
            .padding(10.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            task.apply {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.plus(
                        TextStyle(
                            textDecoration = if (completed != null) TextDecoration.LineThrough else null
                        )
                    ),
                )
                description?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = SlightlyDeemphasizedAlpha),
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                getTimeRangeString().let {
                    if (it.isNotBlank()) {
                        Text(text = it)
                    }
                }
            }
        }

        Checkbox(
            checked = completed != null,
            onCheckedChange = {
                check(task)
                completed = task.completed
            },

        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplayOptionsSheet(
    options: TaskListScreenModel.Options,
    onDismissRequest: () -> Unit,
    changeFilterOptions: (String, Boolean) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        Column(modifier = Modifier.padding(10.dp)) {
            FilterOption(
                checked = options.showComplete,
                title = "Completed",
                preference = PreferenceRepository.SHOW_COMPLETE,
                changeFilterOptions = changeFilterOptions
            )

            FilterOption(
                checked = options.showIncomplete,
                title = "Incomplete",
                preference = PreferenceRepository.SHOW_INCOMPLETE,
                changeFilterOptions = changeFilterOptions
            )

            FilterOption(
                checked = options.showOld,
                title = "Old",
                preference = PreferenceRepository.SHOW_OLD,
                changeFilterOptions = changeFilterOptions
            )
        }
    }
}

@Composable
fun FilterOption(
    checked: Boolean,
    title: String,
    preference: String,
    changeFilterOptions: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = {
                changeFilterOptions(
                    preference,
                    it
                )
            }
        )

        Text(text = title)
    }
}
