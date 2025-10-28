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
import ap.panini.procrastaint.util.Date
import ap.panini.procrastaint.ui.calendar.components.CalendarFilterBottomSheet
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ap.panini.procrastaint.ui.calendar.daily.DailyScreen
import ap.panini.procrastaint.ui.calendar.monthly.MonthlyScreen
import ap.panini.procrastaint.ui.calendar.weekly.WeeklyScreen
import ap.panini.procrastaint.ui.components.ScreenScaffold
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import org.koin.androidx.compose.koinViewModel
import kotlin.time.ExperimentalTime


@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun CalendarScreen(
    modifier: Modifier = Modifier,
    viewModel: CalendarViewModel = koinViewModel(),
) {


    val state = viewModel.uiState.collectAsStateWithLifecycle().value

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
                }
            )
        }
    }


    val onTodayClick: () -> Unit = {
        viewModel.setSelectedTime(today)
    }

    ScreenScaffold(
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
        ) {
            when (state.displayMode) {
                CalendarDisplayMode.DAILY -> DailyScreen(
                    onTodayClick = onTodayClick,
                    onTitleChange = viewModel::setTitle,
                    viewModel = viewModel
                )

                CalendarDisplayMode.WEEKLY -> WeeklyScreen(
                    onTodayClick = onTodayClick,
                    onTitleChange = viewModel::setTitle,
                    viewModel = viewModel
                )

                CalendarDisplayMode.MONTHLY -> MonthlyScreen(
                    onTodayClick = onTodayClick,
                    onTitleChange = viewModel::setTitle,
                    onDateClick = { date ->
                        viewModel.setDisplayMode(CalendarDisplayMode.DAILY)
                        viewModel.setSelectedTime(
                            date.atStartOfDayIn(TimeZone.currentSystemDefault())
                                .toEpochMilliseconds()
                        )
                    },
                    viewModel = viewModel
                )
            }
        }
    }
}
