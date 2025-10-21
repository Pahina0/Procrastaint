package ap.panini.procrastaint.ui.calendar.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ap.panini.procrastaint.data.entities.TaskSingle
import ap.panini.procrastaint.ui.components.TaskView
import ap.panini.procrastaint.util.Date
import ap.panini.procrastaint.util.Date.formatMilliseconds
import ap.panini.procrastaint.util.Time
import ap.panini.procrastaint.util.hour
import kotlin.time.Duration.Companion.hours

private const val HOURS = 24

/**
 * Day view
 *
 * @param tasks tasks must be all in 1 single day of time. from 00:00 to 24:00.
 * make sure its not the next day
 * @param modifier
 */
@Composable
fun SingleDayView(
    tasks: Map<Int, List<TaskSingle>>,
    onCheck: (TaskSingle) -> Unit,
    onEdit: (TaskSingle) -> Unit,
    isToday: Boolean,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

    val curHour by remember { mutableIntStateOf(Date.getTime().hour()) }

    LazyColumn(
        modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceEvenly,
        state = listState
    ) {
        for (hour in 0 until HOURS) {
            item {
                DividerText(
                    (Date.getTodayStart() + hour.hours.inWholeMilliseconds).formatMilliseconds(
                        setOf(Time.HOUR, Time.MINUTE),
                        smart = false
                    ),
                    highlight = isToday && hour == curHour
                )
            }

            val tasksForHour = tasks[hour]

            if (tasksForHour.isNullOrEmpty()) {
                item { Spacer(modifier = Modifier.height(20.dp)) }
            } else {
                items(tasksForHour) { task ->
                    TaskView(task, onCheck, onEdit)
                }
            }
        }
    }

    LaunchedEffect(isToday) {
        if (isToday) {
            listState.animateScrollToItem(curHour * 2)
        }
    }
}

@Preview
@Composable
private fun DayViewPreview() {
    SingleDayView(
        mapOf(
            4 to listOf(
                TaskSingle(
                    0,
                    0,
                    0,
                    "IDK",
                    "HII",
                    null,
                    null,
                    null,
                    null,
                    null,
                    4.3.hours.inWholeMilliseconds
                )
            ),
            8 to listOf(
                TaskSingle(
                    0,
                    0,
                    0,
                    "num 2",
                    "desc",
                    null,
                    null,
                    null,
                    null,
                    null,
                    8.hours.inWholeMilliseconds
                )
            )
        ),
        {},
        {},
        false,
        modifier = Modifier.fillMaxSize()
    )
}
