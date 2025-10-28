package ap.panini.procrastaint.ui.calendar.daily

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import ap.panini.procrastaint.ui.calendar.CalendarPageData
import ap.panini.procrastaint.ui.calendar.CalendarViewModel
import ap.panini.procrastaint.ui.calendar.components.DayView
import ap.panini.procrastaint.ui.calendar.components.ViewingType
import kotlinx.coroutines.launch

@Composable
fun DailyCalendarView(
    dateState: LazyPagingItems<CalendarPageData>,
    selectableListState: LazyListState,
    selectedTime: Long,
    today: Long,
    viewModel: CalendarViewModel
) {
    val coroutineScope = rememberCoroutineScope()

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        state = selectableListState
    ) {
        items(
            count = dateState.itemCount,
            key = dateState.itemKey { it.time }
        ) { i ->
            val dayData = dateState[i]
            if (dayData != null) {
                val itemState by dayData.tasks.collectAsStateWithLifecycle(listOf())

                DayView(
                    dayData.time,
                    itemState,
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
                            viewModel.setSelectedTime(dayData.time)
                        }
                    }
                )
            }
        }
    }
}
