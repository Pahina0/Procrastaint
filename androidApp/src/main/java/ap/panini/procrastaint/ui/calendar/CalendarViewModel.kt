package ap.panini.procrastaint.ui.calendar

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ap.panini.procrastaint.data.entities.ExternalCalendarEvent
import ap.panini.procrastaint.data.entities.TaskCompletion
import ap.panini.procrastaint.data.entities.TaskSingle
import ap.panini.procrastaint.data.repositories.DeviceCalendarRepository
import ap.panini.procrastaint.data.repositories.PreferenceRepository
import ap.panini.procrastaint.data.repositories.TaskRepository
import ap.panini.procrastaint.data.repositories.calendars.GoogleCalendarRepository
import ap.panini.procrastaint.util.Date
import ap.panini.procrastaint.util.Date.formatMilliseconds
import ap.panini.procrastaint.util.Time
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class CalendarViewModel(
    private val db: TaskRepository,
    private val preferenceRepository: PreferenceRepository,
    private val deviceCalendarRepository: DeviceCalendarRepository,
    private val googleCalendarRepository: GoogleCalendarRepository
) : ViewModel() {

    private val today = Date.getTodayStart()

    private val _uiState = MutableStateFlow(
        CalendarUiState(
            focusedDate = today,
            displayMode = CalendarDisplayMode.DAILY,
            title = today.formatMilliseconds(setOf(Time.MONTH, Time.DAY)),
            showProcrastaintTasks = true,
            showDeviceCalendar = true,
            showGoogleCalendar = true,
            showCompleted = true,
            showIncomplete = true,
            hasDeviceCalendarPermission = deviceCalendarRepository.hasPermission(),
            isGoogleLoggedIn = false
        )
    )
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    private val activeRangeFlow = MutableStateFlow(
        Pair(today - 30.days.inWholeMilliseconds, today + 30.days.inWholeMilliseconds)
    )

    init {
        viewModelScope.launch {
            preferenceRepository.getCalendarDisplayMode().collect { mode ->
                _uiState.update { it.copy(displayMode = mode) }
            }
        }
        viewModelScope.launch {
            preferenceRepository.getShowCompletedTasks().collect { show ->
                _uiState.update { it.copy(showCompleted = show) }
            }
        }
        viewModelScope.launch {
            preferenceRepository.getShowIncompleteTasks().collect { show ->
                _uiState.update { it.copy(showIncomplete = show) }
            }
        }
        viewModelScope.launch {
            preferenceRepository.getShowProcrastaintTasks().collect { show ->
                _uiState.update { it.copy(showProcrastaintTasks = show) }
            }
        }
        viewModelScope.launch {
            preferenceRepository.getShowDeviceCalendar().collect { show ->
                _uiState.update { it.copy(showDeviceCalendar = show) }
                loadExternalEvents()
            }
        }
        viewModelScope.launch {
            preferenceRepository.getShowGoogleCalendar().collect { show ->
                _uiState.update { it.copy(showGoogleCalendar = show) }
            }
        }
        viewModelScope.launch {
            googleCalendarRepository.isLoggedIn().collect { loggedIn ->
                _uiState.update { it.copy(isGoogleLoggedIn = loggedIn) }
            }
        }

        viewModelScope.launch {
            combine(
                activeRangeFlow,
                preferenceRepository.getShowCompletedTasks(),
                preferenceRepository.getShowIncompleteTasks(),
                preferenceRepository.getShowProcrastaintTasks()
            ) { range, showCompleted, showIncomplete, showProcrastaint ->
                Triple(range, Pair(showCompleted, showIncomplete), showProcrastaint)
            }.distinctUntilChanged().flatMapLatest { (range, _, showProcrastaint) ->
                if (!showProcrastaint) {
                    kotlinx.coroutines.flow.flowOf(emptyList())
                } else {
                    db.getTasks(from = range.first, to = range.second, includeNoTimeTasks = true)
                }
            }.collectLatest { allTasks ->
                val showCompleted = _uiState.value.showCompleted
                val showIncomplete = _uiState.value.showIncomplete
                val filtered = allTasks.filter { task ->
                    if (!showCompleted && task.completed != null) return@filter false
                    if (!showIncomplete && task.completed == null) return@filter false
                    true
                }
                val grouped = filtered.groupBy { task ->
                    Instant.fromEpochMilliseconds(task.currentEventTime)
                        .toLocalDateTime(TimeZone.currentSystemDefault()).date
                }
                _uiState.update { it.copy(tasksByDate = grouped) }
            }
        }

        loadExternalEvents()
    }

    fun refresh() {
        _uiState.update {
            it.copy(hasDeviceCalendarPermission = deviceCalendarRepository.hasPermission())
        }
        loadExternalEvents()
    }

    fun loadExternalEvents() {
        viewModelScope.launch {
            val range = activeRangeFlow.value
            val deviceEvents = if (_uiState.value.showDeviceCalendar && deviceCalendarRepository.hasPermission()) {
                deviceCalendarRepository.getEvents(range.first, range.second)
            } else {
                emptyList()
            }

            // Deduplicate events by title and time window
            val uniqueEvents = deviceEvents.distinctBy {
                "${it.title.trim().lowercase()}_${it.startTime}_${it.endTime}"
            }

            val grouped = uniqueEvents.groupBy { event ->
                Instant.fromEpochMilliseconds(event.startTime)
                    .toLocalDateTime(TimeZone.currentSystemDefault()).date
            }
            _uiState.update { it.copy(externalEventsByDate = grouped) }
        }
    }

    fun setTitle(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun jumpToDate(time: Long, displayMode: CalendarDisplayMode = _uiState.value.displayMode) {
        _uiState.update {
            it.copy(
                focusedDate = time,
                displayMode = displayMode
            )
        }
        updateActiveRange(time)
        viewModelScope.launch {
            preferenceRepository.setCalendarDisplayMode(displayMode)
        }
    }

    fun setFocusedDate(time: Long) {
        _uiState.update { it.copy(focusedDate = time) }
        updateActiveRange(time)
    }

    private fun updateActiveRange(focusedTime: Long) {
        val start = focusedTime - 30.days.inWholeMilliseconds
        val end = focusedTime + 30.days.inWholeMilliseconds
        if (activeRangeFlow.value.first > focusedTime || activeRangeFlow.value.second < focusedTime) {
            activeRangeFlow.value = Pair(start, end)
            loadExternalEvents()
        }
    }

    fun setDisplayMode(displayMode: CalendarDisplayMode) {
        _uiState.update { it.copy(displayMode = displayMode) }
        viewModelScope.launch {
            preferenceRepository.setCalendarDisplayMode(displayMode)
        }
    }

    fun setShowCompleted(show: Boolean) {
        _uiState.update { it.copy(showCompleted = show) }
        viewModelScope.launch {
            preferenceRepository.setShowCompletedTasks(show)
        }
    }

    fun setShowIncomplete(show: Boolean) {
        _uiState.update { it.copy(showIncomplete = show) }
        viewModelScope.launch {
            preferenceRepository.setShowIncompleteTasks(show)
        }
    }

    fun setShowProcrastaintTasks(show: Boolean) {
        _uiState.update { it.copy(showProcrastaintTasks = show) }
        viewModelScope.launch {
            preferenceRepository.setShowProcrastaintTasks(show)
        }
    }

    fun setShowDeviceCalendar(show: Boolean) {
        _uiState.update { it.copy(showDeviceCalendar = show) }
        loadExternalEvents()
        viewModelScope.launch {
            preferenceRepository.setShowDeviceCalendar(show)
        }
    }

    fun setShowGoogleCalendar(show: Boolean) {
        _uiState.update { it.copy(showGoogleCalendar = show) }
        viewModelScope.launch {
            preferenceRepository.setShowGoogleCalendar(show)
        }
    }

    fun checkTask(task: TaskSingle) {
        val completion = TaskCompletion(
            Date.getTime(),
            task.currentEventTime,
            task.taskId,
            task.metaId,
            task.completionId
        )
        viewModelScope.launch {
            if (task.completed == null) {
                db.addCompletion(completion)
            } else {
                db.removeCompletion(completion)
            }
        }
    }

    @Immutable
    data class CalendarUiState(
        val focusedDate: Long,
        val displayMode: CalendarDisplayMode = CalendarDisplayMode.DAILY,
        val title: String,
        val showProcrastaintTasks: Boolean = true,
        val showDeviceCalendar: Boolean = true,
        val showGoogleCalendar: Boolean = true,
        val showCompleted: Boolean = true,
        val showIncomplete: Boolean = true,
        val hasDeviceCalendarPermission: Boolean = true,
        val isGoogleLoggedIn: Boolean = false,
        val tasksByDate: Map<LocalDate, List<TaskSingle>> = emptyMap(),
        val externalEventsByDate: Map<LocalDate, List<ExternalCalendarEvent>> = emptyMap()
    )
}
