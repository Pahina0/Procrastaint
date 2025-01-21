package ap.panini.procrastaint.data.repositories

import ap.panini.procrastaint.data.database.dao.TaskDao
import ap.panini.procrastaint.data.model.Task
import ap.panini.procrastaint.util.TaskGroup
import kotlinx.coroutines.flow.Flow

class AppRepository(private val taskDao: TaskDao) {
    suspend fun insertTask(task: TaskGroup): Boolean {
        val tasks = task.toTaskList()

        if (tasks != null) {
            taskDao.insertTasks(tasks)
        }

        return tasks != null
    }

    fun getAllTasks(): Flow<List<Task>> = taskDao.getAllTasks()

    fun getTaskHistory(): Flow<List<Task>> = taskDao.getTaskHistory()
    fun getIncompleteTasks(): Flow<List<Task>> = taskDao.getIncompleteTasks()

    fun getUpcomingTasks(from: Long): Flow<List<Task>> = taskDao.getUpcomingTasksGrouped(from)

    suspend fun updateTask(task: Task) = taskDao.updateTask(task)
}
