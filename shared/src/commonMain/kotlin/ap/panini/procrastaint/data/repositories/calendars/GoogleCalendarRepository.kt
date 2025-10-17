package ap.panini.procrastaint.data.repositories.calendars

import ap.panini.procrastaint.data.entities.Task
import ap.panini.procrastaint.data.entities.TaskCompletion
import ap.panini.procrastaint.data.entities.TaskInfo
import ap.panini.procrastaint.data.entities.TaskMeta
import ap.panini.procrastaint.data.entities.google.GoogleCalendar
import ap.panini.procrastaint.data.entities.google.GoogleEvent
import ap.panini.procrastaint.data.entities.google.GoogleEvent.Companion.getGoogleEvents
import ap.panini.procrastaint.data.network.api.GoogleCalendarApi
import ap.panini.procrastaint.data.repositories.PreferenceRepository
import ap.panini.procrastaint.util.toRFC3339
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlin.time.Duration.Companion.minutes

class GoogleCalendarRepository internal constructor(
    private val preference: PreferenceRepository,
    private val gcApi: GoogleCalendarApi
) : CalendarRepository {
    override fun isLoggedIn(): Flow<Boolean> {
        return combine(
            preference.getString(PreferenceRepository.GOOGLE_REFRESH_TOKEN),
            preference.getString(PreferenceRepository.GOOGLE_CALENDAR_ID),
            preference.getString(PreferenceRepository.GOOGLE_REFRESH_TOKEN)
        ) { token, id, refresh ->
            token.isNotBlank() && id.isNotBlank() && refresh.isNotBlank()
        }
    }

    override suspend fun logout() {
        preference.setString(PreferenceRepository.GOOGLE_ACCESS_TOKEN)
        preference.setString(PreferenceRepository.GOOGLE_CALENDAR_ID)
        preference.setString(PreferenceRepository.GOOGLE_REFRESH_TOKEN)
    }

    override suspend fun login(accessToken: String?, refreshToken: String?) {
        preference.setString(
            PreferenceRepository.GOOGLE_ACCESS_TOKEN,
            accessToken!!
        )
        preference.setString(
            PreferenceRepository.GOOGLE_REFRESH_TOKEN,
            refreshToken!!
        )

        createCalendar()
    }

    override suspend fun createCalendar(): CalendarRepository.Response {
        var error = Throwable("Unknown error occurred")
        // creates a new calendar if only one doesn't exist already
        val possibleCalendar = gcApi.getCalendars()
            .firstOrNull()?.items?.firstOrNull { it.summary == "Procrastaint" }

        if (possibleCalendar?.id == null) {
            val create = gcApi.createCalendar(GoogleCalendar("Procrastaint"))
                .catch { error = it }
                .firstOrNull()
                ?.id ?: return CalendarRepository.Response.Error(error)

            preference.setString(
                PreferenceRepository.GOOGLE_CALENDAR_ID,
                create
            )

            return CalendarRepository.Response.Success
        }

        preference.setString(
            PreferenceRepository.GOOGLE_CALENDAR_ID,
            possibleCalendar.id
        )

        return CalendarRepository.Response.Success
    }

    override suspend fun createTask(
        task: Task,
    ): CalendarRepository.Response {
        var error = Throwable("Unknown error occurred")
        getGoogleEvents(task, preference).forEach {
            val success = gcApi.createEvent(
                it,
                preference.getString(PreferenceRepository.GOOGLE_CALENDAR_ID).first()
            ).catch { err -> error = err }.firstOrNull()

            success ?: return CalendarRepository.Response.Error(error)
        }

        return CalendarRepository.Response.Success
    }

    override suspend fun deleteTask(task: Task): CalendarRepository.Response {
        var error = Throwable("Unknown error occurred")
        getGoogleEvents(task, preference).forEach {
            val success = gcApi.deleteEvent(
                preference.getString(PreferenceRepository.GOOGLE_CALENDAR_ID).first(),
                it.id
            ).catch { err -> error = err }.firstOrNull()

            success ?: return CalendarRepository.Response.Error(error)
        }

        return CalendarRepository.Response.Success
    }

    override suspend fun addCompletion(
        task: Task,
        completion: TaskCompletion,
    ): CalendarRepository.Response {
        return updateCompletion(
            task,
            completion,
        ) { "✓ $it" }
    }

    override suspend fun removeCompletion(
        task: Task,
        completion: TaskCompletion
    ): CalendarRepository.Response {
        return updateCompletion(
            task,
            completion,
        )
    }


    override suspend fun updateTask(
        task: Task,
    ): CalendarRepository.Response {
        var error = Throwable("Unknown error occurred")

        val events = getGoogleEvents(task, preference)
        for (event in events) {
            gcApi.updateEvent(
                event,
                preference.getString(PreferenceRepository.GOOGLE_CALENDAR_ID).first(),
                event.id
            ).catch { error = it }
                .firstOrNull() ?: return CalendarRepository.Response.Error(error)
        }

        return CalendarRepository.Response.Success
    }


    /**
     * Update completion
     *
     *
     *
     * @param task
     * @param completion **taskId** is completely irrelevant
     * @param updatedText
     * @receiver
     * @return
     */
    private suspend fun updateCompletion(
        task: Task,
        completion: TaskCompletion,
        updatedText: (String) -> String = { it }
    ): CalendarRepository.Response {
        var error = Throwable("Unknown error occurred")

        val recurring = task.meta.first().let { it.repeatTag != null && it.repeatOften != null }
        val event = getGoogleEvents(task, preference).firstOrNull { it.metaId == completion.metaId }

        if (event == null) {
            // prob something wrong with my db if anything
            return CalendarRepository.Response.Success
        }

        if (!recurring) {
            gcApi.updateEvent(
                event.copy(
                    summary = updatedText(event.summary),
                    reminders = GoogleEvent.Reminder(overrides = listOf())
                ),
                preference.getString(PreferenceRepository.GOOGLE_CALENDAR_ID).first(),
                event.id
            ).catch { error = it }
                .firstOrNull() ?: return CalendarRepository.Response.Error(error)

            return CalendarRepository.Response.Success
        }

        val events = gcApi.getInstances(
            preference.getString(PreferenceRepository.GOOGLE_CALENDAR_ID).first(),
            event.id,
            (completion.forTime + 1.minutes.inWholeMilliseconds).toRFC3339(),
            (completion.forTime - 1.minutes.inWholeMilliseconds).toRFC3339(),
        ).catch { error = it }
            .firstOrNull() ?: return CalendarRepository.Response.Error(error)

        val modifyEvent =
            events.items.firstOrNull { it.start.dateTime == completion.forTime.toRFC3339() }

        // they don't have it in the calendar anymore, most likely user delete, ignore
        if (modifyEvent == null) {
            return CalendarRepository.Response.Success
        }

        gcApi.updateEvent(
            modifyEvent.copy(
                summary = updatedText(event.summary),
                reminders = GoogleEvent.Reminder(overrides = listOf())
            ),
            preference.getString(PreferenceRepository.GOOGLE_CALENDAR_ID).first(),
            modifyEvent.id
        ).catch { error = it }
            .firstOrNull() ?: return CalendarRepository.Response.Error(error)

        return CalendarRepository.Response.Success
    }
}
