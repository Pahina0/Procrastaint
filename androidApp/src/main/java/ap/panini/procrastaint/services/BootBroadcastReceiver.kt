package ap.panini.procrastaint.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import ap.panini.procrastaint.notification.NotificationAlarmReceiver
import ap.panini.procrastaint.notifications.NotificationManager
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class BootBroadcastReceiver : BroadcastReceiver(), KoinComponent {

    private val notificationManager: NotificationManager by inject()

    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED != intent.action) {
            return
        }

        notificationManager.callback =
            { NotificationAlarmReceiver.notificationCallback(it, context) }
        notificationManager.getNextDayNotifications()
    }
}
