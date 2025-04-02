package ap.panini.procrastaint.services

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import ap.panini.procrastaint.notification.NotificationAlarmReceiver
import ap.panini.procrastaint.notifications.NotificationManager

class NotificationWorker(
    private val notificationManager: NotificationManager,
    val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {
    override fun doWork(): Result {
        notificationManager.callback =
            { NotificationAlarmReceiver.notificationCallback(it, context) }
        notificationManager.getNextDayNotifications()
        return Result.success()
    }
}
