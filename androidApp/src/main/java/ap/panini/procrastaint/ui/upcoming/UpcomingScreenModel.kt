package ap.panini.procrastaint.ui.upcoming

import ap.panini.procrastaint.data.AppRepository
import ap.panini.procrastaint.data.model.Task
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UpcomingScreenModel(
    private val db: AppRepository
) : ScreenModel {

    private val _tasks = MutableStateFlow(emptyList<Task>())
    val tasks = _tasks.asStateFlow()

    init {
        getAllTasks()
    }

    private fun getAllTasks() {
        screenModelScope.launch {
            db.getAllTasks().flowOn(Dispatchers.IO).collectLatest { task: List<Task> ->
                _tasks.update { task }
            }
        }
    }

    fun checkTask(task: Task) {
        task.completed = !task.completed
        screenModelScope.launch { db.updateTask(task) }
    }

    // @Immutable
    // data class State(
    //    val tasks: Map<Long, TaskGroup>
    // )
}
