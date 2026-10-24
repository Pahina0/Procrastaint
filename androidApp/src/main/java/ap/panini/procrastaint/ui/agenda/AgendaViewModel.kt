package ap.panini.procrastaint.ui.agenda

import androidx.lifecycle.viewModelScope
import ap.panini.procrastaint.data.entities.ExternalCalendarEvent
import ap.panini.procrastaint.data.entities.TaskSingle
import ap.panini.procrastaint.data.entities.TaskTag
import ap.panini.procrastaint.data.repositories.DeviceCalendarRepository
import ap.panini.procrastaint.data.repositories.PreferenceRepository
import ap.panini.procrastaint.data.repositories.TaskRepository
import ap.panini.procrastaint.data.repositories.calendars.GoogleCalendarRepository
import ap.panini.procrastaint.ui.viewmodel.CheckableTaskViewModel
import ap.panini.procrastaint.util.Date
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
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

private const val ExtendedWindowDays = 60L

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class AgendaViewModel(
    taskRepository: TaskRepository,
    private val preferenceRepository: PreferenceRepository,
    private val deviceCalendarRepository: DeviceCalendarRepository? = null,
    private val googleCalendarRepository: GoogleCalendarRepository? = null,
    private val todayStartProvider: () -> Long = { Date.getTodayStart() }
) : CheckableTaskViewModel(taskRepository) {

    private val todayStartFlow = MutableStateFlow(todayStartProvider())
    private val focusedDateFlow = MutableStateFlow(todayStartProvider())
    private val selectedTagIdFlow = MutableStateFlow<Long?>(null)
    private val searchQueryFlow = MutableStateFlow("")
    private val rawScheduledTasks = MutableStateFlow<List<TaskSingle>>(emptyList())
    private val rawExternalEventsByDate = MutableStateFlow<Map<Long, List<ExternalCalendarEvent>>>(emptyMap())

    private val _uiState = MutableStateFlow(
        AgendaUiState(
            focusedDate = todayStartProvider(),
            hasDeviceCalendarPermission = deviceCalendarRepository?.hasPermission() ?: false,
            isGoogleLoggedIn = false
        )
    )
    val uiState: StateFlow<AgendaUiState> = _uiState.asStateFlow()

    val recentlyCompleted: StateFlow<Set<Pair<Long, Long>>> = recentlyCompletedInternal

    private val _scrollToTodayEvent = MutableSharedFlow<Long>(extraBufferCapacity = 1)
    val scrollToTodayEvent: SharedFlow<Long> = _scrollToTodayEvent.asSharedFlow()

    fun scrollToToday() {
        val today = todayStartProvider()
        setFocusedDate(today)
        _scrollToTodayEvent.tryEmit(today)
    }

    init {
        viewModelScope.launch {
            todayStartFlow.flatMapLatest { todayStart ->
                taskRepository.getOverdueTasks(todayStart)
                    .combine(recentlyCompletedInternal) { tasks, recentlyCompleted ->
                        tasks.filter { task ->
                            Pair(task.taskId, task.currentEventTime) !in recentlyCompleted
                        }
                    }
            }.collect { overdue ->
                _uiState.update { it.copy(overdueTasks = overdue) }
            }
        }

        viewModelScope.launch {
            combine(todayStartFlow, focusedDateFlow) { todayStart, focusedDate ->
                val from = todayStart
                val to = maxOf(todayStart, focusedDate) + ExtendedWindowDays.days.inWholeMilliseconds - 1
                Pair(from, to)
            }.distinctUntilChanged()
                .flatMapLatest { (from, to) ->
                    taskRepository.getTasks(
                        from = from,
                        to = to,
                        includeNoTimeTasks = true
                    )
                }.collect { tasks ->
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
            combine(rawExternalEventsByDate, selectedTagIdFlow) { events, tagId ->
                if (tagId != null) emptyMap() else events
            }.collect { filteredEvents ->
                _uiState.update { it.copy(externalEventsByDate = filteredEvents) }
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

        viewModelScope.launch {
            focusedDateFlow
                .map { it / (7.days.inWholeMilliseconds) }
                .distinctUntilChanged()
                .collect {
                    loadExternalEvents()
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
            taskRepository.moveOverdueTasksToDate(_uiState.value.overdueTasks, todayStartFlow.value)
        }
    }

    override fun checkTask(task: TaskSingle) {
        super.checkTask(task)
    }

    fun refresh() {
        todayStartFlow.value = todayStartProvider()
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
            val today = todayStartProvider()
            val focused = focusedDateFlow.value
            val from = today
            val to = maxOf(today, focused) + ExtendedWindowDays.days.inWholeMilliseconds - 1

            val deviceEvents = if (hasPermission) {
                val showDevice = preferenceRepository.getShowDeviceCalendar().first()
                if (showDevice) {
                    deviceRepo.getEvents(from, to)
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
            rawExternalEventsByDate.value = grouped
            _uiState.update {
                it.copy(
                    hasDeviceCalendarPermission = hasPermission
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
                todayStartFlow.value
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
