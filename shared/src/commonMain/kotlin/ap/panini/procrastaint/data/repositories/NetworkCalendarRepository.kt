package ap.panini.procrastaint.data.repositories

import ap.panini.procrastaint.data.database.dao.NetworkSyncDao
import ap.panini.procrastaint.data.database.dao.TaskDao
import ap.panini.procrastaint.data.entities.NetworkSyncItem
import ap.panini.procrastaint.data.entities.Task
import ap.panini.procrastaint.data.entities.TaskCompletion
import ap.panini.procrastaint.data.entities.TaskInfo
import ap.panini.procrastaint.data.repositories.calendars.CalendarRepository
import ap.panini.procrastaint.data.repositories.calendars.GoogleCalendarRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Calendar repository used to access network api's of calendars
 * also used for syncing
 *
 * @constructor Create empty Calendar repository
 */
class NetworkCalendarRepository(
    private val nsDao: NetworkSyncDao,
    private val taskDao: TaskDao,
    private val googleCalendarRepository: GoogleCalendarRepository
) {
    fun getNetworkSyncItems(): Flow<List<NetworkSyncItem>> = nsDao.getSyncItems()

    suspend fun trySync(): CalendarRepository.Response {
        val items = getNetworkSyncItems().first()

        for (item in items) {
            val response = executeNetworkSyncItem(item)

            if (response is CalendarRepository.Response.Error) return response
        }

        return CalendarRepository.Response.Success
    }

    private suspend fun executeNetworkSyncItem(item: NetworkSyncItem): CalendarRepository.Response {
        val repo: CalendarRepository = when (item.location) {
            NetworkSyncItem.SyncData.GOOGLE -> googleCalendarRepository
        }

        if (!repo.isLoggedIn().first()) {
            return CalendarRepository.Response.Error(Throwable("Not logged in"))
        }

        val response = when (item.action) {
            NetworkSyncItem.SyncAction.CREATE_CALENDAR -> repo.createCalendar()
            NetworkSyncItem.SyncAction.CREATE_TASK -> handleCreateTask(repo, item)
            NetworkSyncItem.SyncAction.DELETE_TASK -> handleDeleteTask(repo, item)
            NetworkSyncItem.SyncAction.CHECK -> handleCheckTask(repo, item)
            NetworkSyncItem.SyncAction.UNCHECK -> handleUncheckTask(repo, item)
            NetworkSyncItem.SyncAction.UPDATE_TASK -> handleUpdateTask(repo, item)
        }

        if (response is CalendarRepository.Response.Success) {
            deleteSyncItem(item)
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                nsDao.updateSyncItem(item.copy(failCount = item.failCount + 1))
            }
        }

        return response
    }

    private suspend fun handleCreateTask(
        repo: CalendarRepository,
        item: NetworkSyncItem
    ): CalendarRepository.Response {
        val task = taskDao.getTask(item.taskId ?: return CalendarRepository.Response.Success)
        return repo.createTask(task)
    }

    private suspend fun handleDeleteTask(
        repo: CalendarRepository,
        item: NetworkSyncItem
    ): CalendarRepository.Response {
        val task = Task(
            TaskInfo(
                taskId = item.taskId ?: return CalendarRepository.Response.Success,
                title = ""
            ),
            meta = listOf()
        )
        return repo.deleteTask(task)
    }

    private suspend fun handleCheckTask(
        repo: CalendarRepository,
        item: NetworkSyncItem
    ): CalendarRepository.Response {
        val task = taskDao.getTask(item.taskId ?: return CalendarRepository.Response.Success)
        val completion =
            TaskCompletion(0, item.time, item.taskId, item.metaId!!, item.completionId!!)
        return repo.addCompletion(task, completion)
    }

    private suspend fun handleUncheckTask(
        repo: CalendarRepository,
        item: NetworkSyncItem
    ): CalendarRepository.Response {
        val task = taskDao.getTask(item.taskId ?: return CalendarRepository.Response.Success)
        val completion = TaskCompletion(0, item.time, item.taskId, item.metaId!!)
        return repo.removeCompletion(task, completion)
    }

    private suspend fun handleUpdateTask(
        repo: CalendarRepository,
        item: NetworkSyncItem
    ): CalendarRepository.Response {
        val task = taskDao.getTask(item.taskId ?: return CalendarRepository.Response.Success)
        return repo.updateTask(task)
    }

    fun deleteSyncItem(item: NetworkSyncItem) {
        CoroutineScope(Dispatchers.IO).launch {
            nsDao.deleteSyncItem(item)
        }
    }

    private val calendars = mapOf(
        googleCalendarRepository to NetworkSyncItem.SyncData.GOOGLE,
        // can have future calendars here
    )

    @OptIn(ExperimentalTime::class)
    suspend fun createTask(task: Task) {
        val now = Clock.System.now().toEpochMilliseconds()

        calendars.forEach { (calendar, loc) ->
            val response = if (!calendar.isLoggedIn().first()) {
                CalendarRepository.Response.Error(
                    Throwable("Not logged in")
                )
            } else {
                calendar.createTask(task)
            }

            if (response is CalendarRepository.Response.Error) {
                CoroutineScope(Dispatchers.IO).launch {
                    nsDao.insertNetworkSyncItem(
                        NetworkSyncItem(
                            time = now,
                            location = loc,
                            action = NetworkSyncItem.SyncAction.CREATE_TASK,
                            taskId = task.taskInfo.taskId
                        )
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    suspend fun deleteTask(task: Task) {
        val now = Clock.System.now().toEpochMilliseconds()
        nsDao.deleteTask(task.taskInfo.taskId)

        calendars.forEach { (calendar, loc) ->
            val response = if (!calendar.isLoggedIn().first()) {
                CalendarRepository.Response.Error(
                    Throwable("Not logged in")
                )
            } else {
                calendar.deleteTask(task)
            }

            if (response is CalendarRepository.Response.Error) {
                CoroutineScope(Dispatchers.IO).launch {
                    nsDao.insertNetworkSyncItem(
                        NetworkSyncItem(
                            time = now,
                            location = loc,
                            action = NetworkSyncItem.SyncAction.DELETE_TASK,
                            taskId = task.taskInfo.taskId
                        )
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    suspend fun addCompletion(task: Task, completion: TaskCompletion) {
        val now = Clock.System.now().toEpochMilliseconds()
        nsDao.deleteUnchecked(task.taskInfo.taskId, completion.metaId, now)

        calendars.forEach { (calendar, loc) ->
            val response = if (!calendar.isLoggedIn().first()) {
                CalendarRepository.Response.Error(
                    Throwable("Not logged in")
                )
            } else {
                calendar.addCompletion(task, completion)
            }

            if (response is CalendarRepository.Response.Error) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        nsDao.insertNetworkSyncItem(
                            NetworkSyncItem(
                                time = now,
                                location = loc,
                                action = NetworkSyncItem.SyncAction.CHECK,
                                taskId = task.taskInfo.taskId,
                                metaId = completion.metaId,
                                completionId = completion.completionId
                            )
                        )
                    } catch (_: Exception) {
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    suspend fun removeCompletion(task: Task, completion: TaskCompletion) {
        val now = Clock.System.now().toEpochMilliseconds()
        nsDao.deleteChecked(task.taskInfo.taskId, completion.metaId, now)

        calendars.forEach { (calendar, loc) ->
            val response = if (!calendar.isLoggedIn().first()) {
                CalendarRepository.Response.Error(
                    Throwable("Not logged in")
                )
            } else {
                calendar.removeCompletion(task, completion)
            }

            if (response is CalendarRepository.Response.Error) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        nsDao.insertNetworkSyncItem(
                            NetworkSyncItem(
                                time = now,
                                location = loc,
                                action = NetworkSyncItem.SyncAction.UNCHECK,
                                taskId = task.taskInfo.taskId,
                                metaId = completion.metaId,
                            )
                        )
                    } catch (_: Exception) {
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    suspend fun updateTask(task: Task) {
        val now = Clock.System.now().toEpochMilliseconds()

        calendars.forEach { (calendar, loc) ->
            val response = if (!calendar.isLoggedIn().first()) {
                CalendarRepository.Response.Error(
                    Throwable("Not logged in")
                )
            } else {
                calendar.updateTask(task)
            }

            if (response is CalendarRepository.Response.Error) {
                CoroutineScope(Dispatchers.IO).launch {
                    nsDao.insertNetworkSyncItem(
                        NetworkSyncItem(
                            time = now,
                            location = loc,
                            action = NetworkSyncItem.SyncAction.UPDATE_TASK,
                            taskId = task.taskInfo.taskId
                        )
                    )
                }
            }
        }
    }
}
