package ap.panini.procrastaint.data.repositories.calendars

import ap.panini.procrastaint.data.entities.Task
import ap.panini.procrastaint.data.entities.TaskCompletion
import ap.panini.procrastaint.data.entities.google.GoogleCalendar
import ap.panini.procrastaint.data.entities.google.GoogleEvent
import ap.panini.procrastaint.data.entities.google.GoogleEvent.Companion.getGoogleEvents
import ap.panini.procrastaint.data.network.api.GoogleCalendarApi
import ap.panini.procrastaint.data.repositories.PreferenceRepository
import ap.panini.procrastaint.util.toRFC3339
import kotlinx.coroutines.flow.first
import kotlin.time.Duration.Companion.minutes

class GoogleCalendarRepository(
    private val preference: PreferenceRepository,
    private val gcApi: GoogleCalendarApi
) : CalendarRepository {
    override suspend fun createCalendar(onSuccess: () -> Unit, onFailure: (ex: Throwable) -> Unit) {
        val existingCalendars = gcApi.getCalendars()

        // creates a new calendar if only one doesn't exist already
        val possibleCalendar = existingCalendars.items.firstOrNull { it.summary == "Procrastaint" }

        preference.setString(
            PreferenceRepository.GOOGLE_CALENDAR_ID,
            possibleCalendar?.id
                ?: gcApi.createCalendar(GoogleCalendar("Procrastaint"))
                    .id!!

        )
    }

    override suspend fun createEvent(
        task: Task,
        onSuccess: () -> Unit,
        onFailure: (ex: Throwable) -> Unit
    ) {
        getGoogleEvents(task, preference).forEach {
            gcApi.createEvent(
                it,
                preference.getString(PreferenceRepository.GOOGLE_CALENDAR_ID).first()
            )
        }
    }

    override suspend fun addCompletion(
        task: Task,
        completion: TaskCompletion,
        onSuccess: () -> Unit,
        onFailure: (ex: Throwable) -> Unit
    ) {
        val recurring = task.meta.first().let { it.repeatTag != null && it.repeatOften != null }
        val event = getGoogleEvents(task, preference).firstOrNull { it.metaId == completion.metaId }
            ?: return

        if (!recurring) {
            gcApi.updateEvent(
                event.copy(
                    summary = "✓ ${event.summary}",
                    reminders = GoogleEvent.Reminder(overrides = listOf())
                ),
                preference.getString(PreferenceRepository.GOOGLE_CALENDAR_ID).first(),
                event.id
            )
            return
        }

        val events = gcApi.getInstances(
            preference.getString(PreferenceRepository.GOOGLE_CALENDAR_ID).first(),
            event.id,
            (completion.forTime + 1.minutes.inWholeMilliseconds).toRFC3339(),
            (completion.forTime - 1.minutes.inWholeMilliseconds).toRFC3339(),
        )

        val modifyEvent =
            events.items.firstOrNull { it.start.dateTime == completion.forTime.toRFC3339() }
                ?: return

        gcApi.updateEvent(
            modifyEvent.copy(
                summary = "✓ ${event.summary}",
                reminders = GoogleEvent.Reminder(overrides = listOf())
            ),
            preference.getString(PreferenceRepository.GOOGLE_CALENDAR_ID).first(),
            modifyEvent.id
        )
    }
}
