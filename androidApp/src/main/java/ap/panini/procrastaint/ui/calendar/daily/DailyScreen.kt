package ap.panini.procrastaint.ui.calendar.daily

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import ap.panini.procrastaint.ui.MainActivityViewModel
import ap.panini.procrastaint.ui.calendar.CalendarViewModel
import ap.panini.procrastaint.ui.calendar.components.SingleDayView
import ap.panini.procrastaint.util.Date
import ap.panini.procrastaint.util.Date.formatMilliseconds
import ap.panini.procrastaint.util.Time
import ap.panini.procrastaint.util.formatToMMDDYYYY
import ap.panini.procrastaint.util.hour
import ap.panini.procrastaint.util.toAmPmHour
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.androidx.compose.koinViewModel
import kotlin.math.max
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
@Composable
fun DailyScreen(
    modifier: Modifier = Modifier,
    viewModel: CalendarViewModel,
    initialDate: Long,
    onTitleChange: (String) -> Unit
) {
    val activityViewModel = koinViewModel<MainActivityViewModel>(
        viewModelStoreOwner = LocalActivity.current as ComponentActivity
    )

    LaunchedEffect(initialDate) {

    }

    val today by remember { mutableLongStateOf(Date.getTodayStart()) }

    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val dateState = viewModel.dateState.collectAsLazyPagingItems()

    val selectableListState = rememberLazyListState()
    val pagerState = rememberPagerState { dateState.itemCount }

    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(dateState) {
        snapshotFlow { dateState.itemSnapshotList }
            .map { it.items }
            .collect { list ->
                if (list.isNotEmpty()) {
                    dateState.peek(0) // prevents glitchy loading
                }
            }
    }

    LaunchedEffect(state.focusedDate) {

        coroutineScope.launch {
            if (pagerState.pageCount == 0) return@launch

            var index =
                max(dateState.itemSnapshotList.indexOfFirst { it?.time == state.focusedDate }, 0)
            pagerState.animateScrollToPage(index)
            selectableListState.animateScrollToItem(max(index - 1, 0))

            index =
                max(dateState.itemSnapshotList.indexOfFirst { it?.time == state.focusedDate }, 0)
            pagerState.animateScrollToPage(index)
            selectableListState.animateScrollToItem(max(index - 1, 0))
            onTitleChange(state.focusedDate.formatMilliseconds(setOf(Time.MONTH, Time.DAY)))
        }
    }

    LaunchedEffect(pagerState.settledPage, dateState.itemCount) {
        if (dateState.itemCount == 0) return@LaunchedEffect
        dateState[pagerState.settledPage]?.let {
            viewModel.setFocusedDate(it.time)
        }
    }

    Column(modifier = modifier) {
        DailyCalendarView(
            dateState = dateState,
            selectableListState = selectableListState,
            selectedTime = state.focusedDate,
            today = today,
            viewModel = viewModel,
        )
        HorizontalPager(
            state = pagerState,
            key = dateState.itemKey { it.time }
        ) { i ->
            val dayData = dateState[i]
            if (dayData != null) {
                val tasksByDay by dayData.tasksByDay.collectAsStateWithLifecycle(initialValue = emptyMap())
                val date = remember(dayData.time) {
                    Instant.fromEpochMilliseconds(dayData.time)
                        .toLocalDateTime(TimeZone.currentSystemDefault()).date
                }
                val tasksForDay = tasksByDay[date] ?: emptyList()
                val itemState = tasksForDay.groupBy { it.currentEventTime.hour() }
                SingleDayView(
                    itemState,
                    viewModel::checkTask,
                    onEdit = activityViewModel::editCreatedTask,
                    isToday = dayData.time == today,
                    modifier = Modifier.fillMaxSize(),

                    onHourClick = { hour ->
                        val day = Instant.fromEpochMilliseconds(dayData.time)
                            .toLocalDateTime(TimeZone.currentSystemDefault()).date
                        val dayString = day.formatToMMDDYYYY()
                        activityViewModel.updateTask("on $dayString at ${hour.toAmPmHour()}")
                        activityViewModel.onShow()
                    }
                )
            }
        }
    }
}