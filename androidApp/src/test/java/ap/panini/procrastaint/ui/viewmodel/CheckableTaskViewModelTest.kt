package ap.panini.procrastaint.ui.viewmodel

import ap.panini.procrastaint.data.database.dao.TaskDao
import ap.panini.procrastaint.data.entities.Task
import ap.panini.procrastaint.data.entities.TaskCompletion
import ap.panini.procrastaint.data.entities.TaskInfo
import ap.panini.procrastaint.data.entities.TaskMeta
import ap.panini.procrastaint.data.entities.TaskSingle
import ap.panini.procrastaint.data.entities.TaskTag
import ap.panini.procrastaint.data.entities.TaskTagCrossRef
import ap.panini.procrastaint.data.repositories.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CheckableTaskViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val addedCompletions = mutableListOf<TaskCompletion>()
    private val removedCompletions = mutableListOf<TaskCompletion>()

    private val fakeRepository = object : TaskRepository(
        taskDao = FakeDaoStub(),
        calendar = null,
        notificationManager = null
    ) {
        override suspend fun addCompletion(taskCompletion: TaskCompletion) {
            addedCompletions.add(taskCompletion)
        }

        override suspend fun removeCompletion(taskCompletion: TaskCompletion) {
            removedCompletions.add(taskCompletion)
        }
    }

    private class TestCheckableViewModel(repo: TaskRepository) : CheckableTaskViewModel(repo) {
        val recentlyCompleted = recentlyCompletedInternal
    }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        addedCompletions.clear()
        removedCompletions.clear()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testQuickUncheckCancelsPendingCompletionImmediately() = runTest(testDispatcher) {
        val viewModel = TestCheckableViewModel(fakeRepository)
        val task = TaskSingle(
            taskId = 10L,
            metaId = 20L,
            completionId = 0L,
            title = "Test Task",
            description = "",
            completed = null,
            startTime = 1_000_000L,
            endTime = null,
            repeatTag = null,
            repeatOften = null,
            currentEventTime = 1_000_000L
        )

        // 1. Check task
        viewModel.checkTask(task)
        testDispatcher.scheduler.advanceTimeBy(100L) // only 100ms passed, delay is 1500ms

        val taskIdPair = Pair(10L, 1_000_000L)
        assertTrue(viewModel.recentlyCompleted.value.contains(taskIdPair), "Should be optimistically completed")
        assertTrue(addedCompletions.isEmpty(), "DB completion should not be called yet")

        // 2. Quickly uncheck within delay window
        viewModel.checkTask(task)
        testDispatcher.scheduler.advanceTimeBy(100L)

        assertFalse(viewModel.recentlyCompleted.value.contains(taskIdPair), "Should immediately be un-completed")
        assertTrue(addedCompletions.isEmpty(), "DB completion should not be called")

        // 3. Advance past the original delay window (1500ms)
        testDispatcher.scheduler.advanceTimeBy(2000L)
        assertTrue(addedCompletions.isEmpty(), "Pending job was cancelled, so DB is never written")
    }

    @Test
    fun testCompletesAfterDelayIfNotUnchecked() = runTest(testDispatcher) {
        val viewModel = TestCheckableViewModel(fakeRepository)
        val task = TaskSingle(
            taskId = 10L,
            metaId = 20L,
            completionId = 0L,
            title = "Test Task",
            description = "",
            completed = null,
            startTime = 1_000_000L,
            endTime = null,
            repeatTag = null,
            repeatOften = null,
            currentEventTime = 1_000_000L
        )

        viewModel.checkTask(task)
        testDispatcher.scheduler.advanceTimeBy(500L)
        assertTrue(addedCompletions.isEmpty())

        testDispatcher.scheduler.advanceTimeBy(1100L) // total > 1500ms
        assertEquals(1, addedCompletions.size)
        assertEquals(10L, addedCompletions.first().taskId)
    }

    @Suppress("EmptyFunctionBlock")
    private class FakeDaoStub : TaskDao {
        override suspend fun insertTag(taskTag: TaskTag): Long = 1L
        override suspend fun updateTag(taskTag: TaskTag) {}
        override suspend fun updateTaskInfo(taskInfo: TaskInfo) {}
        override suspend fun insertTagCrossRef(taskTagCrossRef: TaskTagCrossRef): Long = 0L
        override suspend fun insertTaskInfo(taskInfo: TaskInfo): Long = 1L
        override suspend fun insertTaskMeta(taskMeta: TaskMeta): Long = 1L
        override suspend fun updateTaskMeta(taskMeta: TaskMeta) {}
        override suspend fun insertTaskCompletion(taskCompletion: TaskCompletion): Long = 1L
        override suspend fun deleteTaskCompletion(taskCompletion: TaskCompletion) {}
        override suspend fun deleteTask(task: TaskInfo) {}
        override suspend fun deleteTag(tag: TaskTag) {}
        override suspend fun getTaskTagCrossRef(tagId: Long): List<TaskTagCrossRef> = emptyList()
        override suspend fun deleteTagsCrossRef(taskId: Long) {}
        override fun getTagCrossRefs(): Flow<List<TaskTagCrossRef>> = MutableStateFlow(emptyList())
        override fun getTags(): Flow<List<TaskTag>> = MutableStateFlow(emptyList())
        override suspend fun getTagsStarting(title: String): List<TaskTag> = emptyList()
        override suspend fun getTagOrNull(title: String): TaskTag? = null
        override suspend fun getTag(id: Long): TaskTag = error("stub")
        override suspend fun getTask(id: Long): Task = error("stub")
        override suspend fun getTaskOrNull(id: Long): Task? = null
        override suspend fun getCompletion(id: Long): TaskCompletion = error("stub")
        override fun getTasks(
            from: Long,
            to: Long?,
            taskId: Long?,
            tagId: Long?,
            includeNoTimeTasks: Boolean
        ): Flow<List<TaskSingle>> = MutableStateFlow(emptyList())
    }
}
