package ap.panini.procrastaint.data.repositories

import ap.panini.procrastaint.data.database.dao.TaskDao
import ap.panini.procrastaint.data.entities.Task
import ap.panini.procrastaint.data.entities.TaskCompletion
import ap.panini.procrastaint.data.entities.TaskSingle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlin.math.min

class TaskRepository(
    private val taskDao: TaskDao, private val calendar: NetworkCalendarRepository
) {


     suspend fun insertTask(tasks: Task): Boolean {

        val id = taskDao.insertTaskInfo(tasks.taskInfo)

        tasks.meta.forEach {
            taskDao.insertTaskMeta(it.copy(taskId = id))
        }

        CoroutineScope(Dispatchers.IO).launch {
            calendar.createTask(taskDao.getTask(id))
        }

        return true
    }

    suspend fun getTask(id: Long): Task = taskDao.getTask(id)

    suspend fun editTask(oldTask: Task, newTask: Task) {
        deleteTask(oldTask)

        insertTask(newTask)
    }

    suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task.taskInfo)

        CoroutineScope(Dispatchers.IO).launch {
            calendar.deleteTask(task)
        }
    }

    suspend fun addCompletion(taskCompletion: TaskCompletion) {
        val id = taskDao.insertTaskCompletion(
            taskCompletion
        )

        CoroutineScope(Dispatchers.IO).launch {
            val task = taskDao.getTask(taskCompletion.taskId)
            calendar.addCompletion(task, taskCompletion.copy(completionId = id))
        }
    }

    suspend fun removeCompletion(taskCompletion: TaskCompletion) {
        taskDao.deleteTaskCompletion(
            taskCompletion
        )

        CoroutineScope(Dispatchers.IO).launch {
            val task = taskDao.getTask(taskCompletion.taskId)
            calendar.removeCompletion(task, taskCompletion)
        }
    }

    fun getTasksBetween(from: Long, to: Long): Flow<List<TaskSingle>> =
        taskDao.getTasksBetween(from, to).organize(
            from = from,
            to = to,
        )

    fun getTasksFrom(from: Long): Flow<List<TaskSingle>> = taskDao.getAllTasks(from).organize(
        from = from, maxRepetition = 5
    )

    /**
     * Get all repetitions in a tasks given a single task
     *
     * @param from
     * @param to
     * @param maxRepetition
     * @param completed
     * @return the list of all tasks that can be generated from that 1 task
     */
    private fun TaskSingle.getAllTasks(
        from: Long, to: Long, maxRepetition: Long, completed: Map<Long, MutableMap<Long, Long>>
    ): List<TaskSingle> {
        if (repeatOften == null || repeatTag == null || startTime == null) {
            return listOf(
                copy(
                    currentEventTime = startTime ?: 0
                )
            )
        }

        val items = mutableListOf<TaskSingle>()
        val toTime = Instant.fromEpochMilliseconds(min(endTime ?: to, to))
        var curTime = Instant.fromEpochMilliseconds(startTime)

        var timesDuped = 0L

        val fromTime = Instant.fromEpochMilliseconds(from)
        while (curTime < fromTime) {
            curTime = repeatTag.incrementBy(curTime, repeatOften)
        }

        while (curTime <= toTime && timesDuped <= maxRepetition) {
            val isCompleted =
                curTime.toEpochMilliseconds() in (completed[taskId]?.keys ?: emptySet())
            items += copy(
                currentEventTime = curTime.toEpochMilliseconds(), completed = if (isCompleted) {
                    curTime.toEpochMilliseconds()
                } else {
                    ++timesDuped
                    null
                }, completionId = completed[taskId]?.get(curTime.toEpochMilliseconds()) ?: 0
            )

            curTime = repeatTag.incrementBy(curTime, repeatOften)
        }

        return items
    }

    /**
     * Organizes a list of tasks by grouping them by time and generating all its repeating parts
     *
     * @param from
     * @param to
     * @param maxRepetition
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun Flow<List<TaskSingle>>.organize(
        from: Long, to: Long = Long.MAX_VALUE, maxRepetition: Long = Long.MAX_VALUE
    ) = this.mapLatest { list ->
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
            task.getAllTasks(from, to, maxRepetition, completed)
        }.flatten().sortedBy { it.currentEventTime }
    }
}
