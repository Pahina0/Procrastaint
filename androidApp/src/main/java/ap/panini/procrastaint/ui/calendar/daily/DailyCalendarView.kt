package ap.panini.procrastaint.ui.calendar.daily

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ap.panini.procrastaint.data.entities.ExternalCalendarEvent
import ap.panini.procrastaint.data.entities.TaskSingle
import ap.panini.procrastaint.ui.calendar.components.DayView
import ap.panini.procrastaint.ui.calendar.components.ViewingType
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
@Composable
fun DailyCalendarView(
    dates: List<Long>,
    tasksByDate: Map<LocalDate, List<TaskSingle>>,
    eventsByDate: Map<LocalDate, List<ExternalCalendarEvent>>,
    selectableListState: LazyListState,
    today: Long,
    selectedTime: Long,
    setFocusedDate: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        state = selectableListState
    ) {
        items(
            items = dates,
            key = { it }
        ) { dateTime ->
            val date = remember(dateTime) {
                Instant.fromEpochMilliseconds(dateTime)
                    .toLocalDateTime(TimeZone.currentSystemDefault()).date
            }
            val tasksForThisDay = tasksByDate[date] ?: emptyList()
            val eventsForThisDay = eventsByDate[date] ?: emptyList()

            DayView(
                date = dateTime,
                taskCount = tasksForThisDay.count { it.completed == null },
                eventCount = eventsForThisDay.size,
                dateType = when (dateTime) {
                    selectedTime -> ViewingType.Selected
                    today -> ViewingType.Today
                    else -> {
                        if (dateTime < today) {
                            ViewingType.Past
                        } else {
                            ViewingType.Future
                        }
                    }
                },
                onClick = {
                    coroutineScope.launch {
                        setFocusedDate(dateTime)
                    }
                }
            )
        }
    }
}
