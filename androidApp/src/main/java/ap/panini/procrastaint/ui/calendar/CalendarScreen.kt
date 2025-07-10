package ap.panini.procrastaint.ui.calendar

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import ap.panini.procrastaint.ui.MainActivityViewModel
import ap.panini.procrastaint.ui.calendar.components.DayView
import ap.panini.procrastaint.ui.calendar.components.TasksMiniPreview
import ap.panini.procrastaint.ui.calendar.components.ViewingType
import ap.panini.procrastaint.ui.components.ScreenScaffold
import ap.panini.procrastaint.util.Date
import ap.panini.procrastaint.util.Date.formatMilliseconds
import ap.panini.procrastaint.util.Time
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CalendarScreen(
    modifier: Modifier = Modifier,
    viewModel: CalendarViewModel = koinViewModel(),
) {
    val activityViewModel = koinViewModel<MainActivityViewModel>(
        viewModelStoreOwner = LocalActivity.current as ComponentActivity
    )

    val today by remember { mutableLongStateOf(Date.getTodayStart()) }

    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val dateState = viewModel.dateState.collectAsLazyPagingItems()

    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

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

    LaunchedEffect(state.selectedTime) {
        coroutineScope.launch {
            if (pagerState.pageCount == 0) return@launch

            var index =
                max(dateState.itemSnapshotList.indexOfFirst { it?.first == state.selectedTime }, 0)
            pagerState.animateScrollToPage(index)
            selectableListState.animateScrollToItem(max(index - 1, 0))

            index =
                max(dateState.itemSnapshotList.indexOfFirst { it?.first == state.selectedTime }, 0)
            pagerState.animateScrollToPage(index)
            selectableListState.animateScrollToItem(max(index - 1, 0))
        }
    }

    LaunchedEffect(pagerState.currentPage, pagerState.isScrollInProgress) {
        if (dateState.itemCount == 0) return@LaunchedEffect
        if (pagerState.isScrollInProgress) {
            viewModel.setSelectedTime(
                dateState[pagerState.targetPage]?.first ?: return@LaunchedEffect
            )
        }
    }

    ScreenScaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                scrollBehavior = scrollBehavior,
                title = {
                    Text(
                        state.selectedTime.formatMilliseconds(
                            setOf(
                                Time.MONTH,
                                Time.DAY
                            )
                        )
                    )
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.setSelectedTime(
                                today
                            )
                        }
                    ) {
                        Icon(Icons.Outlined.Today, contentDescription = "Today")
                    }
                }
            )
        }
    ) { padding ->
        ScreenScaffold(modifier = Modifier.padding(padding)) {
            Column {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    state = selectableListState
                ) {
                    items(
                        count = dateState.itemCount,
                        key = dateState.itemKey { it.first }
                    ) { i ->
                        val (time, item) = dateState[i]!!
                        val itemState = item.collectAsStateWithLifecycle(listOf()).value
                        TasksMiniPreview(
                            time,
                            itemState,
                            dateType =
                            when (time) {
                                state.selectedTime -> ViewingType.Selected
                                today -> ViewingType.Today
                                else -> {
                                    if (time < today) {
                                        ViewingType.Past
                                    } else {
                                        ViewingType.Future
                                    }
                                }
                            },

                            onClick = {
                                coroutineScope.launch {
                                    viewModel.setSelectedTime(time)
                                }
                            }
                        )
                    }
                }

                HorizontalPager(
                    state = pagerState,
                    key = dateState.itemKey { it.first }
                ) { i ->

                    val (time, item) = dateState[i]!!
                    val itemState = item.collectAsStateWithLifecycle(listOf()).value

                    DayView(
                        itemState,
                        viewModel::checkTask,
                        onEdit = activityViewModel::editCreatedTask,
                        isToday = time == today,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
