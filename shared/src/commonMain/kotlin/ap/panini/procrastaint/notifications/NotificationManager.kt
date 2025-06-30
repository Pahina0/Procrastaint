package ap.panini.procrastaint.notifications

import ap.panini.procrastaint.data.entities.Task
import ap.panini.procrastaint.data.entities.TaskMeta
import ap.panini.procrastaint.data.entities.TaskSingle
import ap.panini.procrastaint.data.repositories.TaskRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime
import org.koin.mp.KoinPlatform.getKoin
import kotlin.time.Duration.Companion.hours

class NotificationManager {
    companion object {
        private const val HOURS_AHEAD = 32
    }

    var callback: ((CallbackBundle) -> Unit)? = null

    // prevent circular dependencies via lazy
    private val tasksRepo: TaskRepository by lazy { getKoin().get() }

    /**
     * Create a notification from a single task event
     *
     * @param task
     */
    internal fun create(task: TaskSingle) {
        val uuid = TaskMeta(
            task.startTime,
            task.endTime,
            task.repeatTag,
            task.repeatOften,
            task.taskId,
            task.metaId
        ).generateUuid(task.currentEventTime)
        callback?.invoke(
            CallbackBundle.Create(
                uuid,
                task.taskId,
                task.metaId,
                task.currentEventTime
            )
        )
    }

    internal fun delete(task: TaskSingle) {
        val uuid = TaskMeta(
            task.startTime,
            task.endTime,
            task.repeatTag,
            task.repeatOften,
            task.taskId,
            task.metaId
        ).generateUuid(task.currentEventTime)
        callback?.invoke(
            CallbackBundle.Delete(
                uuid,
                task.taskId,
                task.metaId,
                task.currentEventTime
            )
        )
    }

    /**
     * Create a notification for the next 24 hrs, calling callback
     *
     * @param task the task to get from
     */
    internal fun create(task: Task) {
        val now = Clock.System.now().toEpochMilliseconds()
        val tasks = runBlocking {
            tasksRepo.getTasksBetweenFiltered(
                now,
                now + HOURS_AHEAD.hours.inWholeMilliseconds,
                task.taskInfo.taskId
            ).first()
        }

        tasks.forEach {
            create(it)
        }
    }

    /**
     * Delete any upcoming notifications in the next 24 hrs
     *
     * @param task
     */
    internal fun delete(task: Task) {
        val now = Clock.System.now().toEpochMilliseconds()
        val tasks = runBlocking {
            tasksRepo.getTasksBetweenFiltered(
                now,
                now + HOURS_AHEAD.hours.inWholeMilliseconds,
                task.taskInfo.taskId
            ).first()
        }

        tasks.forEach {
            delete(it)
        }
    }

    private fun TaskMeta.generateUuid(eventTime: Long): Int {
        val time =
            buildString {
                append(
                    Instant.fromEpochMilliseconds(eventTime).toLocalDateTime(TimeZone.UTC)
                        .format(
                            LocalDateTime.Format {
                                dayOfMonth()
                                hour()
                                minute()
                                second()
                            }
                        )
                )
                append(taskId)
            }

        // ensures its always valid number
        return time.toIntOrNull() ?: -(time.toLong() % Int.MAX_VALUE).toInt()
    }

    /**
     * Get next day notifications and calls callback
     *
     */
    fun getNextDayNotifications() {
        val now = Clock.System.now().toEpochMilliseconds()
        val tasks = runBlocking {
            tasksRepo.getTasksBetween(now, now + HOURS_AHEAD.hours.inWholeMilliseconds).first()
        }

        if (callback == null) throw NullPointerException("Make sure you initialize `callback`")

        tasks.forEach {
            if (it.completed == null) {
                val uuid = TaskMeta(
                    it.startTime,
                    it.endTime,
                    it.repeatTag,
                    it.repeatOften,
                    it.taskId,
                    it.metaId
                ).generateUuid(it.currentEventTime)
                callback?.invoke(
                    CallbackBundle.Create(
                        uuid,
                        it.taskId,
                        it.metaId,
                        it.currentEventTime
                    )
                )
            }
        }
    }

    sealed class CallbackBundle(
        open val uuid: Int,
        open val taskId: Long,
        open val metaId: Long,
        open val scheduledTime: Long,
    ) {
        class Create(
            override val uuid: Int,
            override val taskId: Long,
            override val metaId: Long,
            override val scheduledTime: Long,
        ) : CallbackBundle(uuid, taskId, metaId, scheduledTime)

        class Delete(
            override val uuid: Int,
            override val taskId: Long,
            override val metaId: Long,
            override val scheduledTime: Long,
        ) : CallbackBundle(uuid, taskId, metaId, scheduledTime)
    }
}
