package ap.panini.procrastaint.ui.calendar.daily

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import ap.panini.procrastaint.ui.calendar.CalendarPageData
import ap.panini.procrastaint.ui.calendar.components.DayView
import ap.panini.procrastaint.ui.calendar.components.ViewingType
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun DailyCalendarView(
    dateState: LazyPagingItems<CalendarPageData>,
    selectableListState: LazyListState,
    today: Long,
    selectedTime: Long,
    setFocusedDate: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        state = selectableListState
    ) {
        items(
            count = dateState.itemCount,
            key = dateState.itemKey { it.time }
        ) {
            val dayData = dateState[it]
            if (dayData != null) {
                val tasksByDayMap by dayData.tasksByDay.collectAsStateWithLifecycle(mapOf())
                val date = remember(dayData.time) {
                    kotlin.time.Instant.fromEpochMilliseconds(dayData.time)
                        .toLocalDateTime(TimeZone.currentSystemDefault()).date
                }
                val tasksForThisDay = tasksByDayMap[date] ?: emptyList()

                DayView(
                    dayData.time,
                    tasksForThisDay,
                    dateType =
                    when (dayData.time) {
                        selectedTime -> ViewingType.Selected
                        today -> ViewingType.Today
                        else -> {
                            if (dayData.time < today) {
                                ViewingType.Past
                            } else {
                                ViewingType.Future
                            }
                        }
                    },

                    onClick = {
                        coroutineScope.launch {
                            setFocusedDate(dayData.time)
                        }
                    }
                )
            }
        }
    }
}
