package ap.panini.procrastaint.ui.upcoming

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ap.panini.procrastaint.data.model.Task
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

class UpcomingViewModel(
    private val db: AppRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UpcomingUiState())
    val uiState: StateFlow<UpcomingUiState> = _uiState.asStateFlow()

    init {
        getAllTasks()
    }

    private fun getAllTasks() {
        viewModelScope.launch {
            db.getAllTasks()
                .flowOn(Dispatchers.IO)
                .collectLatest { tasks: List<Task> ->
                    _uiState.update {
                        println(tasks)
                        it.copy(
                            tasks = tasks
                        )
                    }
                }
        }
    }

    fun checkTask(task: Task) {
        println(task)
        viewModelScope.launch {
            db.updateTask(
                task.copy(
                    completed = if (task.completed == null) {
                        Date.getTime()
                    } else {
                        null
                    }
                )
            )
        }
    }

    @Immutable
    data class UpcomingUiState(
        val tasks: List<Task> = listOf(),
    )
}
