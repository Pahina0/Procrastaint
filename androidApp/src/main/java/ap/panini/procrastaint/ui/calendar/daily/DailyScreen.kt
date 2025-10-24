package ap.panini.procrastaint.ui.calendar.daily

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import ap.panini.procrastaint.ui.MainActivityViewModel
import ap.panini.procrastaint.ui.calendar.CalendarPageData
import ap.panini.procrastaint.ui.calendar.components.SingleDayView
import ap.panini.procrastaint.util.Date
import ap.panini.procrastaint.util.Date.formatMilliseconds
import ap.panini.procrastaint.util.Time
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import kotlin.math.max

@Composable
fun DailyScreen(

    modifier: Modifier = Modifier,

    viewModel: DailyViewModel = koinViewModel(),

    onTodayClick: () -> Unit,

    onTitleChange: (String) -> Unit

) {

    val activityViewModel = koinViewModel<MainActivityViewModel>(

        viewModelStoreOwner = LocalActivity.current as ComponentActivity

    )


    val today by remember { mutableLongStateOf(Date.getTodayStart()) }


    val state = viewModel.uiState.collectAsStateWithLifecycle().value

    val dateState = viewModel.dateState.collectAsLazyPagingItems()


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

                max(dateState.itemSnapshotList.indexOfFirst { it?.time == state.selectedTime }, 0)

            pagerState.animateScrollToPage(index)

            selectableListState.animateScrollToItem(max(index - 1, 0))



            index =

                max(dateState.itemSnapshotList.indexOfFirst { it?.time == state.selectedTime }, 0)

            pagerState.animateScrollToPage(index)

            selectableListState.animateScrollToItem(max(index - 1, 0))

        }

    }



    LaunchedEffect(pagerState.currentPage, pagerState.isScrollInProgress) {

        if (dateState.itemCount == 0) return@LaunchedEffect

        if (pagerState.isScrollInProgress) {

            viewModel.setSelectedTime(

                dateState[pagerState.targetPage]?.time ?: return@LaunchedEffect

            )

        }

    }



    LaunchedEffect(state.selectedTime) {

        onTitleChange(state.selectedTime.formatMilliseconds(setOf(Time.MONTH, Time.DAY)))

    }



    Column(modifier = modifier) {
        DailyCalendarView(
            dateState = dateState,
            selectableListState = selectableListState,
            selectedTime = state.selectedTime,
            today = today,
            viewModel = viewModel
        )

        HorizontalPager(
            state = pagerState,
            key = dateState.itemKey { it.time }
        ) { i ->
            val dayData = dateState[i]
            if (dayData != null) {
                when (dayData) {
                    is CalendarPageData.Daily -> {
                        val itemState = dayData.tasks.collectAsStateWithLifecycle(mapOf()).value

                        SingleDayView(
                            itemState,
                            viewModel::checkTask,
                            onEdit = activityViewModel::editCreatedTask,
                            isToday = dayData.time == today,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    else -> {}
                }
            }
        }
    }
}