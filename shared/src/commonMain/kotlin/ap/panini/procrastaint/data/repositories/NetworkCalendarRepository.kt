package ap.panini.procrastaint.data.repositories

import ap.panini.procrastaint.data.database.dao.NetworkSyncDao
import ap.panini.procrastaint.data.database.dao.TaskDao
import ap.panini.procrastaint.data.entities.NetworkSyncItem
import ap.panini.procrastaint.data.entities.Task
import ap.panini.procrastaint.data.entities.TaskCompletion
import ap.panini.procrastaint.data.repositories.calendars.CalendarRepository
import ap.panini.procrastaint.data.repositories.calendars.GoogleCalendarRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
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

    suspend fun executeNetworkSyncItem(item: NetworkSyncItem) {
        val repo: CalendarRepository = when (item.location) {
            NetworkSyncItem.SyncData.GOOGLE -> googleCalendarRepository
        }

        fun onSuccess() {
            CoroutineScope(Dispatchers.IO).launch {
                nsDao.deleteSyncItem(item)
            }
        }

        // if a id doesn't exist anymore can prob skip it, like trying to
        when (item.action) {
            NetworkSyncItem.SyncAction.CREATE_CALENDAR -> repo.createCalendar(
                onSuccess = { onSuccess() }
            )

            NetworkSyncItem.SyncAction.CREATE_EVENT -> repo.createEvent(
                taskDao.getTask(
                    item.taskId ?: return
                ),
                onSuccess = { onSuccess() }
            )

            NetworkSyncItem.SyncAction.CHECK ->
                repo.addCompletion(
                    taskDao.getTask(item.taskId ?: return),
                    TaskCompletion(
                        0,
                        item.time,
                        item.taskId,
                        item.metaId!!,
                        item.completionId!!
                    ),
                    onSuccess = { onSuccess() }
                )

            NetworkSyncItem.SyncAction.UNCHECK ->
                repo.removeCompletion(
                    taskDao.getTask(item.taskId ?: return),
                    TaskCompletion(
                        0,
                        item.time,
                        item.taskId,
                        item.metaId!!,
                        item.completionId!!
                    ),
                    onSuccess = { onSuccess() }
                )
        }
    }

    private val calendars = mapOf(
        googleCalendarRepository to NetworkSyncItem.SyncData.GOOGLE,
        // can have future calendars here
    )

    /**
     * Google create calendar
     * creates a calendar for the application. if one already exists then it saves that one
     * this is called only once when logging into google
     */
    suspend fun googleCreateCalendar() {
        googleCalendarRepository.createCalendar {
            CoroutineScope(Dispatchers.IO).launch {
                nsDao.insertNetworkSyncItem(
                    NetworkSyncItem(
                        time = Clock.System.now().toEpochMilliseconds(),
                        location = NetworkSyncItem.SyncData.GOOGLE,
                        action = NetworkSyncItem.SyncAction.CREATE_CALENDAR
                    )
                )
            }
        }
    }

    suspend fun createEvent(task: Task) {
        calendars.forEach { (calendar, loc) ->
            calendar.createEvent(task) {
                CoroutineScope(Dispatchers.IO).launch {
                    nsDao.insertNetworkSyncItem(
                        NetworkSyncItem(
                            time = Clock.System.now().toEpochMilliseconds(),
                            location = loc,
                            action = NetworkSyncItem.SyncAction.CREATE_CALENDAR,
                            taskId = task.taskInfo.taskId
                        )
                    )
                }
            }
        }
    }

    suspend fun addCompletion(task: Task, completion: TaskCompletion) {
        calendars.forEach { (calendar, loc) ->
            calendar.addCompletion(task, completion) {
                CoroutineScope(Dispatchers.IO).launch {
                    nsDao.insertNetworkSyncItem(
                        NetworkSyncItem(
                            time = Clock.System.now().toEpochMilliseconds(),
                            location = loc,
                            action = NetworkSyncItem.SyncAction.CREATE_CALENDAR,
                            taskId = task.taskInfo.taskId,
                            metaId = completion.metaId,
                            completionId = completion.completionId
                        )
                    )
                }
            }
        }
    }

    suspend fun removeCompletion(task: Task, completion: TaskCompletion) {
        calendars.forEach { (calendar, loc) ->
            calendar.removeCompletion(task, completion) {
                CoroutineScope(Dispatchers.IO).launch {
                    nsDao.insertNetworkSyncItem(
                        NetworkSyncItem(
                            time = Clock.System.now().toEpochMilliseconds(),
                            location = loc,
                            action = NetworkSyncItem.SyncAction.CREATE_CALENDAR,
                            taskId = task.taskInfo.taskId,
                            metaId = completion.metaId,
                            completionId = completion.completionId
                        )
                    )
                }
            }
        }
    }
}
