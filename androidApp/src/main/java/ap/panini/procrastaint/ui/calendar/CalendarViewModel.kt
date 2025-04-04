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
    private val db: TaskRepository
) : ViewModel() {
    private val today = Date.getTodayStart()

    private val _uiState = MutableStateFlow(CalendarUiState(today))
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    val dateState = Pager(
        PagingConfig(
            initialLoadSize = 10,
            enablePlaceholders = false,
            pageSize = 2,
        )
    ) {
        CalendarPagingSource(today)
    }.flow
        .cachedIn(viewModelScope)

    fun setSelectedTime(time: Long) {
        _uiState.update { it.copy(selectedTime = time) }
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
        val selectedTime: Long
    )
}
