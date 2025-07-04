package ap.panini.procrastaint.data.repositories

import ap.panini.procrastaint.data.database.dao.TaskDao
import ap.panini.procrastaint.data.entities.Task
import ap.panini.procrastaint.data.entities.TaskCompletion
import ap.panini.procrastaint.data.entities.TaskSingle
import ap.panini.procrastaint.data.entities.TaskTag
import ap.panini.procrastaint.data.entities.TaskTagCrossRef
import ap.panini.procrastaint.notifications.NotificationManager
import ap.panini.procrastaint.util.Date
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.serialization.builtins.LongArraySerializer
import kotlin.math.min

class TaskRepository(
    private val taskDao: TaskDao,
    private val calendar: NetworkCalendarRepository,
    private val notificationManager: NotificationManager
) {

    suspend fun insertTask(tasks: Task): Boolean {
        val id = taskDao.insertTaskInfo(tasks.taskInfo)

        tasks.tags.forEach {
            val tagId = upsertTaskTag(it)
            upsertTaskTagCrossRef(TaskTagCrossRef(id, tagId))
        }

        tasks.meta.forEach {
            taskDao.insertTaskMeta(it.copy(taskId = id))
        }

        CoroutineScope(Dispatchers.IO).launch {
            val updatedTask = getTask(id)
            notificationManager.create(updatedTask)
            calendar.createTask(updatedTask)
        }

        return true
    }

    suspend fun getTask(id: Long): Task = taskDao.getTask(id)
    suspend fun getTaskOrNull(id: Long): Task? = taskDao.getTaskOrNull(id)

    fun getTags(): Flow<List<TaskTag>> = taskDao.getTags()
    suspend fun getTagOrNull(title: String): TaskTag? = taskDao.getTagOrNull(title)
    suspend fun getTag(id: Long): TaskTag = taskDao.getTag(id)

    suspend fun upsertTaskTagCrossRef(taskTagCrossRef: TaskTagCrossRef) =
        taskDao.upsertTagCrossRef(taskTagCrossRef)

    suspend fun upsertTaskTag(tag: TaskTag): Long {
        // check if valid color
        val tag = if (tag.toRgbOrNull() == null) {
            tag.copy(color = TaskTag.generateRandomColor())
        } else {
            tag
        }

        return taskDao.upsertTag(tag)
    }

    suspend fun editTask(newTask: Task) {
        deleteTask(newTask)

        insertTask(newTask)
    }

    suspend fun editTask(oldTask: Task, newTask: Task) {
        deleteTask(oldTask)

        insertTask(newTask)
    }

    suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task.taskInfo)
        taskDao.deleteTagsCrossRef(task.taskInfo.taskId) // deletes tags

        CoroutineScope(Dispatchers.IO).launch {
            notificationManager.delete(task)
            calendar.deleteTask(task)
        }
    }

    suspend fun getTagStartingWith(title: String) = taskDao.getTagsStarting(title)

    suspend fun deleteTask(taskId: Long) {
        deleteTask(getTask(taskId))
    }

    suspend fun addCompletion(taskCompletion: TaskCompletion) {
        val id = taskDao.insertTaskCompletion(
            taskCompletion
        )

        CoroutineScope(Dispatchers.IO).launch {
            val task = getTask(taskCompletion.taskId)
            getTasks(
                taskCompletion.forTime,
                taskCompletion.forTime,
                taskCompletion.taskId
            ).firstOrNull()?.firstOrNull()?.let {
                notificationManager.delete(it)
            }
            calendar.addCompletion(task, taskCompletion.copy(completionId = id))
        }
    }

    suspend fun removeCompletion(taskCompletion: TaskCompletion) {
        taskDao.deleteTaskCompletion(
            taskCompletion
        )

        CoroutineScope(Dispatchers.IO).launch {
            val task = getTask(taskCompletion.taskId)
            getTasks(
                taskCompletion.forTime,
                taskCompletion.forTime,
                taskCompletion.taskId
            ).firstOrNull()?.firstOrNull()?.let {
                notificationManager.create(it)
            }
            calendar.removeCompletion(task, taskCompletion)
        }
    }

    fun getTasks(
        from: Long = Date.getTodayStart(),
        to: Long? = null,
        taskId: Long? = null,
        tagId: Long? = null,
        maxRepetition: Long = Long.MAX_VALUE,
        includeNoTimeTasks: Boolean = false
    ): Flow<List<TaskSingle>> =
        taskDao.getTasks(from, to, taskId, tagId, includeNoTimeTasks).organize(
            from = from,
            to = to ?: Long.MAX_VALUE,
            maxRepetition = maxRepetition
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
        from: Long,
        to: Long,
        maxRepetition: Long,
        completed: Map<Long, MutableMap<Long, Long>>
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
                currentEventTime = curTime.toEpochMilliseconds(),
                completed = if (isCompleted) {
                    curTime.toEpochMilliseconds()
                } else {
                    ++timesDuped
                    null
                },
                completionId = completed[taskId]?.get(curTime.toEpochMilliseconds()) ?: 0
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
    private fun Flow<List<TaskSingle>>.organize(
        from: Long,
        to: Long = Long.MAX_VALUE,
        maxRepetition: Long = Long.MAX_VALUE
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
