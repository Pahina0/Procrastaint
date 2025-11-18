package ap.panini.procrastaint.ui.calendar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import ap.panini.procrastaint.ui.calendar.components.CalendarFilterBottomSheet
import ap.panini.procrastaint.ui.calendar.daily.DailyScreen
import ap.panini.procrastaint.ui.calendar.monthly.MonthlyScreen
import ap.panini.procrastaint.ui.calendar.weekly.WeeklyScreen
import ap.panini.procrastaint.ui.components.ScreenScaffold
import ap.panini.procrastaint.util.Date
import org.koin.androidx.compose.koinViewModel
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun CalendarScreen(
    modifier: Modifier = Modifier,
    viewModel: CalendarViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val today by remember { mutableLongStateOf(Date.getTodayStart()) }

    var showFilterSheet by remember { mutableStateOf(false) }

    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
        ) {
            CalendarFilterBottomSheet(
                displayMode = state.displayMode,
                onDisplayModeChange = {
                    viewModel.setDisplayMode(it)
                },
                showCompleted = state.showCompleted,
                showIncomplete = state.showIncomplete,
                setShowCompleted = viewModel::setShowCompleted,
                setShowIncomplete = viewModel::setShowIncomplete
            )
        }
    }

    ScreenScaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(state.title)
                },
                actions = {
                    IconButton(onClick = { showFilterSheet = true }) {
                        Icon(Icons.Outlined.FilterList, contentDescription = "Filter calendar view")
                    }
                    IconButton(
                        onClick = {
                            viewModel.jumpToDate(
                                today,
                                state.displayMode
                            )
                        }
                    ) {
                        Icon(Icons.Outlined.Today, contentDescription = "Today")
                    }
                }
            )
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
        ) {
            when (state.displayMode) {
                CalendarDisplayMode.DAILY -> DailyScreen(
                    onTitleChange = viewModel::setTitle,
                    focusedDate = state.focusedDate,
                    dateState = viewModel.dateState.collectAsLazyPagingItems(),
                    setFocusedDate = viewModel::setFocusedDate,
                    checkTask = viewModel::checkTask,
                    initialDate = state.focusedDate
                )

                CalendarDisplayMode.WEEKLY -> WeeklyScreen(
                    onTitleChange = viewModel::setTitle,
                    dateState = viewModel.dateState.collectAsLazyPagingItems(),
                    focusedDate = state.focusedDate,
                    setFocusedDate = viewModel::setFocusedDate,
                    jumpToDate = viewModel::jumpToDate
                )

                CalendarDisplayMode.MONTHLY -> MonthlyScreen(
                    onTitleChange = viewModel::setTitle,
                    dateState = viewModel.dateState.collectAsLazyPagingItems(),
                    focusedDate = state.focusedDate,
                    setFocusedDate = viewModel::setFocusedDate,
                    jumpToDate = viewModel::jumpToDate
                )
            }
        }
    }
}
