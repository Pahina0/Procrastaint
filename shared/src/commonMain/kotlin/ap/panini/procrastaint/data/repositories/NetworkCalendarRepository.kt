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
import kotlinx.datetime.Clock

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

        if (!repo.isLoggedIn()
                .first()
        ) {
            return CalendarRepository.Response.Error(Throwable("Not logged in"))
        }

        // if a id doesn't exist anymore can prob skip it, like trying to
        val response = when (item.action) {
            NetworkSyncItem.SyncAction.CREATE_CALENDAR -> repo.createCalendar()

            NetworkSyncItem.SyncAction.CREATE_TASK -> repo.createTask(
                taskDao.getTask(
                    item.taskId ?: return CalendarRepository.Response.Success
                ),
            )

            NetworkSyncItem.SyncAction.DELETE_TASK -> repo.deleteTask(
                Task(
                    TaskInfo(
                        taskId = item.taskId ?: return CalendarRepository.Response.Success,
                        title = ""
                    ),
                    meta = listOf()
                )
            )

            NetworkSyncItem.SyncAction.CHECK ->
                repo.addCompletion(
                    taskDao.getTask(item.taskId ?: return CalendarRepository.Response.Success),
                    TaskCompletion(
                        0,
                        item.time,
                        item.taskId,
                        item.metaId!!,
                        item.completionId!!
                    ),
                )

            NetworkSyncItem.SyncAction.UNCHECK ->
                repo.removeCompletion(
                    taskDao.getTask(item.taskId ?: return CalendarRepository.Response.Success),
                    TaskCompletion(
                        0,
                        item.time,
                        item.taskId,
                        item.metaId!!,
                    ),
                )
        }

        if (response is CalendarRepository.Response.Success) {
            CoroutineScope(Dispatchers.IO).launch {
                nsDao.deleteSyncItem(item)
            }
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                nsDao.updateSyncItem(item.copy(failCount = item.failCount + 1))
            }
        }

        return response
    }

    private val calendars = mapOf(
        googleCalendarRepository to NetworkSyncItem.SyncData.GOOGLE,
        // can have future calendars here
    )

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
                println("${task.taskInfo} $completion")
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
}
