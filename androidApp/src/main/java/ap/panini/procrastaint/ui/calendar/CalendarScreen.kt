package ap.panini.procrastaint.ui.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
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
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val dateState = viewModel.selectableDatesState.collectAsLazyPagingItems()

    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    ScreenScaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                scrollBehavior = scrollBehavior,
                title = { Text("Calendar") }
            )
        }
    ) { padding ->
        ScreenScaffold(modifier = Modifier.padding(padding)) {
            Column {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(count = dateState.itemCount, key = dateState.itemKey { it.first }) { i ->
                        val (time, item) = dateState[i]!!
                        val itemState = item.collectAsStateWithLifecycle(listOf()).value
                        TasksMiniPreview(
                            time,
                            itemState,
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
                DayView(
                    state.taskInfos,
                    viewModel::checkTask,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
