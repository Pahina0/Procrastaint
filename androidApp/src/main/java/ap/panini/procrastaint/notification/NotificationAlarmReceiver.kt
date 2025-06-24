package ap.panini.procrastaint.notification

import android.Manifest
import android.app.AlarmManager
import android.app.Application.ALARM_SERVICE
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.EXTRA_NOTIFICATION_ID
import androidx.core.app.NotificationManagerCompat
import ap.panini.procrastaint.R
import ap.panini.procrastaint.data.repositories.TaskRepository
import ap.panini.procrastaint.notifications.NotificationManager
import ap.panini.procrastaint.ui.MainActivity
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class NotificationAlarmReceiver : BroadcastReceiver(), KoinComponent {

    private val taskRepo: TaskRepository by inject()

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(TASK_ID, -1)
        val taskTime = intent.getLongExtra(TASK_TIME, -1)
        val taskUuid = intent.getIntExtra(TASK_UUID, -1)
        val taskMetaId = intent.getLongExtra(TASK_META_ID, -1)

        val failed = taskId == -1L || taskUuid == -1 || taskTime == -1L || taskMetaId == -1L

        if (failed) return

        val task = runBlocking { taskRepo.getTask(taskId) }

        val completeIntent = Intent(context, NotificationReceiver::class.java).apply {
            action = NotificationReceiver.ACTION_COMPLETE_TASK
            putExtra(EXTRA_NOTIFICATION_ID, taskUuid)
            putExtra(TASK_ID, taskId)
            putExtra(TASK_TIME, taskTime)
            putExtra(TASK_META_ID, taskMetaId)
        }.let {
            PendingIntent.getBroadcast(context, taskUuid, it, PendingIntent.FLAG_IMMUTABLE)
        }

        val snoozeIntent = Intent(context, NotificationReceiver::class.java).apply {
            action = NotificationReceiver.ACTION_SNOOZE_TASK
            putExtra(EXTRA_NOTIFICATION_ID, taskUuid)
            putExtra(TASK_ID, taskId)
            putExtra(TASK_TIME, taskTime)
            putExtra(TASK_META_ID, taskMetaId)
        }.let {
            PendingIntent.getBroadcast(context, taskUuid, it, PendingIntent.FLAG_IMMUTABLE)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )


        val notification =
            NotificationCompat.Builder(context, Notifications.CHANNEL_TASK_DUE).apply {
                setContentTitle(task.taskInfo.title)
                setContentText(task.taskInfo.description)
                addAction(R.drawable.baseline_check_24, "Complete", completeIntent)
                addAction(R.drawable.baseline_snooze_24, "Snooze 15 min", snoozeIntent)
                setSmallIcon(R.drawable.baseline_task_alt_24)
                setContentIntent(pendingIntent)
                setAutoCancel(true)
            }.build()

        // check for notification perms
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        NotificationManagerCompat.from(context).notify(taskUuid, notification)
    }

    companion object {

        const val TASK_ID = "task_id"
        const val TASK_META_ID = "task_meta_id"
        const val TASK_TIME = "task_time"
        const val TASK_UUID = "task_uuid"

        fun notificationCallback(
            info: NotificationManager.CallbackBundle,
            context: Context,
            fireTime: Long = info.scheduledTime
        ) {
            val alarmMgr: AlarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager

            val intent = Intent(context, NotificationAlarmReceiver::class.java).apply {
                putExtra(TASK_ID, info.taskId)
                putExtra(TASK_META_ID, info.metaId)
                putExtra(TASK_TIME, info.scheduledTime)
                putExtra(TASK_UUID, info.uuid)
            }.let { intent ->
                PendingIntent.getBroadcast(
                    context,
                    info.uuid,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                )
            }

            if (info is NotificationManager.CallbackBundle.Delete) {
                // gets rid of pending notification and alarm
                alarmMgr.cancel(intent)
                NotificationManagerCompat.from(context).cancel(info.uuid)
            } else if (info is NotificationManager.CallbackBundle.Create) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmMgr.canScheduleExactAlarms()) {
                        alarmMgr.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            fireTime,
                            intent
                        )
                    }
                } else {
                    alarmMgr.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        fireTime,
                        intent
                    )
                }
            }
        }
    }
}
