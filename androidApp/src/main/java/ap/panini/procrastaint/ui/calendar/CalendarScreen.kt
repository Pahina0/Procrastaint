package ap.panini.procrastaint.ui.calendar

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ap.panini.procrastaint.ui.calendar.components.CalendarFilterBottomSheet
import ap.panini.procrastaint.ui.calendar.daily.DailyScreen
import ap.panini.procrastaint.ui.calendar.monthly.MonthlyScreen
import ap.panini.procrastaint.ui.calendar.weekly.WeeklyScreen
import ap.panini.procrastaint.ui.components.ScreenScaffold
import ap.panini.procrastaint.ui.settings.auth.GoogleAuth
import ap.panini.procrastaint.util.Date
import org.koin.androidx.compose.koinViewModel
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun CalendarScreen(
    modifier: Modifier = Modifier,
    viewModel: CalendarViewModel = koinViewModel(),
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val today by remember { mutableLongStateOf(Date.getTodayStart()) }
    var showFilterSheet by remember { mutableStateOf(false) }

    val googleAuth = remember(context) { GoogleAuth(context) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.refresh()
        if (isGranted) {
            viewModel.setShowDeviceCalendar(true)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
        ) {
            CalendarFilterBottomSheet(
                displayMode = state.displayMode,
                onDisplayModeChange = viewModel::setDisplayMode,
                showProcrastaintTasks = state.showProcrastaintTasks,
                showDeviceCalendar = state.showDeviceCalendar,
                showGoogleCalendar = state.showGoogleCalendar,
                showCompleted = state.showCompleted,
                showIncomplete = state.showIncomplete,
                onToggleProcrastaintTasks = viewModel::setShowProcrastaintTasks,
                onToggleDeviceCalendar = viewModel::setShowDeviceCalendar,
                onToggleGoogleCalendar = viewModel::setShowGoogleCalendar,
                setShowCompleted = viewModel::setShowCompleted,
                setShowIncomplete = viewModel::setShowIncomplete,
                hasDeviceCalendarPermission = state.hasDeviceCalendarPermission,
                isGoogleLoggedIn = state.isGoogleLoggedIn,
                onRequestDeviceCalendarPermission = {
                    permissionLauncher.launch(Manifest.permission.READ_CALENDAR)
                },
                onConnectGoogle = {
                    googleAuth.auth(context)
                }
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
                    focusedDate = state.focusedDate,
                    tasksByDate = state.tasksByDate,
                    eventsByDate = state.externalEventsByDate,
                    onTitleChange = viewModel::setTitle,
                    setFocusedDate = viewModel::setFocusedDate,
                    checkTask = viewModel::checkTask
                )

                CalendarDisplayMode.WEEKLY -> WeeklyScreen(
                    focusedDate = state.focusedDate,
                    tasksByDate = state.tasksByDate,
                    eventsByDate = state.externalEventsByDate,
                    onTitleChange = viewModel::setTitle,
                    setFocusedDate = viewModel::setFocusedDate,
                    jumpToDate = viewModel::jumpToDate,
                    modifier = Modifier.fillMaxSize()
                )

                CalendarDisplayMode.MONTHLY -> MonthlyScreen(
                    focusedDate = state.focusedDate,
                    tasksByDate = state.tasksByDate,
                    eventsByDate = state.externalEventsByDate,
                    onTitleChange = viewModel::setTitle,
                    setFocusedDate = viewModel::setFocusedDate,
                    jumpToDate = viewModel::jumpToDate,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
