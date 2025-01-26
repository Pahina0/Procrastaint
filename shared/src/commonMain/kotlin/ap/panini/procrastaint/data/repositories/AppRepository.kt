package ap.panini.procrastaint.data.repositories

import ap.panini.procrastaint.data.database.dao.TaskDao
import ap.panini.procrastaint.data.entities.TaskCompletion
import ap.panini.procrastaint.data.entities.TaskSingle
import ap.panini.procrastaint.util.TaskGroup
import kotlinx.coroutines.flow.Flow

class AppRepository(private val taskDao: TaskDao) {
    suspend fun insertTask(task: TaskGroup): Boolean {
        val tasks = task.toTask() ?: return false

        val id = taskDao.insertTaskInfo(tasks.taskInfo)

        tasks.meta.forEach {
            taskDao.insertTaskMeta(it.copy(taskId = id))
        }

        return true

    }

    suspend fun addCompletion(taskCompletion: TaskCompletion) =
        taskDao.insertTaskCompletion(
            taskCompletion
        )

    suspend fun removeCompletion(taskCompletion: TaskCompletion) =
        taskDao.deleteTaskCompletion(
            taskCompletion
        )

    fun getUpcomingTasks(from: Long, to: Long): Flow<List<TaskSingle>> =
        taskDao.getUpcomingTasks(from, to)
//
//    fun getAllTasks(): Flow<List<TaskInfo>> = taskDao.getAllTasks()
//
//    fun getTaskHistory(): Flow<List<TaskInfo>> = taskDao.getTaskHistory()
//    fun getIncompleteTasks(): Flow<List<TaskInfo>> = taskDao.getIncompleteTasks()
//
//    fun getUpcomingTasks(from: Long): Flow<List<TaskInfo>> = taskDao.getUpcomingTasksGrouped(from)
//
//    suspend fun updateTask(taskInfo: TaskInfo) = taskDao.updateTask(taskInfo)
}
