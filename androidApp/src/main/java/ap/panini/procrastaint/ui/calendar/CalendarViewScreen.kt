package ap.panini.procrastaint.ui.calendar

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CalendarScreen(
    calendarState: CalendarViewModel.State,
    modifier: Modifier = Modifier
) {
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    var showDisplayOptions by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Calendar") },
                scrollBehavior = scrollBehavior,
                actions = {
                    IconButton(onClick = { showDisplayOptions = true }) {
                        Icon(
                            imageVector = Icons.Outlined.FilterList,
                            contentDescription = "Change how calendar items are displayed"
                        )
                    }
                }
            )
        },
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            // Group tasks by date
            val tasksByDate = calendarState.tasks.groupBy {
                it.dueDate?.toLocalDate() ?: LocalDate.MAX
            }.toSortedMap()

            tasksByDate.forEach { (date, tasksForDate) ->
                // Date header
                stickyHeader {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }

                // Tasks for this date
                items(
                    items = tasksForDate,
                    key = { it.id }
                ) { task ->
                    CalendarTaskItem(
                        task = task,
                        modifier = Modifier.animateItemPlacement(),
                        check = onCompleteTask
                    )
                }
            }
        }

        if (showDisplayOptions) {
            CalendarDisplayOptionsSheet(
                options = options,
                onDismissRequest = { showDisplayOptions = false },
                changeFilterOptions = changeFilterOptions
            )
        }
    }
}

@Composable
fun CalendarTaskItem(task: Task, modifier: Modifier = Modifier, check: (Task) -> Unit) {
    var completed by remember {
        mutableStateOf(task.completed)
    }

    Row(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
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

                // Display specific time information for calendar view
                Row(verticalAlignment = Alignment.CenterVertically) {
                    task.getTimeRangeString().takeIf { it.isNotBlank() }?.let { timeRange ->
                        Icon(
                            imageVector = Icons.Outlined.AccessTime,
                            contentDescription = null,
                            modifier = Modifier
                                .size(16.dp)
                                .padding(end = 4.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = timeRange,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }

        Checkbox(
            checked = completed != null,
            onCheckedChange = {
                check(task)
                completed = task.completed
            }
        )
    }
}
const val SlightlyDeemphasizedAlpha = 0.6f