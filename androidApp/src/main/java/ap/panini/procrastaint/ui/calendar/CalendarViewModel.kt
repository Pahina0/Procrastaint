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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
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
                getAllTasks()
            }
        }
    }

    private fun getAllTasks() {
        val dayMs = 1.days.inWholeMilliseconds
        viewModelScope.launch {
            _uiState.collectLatest { v ->

                db.getUpcomingTasks(
                    v.minDate,
                    v.maxDate
                ).flowOn(Dispatchers.IO)
                    .collectLatest { taskInfos: List<TaskSingle> ->
                        _uiState.update {
                            val organized = mutableListOf<List<TaskSingle>>()

                            var fromTime = v.minDate
                            var toTime = fromTime + dayMs

                            while (toTime <= v.maxDate) {
                                organized += taskInfos.filter { f -> f.currentEventTime in fromTime..<toTime }

                                fromTime = toTime
                                toTime += dayMs
                            }
                            it.copy(
                                taskInfos = organized
                            )
                        }
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
        val taskInfos: List<List<TaskSingle>> = listOf(),
        val minDate: Long = Date.getTodayStart() - 2.days.inWholeMilliseconds,
        val maxDate: Long = Date.getTodayStart() + 3.days.inWholeMilliseconds,
    )
}
