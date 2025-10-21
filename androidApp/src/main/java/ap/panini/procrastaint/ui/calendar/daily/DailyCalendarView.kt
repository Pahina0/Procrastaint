package ap.panini.procrastaint.ui.calendar.daily

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import ap.panini.procrastaint.ui.calendar.components.DayView
import ap.panini.procrastaint.ui.calendar.components.ViewingType
import kotlinx.coroutines.launch

@Composable
fun DailyCalendarView(
    dateState: LazyPagingItems<Pair<Long, kotlinx.coroutines.flow.Flow<Map<Int, List<ap.panini.procrastaint.data.entities.TaskSingle>>>>>,
    selectableListState: LazyListState,
    selectedTime: Long,
    today: Long,
    viewModel: DailyViewModel
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
            key = dateState.itemKey { it.first }
        ) { i ->
            val (time, item) = dateState[i]!!
            val itemState = item.collectAsStateWithLifecycle(mapOf()).value

            DayView(
                time,
                itemState.values.flatten(),
                dateType =
                when (time) {
                    selectedTime -> ViewingType.Selected
                    today -> ViewingType.Today
                    else -> {
                        if (time < today) {
                            ViewingType.Past
                        } else {
                            ViewingType.Future
                        }
                    }
                },

                onClick = {
                    coroutineScope.launch {
                        viewModel.setSelectedTime(time)
                    }
                }
            )
        }
    }
}