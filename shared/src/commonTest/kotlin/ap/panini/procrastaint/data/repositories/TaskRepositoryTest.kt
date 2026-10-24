package ap.panini.procrastaint.data.repositories

import ap.panini.procrastaint.data.database.dao.TaskDao
import ap.panini.procrastaint.data.entities.Task
import ap.panini.procrastaint.data.entities.TaskCompletion
import ap.panini.procrastaint.data.entities.TaskInfo
import ap.panini.procrastaint.data.entities.TaskMeta
import ap.panini.procrastaint.data.entities.TaskSingle
import ap.panini.procrastaint.data.entities.TaskTag
import ap.panini.procrastaint.data.entities.TaskTagCrossRef
import ap.panini.procrastaint.util.Time
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FakeTaskDao : TaskDao {
    private var nextTaskId = 1L
    private var nextMetaId = 1L
    private var nextCompletionId = 1L
    private var nextTagId = 1L

    private val taskInfos = mutableMapOf<Long, TaskInfo>()
    private val taskMetas = mutableMapOf<Long, TaskMeta>()
    private val taskCompletions = mutableMapOf<Long, TaskCompletion>()
    private val taskTags = mutableMapOf<Long, TaskTag>()
    private val tagCrossRefs = mutableListOf<TaskTagCrossRef>()

    private val changeTrigger = MutableStateFlow(0)

    private fun notifyChanged() {
        changeTrigger.value++
    }

    override suspend fun insertTag(taskTag: TaskTag): Long {
        val id = if (taskTag.tagId != 0L) taskTag.tagId else nextTagId++
        taskTags[id] = taskTag.copy(tagId = id)
        notifyChanged()
        return id
    }

    override suspend fun updateTag(taskTag: TaskTag) {
        taskTags[taskTag.tagId] = taskTag
        notifyChanged()
    }

    override suspend fun updateTaskInfo(taskInfo: TaskInfo) {
        taskInfos[taskInfo.taskId] = taskInfo
        notifyChanged()
    }

    override suspend fun insertTagCrossRef(taskTagCrossRef: TaskTagCrossRef): Long {
        tagCrossRefs.removeAll { it.taskId == taskTagCrossRef.taskId && it.tagId == taskTagCrossRef.tagId }
        tagCrossRefs.add(taskTagCrossRef)
        notifyChanged()
        return 1L
    }

    override suspend fun insertTaskInfo(taskInfo: TaskInfo): Long {
        val id = if (taskInfo.taskId != 0L) taskInfo.taskId else nextTaskId++
        taskInfos[id] = taskInfo.copy(taskId = id)
        notifyChanged()
        return id
    }

    override suspend fun insertTaskMeta(taskMeta: TaskMeta): Long {
        val id = if (taskMeta.metaId != 0L) taskMeta.metaId else nextMetaId++
        taskMetas[id] = taskMeta.copy(metaId = id)
        notifyChanged()
        return id
    }

    override suspend fun updateTaskMeta(taskMeta: TaskMeta) {
        taskMetas[taskMeta.metaId] = taskMeta
        notifyChanged()
    }

    override suspend fun insertTaskCompletion(taskCompletion: TaskCompletion): Long {
        val id = if (taskCompletion.completionId != 0L) taskCompletion.completionId else nextCompletionId++
        taskCompletions[id] = taskCompletion.copy(completionId = id)
        notifyChanged()
        return id
    }

    override suspend fun deleteTaskCompletion(taskCompletion: TaskCompletion) {
        taskCompletions.remove(taskCompletion.completionId)
        notifyChanged()
    }

    override suspend fun deleteTask(task: TaskInfo) {
        taskInfos.remove(task.taskId)
        taskMetas.entries.removeAll { it.value.taskId == task.taskId }
        taskCompletions.entries.removeAll { it.value.taskId == task.taskId }
        tagCrossRefs.removeAll { it.taskId == task.taskId }
        notifyChanged()
    }

    override suspend fun deleteTag(tag: TaskTag) {
        taskTags.remove(tag.tagId)
        tagCrossRefs.removeAll { it.tagId == tag.tagId }
        notifyChanged()
    }

    override suspend fun getTaskTagCrossRef(tagId: Long): List<TaskTagCrossRef> {
        return tagCrossRefs.filter { it.tagId == tagId }
    }

    override suspend fun deleteTagsCrossRef(taskId: Long) {
        tagCrossRefs.removeAll { it.taskId == taskId }
        notifyChanged()
    }

    override fun getTagCrossRefs(): Flow<List<TaskTagCrossRef>> = changeTrigger.map {
        tagCrossRefs.toList()
    }

    override fun getTags(): Flow<List<TaskTag>> = changeTrigger.map {
        taskTags.values.toList()
    }

    override suspend fun getTagsStarting(title: String): List<TaskTag> {
        return taskTags.values.filter { it.title.startsWith(title) }
    }

    override suspend fun getTagOrNull(title: String): TaskTag? {
        return taskTags.values.firstOrNull { it.title == title }
    }

    override suspend fun getTag(id: Long): TaskTag {
        return taskTags.getValue(id)
    }

    override suspend fun getTask(id: Long): Task {
        return getTaskOrNull(id) ?: throw NoSuchElementException("Task not found with id $id")
    }

    override suspend fun getTaskOrNull(id: Long): Task? {
        val info = taskInfos[id] ?: return null
        val metas = taskMetas.values.filter { it.taskId == id }
        val comps = taskCompletions.values.filter { it.taskId == id }
        val tagIds = tagCrossRefs.filter { it.taskId == id }.map { it.tagId }
        val tags = tagIds.mapNotNull { taskTags[it] }
        return Task(
            taskInfo = info,
            meta = metas,
            completions = comps,
            tags = tags
        )
    }

    override suspend fun getCompletion(id: Long): TaskCompletion {
        return taskCompletions.getValue(id)
    }

    private fun matchesTime(
        meta: TaskMeta,
        from: Long,
        to: Long?,
        includeNoTimeTasks: Boolean
    ): Boolean {
        val nonRecurringMatch = meta.startTime != null &&
            meta.startTime >= from &&
            (to == null || meta.startTime <= to)
        val recurringMatch = meta.repeatTag != null &&
            meta.repeatOften != null &&
            (meta.endTime == null || meta.endTime >= from) &&
            (to == null || meta.startTime == null || meta.startTime <= to)
        val noTimeMatch = includeNoTimeTasks && meta.startTime == null
        return nonRecurringMatch || recurringMatch || noTimeMatch
    }

    private fun matchesTag(taskId: Long, tagId: Long?): Boolean {
        if (tagId == null) return true
        return tagCrossRefs.any { it.taskId == taskId && it.tagId == tagId }
    }

    private fun buildTaskSingles(info: TaskInfo, meta: TaskMeta): List<TaskSingle> {
        val comps = taskCompletions.values.filter { it.taskId == info.taskId && it.metaId == meta.metaId }
        if (comps.isEmpty()) {
            return listOf(
                TaskSingle(
                    taskId = info.taskId,
                    metaId = meta.metaId,
                    completionId = 0L,
                    title = info.title,
                    description = info.description,
                    completed = null,
                    startTime = meta.startTime,
                    endTime = meta.endTime,
                    repeatTag = meta.repeatTag,
                    repeatOften = meta.repeatOften,
                    currentEventTime = -1L
                )
            )
        }
        return comps.map { c ->
            TaskSingle(
                taskId = info.taskId,
                metaId = meta.metaId,
                completionId = c.completionId,
                title = info.title,
                description = info.description,
                completed = c.completionTime,
                startTime = meta.startTime,
                endTime = meta.endTime,
                repeatTag = meta.repeatTag,
                repeatOften = meta.repeatOften,
                currentEventTime = c.forTime
            )
        }
    }

    override fun getTasks(
        from: Long,
        to: Long?,
        taskId: Long?,
        tagId: Long?,
        includeNoTimeTasks: Boolean
    ): Flow<List<TaskSingle>> = changeTrigger.map {
        taskInfos.values
            .filter { taskId == null || it.taskId == taskId }
            .flatMap { info ->
                taskMetas.values
                    .filter { it.taskId == info.taskId }
                    .filter { matchesTime(it, from, to, includeNoTimeTasks) }
                    .filter { matchesTag(info.taskId, tagId) }
                    .flatMap { meta -> buildTaskSingles(info, meta) }
            }
            .sortedBy { it.startTime }
    }
}

class TaskRepositoryTest {

    @Test
    fun testGetOverdueTasksReturnsIncompleteTasksBeforeTimestamp() = runBlocking {
        val fakeDao = FakeTaskDao()
        val repository = TaskRepository(fakeDao)

        val pastTimestamp = 1_000_000L
        val todayStart = 2_000_000L
        val futureTimestamp = 3_000_000L

        // 1. Past incomplete task
        repository.insertTask(
            Task(
                taskInfo = TaskInfo(title = "Past incomplete task"),
                meta = listOf(TaskMeta(startTime = pastTimestamp))
            )
        )

        // 2. Past completed task
        repository.insertTask(
            Task(
                taskInfo = TaskInfo(title = "Past completed task"),
                meta = listOf(TaskMeta(startTime = pastTimestamp))
            )
        )
        // Mark past completed task as complete
        val pastCompletedSingle = repository.getTasks(from = 0, to = todayStart).first()
            .first { it.title == "Past completed task" }
        repository.addCompletion(
            TaskCompletion(
                completionTime = pastTimestamp + 100,
                forTime = pastCompletedSingle.currentEventTime,
                taskId = pastCompletedSingle.taskId,
                metaId = pastCompletedSingle.metaId
            )
        )

        // 3. Task due at exactly todayStart (should not be overdue)
        repository.insertTask(
            Task(
                taskInfo = TaskInfo(title = "Today task"),
                meta = listOf(TaskMeta(startTime = todayStart))
            )
        )

        // 4. Future incomplete task
        repository.insertTask(
            Task(
                taskInfo = TaskInfo(title = "Future task"),
                meta = listOf(TaskMeta(startTime = futureTimestamp))
            )
        )

        val overdue = repository.getOverdueTasks(todayStart).first()
        assertEquals(1, overdue.size)
        assertEquals("Past incomplete task", overdue.first().title)
        assertEquals(pastTimestamp, overdue.first().currentEventTime)
        assertNull(overdue.first().completed)
    }

    @Test
    fun testMoveOverdueNonRecurringTaskToDate() = runBlocking {
        val fakeDao = FakeTaskDao()
        val repository = TaskRepository(fakeDao)

        val yesterday = 1_000_000L
        val todayStart = 2_000_000L

        repository.insertTask(
            Task(
                taskInfo = TaskInfo(title = "Overdue non-recurring task"),
                meta = listOf(TaskMeta(startTime = yesterday))
            )
        )

        val overdueTasks = repository.getOverdueTasks(todayStart).first()
        assertEquals(1, overdueTasks.size)

        repository.moveOverdueTasksToDate(overdueTasks, todayStart)

        // Verify no longer overdue
        val overdueAfter = repository.getOverdueTasks(todayStart).first()
        assertTrue(overdueAfter.isEmpty())

        // Verify task is now scheduled for todayStart
        val todayTasks = repository.getTasks(from = todayStart, to = todayStart + 1000).first()
        assertEquals(1, todayTasks.size)
        assertEquals("Overdue non-recurring task", todayTasks.first().title)
        assertEquals(todayStart, todayTasks.first().startTime)
        assertEquals(todayStart, todayTasks.first().currentEventTime)
    }

    @Test
    fun testMoveOverdueRecurringTaskToDate() = runBlocking {
        val fakeDao = FakeTaskDao()
        val repository = TaskRepository(fakeDao)

        val dayDuration = 24 * 60 * 60 * 1000L
        val yesterday = 1_000_000L
        val todayStart = yesterday + dayDuration

        // Daily recurring task starting yesterday
        repository.insertTask(
            Task(
                taskInfo = TaskInfo(title = "Daily habit"),
                meta = listOf(
                    TaskMeta(
                        startTime = yesterday,
                        repeatTag = Time.DAY,
                        repeatOften = 1
                    )
                )
            )
        )

        val overdueTasks = repository.getOverdueTasks(todayStart).first()
        assertEquals(1, overdueTasks.size)
        val overdueHabit = overdueTasks.first()
        assertEquals("Daily habit", overdueHabit.title)
        assertEquals(yesterday, overdueHabit.currentEventTime)

        // Move overdue recurring task to today
        repository.moveOverdueTasksToDate(overdueTasks, todayStart)

        // Verify no longer overdue
        val overdueAfter = repository.getOverdueTasks(todayStart).first()
        assertTrue(overdueAfter.isEmpty())

        // Verify the original recurring task has yesterday marked completed
        val yesterdayTasks = repository.getTasks(from = yesterday, to = yesterday).first()
        val yesterdayHabit = yesterdayTasks.firstOrNull { it.taskId == overdueHabit.taskId }
        assertNotNull(yesterdayHabit)
        assertNotNull(yesterdayHabit.completed)

        // Verify a one-off instance was inserted for today
        val todayTasks = repository.getTasks(from = todayStart, to = todayStart).first()
        val oneOffInstance = todayTasks.firstOrNull { it.taskId != overdueHabit.taskId }
        assertNotNull(oneOffInstance)
        assertEquals("Daily habit", oneOffInstance.title)
        assertEquals(todayStart, oneOffInstance.currentEventTime)
        assertNull(oneOffInstance.repeatTag) // One-off instance is not recurring

        // Verify future occurrences of the recurring task still exist (e.g. today or tomorrow)
        val todayRecurring = todayTasks.firstOrNull { it.taskId == overdueHabit.taskId }
        assertNotNull(todayRecurring)
        assertEquals(Time.DAY, todayRecurring.repeatTag)
        assertNull(todayRecurring.completed)
    }
}
