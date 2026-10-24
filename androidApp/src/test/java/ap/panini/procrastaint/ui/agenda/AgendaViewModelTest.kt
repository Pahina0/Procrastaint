package ap.panini.procrastaint.ui.agenda

import androidx.lifecycle.viewModelScope
import ap.panini.procrastaint.data.database.dao.TaskDao
import ap.panini.procrastaint.data.entities.Task
import ap.panini.procrastaint.data.entities.TaskCompletion
import ap.panini.procrastaint.data.entities.TaskInfo
import ap.panini.procrastaint.data.entities.TaskMeta
import ap.panini.procrastaint.data.entities.TaskSingle
import ap.panini.procrastaint.data.entities.TaskTag
import ap.panini.procrastaint.data.entities.TaskTagCrossRef
import ap.panini.procrastaint.data.repositories.PreferenceRepository
import ap.panini.procrastaint.data.repositories.TaskRepository
import ap.panini.procrastaint.di.createDataStore
import ap.panini.procrastaint.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okio.FileSystem
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalUuidApi::class)
class AgendaViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var fakeTaskRepository: FakeTaskRepository
    private lateinit var fakePreferenceRepository: PreferenceRepository
    private val createdViewModels = mutableListOf<AgendaViewModel>()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        fakeTaskRepository = FakeTaskRepository()
        val tempPath = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "test_${Uuid.random().toHexString()}.preferences_pb"
        val dataStore = createDataStore { tempPath.toString() }
        fakePreferenceRepository = PreferenceRepository(dataStore)
    }

    @AfterTest
    fun tearDown() {
        createdViewModels.forEach { it.viewModelScope.cancel() }
        createdViewModels.clear()
        Dispatchers.resetMain()
    }

    private fun createViewModel(): AgendaViewModel {
        val vm = AgendaViewModel(fakeTaskRepository, fakePreferenceRepository)
        createdViewModels.add(vm)
        return vm
    }

    @Test
    fun testAgendaViewModelCombinesOverdueAndScheduledTasks() = runTest {
        val todayStart = Date.getTodayStart()
        val overdueTask = createTask(id = 1L, title = "Overdue task", eventTime = todayStart - 86400000L)
        val scheduledTask = createTask(id = 2L, title = "Today task", eventTime = todayStart + 3600000L)

        fakeTaskRepository.overdueTasksFlow.value = listOf(overdueTask)
        fakeTaskRepository.scheduledTasksFlow.value = listOf(scheduledTask)

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertEquals(1, state.overdueTasks.size)
        assertEquals("Overdue task", state.overdueTasks.first().title)

        val todayScheduled = state.scheduledTasksByDate[todayStart]
        assertNotNull(todayScheduled)
        assertEquals(1, todayScheduled.size)
        assertEquals("Today task", todayScheduled.first().title)
    }

    @Test
    fun testTagFilteringFiltersScheduledTasks() = runTest {
        val todayStart = Date.getTodayStart()
        val tagWork = TaskTag(displayTitle = "Work", title = "Work", info = "", color = "#FF0000", tagId = 10L)
        val tagHome = TaskTag(displayTitle = "Home", title = "Home", info = "", color = "#00FF00", tagId = 20L)

        val task1 = createTask(id = 1L, title = "Work task", eventTime = todayStart + 3600000L, tags = listOf(tagWork))
        val task2 = createTask(id = 2L, title = "Home task", eventTime = todayStart + 7200000L, tags = listOf(tagHome))

        fakeTaskRepository.tagsFlow.value = listOf(tagWork, tagHome)
        fakeTaskRepository.scheduledTasksFlow.value = listOf(task1, task2)

        val viewModel = createViewModel()

        // Initial: both tasks visible
        assertEquals(2, viewModel.uiState.value.scheduledTasksByDate[todayStart]?.size)

        // Select Work tag
        viewModel.selectTag(tagWork.tagId)
        val workFiltered = viewModel.uiState.value.scheduledTasksByDate[todayStart]
        assertEquals(1, workFiltered?.size)
        assertEquals("Work task", workFiltered?.first()?.title)
        assertEquals(tagWork.tagId, viewModel.uiState.value.selectedTagId)

        // Toggling the same tag deselects it
        viewModel.selectTag(tagWork.tagId)
        assertNull(viewModel.uiState.value.selectedTagId)
        assertEquals(2, viewModel.uiState.value.scheduledTasksByDate[todayStart]?.size)
    }

    @Test
    fun testMoveAllOverdueToTodayInvokesRepository() = runTest {
        val todayStart = Date.getTodayStart()
        val overdue1 = createTask(id = 1L, title = "Overdue 1", eventTime = todayStart - 86400000L)
        val overdue2 = createTask(id = 2L, title = "Overdue 2", eventTime = todayStart - 172800000L)

        fakeTaskRepository.overdueTasksFlow.value = listOf(overdue1, overdue2)

        val viewModel = createViewModel()
        assertEquals(2, viewModel.uiState.value.overdueTasks.size)

        viewModel.moveAllOverdueToToday()

        assertEquals(2, fakeTaskRepository.movedTasks?.size)
        assertEquals(todayStart, fakeTaskRepository.movedTargetDate)
    }

    @Test
    fun testTogglingMonthExpansionUpdatesState() = runTest {
        val viewModel = createViewModel()

        assertFalse(viewModel.uiState.value.isMonthExpanded)

        viewModel.toggleMonthExpansion()
        assertTrue(viewModel.uiState.value.isMonthExpanded)

        viewModel.toggleMonthExpansion()
        assertFalse(viewModel.uiState.value.isMonthExpanded)
    }

    @Test
    fun testSetFocusedDateUpdatesState() = runTest {
        val viewModel = createViewModel()
        val targetDate = 1_800_000_000_000L

        viewModel.setFocusedDate(targetDate)
        assertEquals(targetDate, viewModel.uiState.value.focusedDate)
    }

    @Test
    fun testSearchQueryFiltersScheduledTasks() = runTest {
        val todayStart = Date.getTodayStart()
        val task1 = createTask(id = 1L, title = "Buy groceries", eventTime = todayStart + 3600000L)
        val task2 = createTask(id = 2L, title = "Call mom", eventTime = todayStart + 7200000L)

        fakeTaskRepository.scheduledTasksFlow.value = listOf(task1, task2)

        val viewModel = createViewModel()
        assertEquals(2, viewModel.uiState.value.scheduledTasksByDate[todayStart]?.size)

        viewModel.setSearchQuery("groceries")
        val filtered = viewModel.uiState.value.scheduledTasksByDate[todayStart]
        assertEquals(1, filtered?.size)
        assertEquals("Buy groceries", filtered?.first()?.title)

        viewModel.setSearchQuery("")
        assertEquals(2, viewModel.uiState.value.scheduledTasksByDate[todayStart]?.size)
    }

    private fun createTask(
        id: Long,
        title: String,
        eventTime: Long,
        tags: List<TaskTag> = emptyList()
    ): TaskSingle = TaskSingle(
        taskId = id,
        metaId = id,
        completionId = 0L,
        title = title,
        description = "",
        completed = null,
        startTime = eventTime,
        endTime = eventTime + 3600000L,
        repeatTag = null,
        repeatOften = null,
        currentEventTime = eventTime,
        tags = tags
    )

    private class FakeTaskRepository : TaskRepository(FakeTaskDaoStub()) {
        val overdueTasksFlow = MutableStateFlow<List<TaskSingle>>(emptyList())
        val scheduledTasksFlow = MutableStateFlow<List<TaskSingle>>(emptyList())
        val tagsFlow = MutableStateFlow<List<TaskTag>>(emptyList())

        var movedTasks: List<TaskSingle>? = null
        var movedTargetDate: Long? = null

        override fun getOverdueTasks(before: Long): Flow<List<TaskSingle>> = overdueTasksFlow

        override fun getTasks(
            from: Long,
            to: Long?,
            taskId: Long?,
            tagId: Long?,
            maxRepetition: Long,
            includeNoTimeTasks: Boolean
        ): Flow<List<TaskSingle>> = scheduledTasksFlow

        override fun getTags(): Flow<List<TaskTag>> = tagsFlow

        override suspend fun moveOverdueTasksToDate(tasks: List<TaskSingle>, targetDate: Long) {
            movedTasks = tasks
            movedTargetDate = targetDate
        }
    }

    @Suppress("EmptyFunctionBlock")
    private class FakeTaskDaoStub : TaskDao {
        override suspend fun insertTag(taskTag: TaskTag): Long = 0L
        override suspend fun updateTag(taskTag: TaskTag) {}
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
