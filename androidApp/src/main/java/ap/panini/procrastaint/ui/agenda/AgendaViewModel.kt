package ap.panini.procrastaint.ui.agenda

import androidx.lifecycle.viewModelScope
import ap.panini.procrastaint.data.entities.TaskSingle
import ap.panini.procrastaint.data.entities.TaskTag
import ap.panini.procrastaint.data.repositories.DeviceCalendarRepository
import ap.panini.procrastaint.data.repositories.PreferenceRepository
import ap.panini.procrastaint.data.repositories.TaskRepository
import ap.panini.procrastaint.data.repositories.calendars.GoogleCalendarRepository
import ap.panini.procrastaint.ui.viewmodel.CheckableTaskViewModel
import ap.panini.procrastaint.util.Date
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

private const val RollingWindowDays = 30L

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class AgendaViewModel(
    taskRepository: TaskRepository,
    private val preferenceRepository: PreferenceRepository,
    private val deviceCalendarRepository: DeviceCalendarRepository? = null,
    private val googleCalendarRepository: GoogleCalendarRepository? = null
) : CheckableTaskViewModel(taskRepository) {

    private val focusedDateFlow = MutableStateFlow(Date.getTodayStart())
    private val selectedTagIdFlow = MutableStateFlow<Long?>(null)
    private val searchQueryFlow = MutableStateFlow("")
    private val rawScheduledTasks = MutableStateFlow<List<TaskSingle>>(emptyList())

    private val _uiState = MutableStateFlow(
        AgendaUiState(
            focusedDate = Date.getTodayStart(),
            hasDeviceCalendarPermission = deviceCalendarRepository?.hasPermission() ?: false,
            isGoogleLoggedIn = false
        )
    )
    val uiState: StateFlow<AgendaUiState> = _uiState.asStateFlow()

    val recentlyCompleted: StateFlow<Set<Pair<Long, Long>>> = recentlyCompletedInternal

    init {
        val today = Date.getTodayStart()
        val rollingEnd = today + (RollingWindowDays + 1).days.inWholeMilliseconds - 1

        viewModelScope.launch {
            taskRepository.getOverdueTasks(Date.getTodayStart())
                .combine(recentlyCompletedInternal) { tasks, recentlyCompleted ->
                    tasks.filter { task ->
                        Pair(task.taskId, task.currentEventTime) !in recentlyCompleted
                    }
                }.collect { overdue ->
                    _uiState.update { it.copy(overdueTasks = overdue) }
                }
        }

        viewModelScope.launch {
            taskRepository.getTasks(
                from = today,
                to = rollingEnd,
                includeNoTimeTasks = true
            ).collect { tasks ->
                rawScheduledTasks.value = tasks
            }
        }

        viewModelScope.launch {
            combine(
                rawScheduledTasks,
                selectedTagIdFlow,
                searchQueryFlow
            ) { tasks, tagId, query ->
                filterAndGroupScheduledTasks(tasks, tagId, query)
            }.collect { grouped ->
                _uiState.update { it.copy(scheduledTasksByDate = grouped) }
            }
        }

        viewModelScope.launch {
            taskRepository.getTags().collect { tagsList ->
                _uiState.update { it.copy(tags = tagsList) }
            }
        }

        viewModelScope.launch {
            focusedDateFlow
                .map { getMonthRange(it) }
                .distinctUntilChanged()
                .flatMapLatest { (start, end) ->
                    taskRepository.getTasks(from = start, to = end, includeNoTimeTasks = true)
                }.collect { monthTasks ->
                    val grouped = monthTasks.groupBy { getStartOfDay(it.currentEventTime) }
                    _uiState.update { it.copy(monthTasksByDate = grouped) }
                }
        }

        viewModelScope.launch {
            preferenceRepository.getShowDeviceCalendar().collect {
                loadExternalEvents()
            }
        }

        googleCalendarRepository?.let { googleRepo ->
            viewModelScope.launch {
                googleRepo.isLoggedIn().collect { loggedIn ->
                    _uiState.update { it.copy(isGoogleLoggedIn = loggedIn) }
                }
            }
        }

        loadExternalEvents()
    }

    fun setFocusedDate(date: Long) {
        focusedDateFlow.value = date
        _uiState.update { it.copy(focusedDate = date) }
    }

    fun toggleMonthExpansion() {
        _uiState.update { it.copy(isMonthExpanded = !it.isMonthExpanded) }
    }

    fun selectTag(tagId: Long?) {
        val newTagId = if (selectedTagIdFlow.value == tagId) null else tagId
        selectedTagIdFlow.value = newTagId
        _uiState.update { it.copy(selectedTagId = newTagId) }
    }

    fun setSearchQuery(query: String) {
        searchQueryFlow.value = query
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun moveAllOverdueToToday() {
        viewModelScope.launch {
            taskRepository.moveOverdueTasksToDate(_uiState.value.overdueTasks, Date.getTodayStart())
        }
    }

    override fun checkTask(task: TaskSingle) {
        super.checkTask(task)
    }

    fun refresh() {
        _uiState.update {
            it.copy(hasDeviceCalendarPermission = deviceCalendarRepository?.hasPermission() ?: false)
        }
        loadExternalEvents()
    }

    fun isValidTag(tag: TaskTag): Boolean {
        return runBlocking {
            val found = taskRepository.getTagOrNull(tag.title)
            if (found == null) return@runBlocking true
            found.tagId == tag.tagId
        }
    }

    fun saveTag(tag: TaskTag) {
        viewModelScope.launch {
            taskRepository.upsertTaskTag(tag)
        }
    }

    fun loadExternalEvents() {
        val deviceRepo = deviceCalendarRepository ?: return
        viewModelScope.launch {
            val hasPermission = deviceRepo.hasPermission()
            val today = Date.getTodayStart()
            val rollingEnd = today + (RollingWindowDays + 1).days.inWholeMilliseconds - 1

            val deviceEvents = if (hasPermission) {
                val showDevice = preferenceRepository.getShowDeviceCalendar().first()
                if (showDevice) {
                    deviceRepo.getEvents(today, rollingEnd)
                } else {
                    emptyList()
                }
            } else {
                emptyList()
            }

            val uniqueEvents = deviceEvents.distinctBy {
                "${it.title.trim().lowercase()}_${it.startTime}_${it.endTime}"
            }

            val grouped = uniqueEvents.groupBy { getStartOfDay(it.startTime) }
            _uiState.update {
                it.copy(
                    hasDeviceCalendarPermission = hasPermission,
                    externalEventsByDate = grouped
                )
            }
        }
    }

    private fun filterAndGroupScheduledTasks(
        tasks: List<TaskSingle>,
        selectedTagId: Long?,
        searchQuery: String
    ): Map<Long, List<TaskSingle>> {
        var filtered = tasks
        if (selectedTagId != null) {
            filtered = filtered.filter { task ->
                task.tags.any { it.tagId == selectedTagId }
            }
        }
        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            filtered = filtered.filter { task ->
                task.title.lowercase().contains(q) ||
                    task.description.lowercase().contains(q) ||
                    task.tags.any { it.title.lowercase().contains(q) }
            }
        }
        return filtered.groupBy { task ->
            if (task.currentEventTime > 0) {
                getStartOfDay(task.currentEventTime)
            } else {
                Date.getTodayStart()
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
internal fun getStartOfDay(timestamp: Long): Long {
    if (timestamp <= 0L) return Date.getTodayStart()
    val localDateTime = Instant.fromEpochMilliseconds(timestamp)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    return LocalDateTime(
        year = localDateTime.year,
        month = localDateTime.month,
        day = localDateTime.day,
        hour = 0,
        minute = 0,
        second = 0,
        nanosecond = 0
    ).toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
}

@OptIn(ExperimentalTime::class)
internal fun getMonthRange(timestamp: Long): Pair<Long, Long> {
    val date = Instant.fromEpochMilliseconds(timestamp)
        .toLocalDateTime(TimeZone.currentSystemDefault()).date
    val startOfMonth = LocalDate(date.year, date.month, 1)
    val startOfMonthMillis = LocalDateTime(startOfMonth.year, startOfMonth.month, 1, 0, 0, 0, 0)
        .toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
    val nextMonth = startOfMonth.plus(1, DateTimeUnit.MONTH)
    val startOfNextMonthMillis = LocalDateTime(nextMonth.year, nextMonth.month, 1, 0, 0, 0, 0)
        .toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
    return Pair(startOfMonthMillis, startOfNextMonthMillis - 1)
}
