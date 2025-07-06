package ap.panini.procrastaint.ui.library.tag

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ap.panini.procrastaint.data.entities.TaskSingle
import ap.panini.procrastaint.data.repositories.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class TagViewModel(
    tagId: Long,
    private val db: TaskRepository
) : ViewModel() {
    val tag = runBlocking { db.getTag(tagId) }
    val tasks: Flow<List<TaskSingle>> = db.getTasks(from = 0, tagId = tagId, includeNoTimeTasks = true)

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

    fun onEdit() {
        // TODO: Implement edit functionality
    }
}
