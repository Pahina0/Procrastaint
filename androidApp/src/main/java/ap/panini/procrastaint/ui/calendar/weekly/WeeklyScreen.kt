package ap.panini.procrastaint.ui.calendar.weekly

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import ap.panini.procrastaint.data.entities.ExternalCalendarEvent
import ap.panini.procrastaint.data.entities.TaskSingle
import ap.panini.procrastaint.ui.MainActivityViewModel
import ap.panini.procrastaint.util.Date
import ap.panini.procrastaint.util.Date.formatMilliseconds
import ap.panini.procrastaint.util.Time
import ap.panini.procrastaint.util.formatCustom
import ap.panini.procrastaint.util.toAmPmHour
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import org.koin.androidx.compose.koinViewModel
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

private const val WindowWeeksBefore = 8
private const val WindowWeeksAfter = 8
private const val TotalWindowWeeks = WindowWeeksBefore + WindowWeeksAfter + 1
private const val DaysInWeek = 7
private const val PadLength = 2

@OptIn(ExperimentalTime::class)
@Composable
fun WeeklyScreen(
    focusedDate: Long,
    tasksByDate: Map<LocalDate, List<TaskSingle>>,
    eventsByDate: Map<LocalDate, List<ExternalCalendarEvent>>,
    onTitleChange: (String) -> Unit,
    setFocusedDate: (Long) -> Unit,
    jumpToDate: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val activityViewModel = koinViewModel<MainActivityViewModel>(
        viewModelStoreOwner = LocalActivity.current as ComponentActivity
    )

    val currentOnTitleChange by rememberUpdatedState(onTitleChange)
    val currentSetFocusedDate by rememberUpdatedState(setFocusedDate)
    val currentJumpToDate by rememberUpdatedState(jumpToDate)

    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

    // Calculate start of current week (Sunday)
    val dayOfWeekOffset = today.dayOfWeek.ordinal % DaysInWeek
    val currentWeekStartDate = today.minus(dayOfWeekOffset.toLong(), DateTimeUnit.DAY)

    // Generate week list
    val weekStartsList = remember(currentWeekStartDate) {
        (-WindowWeeksBefore..WindowWeeksAfter).map { weekOffset ->
            val weekDate = if (weekOffset < 0) {
                currentWeekStartDate.minus((-weekOffset * DaysInWeek).toLong(), DateTimeUnit.DAY)
            } else {
                currentWeekStartDate.plus((weekOffset * DaysInWeek).toLong(), DateTimeUnit.DAY)
            }
            weekDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        }
    }

    val pagerState = rememberPagerState(
        initialPage = WindowWeeksBefore,
        pageCount = { TotalWindowWeeks }
    )

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(focusedDate) {
        coroutineScope.launch {
            val index = weekStartsList.indexOfFirst { weekStart ->
                val weekEnd = weekStart + 7.days.inWholeMilliseconds
                focusedDate in weekStart until weekEnd
            }
            if (index != -1 && index != pagerState.currentPage) {
                pagerState.animateScrollToPage(index)
            }
            currentOnTitleChange(focusedDate.formatMilliseconds(known = setOf(Time.MONTH), useAbbreviated = false))
        }
    }

    LaunchedEffect(pagerState.settledPage) {
        val weekStart = weekStartsList.getOrNull(pagerState.settledPage) ?: return@LaunchedEffect
        val weekEnd = weekStart + 7.days.inWholeMilliseconds
        if (focusedDate !in weekStart until weekEnd) {
            currentSetFocusedDate(weekStart)
        }
        currentOnTitleChange(weekStart.formatMilliseconds(known = setOf(Time.MONTH), useAbbreviated = false))
    }

    HorizontalPager(
        state = pagerState,
        modifier = modifier.fillMaxSize(),
        key = { weekStartsList[it] }
    ) { pageIndex ->
        val weekStartTime = weekStartsList.getOrNull(pageIndex)
        if (weekStartTime != null) {
            val weekStartInstant = Instant.fromEpochMilliseconds(weekStartTime)
            val weekDates = remember(weekStartTime) {
                (0 until DaysInWeek).map {
                    (weekStartInstant + it.days).toLocalDateTime(TimeZone.currentSystemDefault()).date
                }
            }

            ScrollableWeekView(
                weekDates = weekDates,
                tasksByDate = tasksByDate,
                eventsByDate = eventsByDate,
                today = today,
                currentHour = now.hour,
                onCellClick = { date, hour ->
                    val dayString = date.formatCustom()
                    val hourString = if (Date.displayConfig.use24Hour) {
                        "${hour.toString().padStart(PadLength, '0')}:00"
                    } else {
                        hour.toAmPmHour()
                    }
                    activityViewModel.updateTask("on $dayString at $hourString")
                    activityViewModel.onShow()
                },
                onCellFocus = { date ->
                    currentJumpToDate(
                        date.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
                    )
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
