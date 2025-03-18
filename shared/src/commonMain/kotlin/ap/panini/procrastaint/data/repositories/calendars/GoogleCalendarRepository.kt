package ap.panini.procrastaint.data.repositories.calendars

import ap.panini.procrastaint.data.entities.Task
import ap.panini.procrastaint.data.entities.TaskCompletion
import ap.panini.procrastaint.data.entities.google.GoogleCalendar
import ap.panini.procrastaint.data.entities.google.GoogleEvent
import ap.panini.procrastaint.data.entities.google.GoogleEvent.Companion.getGoogleEvents
import ap.panini.procrastaint.data.network.api.GoogleCalendarApi
import ap.panini.procrastaint.data.repositories.PreferenceRepository
import ap.panini.procrastaint.util.toRFC3339
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlin.time.Duration.Companion.minutes

class GoogleCalendarRepository(
    private val preference: PreferenceRepository,
    private val gcApi: GoogleCalendarApi
) : CalendarRepository {
    override suspend fun createCalendar(onSuccess: () -> Unit, onFailure: (ex: Throwable) -> Unit) {
        // creates a new calendar if only one doesn't exist already
        val possibleCalendar = gcApi.getCalendars()
            .firstOrNull()?.items?.firstOrNull { it.summary == "Procrastaint" }

        preference.setString(
            PreferenceRepository.GOOGLE_CALENDAR_ID,
            possibleCalendar?.id
                ?: gcApi.createCalendar(GoogleCalendar("Procrastaint"))
                    .catch { onFailure(it) }
                    .firstOrNull()
                    ?.id!!

        )

        onSuccess()
    }

    override suspend fun createEvent(
        task: Task,
        onSuccess: () -> Unit,
        onFailure: (ex: Throwable) -> Unit
    ) {
        getGoogleEvents(task, preference).forEach {
            val success = gcApi.createEvent(
                it,
                preference.getString(PreferenceRepository.GOOGLE_CALENDAR_ID).first()
            ).catch { err -> onFailure(err) }.firstOrNull()

            success ?: return
        }

        onSuccess()
    }

    override suspend fun addCompletion(
        task: Task,
        completion: TaskCompletion,
        onSuccess: () -> Unit,
        onFailure: (ex: Throwable) -> Unit
    ) {
        updateCompletion(
            task,
            completion,
            onSuccess,
            onFailure
        ) { "✓ $it" }
    }

    override suspend fun removeCompletion(
        task: Task,
        completion: TaskCompletion,
        onSuccess: () -> Unit,
        onFailure: (ex: Throwable) -> Unit
    ) {
        updateCompletion(
            task,
            completion,
            onSuccess,
            onFailure
        )
    }

    private suspend fun updateCompletion(
        task: Task,
        completion: TaskCompletion,
        onSuccess: () -> Unit,
        onFailure: (ex: Throwable) -> Unit,
        updatedText: (String) -> String = { it }
    ) {
        val recurring = task.meta.first().let { it.repeatTag != null && it.repeatOften != null }
        val event = getGoogleEvents(task, preference).firstOrNull { it.metaId == completion.metaId }

        if (event == null) {
            // prob something wrong with my db if anything
            onSuccess()
            return
        }

        if (!recurring) {
            gcApi.updateEvent(
                event.copy(
                    summary = updatedText(event.summary),
                    reminders = GoogleEvent.Reminder(overrides = listOf())
                ),
                preference.getString(PreferenceRepository.GOOGLE_CALENDAR_ID).first(),
                event.id
            ).catch { onFailure(it) }
                .firstOrNull()

            onSuccess()
            return
        }

        val events = gcApi.getInstances(
            preference.getString(PreferenceRepository.GOOGLE_CALENDAR_ID).first(),
            event.id,
            (completion.forTime + 1.minutes.inWholeMilliseconds).toRFC3339(),
            (completion.forTime - 1.minutes.inWholeMilliseconds).toRFC3339(),
        ).catch { onFailure(it) }
            .firstOrNull() ?: return

        val modifyEvent =
            events.items.firstOrNull { it.start.dateTime == completion.forTime.toRFC3339() }

        // they dont have it in the calendar anymore, most likely user delete, ignore
        if (modifyEvent == null) {
            onSuccess()
            return
        }

        gcApi.updateEvent(
            modifyEvent.copy(
                summary = updatedText(event.summary),
                reminders = GoogleEvent.Reminder(overrides = listOf())
            ),
            preference.getString(PreferenceRepository.GOOGLE_CALENDAR_ID).first(),
            modifyEvent.id
        ).catch { onFailure(it) }
            .firstOrNull() ?: return

        onSuccess()

    }
}
