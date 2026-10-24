package ap.panini.procrastaint.ui.widget

import androidx.lifecycle.viewModelScope
import ap.panini.procrastaint.data.database.dao.TaskDao
import ap.panini.procrastaint.data.entities.Task
import ap.panini.procrastaint.data.entities.TaskCompletion
import ap.panini.procrastaint.data.entities.TaskInfo
import ap.panini.procrastaint.data.entities.TaskMeta
import ap.panini.procrastaint.data.entities.TaskSingle
import ap.panini.procrastaint.data.entities.TaskTag
import ap.panini.procrastaint.data.entities.TaskTagCrossRef
import ap.panini.procrastaint.data.repositories.TaskRepository
import ap.panini.procrastaint.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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

private const val OneHourMillis = 3600000L
private const val OneDayMillis = 86400000L
private const val UndoDelayMillis = 1501L

@OptIn(ExperimentalCoroutinesApi::class)
class UpcomingWidgetViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var fakeTaskDao: FakeTaskDaoStub
    private lateinit var fakeTaskRepository: FakeTaskRepository
    private val createdViewModels = mutableListOf<UpcomingWidgetViewModel>()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeTaskDao = FakeTaskDaoStub()
        fakeTaskRepository = FakeTaskRepository(fakeTaskDao)
    }

    @AfterTest
    fun tearDown() {
        createdViewModels.forEach { it.viewModelScope.cancel() }
        createdViewModels.clear()
        Dispatchers.resetMain()
    }

    private fun createViewModel(): UpcomingWidgetViewModel {
        val vm = UpcomingWidgetViewModel(fakeTaskRepository)
        createdViewModels.add(vm)
        return vm
    }

    @Test
    fun testOverdueTasksEmitsIncompleteTasksDueBeforeToday() = runTest {
        val todayStart = Date.getTodayStart()
        val overdueTask = createTask(id = 1L, title = "Overdue task", eventTime = todayStart - OneDayMillis)

        fakeTaskRepository.overdueTasksFlow.value = listOf(overdueTask)

        val viewModel = createViewModel()
        backgroundScope.launch(testDispatcher) {
            viewModel.overdueTasks.collect()
        }

        assertEquals(1, viewModel.overdueTasks.value.size)
        assertEquals("Overdue task", viewModel.overdueTasks.value.first().title)
        assertEquals(todayStart, fakeTaskRepository.queriedOverdueBefore)
    }

    @Test
    fun testUpcomingTasksEmitsTasksFromTodayOnwards() = runTest {
        val todayStart = Date.getTodayStart()
        val upcomingTask = createTask(id = 2L, title = "Upcoming task", eventTime = todayStart + OneHourMillis)

        fakeTaskRepository.upcomingTasksFlow.value = listOf(upcomingTask)

        val viewModel = createViewModel()
        backgroundScope.launch(testDispatcher) {
            viewModel.upcomingTasks.collect()
        }

        assertEquals(1, viewModel.upcomingTasks.value.size)
        assertEquals("Upcoming task", viewModel.upcomingTasks.value.first().title)
        assertEquals(todayStart, fakeTaskRepository.queriedTasksFrom)
    }

    @Test
    fun testToggleTaskCompletionResolvesOverdueTasks() = runTest {
        val todayStart = Date.getTodayStart()
        val overdueTask = createTask(id = 10L, title = "Overdue 10", eventTime = todayStart - OneHourMillis)

        fakeTaskRepository.overdueTasksFlow.value = listOf(overdueTask)

        val viewModel = createViewModel()
        backgroundScope.launch(testDispatcher) {
            viewModel.overdueTasks.collect()
        }

        viewModel.toggleTaskCompletion(overdueTask.taskId, overdueTask.currentEventTime)

        val completedKey = Pair(overdueTask.taskId, overdueTask.currentEventTime)
        assertTrue(viewModel.recentlyCompleted.value.contains(completedKey))
        assertEquals(0, fakeTaskRepository.addedCompletions.size)

        advanceTimeBy(UndoDelayMillis)

        assertEquals(1, fakeTaskRepository.addedCompletions.size)
        assertEquals(overdueTask.taskId, fakeTaskRepository.addedCompletions.first().taskId)
        assertFalse(viewModel.recentlyCompleted.value.contains(completedKey))
    }

    @Test
    fun testToggleTaskCompletionResolvesUpcomingTasks() = runTest {
        val todayStart = Date.getTodayStart()
        val upcomingTask = createTask(id = 20L, title = "Upcoming 20", eventTime = todayStart + OneHourMillis)

        fakeTaskRepository.upcomingTasksFlow.value = listOf(upcomingTask)

        val viewModel = createViewModel()
        backgroundScope.launch(testDispatcher) {
            viewModel.upcomingTasks.collect()
        }

        viewModel.toggleTaskCompletion(upcomingTask.taskId, upcomingTask.currentEventTime)

        val completedKey = Pair(upcomingTask.taskId, upcomingTask.currentEventTime)
        assertTrue(viewModel.recentlyCompleted.value.contains(completedKey))
        assertEquals(0, fakeTaskRepository.addedCompletions.size)

        advanceTimeBy(UndoDelayMillis)

        assertEquals(1, fakeTaskRepository.addedCompletions.size)
        assertEquals(upcomingTask.taskId, fakeTaskRepository.addedCompletions.first().taskId)
        assertFalse(viewModel.recentlyCompleted.value.contains(completedKey))
    }

    private fun createTask(
        id: Long,
        title: String,
        eventTime: Long,
        completed: Long? = null
    ): TaskSingle = TaskSingle(
        taskId = id,
        metaId = id,
        completionId = 0L,
        title = title,
        description = "",
        completed = completed,
        startTime = eventTime,
        endTime = eventTime + OneHourMillis,
        repeatTag = null,
        repeatOften = null,
        currentEventTime = eventTime,
        tags = emptyList()
    )

    private class FakeTaskRepository(taskDao: TaskDao) : TaskRepository(taskDao) {
        val overdueTasksFlow = MutableStateFlow<List<TaskSingle>>(emptyList())
        val upcomingTasksFlow = MutableStateFlow<List<TaskSingle>>(emptyList())
        val addedCompletions = mutableListOf<TaskCompletion>()

        var queriedOverdueBefore: Long? = null
        var queriedTasksFrom: Long? = null

        override fun getOverdueTasks(before: Long): Flow<List<TaskSingle>> {
            queriedOverdueBefore = before
            return overdueTasksFlow
        }

        override fun getTasks(
            from: Long,
            to: Long?,
            taskId: Long?,
            tagId: Long?,
            maxRepetition: Long,
            includeNoTimeTasks: Boolean
        ): Flow<List<TaskSingle>> {
            queriedTasksFrom = from
            return upcomingTasksFlow
        }

        override suspend fun addCompletion(taskCompletion: TaskCompletion) {
            addedCompletions.add(taskCompletion)
        }
    }

    @Suppress("EmptyFunctionBlock")
    private class FakeTaskDaoStub : TaskDao {
        override suspend fun insertTag(taskTag: TaskTag): Long = 0L
        override suspend fun updateTag(taskTag: TaskTag) {}
        override suspend fun getTagOrNull(title: String): TaskTag? = null
        override suspend fun updateTaskInfo(taskInfo: TaskInfo) {}
        override suspend fun insertTagCrossRef(taskTagCrossRef: TaskTagCrossRef): Long = 0L
        override suspend fun insertTaskInfo(taskInfo: TaskInfo): Long = 0L
        override suspend fun insertTaskMeta(taskMeta: TaskMeta): Long = 0L
        override suspend fun updateTaskMeta(taskMeta: TaskMeta) {}
        override suspend fun insertTaskCompletion(taskCompletion: TaskCompletion): Long = 0L
        override suspend fun deleteTaskCompletion(taskCompletion: TaskCompletion) {}
        override suspend fun deleteTask(task: TaskInfo) {}
        override suspend fun deleteTag(tag: TaskTag) {}
        override suspend fun getTaskTagCrossRef(tagId: Long): List<TaskTagCrossRef> = emptyList()
        override suspend fun deleteTagsCrossRef(taskId: Long) {}
        override fun getTagCrossRefs(): Flow<List<TaskTagCrossRef>> = MutableStateFlow(emptyList())
        override fun getTags(): Flow<List<TaskTag>> = MutableStateFlow(emptyList())
        override suspend fun getTagsStarting(title: String): List<TaskTag> = emptyList()
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
