package ap.panini.procrastaint.ui.calendar


import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import ap.panini.procrastaint.ui.calendar.components.CalendarFilterBottomSheet
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
import org.koin.androidx.compose.koinViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    modifier: Modifier = Modifier,
    viewModel: CalendarViewModel = koinViewModel(),
) {




    val state = viewModel.uiState.collectAsStateWithLifecycle().value

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




    ScreenScaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Calendar")
                },
                actions = {
                    IconButton(onClick = { showFilterSheet = true }) {
                        Icon(Icons.Outlined.FilterList, contentDescription = "Filter calendar view")
                    }

                }
            )
        }
    ) { padding ->
        ScreenScaffold(modifier = Modifier.padding(padding)) {
            when (state.displayMode) {
                CalendarDisplayMode.DAILY -> DailyScreen()
                CalendarDisplayMode.WEEKLY -> WeeklyScreen()
                CalendarDisplayMode.MONTHLY -> MonthlyScreen()
            }
        }
    }
}
