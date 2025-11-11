package ap.panini.procrastaint.ui.calendar.monthly

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.pager.HorizontalPager
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
import kotlinx.coroutines.flow.map
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import org.koin.androidx.compose.koinViewModel
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun MonthlyScreen(
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

    SetupMonthlyScreenEffects(
        dateState = dateState,
        focusedDate = focusedDate,
        pagerState = pagerState,
        currentOnTitleChange = currentOnTitleChange,
        currentSetFocusedDate = currentSetFocusedDate
    )

    MonthlyPager(
        pagerState = pagerState,
        dateState = dateState,
        activityViewModel = activityViewModel,
        currentJumpToDate = currentJumpToDate,
        modifier = modifier
    )
}

@OptIn(ExperimentalTime::class)
@Composable
private fun SetupMonthlyScreenEffects(
    dateState: LazyPagingItems<CalendarPageData>,
    focusedDate: Long,
    pagerState: androidx.compose.foundation.pager.PagerState,
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

        val focusedInstant = kotlin.time.Instant.fromEpochMilliseconds(focusedDate)
        val focusedLocalDate = focusedInstant.toLocalDateTime(TimeZone.currentSystemDefault())

        val index = dateState.itemSnapshotList.indexOfFirst {
            if (it == null) return@indexOfFirst false
            val pageInstant = kotlin.time.Instant.fromEpochMilliseconds(it.time)
            val pageDate = pageInstant.toLocalDateTime(TimeZone.currentSystemDefault())
            pageDate.year == focusedLocalDate.year && pageDate.month == focusedLocalDate.month
        }

        if (index != -1 && index != pagerState.currentPage) {
            pagerState.animateScrollToPage(index)
        }
    }

    LaunchedEffect(pagerState.currentPage, pagerState.isScrollInProgress, currentOnTitleChange, currentSetFocusedDate) {
        if (dateState.itemCount == 0) return@LaunchedEffect

        if (pagerState.isScrollInProgress) {
            updateMonthlyTitle(
                time = dateState[pagerState.targetPage]?.time,
                currentOnTitleChange = currentOnTitleChange
            )
        } else {
            dateState[pagerState.currentPage]?.let {
                if (it.time != focusedDate) {
                    currentSetFocusedDate(it.time)
                }
                updateMonthlyTitle(
                    time = it.time,
                    currentOnTitleChange = currentOnTitleChange
                )
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
private fun updateMonthlyTitle(time: Long?, currentOnTitleChange: (String) -> Unit) {
    time?.let {
        currentOnTitleChange(
            it.formatMilliseconds(
                known = setOf(Time.MONTH),
                smart = false
            )
        )
    }
}

@OptIn(ExperimentalTime::class)
@Composable
private fun MonthlyPager(
    pagerState: androidx.compose.foundation.pager.PagerState,
    dateState: LazyPagingItems<CalendarPageData>,
    activityViewModel: MainActivityViewModel,
    currentJumpToDate: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    HorizontalPager(
        state = pagerState,
        modifier = modifier,
    ) { page ->
        val monthData = if (page < dateState.itemCount) dateState[page] else null

        if (monthData != null) {
            val tasksByDay by monthData.tasksByDay.collectAsState(initial = emptyMap())
            val tasks = tasksByDay.values.flatten()

            MonthGrid(
                month = monthData.time,
                tasks = tasks,
                onDateFocus = { date ->
                    currentJumpToDate(
                        date.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
                    )
                },
                onDateClick = { date ->
                    val dayString = date.formatToMMDDYYYY()
                    activityViewModel.updateTask("on $dayString")
                    activityViewModel.onShow()
                },
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
