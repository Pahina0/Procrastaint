package ap.panini.procrastaint.services

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ap.panini.procrastaint.data.repositories.NetworkCalendarRepository
import ap.panini.procrastaint.data.repositories.calendars.CalendarRepository

class SyncWorker(
    private val nsRepo: NetworkCalendarRepository,
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(
    appContext,
    params
) {
    override suspend fun doWork(): Result {
        return when (nsRepo.trySync()) {
            is CalendarRepository.Response.Success -> Result.success()
            is CalendarRepository.Response.Error -> Result.retry()
        }
    }
}
