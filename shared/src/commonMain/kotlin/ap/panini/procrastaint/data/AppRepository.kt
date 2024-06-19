package ap.panini.procrastaint.data

import ap.panini.procrastaint.data.database.dao.TaskDao
import ap.panini.procrastaint.data.model.Task
import ap.panini.procrastaint.util.TaskGroup
import kotlinx.coroutines.flow.Flow

class AppRepository(private val taskDao: TaskDao) {
    suspend fun insertTask(task: TaskGroup) = taskDao.insertTasks(task.toTaskList())

    fun getAllTasks(): Flow<List<Task>> = taskDao.getAllTasks()

    fun getIncompleteTasks(): Flow<List<Task>> = taskDao.getIncompleteTasks()

    suspend fun updateTask(task: Task) = taskDao.updateTask(task)
}
