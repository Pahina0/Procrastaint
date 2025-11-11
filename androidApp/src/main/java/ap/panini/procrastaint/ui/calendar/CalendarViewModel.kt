package ap.panini.procrastaint.ui.calendar

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import ap.panini.procrastaint.data.entities.TaskCompletion
import ap.panini.procrastaint.data.entities.TaskSingle
import ap.panini.procrastaint.data.repositories.TaskRepository
import ap.panini.procrastaint.util.Date
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CalendarViewModel(
    private val db: TaskRepository
) : ViewModel() {
    private val today = Date.getTodayStart()

    private val _uiState =
        MutableStateFlow(CalendarUiState(today, CalendarDisplayMode.DAILY, "Calendar"))
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    fun setTitle(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val dateState =
        uiState.map { Pair(it.initialDate, it.displayMode) }.distinctUntilChanged()
            .flatMapLatest { (initialDate, displayMode) ->
                Pager(
                    PagingConfig(
                        initialLoadSize = 100,
                        enablePlaceholders = false,
                        pageSize = 35,
                    )
                ) {
                    CalendarPagingSource(initialDate, displayMode)
                }.flow
            }.cachedIn(viewModelScope)

    fun jumpToDate(time: Long, displayMode: CalendarDisplayMode = CalendarDisplayMode.DAILY) {
        _uiState.update {
            it.copy(
                focusedDate = time,
                displayMode = displayMode,
                initialDate = time
            )
        }
    }

    fun setDisplayMode(displayMode: CalendarDisplayMode) {
        _uiState.update { it.copy(displayMode = displayMode) }
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

    @Immutable
    data class CalendarUiState(
        val initialDate: Long,
        val displayMode: CalendarDisplayMode = CalendarDisplayMode.DAILY,
        val title: String,
        val focusedDate: Long = initialDate,
    )
}
