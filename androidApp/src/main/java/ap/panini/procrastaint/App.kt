package ap.panini.procrastaint

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import ap.panini.procrastaint.di.androidModule
import ap.panini.procrastaint.di.appModule
import ap.panini.procrastaint.notification.NotificationAlarmReceiver
import ap.panini.procrastaint.notification.Notifications
import ap.panini.procrastaint.notifications.NotificationManager
import ap.panini.procrastaint.services.NotificationWorker
import ap.panini.procrastaint.services.SyncWorker
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin
import java.util.concurrent.TimeUnit

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@App)
            modules(appModule, androidModule)
            workManagerFactory()
        }

        setupWorkers()
        Notifications.createChannels(this)

        val notificationManager: NotificationManager by inject()
        notificationManager.callback = { NotificationAlarmReceiver.notificationCallback(it, this) }
    }

    private fun setupWorkers() {
        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork(
                "sync_calendar",
                ExistingPeriodicWorkPolicy.KEEP,
                PeriodicWorkRequestBuilder<SyncWorker>(
                    1,
                    TimeUnit.HOURS
                ).setConstraints(
                    Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
                )
                    .build()
            )

        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork(
                "update_notifications",
                ExistingPeriodicWorkPolicy.KEEP,
                PeriodicWorkRequestBuilder<NotificationWorker>(24, TimeUnit.HOURS)
                    .build()
            )
    }
}
