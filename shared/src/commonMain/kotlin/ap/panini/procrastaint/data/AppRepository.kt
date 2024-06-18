package ap.panini.procrastaint.data

import ap.panini.procrastaint.data.database.dao.TaskDao
import ap.panini.procrastaint.data.model.Task
import kotlinx.coroutines.flow.Flow

class AppRepository(private val taskDao: TaskDao) {
    suspend fun insertTask(task: Task) = taskDao.insertTask(task)

    fun getAllTasks(): Flow<List<Task>> = taskDao.getAllTasks()
}
