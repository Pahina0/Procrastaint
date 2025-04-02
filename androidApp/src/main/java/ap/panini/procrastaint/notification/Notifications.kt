package ap.panini.procrastaint.notification

import android.app.NotificationManager.IMPORTANCE_HIGH
import android.content.Context
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationChannelGroupCompat
import androidx.core.app.NotificationManagerCompat

object Notifications {

    private const val GROUP_TASK = "group_task"
    const val CHANNEL_TASK_DUE = "channel_task_due"


    fun createChannels(context: Context) {
        val notificationManager = NotificationManagerCompat.from(context)

        notificationManager.createNotificationChannelGroupsCompat(
            listOf(
                NotificationChannelGroupCompat.Builder(GROUP_TASK).setName("Tasks").build()
            )
        )
        notificationManager.createNotificationChannelsCompat(
            listOf(
                NotificationChannelCompat
                    .Builder(CHANNEL_TASK_DUE, IMPORTANCE_HIGH)
                    .setGroup(GROUP_TASK)
                    .setName("Task due")
                    .build()
            )
        )
    }
}