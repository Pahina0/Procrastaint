package ap.panini.procrastaint.data.entities.google

import ap.panini.procrastaint.data.entities.Task
import ap.panini.procrastaint.data.entities.TaskMeta
import ap.panini.procrastaint.data.repositories.PreferenceRepository
import ap.panini.procrastaint.util.Time
import ap.panini.procrastaint.util.toRFC3339
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.time.Duration.Companion.minutes

@Serializable
data class GoogleEvent(
    val summary: String,

    val id: String,

    @Transient
    val metaId: Long = 0,

    @Transient
    val startTime: Long = 0,

    val start: Time = Time(startTime.toRFC3339()),

    val end: Time = Time((startTime + 30.minutes.inWholeMilliseconds).toRFC3339()),

    val description: String = "",

    val recurrence: List<String> = emptyList(),

    val reminders: Reminder = Reminder(),
) {

    @Serializable
    data class Time(val dateTime: String, val timeZone: String = "UTC")

    @Serializable
    data class Reminder(
        val useDefault: Boolean = false,

        val overrides: List<Override> = listOf(Override())
    ) {
        @Serializable
        data class Override(
            val method: String = "popup",
            val minutes: Int = 0
        )
    }

    companion object {
        private fun TaskMeta.getGoogleId(preferences: PreferenceRepository) =
            "$taskId${preferences.getUuid()}$metaId"

        fun getGoogleEvents(task: Task, preferences: PreferenceRepository): List<GoogleEvent> {
            if (task.meta.isEmpty()) return emptyList()

            if (task.meta.first().repeatOften == null || task.meta.first().repeatTag == null) {
                return task.meta.map { meta ->
                    GoogleEvent(
                        summary = task.taskInfo.title,
                        description = task.taskInfo.description,
                        id = meta.getGoogleId(preferences),
                        startTime = meta.startTime ?: Clock.System.now().toEpochMilliseconds(),
                        metaId = meta.metaId
                    )
                }
            }

            return task.meta.mapNotNull { meta ->
                // google calendar doesn't support these
                if (meta.repeatTag == ap.panini.procrastaint.util.Time.HOUR
                    || meta.repeatTag == ap.panini.procrastaint.util.Time.MINUTE
                    || meta.repeatTag == ap.panini.procrastaint.util.Time.SECOND
                ) return@mapNotNull null

                val recurs = buildString {
                    append("RRULE:FREQ=${meta.repeatTag!!.toTimeRepeatString()}")
                    meta.endTime?.let {
                        append(";UNTIL=${it.toRFC3339(includeFiller = false)}")
                    }
                }
                GoogleEvent(
                    summary = task.taskInfo.title,
                    description = task.taskInfo.description,
                    id = meta.getGoogleId(preferences),
                    startTime = meta.startTime ?: Clock.System.now().toEpochMilliseconds(),
                    recurrence = listOf(recurs),
                    metaId = meta.metaId
                )
            }
        }
    }
}
