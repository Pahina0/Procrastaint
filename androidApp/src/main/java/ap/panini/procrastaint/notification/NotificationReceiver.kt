package ap.panini.procrastaint.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat.EXTRA_NOTIFICATION_ID
import androidx.core.app.NotificationManagerCompat
import ap.panini.procrastaint.data.entities.TaskCompletion
import ap.panini.procrastaint.data.repositories.TaskRepository
import ap.panini.procrastaint.notification.NotificationAlarmReceiver.Companion.TASK_ID
import ap.panini.procrastaint.notification.NotificationAlarmReceiver.Companion.TASK_META_ID
import ap.panini.procrastaint.notification.NotificationAlarmReceiver.Companion.TASK_TIME
import ap.panini.procrastaint.notifications.NotificationManager
import ap.panini.procrastaint.util.Date
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime

class NotificationReceiver : BroadcastReceiver(), KoinComponent {

    private val taskRepo: TaskRepository by inject()

    @OptIn(ExperimentalTime::class)
    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = NotificationManagerCompat.from(context)
        when (intent.action!!) {
            // sets completion
            ACTION_COMPLETE_TASK -> {
                val taskId = intent.getLongExtra(TASK_ID, -1)
                val metaId = intent.getLongExtra(TASK_META_ID, -1)
                val taskTime = intent.getLongExtra(TASK_TIME, -1)
                val uuid = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)

                if (taskId == -1L || taskTime == -1L || metaId == -1L || uuid == -1) return
                goAsync {
                    taskRepo.addCompletion(TaskCompletion(Date.getTime(), taskTime, taskId, metaId))
                }

                notificationManager.cancel(uuid)
            }

            // sends same reminder in 15 min
            ACTION_SNOOZE_TASK -> {
                val taskId = intent.getLongExtra(TASK_ID, -1)
                val metaId = intent.getLongExtra(TASK_META_ID, -1)
                val taskTime = intent.getLongExtra(TASK_TIME, -1)
                val uuid = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)

                if (taskId == -1L || taskTime == -1L || metaId == -1L || uuid == -1) return
                NotificationAlarmReceiver.notificationCallback(
                    NotificationManager.CallbackBundle.Create(
                        uuid,
                        taskId,
                        metaId,
                        taskTime
                    ),
                    context,
                    Clock.System.now().toEpochMilliseconds() + 15.minutes.inWholeMilliseconds
                )

                notificationManager.cancel(uuid)
            }
        }
    }

    private fun BroadcastReceiver.goAsync(
        context: CoroutineContext = EmptyCoroutineContext,
        block: suspend CoroutineScope.(PendingResult) -> Unit
    ) {
        val pendingResult = goAsync()
        @OptIn(DelicateCoroutinesApi::class) // Must run globally; there's no teardown callback.
        GlobalScope.launch(context) {
            try {
                block(pendingResult)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_COMPLETE_TASK = "action_complete_task"
        const val ACTION_SNOOZE_TASK = "action_snooze_task"
    }
}
