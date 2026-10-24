package ap.panini.procrastaint.ui.calendar.monthly

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
import ap.panini.procrastaint.util.Date.formatMilliseconds
import ap.panini.procrastaint.util.Time
import ap.panini.procrastaint.util.formatCustom
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
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

private const val WindowMonthsBefore = 12
private const val WindowMonthsAfter = 12
private const val TotalWindowMonths = WindowMonthsBefore + WindowMonthsAfter + 1

@OptIn(ExperimentalTime::class)
@Composable
fun MonthlyScreen(
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
    val currentMonthStart = LocalDate(today.year, today.month, 1)

    val monthStartsList = remember(currentMonthStart) {
        (-WindowMonthsBefore..WindowMonthsAfter).map { offset ->
            val monthDate = if (offset < 0) {
                currentMonthStart.minus((-offset).toLong(), DateTimeUnit.MONTH)
            } else {
                currentMonthStart.plus(offset.toLong(), DateTimeUnit.MONTH)
            }
            monthDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        }
    }

    val pagerState = rememberPagerState(
        initialPage = WindowMonthsBefore,
        pageCount = { TotalWindowMonths }
    )

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(focusedDate) {
        coroutineScope.launch {
            val focusedLocalDate = Instant.fromEpochMilliseconds(focusedDate)
                .toLocalDateTime(TimeZone.currentSystemDefault()).date

            val index = monthStartsList.indexOfFirst { monthStart ->
                val monthDate = Instant.fromEpochMilliseconds(monthStart)
                    .toLocalDateTime(TimeZone.currentSystemDefault()).date
                monthDate.year == focusedLocalDate.year && monthDate.month == focusedLocalDate.month
            }
            if (index != -1 && index != pagerState.currentPage) {
                pagerState.animateScrollToPage(index)
            }
            currentOnTitleChange(focusedDate.formatMilliseconds(known = setOf(Time.MONTH), smart = false))
        }
    }

    LaunchedEffect(pagerState.settledPage) {
        val monthStart = monthStartsList.getOrNull(pagerState.settledPage) ?: return@LaunchedEffect
        val monthDate = Instant.fromEpochMilliseconds(monthStart)
            .toLocalDateTime(TimeZone.currentSystemDefault()).date
        val focusedLocalDate = Instant.fromEpochMilliseconds(focusedDate)
            .toLocalDateTime(TimeZone.currentSystemDefault()).date

        if (monthDate.year != focusedLocalDate.year || monthDate.month != focusedLocalDate.month) {
            currentSetFocusedDate(monthStart)
        }
        currentOnTitleChange(monthStart.formatMilliseconds(known = setOf(Time.MONTH), smart = false))
    }

    HorizontalPager(
        state = pagerState,
        modifier = modifier.fillMaxSize(),
        key = { monthStartsList[it] }
    ) { pageIndex ->
        val monthStartTime = monthStartsList.getOrNull(pageIndex)
        if (monthStartTime != null) {
            val monthDate = Instant.fromEpochMilliseconds(monthStartTime)
                .toLocalDateTime(TimeZone.currentSystemDefault()).date

            // Filter tasks and events for this month
            val monthTasks = tasksByDate
                .filterKeys { it.year == monthDate.year && it.month == monthDate.month }
                .values
                .flatten()
            val monthEvents = eventsByDate
                .filterKeys { it.year == monthDate.year && it.month == monthDate.month }
                .values
                .flatten()

            MonthGrid(
                month = monthStartTime,
                tasks = monthTasks,
                events = monthEvents,
                onDateFocus = { date ->
                    currentJumpToDate(
                        date.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
                    )
                },
                onDateClick = { date ->
                    val dayString = date.formatCustom()
                    activityViewModel.updateTask("on $dayString")
                    activityViewModel.onShow()
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
