package ap.panini.procrastaint.ui.calendar

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ap.panini.procrastaint.data.entities.TaskCompletion
import ap.panini.procrastaint.data.entities.TaskSingle
import ap.panini.procrastaint.data.repositories.AppRepository
import ap.panini.procrastaint.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.days

class CalendarViewModel(
    private val db: AppRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.collectLatest {
                getAllTasks(it.dateOffset)
            }
        }
    }

    private fun getAllTasks(offset: Int) {

        viewModelScope.launch {
            db.getUpcomingTasks(
//                Date.getTodayStart() - 2.days.inWholeMilliseconds + offset,
//                Date.getTodayStart() + 3.days.inWholeMilliseconds + offset
                Date.getTodayStart() ,
                Date.getTodayStart() + 1.days.inWholeMilliseconds + offset
            ).flowOn(Dispatchers.IO)
                .collectLatest { taskInfos: List<TaskSingle> ->
                    _uiState.update {
                        it.copy(
                            taskInfos = taskInfos
                        )
                    }
                }
        }
    }

    fun checkTask(task: TaskSingle) {
        viewModelScope.launch {
            if (task.completed == null) {
                db.addCompletion(
                    TaskCompletion(
                        Date.getTime(),
                        task.currentEventTime,
                        task.taskId,
                        task.metaId,
                        task.completionId
                    )
                )
            } else {
                db.removeCompletion(
                    TaskCompletion(
                        Date.getTime(),
                        task.currentEventTime,
                        task.taskId,
                        task.metaId,
                        task.completionId
                    )
                )
            }
        }
    }

    @Immutable
    data class CalendarUiState(
        val taskInfos: List<TaskSingle> = listOf(),
        val dateOffset: Int = 0
    )

}