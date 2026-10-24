package ap.panini.procrastaint.data.repositories

import ap.panini.procrastaint.data.database.dao.TaskDao
import ap.panini.procrastaint.data.entities.Task
import ap.panini.procrastaint.data.entities.TaskCompletion
import ap.panini.procrastaint.data.entities.TaskInfo
import ap.panini.procrastaint.data.entities.TaskMeta
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import kotlin.math.min
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Suppress("TooManyFunctions")
open class TaskRepository(
    private val taskDao: TaskDao,
    private val calendar: NetworkCalendarRepository? = null,
    private val notificationManager: NotificationManager? = null,
    private val callbacks: List<RepositoryCallback> = emptyList()
) {

    open suspend fun insertTask(tasks: Task): Boolean {
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
            notificationManager?.create(updatedTask)
            calendar?.createTask(updatedTask)
            callbacks.forEach { it.onDataChanged() }
        }

        return true
    }

    suspend fun getTask(id: Long): Task = taskDao.getTask(id)
    suspend fun getTaskOrNull(id: Long): Task? = taskDao.getTaskOrNull(id)

    fun getTags(): Flow<List<TaskTag>> = taskDao.getTags()
    suspend fun getTagOrNull(title: String): TaskTag? = taskDao.getTagOrNull(title)
    suspend fun getTag(id: Long): TaskTag = taskDao.getTag(id)

    suspend fun upsertTaskTagCrossRef(taskTagCrossRef: TaskTagCrossRef) =
        taskDao.insertTagCrossRef(taskTagCrossRef)

    suspend fun upsertTaskTag(tag: TaskTag): Long {
        // check if valid color
        val tag = if (tag.toRgbOrNull() == null) {
            tag.copy(color = TaskTag.generateRandomColor())
        } else {
            tag
        }

        if (tag.tagId != 0L) {
            taskDao.updateTag(tag)
            return tag.tagId
        }

        return taskDao.insertTag(tag)
    }

    suspend fun deleteTag(tag: TaskTag) {
        taskDao.deleteTag(tag)
        callbacks.forEach { it.onDataChanged() }
    }

    open suspend fun deleteTagWithTasks(tag: TaskTag) {
        val tasks = taskDao.getTaskTagCrossRef(tag.tagId)
        deleteTag(tag)

        for (task in tasks) {
            deleteTask(task.taskId)
        }
        callbacks.forEach { it.onDataChanged() }
    }

    open suspend fun editTask(newTask: Task) {
        val oldTask = getTask(newTask.taskInfo.taskId)
        editTask(oldTask, newTask)
    }

    open suspend fun editTask(oldTask: Task, newTask: Task) {
        if (oldTask.meta.size != newTask.meta.size || oldTask.meta.zip(newTask.meta).any { (o, n) ->
                o.repeatOften != n.repeatOften || o.repeatTag != n.repeatTag || o.startTime != n.startTime
            }
        ) {
            deleteTask(oldTask)
            insertTask(newTask)
            callbacks.forEach { it.onDataChanged() }
            return
        }

        if (oldTask.taskInfo != newTask.taskInfo) {
            taskDao.updateTaskInfo(newTask.taskInfo)
        }

        oldTask.meta.zip(newTask.meta) { old, new ->
            if (old != new) {
                taskDao.updateTaskMeta(new)
            }
        }

        calendar?.updateTask(newTask)

        callbacks.forEach { it.onDataChanged() }
    }

    /**
     * Complete forever
     * must be a repeating task
     * @param task
     */
    open suspend fun removeFutureRepeats(task: Task, from: Long = Date.getTime()) {
        editTask(task.copy(meta = task.meta.map { it.copy(endTime = from) }))
    }

    open suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task.taskInfo)
        taskDao.deleteTagsCrossRef(task.taskInfo.taskId) // deletes tags

        CoroutineScope(Dispatchers.IO).launch {
            notificationManager?.delete(task)
            calendar?.deleteTask(task)
            callbacks.forEach { it.onDataChanged() }
        }
    }

    suspend fun getTagStartingWith(title: String) = taskDao.getTagsStarting(title)

    open suspend fun deleteTask(taskId: Long) {
        deleteTask(getTask(taskId))
        callbacks.forEach { it.onDataChanged() }
    }

    open suspend fun completeTask(task: TaskSingle, completionTime: Long = Date.getTime()) {
        addCompletion(
            TaskCompletion(
                completionTime = completionTime,
                forTime = task.currentEventTime,
                taskId = task.taskId,
                metaId = task.metaId
            )
        )
    }

    open suspend fun addCompletion(taskCompletion: TaskCompletion) {
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
                notificationManager?.delete(it)
            }
            calendar?.addCompletion(task, taskCompletion.copy(completionId = id))
            callbacks.forEach { it.onDataChanged() }
        }
    }

    open suspend fun removeCompletion(taskCompletion: TaskCompletion) {
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
                notificationManager?.create(it)
            }
            calendar?.removeCompletion(task, taskCompletion)
            callbacks.forEach { it.onDataChanged() }
        }
    }

    fun getTaskTagsMap(): Flow<Map<Long, List<TaskTag>>> =
        combine(taskDao.getTags(), taskDao.getTagCrossRefs()) { tags, refs ->
            val tagById = tags.associateBy { it.tagId }
            refs.groupBy({ it.taskId }, { tagById[it.tagId] })
                .mapValues { (_, tagList) -> tagList.filterNotNull() }
        }

    fun getTasks(
        from: Long = Date.getTodayStart(),
        to: Long? = null,
        taskId: Long? = null,
        tagId: Long? = null,
        maxRepetition: Long = Long.MAX_VALUE,
        includeNoTimeTasks: Boolean = false
    ): Flow<List<TaskSingle>> =
        combine(
            taskDao.getTasks(from, to, taskId, tagId, includeNoTimeTasks),
            getTaskTagsMap()
        ) { rawTasks, tagMap ->
            Pair(rawTasks, tagMap)
        }.organize(
            from = from,
            to = to ?: Long.MAX_VALUE,
            maxRepetition = maxRepetition
        )

    open fun getOverdueTasks(before: Long): Flow<List<TaskSingle>> =
        getTasks(from = 0, to = before).map { tasks ->
            tasks.filter { it.completed == null && it.currentEventTime < before }
        }

    private suspend fun rescheduleNonRecurringTask(taskSingle: TaskSingle, targetDate: Long) {
        val originalTask = getTaskOrNull(taskSingle.taskId) ?: return
        val updatedMeta = originalTask.meta.map { meta ->
            if (meta.metaId == taskSingle.metaId) {
                meta.copy(startTime = targetDate)
            } else {
                meta
            }
        }
        updatedMeta.filter { it.metaId == taskSingle.metaId }.forEach { meta ->
            taskDao.updateTaskMeta(meta)
        }
        val updatedTask = originalTask.copy(meta = updatedMeta)
        calendar?.updateTask(updatedTask)
        callbacks.forEach { it.onDataChanged() }
    }

    private suspend fun rescheduleRecurringTask(taskSingle: TaskSingle, targetDate: Long) {
        completeTask(taskSingle)
        val originalTask = getTaskOrNull(taskSingle.taskId)
        val oneOffTask = Task(
            taskInfo = TaskInfo(
                title = originalTask?.taskInfo?.title ?: taskSingle.title,
                description = originalTask?.taskInfo?.description ?: taskSingle.description,
                extractedTimePhrase = originalTask?.taskInfo?.extractedTimePhrase ?: "",
                taskId = 0
            ),
            meta = listOf(
                TaskMeta(
                    startTime = targetDate,
                    endTime = null,
                    repeatTag = null,
                    repeatOften = null,
                    taskId = 0,
                    metaId = 0
                )
            ),
            completions = emptyList(),
            tags = originalTask?.tags ?: taskSingle.tags
        )
        insertTask(oneOffTask)
    }

    open suspend fun moveOverdueTasksToDate(tasks: List<TaskSingle>, targetDate: Long) {
        for (taskSingle in tasks) {
            val isRecurring = taskSingle.repeatOften != null && taskSingle.repeatTag != null
            if (isRecurring) {
                rescheduleRecurringTask(taskSingle, targetDate)
            } else {
                rescheduleNonRecurringTask(taskSingle, targetDate)
            }
        }
    }

    /**
     * Get all repetitions in a tasks given a single task
     *
     * @param from
     * @param to
     * @param maxRepetition
     * @param completed
     * @return the list of all tasks that can be generated from that 1 task
     */
    @OptIn(ExperimentalTime::class)
    private fun TaskSingle.getAllTasks(
        from: Long,
        to: Long,
        maxRepetition: Long,
        completed: Map<Long, MutableMap<Long, Long>>
    ): List<TaskSingle> {
        val effectiveStartTime = startTime ?: Date.getTodayStart()
        if (repeatOften == null || repeatTag == null) {
            return listOf(
                copy(
                    currentEventTime = startTime ?: 0
                )
            )
        }

        val items = mutableListOf<TaskSingle>()
        val toTime = Instant.fromEpochMilliseconds(min(endTime ?: to, to))
        var curTime = Instant.fromEpochMilliseconds(effectiveStartTime)

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
    private fun Flow<Pair<List<TaskSingle>, Map<Long, List<TaskTag>>>>.organize(
        from: Long,
        to: Long = Long.MAX_VALUE,
        maxRepetition: Long = Long.MAX_VALUE
    ) = this.mapLatest { (list, tagMap) ->
        val filteredList = mutableListOf<TaskSingle>()

        // task id to a list of all the completed times to the completed id
        val completed = mutableMapOf<Long, MutableMap<Long, Long>>()
        for (task in list) {
            // saves all the completed into a map
            if (task.completed != null) {
                completed.getOrPut(task.taskId) { mutableMapOf() }[task.currentEventTime] =
                    task.completionId
            }

            // saves all the unique tasks with their preloaded tags
            if (filteredList.isEmpty() || task.metaId != filteredList.last().metaId) {
                filteredList += task.copy(tags = tagMap[task.taskId] ?: emptyList())
            }
        }

        filteredList.map { task ->
            task.getAllTasks(from, to, maxRepetition, completed)
        }.flatten().sortedBy { it.currentEventTime }
    }
}
