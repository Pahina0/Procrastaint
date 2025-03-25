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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CalendarViewModel(
    time: Long,
    private val db: TaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState(time))
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    val currentEventsState = Pager(
        PagingConfig(
            initialLoadSize = 3,
            enablePlaceholders = false,
            pageSize = 3
        )
    ) {
        CalendarPagingSource(time)
    }.flow
        .cachedIn(viewModelScope)

    val selectableDatesState = Pager(
        PagingConfig(
            initialLoadSize = 10,
            enablePlaceholders = false,
            pageSize = 5
        )
    ) {
        CalendarPagingSource(time)
    }.flow
        .cachedIn(viewModelScope)

    fun setSelectedTime(time: Long) {
        _uiState.update { it.copy(selectedTime = time) }
    }

//    init {
//        viewModelScope.launch {
//            _uiState.collectLatest {
//                getAllTasks()
//            }
//        }
//    }
//
//    private fun getAllTasks() {
//        viewModelScope.launch {
//            db.getTasksBetween(
//                time,
//                time + 24.hours.inWholeMilliseconds
//            ).flowOn(Dispatchers.IO)
//                .collectLatest { tasks: List<TaskSingle> ->
//                    _uiState.update {
//                        it.copy(taskInfos = tasks)
//                    }
//                }
//        }
//    }

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
        val selectedTime: Long
    )
}
