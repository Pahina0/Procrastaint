package ap.panini.procrastaint.ui.components

import androidx.collection.mutableIntIntMapOf
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ap.panini.procrastaint.data.entities.TaskSingle
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
fun DayView(
    tasks: List<TaskSingle>,
    onCheck: (TaskSingle) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    var amt = 0
    val indexes = remember { mutableIntIntMapOf(0, 0) }

    var remainingItems = tasks
    LazyColumn(
        modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceEvenly,
        state = listState
    ) {
        item {
            DividerText(
                Date.getTodayStart()
                    .formatMilliseconds(setOf(Time.HOUR, Time.MINUTE), smart = false)
            )
        }

        for (hour in 1 until HOURS) {
            // gets all tasks under x hour
            val usingItems =
                remainingItems.filter {
                    it.currentEventTime.hour().toLong() < hour.hours.inWholeHours
                }
            amt += usingItems.size

            // removes those under x hour
            remainingItems = remainingItems.drop(usingItems.size)

            // displays all tasks under x hour
            items(items = usingItems) {
                TaskView(it, onCheck)
            }

            if (usingItems.isEmpty()) {
                item { Spacer(modifier = Modifier.height(50.dp)) }
                ++amt
            }

            indexes[hour] = ++amt

            item {
                DividerText(
                    (Date.getTodayStart() + hour.hours.inWholeMilliseconds).formatMilliseconds(
                        setOf(Time.HOUR, Time.MINUTE),
                        smart = false
                    )
                )
            }
        }

        // displays all tasks under x hour
        items(items = remainingItems) {
            TaskView(it, onCheck)
        }
        if (remainingItems.isEmpty()) {
            item { Spacer(modifier = Modifier.height(50.dp)) }
        }
    }

    LaunchedEffect(true) {
        // scrolls to current time
        listState.animateScrollToItem(indexes[Date.getTime().hour()])
    }
}

@Preview
@Composable
private fun DayViewPreview() {
    DayView(
        listOf(
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
                true,
                4.3.hours.inWholeMilliseconds
            ),

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
                true,
                8.hours.inWholeMilliseconds
            )
        ),
        {},
        modifier = Modifier.fillMaxSize()
    )
}
