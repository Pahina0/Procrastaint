package ap.panini.procrastaint.ui.inbox

import androidx.compose.runtime.Immutable
import ap.panini.procrastaint.data.model.Task
import ap.panini.procrastaint.data.repositories.AppRepository
import ap.panini.procrastaint.data.repositories.PreferenceRepository
import ap.panini.procrastaint.util.Date
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TaskListScreenModel(
    private val db: AppRepository,
    private val pref: PreferenceRepository
) : StateScreenModel<TaskListScreenModel.State>(State()) {

    val optionsFlow: Flow<Options> =
        combine(
            pref.getBoolean(PreferenceRepository.SHOW_COMPLETE),
            pref.getBoolean(PreferenceRepository.SHOW_INCOMPLETE)
        ) { complete, incomplete ->
            Options(
                showComplete = complete,
                showIncomplete = incomplete
            )
        }.flowOn(Dispatchers.IO)


    init {
        getAllTasks()
    }

    private fun getAllTasks() {
        screenModelScope.launch {
            db.getAllTasks()
                .flowOn(Dispatchers.IO)
                .combine(optionsFlow) { tasks, options ->
                    tasks.applyFilters(options)
                }
                .collectLatest { tasks: List<Task> ->
                    mutableState.update {
                        it.copy(
                            tasks = tasks
                        )
                    }
                }
        }
    }

    fun checkTask(task: Task) {
        if (task.completed == null) {
            task.completed = Date.getTime()
        } else {
            task.completed = null
        }

        screenModelScope.launch { db.updateTask(task) }
    }


    private fun List<Task>.applyFilters(options: Options): List<Task> {
        val filterComplete: (Task) -> Boolean = {
            if (options.showComplete) {
                true
            } else {
                it.completed == null
            }
        }

        val filterIncomplete: (Task) -> Boolean = {
            if (options.showIncomplete) {
                true
            } else {
                it.completed != null
            }
        }

        val allFilters: (Task) -> Boolean = {
            filterComplete(it) &&
                    filterIncomplete(it)
        }

        return filter(allFilters)
    }

    fun changeFilterOptions(key: String, to: Boolean) {
        screenModelScope.launch {
            pref.putBoolean(key, to)
        }
    }


    @Immutable
    data class Options(
        val showComplete: Boolean,
        val showIncomplete: Boolean
    )


    @Immutable
    data class State(
        val tasks: List<Task> = listOf(),
    )
}
