package ap.panini.procrastaint.ui.calendar.monthly

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import ap.panini.procrastaint.ui.calendar.CalendarViewModel
import ap.panini.procrastaint.util.Date.formatMilliseconds
import ap.panini.procrastaint.util.Time
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun MonthlyScreen(
    modifier: Modifier = Modifier,
    viewModel: CalendarViewModel,
    onTodayClick: () -> Unit,
    onTitleChange: (String) -> Unit,
    onDateClick: (LocalDate) -> Unit
) {
    val lazyPagingItems = viewModel.dateState.collectAsLazyPagingItems()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { lazyPagingItems.itemCount }
    )

    LaunchedEffect(state.focusedDate, lazyPagingItems.itemCount) {
        if (lazyPagingItems.itemCount == 0 || pagerState.isScrollInProgress) return@LaunchedEffect

        val focusedInstant = kotlin.time.Instant.fromEpochMilliseconds(state.focusedDate)
        val focusedDate = focusedInstant.toLocalDateTime(TimeZone.currentSystemDefault())

        val index = lazyPagingItems.itemSnapshotList.indexOfFirst {
            if (it == null) return@indexOfFirst false
            val pageInstant = Instant.fromEpochMilliseconds(it.time)
            val pageDate = pageInstant.toLocalDateTime(TimeZone.currentSystemDefault())
            pageDate.year == focusedDate.year && pageDate.month == focusedDate.month
        }

        if (index != -1 && index != pagerState.currentPage) {
            pagerState.animateScrollToPage(index)
        }
    }

    LaunchedEffect(pagerState.currentPage, pagerState.isScrollInProgress) {
        if (lazyPagingItems.itemCount == 0) return@LaunchedEffect

        if (pagerState.isScrollInProgress) {
            lazyPagingItems[pagerState.targetPage]?.let {
                onTitleChange(
                    it.time.formatMilliseconds(
                        known = setOf(Time.MONTH),
                        smart = false
                    )
                )
            }
        } else {
            lazyPagingItems[pagerState.currentPage]?.let {
                viewModel.setFocusedDate(it.time)
                onTitleChange(
                    it.time.formatMilliseconds(
                        known = setOf(Time.MONTH),
                        smart = false
                    )
                )
            }
        }
    }

    HorizontalPager(
        state = pagerState,
        modifier = modifier.fillMaxSize(),
    ) { page ->
        val monthData = if (page < lazyPagingItems.itemCount) lazyPagingItems[page] else null

        if (monthData != null) {
            val tasksByDay by monthData.tasksByDay.collectAsState(initial = emptyMap())
            val tasks = tasksByDay.values.flatten()

            MonthGrid(month = monthData.time, tasks = tasks, onDateClick = onDateClick)
        } else {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Loading...")
            }
        }
    }
}