package ap.panini.procrastaint.ui.tag

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ap.panini.procrastaint.data.entities.TaskCompletion
import ap.panini.procrastaint.data.entities.TaskSingle
import ap.panini.procrastaint.data.repositories.TaskRepository
import ap.panini.procrastaint.util.Date
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class TagViewModel(
    tagId: Long,
    private val db: TaskRepository
) : ViewModel() {
    val tag = runBlocking { db.getTag(tagId) }
    private val tasks: Flow<List<TaskSingle>> =
        db.getTasks(from = 0, maxRepetition = 6, tagId = tagId, includeNoTimeTasks = true)

    val incompleteTasks: Flow<List<TaskSingle>> = tasks.map { list ->
        list.filter { it.completed == null }
    }
    val completedTasks: Flow<List<TaskSingle>> = tasks.map { list ->
        list.filter { it.completed != null }
    }

    fun checkTask(task: TaskSingle) {
        viewModelScope.launch {
            val completion =
                TaskCompletion(
                    Date.getTime(),
                    task.currentEventTime,
                    task.taskId,
                    task.metaId,
                    task.completionId
                )
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

    fun onDelete() {
        viewModelScope.launch {
            db.deleteTag(tag)
        }
    }

    fun onDeleteWithTasks() {
        viewModelScope.launch {
            db.deleteTagWithTasks(tag)
        }
    }
}
