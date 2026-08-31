package ap.panini.procrastaint.ui.calendar.daily

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import ap.panini.procrastaint.data.entities.ExternalCalendarEvent
import ap.panini.procrastaint.data.entities.TaskSingle
import ap.panini.procrastaint.ui.MainActivityViewModel
import ap.panini.procrastaint.ui.calendar.components.SingleDayView
import ap.panini.procrastaint.util.Date
import ap.panini.procrastaint.util.Date.formatMilliseconds
import ap.panini.procrastaint.util.Time
import ap.panini.procrastaint.util.formatToMMDDYYYY
import ap.panini.procrastaint.util.hour
import ap.panini.procrastaint.util.toAmPmHour
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.androidx.compose.koinViewModel
import kotlin.math.max
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

private const val WindowDaysBefore = 30
private const val WindowDaysAfter = 30
private const val TotalWindowDays = WindowDaysBefore + WindowDaysAfter + 1

@OptIn(ExperimentalTime::class)
@Composable
fun DailyScreen(
    focusedDate: Long,
    tasksByDate: Map<LocalDate, List<TaskSingle>>,
    eventsByDate: Map<LocalDate, List<ExternalCalendarEvent>>,
    onTitleChange: (String) -> Unit,
    setFocusedDate: (Long) -> Unit,
    checkTask: (TaskSingle) -> Unit,
    modifier: Modifier = Modifier
) {
    val activityViewModel = koinViewModel<MainActivityViewModel>(
        viewModelStoreOwner = LocalActivity.current as ComponentActivity
    )

    val currentOnTitleChange by rememberUpdatedState(onTitleChange)
    val currentSetFocusedDate by rememberUpdatedState(setFocusedDate)

    val today by remember { mutableLongStateOf(Date.getTodayStart()) }

    // Generate date window anchored around today
    val dateList = remember(today) {
        (-WindowDaysBefore..WindowDaysAfter).map { dayOffset ->
            today + dayOffset.days.inWholeMilliseconds
        }
    }

    val selectableListState = rememberLazyListState()
    val pagerState = rememberPagerState(
        initialPage = WindowDaysBefore,
        pageCount = { TotalWindowDays }
    )

    val coroutineScope = rememberCoroutineScope()

    // Sync pager & carousel when focusedDate changes externally
    LaunchedEffect(focusedDate) {
        coroutineScope.launch {
            val index = dateList.indexOfFirst { it == focusedDate }
            if (index != -1 && index != pagerState.currentPage) {
                pagerState.animateScrollToPage(index)
                selectableListState.animateScrollToItem(max(index - 1, 0))
            }
            currentOnTitleChange(focusedDate.formatMilliseconds(setOf(Time.MONTH, Time.DAY)))
        }
    }

    // Sync focusedDate when user swipes pager
    LaunchedEffect(pagerState.settledPage) {
        val targetDate = dateList.getOrNull(pagerState.settledPage) ?: return@LaunchedEffect
        if (targetDate != focusedDate) {
            currentSetFocusedDate(targetDate)
            selectableListState.animateScrollToItem(max(pagerState.settledPage - 1, 0))
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        DailyCalendarView(
            dates = dateList,
            tasksByDate = tasksByDate,
            eventsByDate = eventsByDate,
            selectableListState = selectableListState,
            selectedTime = focusedDate,
            today = today,
            setFocusedDate = currentSetFocusedDate,
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            key = { dateList[it] }
        ) { pageIndex ->
            val dayTimestamp = dateList.getOrNull(pageIndex)
            if (dayTimestamp != null) {
                val date = remember(dayTimestamp) {
                    Instant.fromEpochMilliseconds(dayTimestamp)
                        .toLocalDateTime(TimeZone.currentSystemDefault()).date
                }
                val tasksForDay = tasksByDate[date] ?: emptyList()
                val eventsForDay = eventsByDate[date] ?: emptyList()

                val tasksByHour = remember(tasksForDay) {
                    tasksForDay.groupBy { it.currentEventTime.hour() }
                }

                val eventsByHour = remember(eventsForDay) {
                    eventsForDay.groupBy {
                        Instant.fromEpochMilliseconds(it.startTime)
                            .toLocalDateTime(TimeZone.currentSystemDefault()).hour
                    }
                }

                SingleDayView(
                    tasks = tasksByHour,
                    events = eventsByHour,
                    onCheck = checkTask,
                    onEdit = activityViewModel::editCreatedTask,
                    isToday = dayTimestamp == today,
                    modifier = Modifier.fillMaxSize(),
                    onHourClick = { hour ->
                        val dayString = date.formatToMMDDYYYY()
                        activityViewModel.updateTask("on $dayString at ${hour.toAmPmHour()}")
                        activityViewModel.onShow()
                    }
                )
            }
        }
    }
}
