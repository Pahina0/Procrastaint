package ap.panini.procrastaint.ui.calendar

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import androidx.paging.map
import ap.panini.procrastaint.data.entities.TaskCompletion
import ap.panini.procrastaint.data.entities.TaskSingle
import ap.panini.procrastaint.data.repositories.PreferenceRepository
import ap.panini.procrastaint.data.repositories.TaskRepository
import ap.panini.procrastaint.util.Date
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class CalendarViewModel(
    private val db: TaskRepository,
    private val preferenceRepository: PreferenceRepository
) : ViewModel() {
    private val today = Date.getTodayStart()

    private val _uiState =
        MutableStateFlow(
            CalendarUiState(
                today,
                runBlocking { preferenceRepository.getCalendarDisplayMode().first() },
                "Calendar",
                showCompleted = runBlocking {
                    preferenceRepository.getShowCompletedTasks().first()
                },
                showIncomplete = runBlocking {
                    preferenceRepository.getShowIncompleteTasks().first()
                }
            )
        )
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            preferenceRepository.getCalendarDisplayMode().collect { displayMode ->
                _uiState.update { it.copy(displayMode = displayMode) }
            }
        }
        viewModelScope.launch {
            preferenceRepository.getShowCompletedTasks().collect { showCompleted ->
                _uiState.update { it.copy(showCompleted = showCompleted) }
            }
        }
        viewModelScope.launch {
            preferenceRepository.getShowIncompleteTasks().collect { showIncomplete ->
                _uiState.update { it.copy(showIncomplete = showIncomplete) }
            }
        }
    }

    fun setTitle(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val dateState =
        uiState.map {
            Triple(
                it.initialDate,
                it.displayMode,
                Pair(it.showCompleted, it.showIncomplete)
            )
        }.distinctUntilChanged()
            .flatMapLatest { (initialDate, displayMode, filter) ->
                Pager(
                    PagingConfig(
                        initialLoadSize = 100,
                        enablePlaceholders = false,
                        pageSize = 35,
                    )
                ) {
                    CalendarPagingSource(initialDate, displayMode)
                }.flow.map { pagingData ->
                    pagingData.map { calendarPageData ->
                        val filteredTasksByDayFlow = calendarPageData.tasksByDay.map { tasksByDay ->
                            tasksByDay.mapValues { (_, tasks) ->
                                filterTasks(tasks, filter.first, filter.second)
                            }
                        }
                        calendarPageData.copy(tasksByDay = filteredTasksByDayFlow)
                    }
                }
            }.cachedIn(viewModelScope)

    fun jumpToDate(time: Long, displayMode: CalendarDisplayMode = CalendarDisplayMode.DAILY) {
        _uiState.update {
            it.copy(
                focusedDate = time,
                displayMode = displayMode,
                initialDate = time
            )
        }
        viewModelScope.launch {
            preferenceRepository.setCalendarDisplayMode(displayMode)
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

    fun setFocusedDate(time: Long) {
        _uiState.update { it.copy(focusedDate = time) }
    }

    fun checkTask(task: TaskSingle) {
        val completion =
            TaskCompletion(
                Date.getTime(),
                task.currentEventTime,
                task.taskId,
                task.metaId,
                task.completionId
            )
        viewModelScope.launch {
            if (task.completed == null) {
                db.addCompletion(
                    completion
                )
            } else {
                db.removeCompletion(
                    completion
                )
            }
        }
    }

    fun filterTasks(
        tasks: List<TaskSingle>,
        showCompleted: Boolean,
        showIncomplete: Boolean
    ): List<TaskSingle> {
        return tasks.filter { task ->
            if (!showCompleted && task.completed != null) return@filter false
            if (!showIncomplete && task.completed == null) return@filter false
            true
        }
    }

    @Immutable
    data class CalendarUiState(
        val initialDate: Long,
        val displayMode: CalendarDisplayMode = CalendarDisplayMode.DAILY,
        val title: String,
        val focusedDate: Long = initialDate,
        val showCompleted: Boolean = true,
        val showIncomplete: Boolean = true,
    )
}
