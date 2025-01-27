package ap.panini.procrastaint.data.repositories

import ap.panini.procrastaint.data.database.dao.TaskDao
import ap.panini.procrastaint.data.entities.TaskCompletion
import ap.panini.procrastaint.data.entities.TaskSingle
import ap.panini.procrastaint.util.TaskGroup
import ap.panini.procrastaint.util.Time
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlin.math.min
import kotlin.time.Duration

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

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getUpcomingTasks(from: Long, to: Long): Flow<List<TaskSingle>> =
        taskDao.getUpcomingTasks(from, to)
            .mapLatest { list ->
                val filteredList = mutableListOf<TaskSingle>()

                // task id to a list of all the completed times to the completed id
                val completed = mutableMapOf<Long, MutableMap<Long, Long>>()
                for (task in list) {

                    // saves all the completed into a map
                    if (task.completed != null) {
                        completed.getOrPut(task.taskId) { mutableMapOf() }[task.currentEventTime] =
                            task.completionId
                    }

                    // saves all the unique tasks
                    if (filteredList.isEmpty() || task.metaId != filteredList.last().metaId) {
                        filteredList += task
                    }
                }


                filteredList.map { task ->
                    if (task.repeatOften == null || task.repeatTag == null || task.startTime == null) {
                        return@map listOf(
                            task.copy(
                                currentEventTime = task.startTime ?: 0
                            )
                        )
                    }


                    val items = mutableListOf<TaskSingle>()
                    val toTime = Instant.fromEpochMilliseconds(min(task.endTime ?: to, to))
                    var curTime = Instant.fromEpochMilliseconds(task.startTime)

                    val fromTime = Instant.fromEpochMilliseconds(from)
                    while (curTime < fromTime) {
                        curTime = task.repeatTag.incrementBy(curTime, task.repeatOften)
                    }


                    while (curTime <= toTime) {

                        val isCompleted =
                            curTime.toEpochMilliseconds() in (completed[task.taskId]?.keys ?: emptySet())
                        items += task.copy(
                            currentEventTime = curTime.toEpochMilliseconds(),
                            completed = if (isCompleted) {
                                curTime.toEpochMilliseconds()
                            } else {
                                null
                            },
                            completionId = completed[task.taskId]?.get(curTime.toEpochMilliseconds()) ?: 0
                        )

                        curTime = task.repeatTag.incrementBy(curTime, task.repeatOften)
                    }


                    return@map items
                }.flatten()
                    .sortedBy { it.currentEventTime }
            }
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
