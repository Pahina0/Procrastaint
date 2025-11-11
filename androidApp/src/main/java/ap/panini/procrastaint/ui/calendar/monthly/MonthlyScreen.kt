package ap.panini.procrastaint.ui.calendar.monthly

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import ap.panini.procrastaint.ui.MainActivityViewModel
import ap.panini.procrastaint.ui.calendar.CalendarViewModel
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
    modifier: Modifier = Modifier,
    viewModel: CalendarViewModel,
    onTitleChange: (String) -> Unit,
) {
    val activityViewModel = koinViewModel<MainActivityViewModel>(
        viewModelStoreOwner = LocalActivity.current as ComponentActivity
    )
    val lazyPagingItems = viewModel.dateState.collectAsLazyPagingItems()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { lazyPagingItems.itemCount }
    )

    LaunchedEffect(lazyPagingItems) {
        snapshotFlow { lazyPagingItems.itemSnapshotList }
            .map { it.items }
            .collect { list ->
                if (list.isNotEmpty()) {
                    lazyPagingItems.peek(0)
                }
            }
    }

    LaunchedEffect(state.focusedDate, lazyPagingItems.itemCount) {
        if (lazyPagingItems.itemCount == 0 || pagerState.isScrollInProgress) return@LaunchedEffect

        val focusedInstant = kotlin.time.Instant.fromEpochMilliseconds(state.focusedDate)
        val focusedDate = focusedInstant.toLocalDateTime(TimeZone.currentSystemDefault())

        val index = lazyPagingItems.itemSnapshotList.indexOfFirst {
            if (it == null) return@indexOfFirst false
            val pageInstant = kotlin.time.Instant.fromEpochMilliseconds(it.time)
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

            MonthGrid(
                month = monthData.time,
                tasks = tasks,
                onDateFocused = { date ->
                    viewModel.jumpToDate(
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
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Loading...")
            }
        }
    }
}