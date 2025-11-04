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
import androidx.paging.compose.collectAsLazyPagingItems
import ap.panini.procrastaint.ui.calendar.CalendarPageData
import ap.panini.procrastaint.ui.calendar.CalendarViewModel
import ap.panini.procrastaint.util.Date.formatMilliseconds
import ap.panini.procrastaint.util.Time
import kotlinx.datetime.LocalDate

@Composable
fun MonthlyScreen(
    modifier: Modifier = Modifier,
    viewModel: CalendarViewModel,
    onTodayClick: () -> Unit,
    onTitleChange: (String) -> Unit,
    onDateClick: (LocalDate) -> Unit
) {
    val lazyPagingItems = viewModel.dateState.collectAsLazyPagingItems()

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { lazyPagingItems.itemCount }
    )

    LaunchedEffect(lazyPagingItems.itemCount) {
        if (lazyPagingItems.itemCount > 0) {
            pagerState.scrollToPage(lazyPagingItems.itemCount / 2)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage < lazyPagingItems.itemCount) {
            lazyPagingItems[pagerState.currentPage]?.let {
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