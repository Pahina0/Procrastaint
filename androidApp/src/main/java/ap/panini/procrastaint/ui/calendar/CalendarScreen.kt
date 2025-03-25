package ap.panini.procrastaint.ui.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import ap.panini.procrastaint.ui.components.DayView
import ap.panini.procrastaint.ui.components.ScreenScaffold
import ap.panini.procrastaint.ui.components.TasksMiniPreview
import ap.panini.procrastaint.util.Date
import ap.panini.procrastaint.util.Date.formatMilliseconds
import ap.panini.procrastaint.util.Time
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.CalendarScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Destination<RootGraph>(
    style = CalendarTransitions::class,
    start = true
)
@Composable
fun CalendarScreen(
    navigator: DestinationsNavigator,
    modifier: Modifier = Modifier,
    startTime: Long = Date.getTodayStart(),
    viewModel: CalendarViewModel = koinViewModel(parameters = { parametersOf(startTime) }),
) {
    val today by remember { mutableLongStateOf(Date.getTodayStart()) }

    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val dateState = viewModel.selectableDatesState.collectAsLazyPagingItems()
    val currentEventState = viewModel.currentEventsState.collectAsLazyPagingItems()

    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    val selectableListState = rememberLazyListState()
    val pagerState = rememberPagerState { currentEventState.itemCount }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .collect { page ->
                if (currentEventState.itemCount != 0) {
                    currentEventState[page]?.first?.let {
                        viewModel.setSelectedTime(it)
                    }
//                    selectableListState.animateScrollToItem(dateState.)
                }
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
                    items(count = dateState.itemCount, key = dateState.itemKey { it.first }) { i ->
                        val (time, item) = dateState[i]!!
                        val itemState = item.collectAsStateWithLifecycle(listOf()).value
                        TasksMiniPreview(
                            time,
                            itemState,
                            currentDateColor = with(MaterialTheme.colorScheme) {
                                when (time) {
                                    state.selectedTime -> tertiary
                                    today -> primary
                                    else -> onSurface
                                }
                            },

                            onClick = {
                                navigator.navigate(
                                    CalendarScreenDestination(startTime = time),
                                ) {
                                    popUpTo(CalendarScreenDestination) {
                                        inclusive = true
                                    }
                                }
                            }
                        )
                    }
                }

                HorizontalPager(
                    state = pagerState,
                    key = currentEventState.itemKey { it.first }
                ) { i ->

                    val (_, item) = currentEventState[i]!!
                    val itemState = item.collectAsStateWithLifecycle(listOf()).value

                    DayView(
                        itemState,
                        viewModel::checkTask,
                        modifier = Modifier.fillMaxSize()
                    )
                }
//                LazyRow(
//                    modifier = Modifier.fillMaxSize().wrapContentSize(),
//                    flingBehavior = rememberSnapFlingBehavior(lazyListState = state)
//                ) {
//                    items(
//                        count = currentEventState.itemCount,
//                        key = currentEventState.itemKey { it.first }) { i ->
//                        val (time, item) = currentEventState[i]!!
//                        val itemState = item.collectAsStateWithLifecycle(listOf()).value
//
//                        DayView(
//                            itemState,
//                            viewModel::checkTask,
//                            modifier = Modifier.fillMaxSize()
//                        )
//
//                    }

//                }
            }
        }
    }
}
