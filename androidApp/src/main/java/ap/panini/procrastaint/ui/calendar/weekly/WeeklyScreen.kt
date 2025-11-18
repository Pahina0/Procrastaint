package ap.panini.procrastaint.ui.calendar.weekly

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.paging.compose.LazyPagingItems
import ap.panini.procrastaint.ui.MainActivityViewModel
import ap.panini.procrastaint.ui.calendar.CalendarPageData
import ap.panini.procrastaint.util.Date.formatMilliseconds
import ap.panini.procrastaint.util.Time
import ap.panini.procrastaint.util.formatToMMDDYYYY
import ap.panini.procrastaint.util.toAmPmHour
import kotlinx.coroutines.flow.map
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import org.koin.androidx.compose.koinViewModel
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

private const val WeekDays = 6

@OptIn(ExperimentalTime::class)
@Composable
fun WeeklyScreen(
    dateState: LazyPagingItems<CalendarPageData>,
    focusedDate: Long,
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

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { dateState.itemCount }
    )

    SetupWeeklyScreenEffects(
        dateState = dateState,
        focusedDate = focusedDate,
        pagerState = pagerState,
        currentOnTitleChange = currentOnTitleChange,
        currentSetFocusedDate = currentSetFocusedDate
    )

    WeeklyPager(
        pagerState = pagerState,
        dateState = dateState,
        activityViewModel = activityViewModel,
        currentJumpToDate = currentJumpToDate,
        modifier = modifier
    )
}

@OptIn(ExperimentalTime::class)
@Composable
private fun SetupWeeklyScreenEffects(
    dateState: LazyPagingItems<CalendarPageData>,
    focusedDate: Long,
    pagerState: PagerState,
    currentOnTitleChange: (String) -> Unit,
    currentSetFocusedDate: (Long) -> Unit
) {
    LaunchedEffect(dateState) {
        snapshotFlow { dateState.itemSnapshotList }
            .map { it.items }
            .collect { list ->
                if (list.isNotEmpty()) {
                    dateState.peek(0)
                }
            }
    }

    LaunchedEffect(focusedDate, dateState.itemCount) {
        if (dateState.itemCount == 0 || pagerState.isScrollInProgress) return@LaunchedEffect

        val index = dateState.itemSnapshotList.indexOfFirst {
            if (it == null) return@indexOfFirst false
            val weekStart = it.time
            val weekEnd = weekStart + WeekDays.days.inWholeMilliseconds
            focusedDate in weekStart..weekEnd
        }

        if (index != -1 && index != pagerState.currentPage) {
            pagerState.animateScrollToPage(index)
        }
    }

    LaunchedEffect(
        pagerState.currentPage,
        pagerState.isScrollInProgress,
        currentOnTitleChange,
        currentSetFocusedDate
    ) {
        if (dateState.itemCount == 0) return@LaunchedEffect

        if (pagerState.isScrollInProgress) {
            updateWeeklyTitle(
                time = dateState[pagerState.targetPage]?.time,
                currentOnTitleChange = currentOnTitleChange
            )
        } else {
            dateState[pagerState.currentPage]?.let {
                if (it.time != focusedDate) {
                    currentSetFocusedDate(it.time)
                }
                updateWeeklyTitle(
                    time = it.time,
                    currentOnTitleChange = currentOnTitleChange
                )
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
private fun updateWeeklyTitle(time: Long?, currentOnTitleChange: (String) -> Unit) {
    time?.let {
        val startStr = it.formatMilliseconds(known = setOf(Time.MONTH), useAbbreviated = false)
        currentOnTitleChange(startStr)
    }
}

@OptIn(ExperimentalTime::class)
@Composable
private fun WeeklyPager(
    pagerState: PagerState,
    dateState: LazyPagingItems<CalendarPageData>,
    activityViewModel: MainActivityViewModel,
    currentJumpToDate: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    HorizontalPager(
        state = pagerState,
        modifier = modifier,
    ) { page ->
        val weekData = if (page < dateState.itemCount) dateState[page] else null

        if (weekData != null) {
            val tasksByDay by weekData.tasksByDay.collectAsState(initial = emptyMap())
            val weekStartInstant = Instant.fromEpochMilliseconds(weekData.time)
            val weekDates = (0..WeekDays).map {
                val date =
                    (weekStartInstant + it.days).toLocalDateTime(TimeZone.currentSystemDefault()).date
                val tasks = tasksByDay.getOrElse(date) { emptyList() }
                date to tasks
            }

            ScrollableWeekView(
                weekData = weekDates,
                today = today,
                currentHour = now.hour,

                onCellClick = { date, hour ->
                    val dayString = date.formatToMMDDYYYY()
                    activityViewModel.updateTask("on $dayString at ${hour.toAmPmHour()}")
                    activityViewModel.onShow()
                },

                onCellFocus = { date ->
                    currentJumpToDate(
                        date.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
                    )
                },

                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Text("Loading...")
            }
        }
    }
}
