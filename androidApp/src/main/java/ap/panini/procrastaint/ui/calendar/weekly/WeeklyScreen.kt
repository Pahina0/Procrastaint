package ap.panini.procrastaint.ui.calendar.weekly

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import ap.panini.procrastaint.ui.MainActivityViewModel
import ap.panini.procrastaint.ui.calendar.CalendarViewModel
import ap.panini.procrastaint.util.Date.formatMilliseconds
import ap.panini.procrastaint.util.Time
import ap.panini.procrastaint.util.formatToMMDDYYYY
import ap.panini.procrastaint.util.toAmPmHour
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.format
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.koin.androidx.compose.koinViewModel
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun WeeklyScreen(
    modifier: Modifier = Modifier,
    viewModel: CalendarViewModel,
    onTodayClick: () -> Unit,
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

    LaunchedEffect(state.focusedDate, lazyPagingItems.itemCount) {
        if (lazyPagingItems.itemCount == 0 || pagerState.isScrollInProgress) return@LaunchedEffect

        val index = lazyPagingItems.itemSnapshotList.indexOfFirst {
            if (it == null) return@indexOfFirst false
            val weekStart = it.time
            val weekEnd = weekStart + 6.days.inWholeMilliseconds
            state.focusedDate in weekStart..weekEnd
        }

        if (index != -1 && index != pagerState.currentPage) {
            pagerState.animateScrollToPage(index)
        }
    }

    fun updateTitle(time: Long) {
        val startStr = time.formatMilliseconds(known = setOf(Time.MONTH), useAbbreviated = false)
        onTitleChange(startStr)
    }

    LaunchedEffect(pagerState.currentPage, pagerState.isScrollInProgress) {
        if (lazyPagingItems.itemCount == 0) return@LaunchedEffect

        if (pagerState.isScrollInProgress) {
            lazyPagingItems[pagerState.targetPage]?.let {
                updateTitle(it.time)
            }
        } else {
            lazyPagingItems[pagerState.currentPage]?.let {
                viewModel.setFocusedDate(it.time)
                updateTitle(it.time)
            }
        }
    }

    HorizontalPager(
        state = pagerState,
        modifier = modifier.fillMaxSize(),
    ) { page ->
        val weekData = if (page < lazyPagingItems.itemCount) lazyPagingItems[page] else null

        if (weekData != null) {
            val tasksByDay by weekData.tasksByDay.collectAsState(initial = emptyMap())
            val weekStartInstant = Instant.fromEpochMilliseconds(weekData.time)
            val weekDates = (0..6).map {
                val date =
                    (weekStartInstant + it.days).toLocalDateTime(TimeZone.currentSystemDefault()).date
                val tasks = tasksByDay.getOrElse(date) { emptyList() }
                date to tasks
            }

            ScrollableWeekView(
                weekData = weekDates,
                onCheck = viewModel::checkTask,
                onEdit = activityViewModel::editCreatedTask,

                onCellClick = { date, hour ->
                    val dayString = date.formatToMMDDYYYY()
                    activityViewModel.updateTask("on $dayString at ${hour.toAmPmHour()}")
                    activityViewModel.onShow()
                },

                modifier = Modifier.fillMaxSize()
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