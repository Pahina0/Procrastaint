package ap.panini.procrastaint.data.entities.google

import ap.panini.procrastaint.data.entities.Task
import ap.panini.procrastaint.data.entities.TaskMeta
import ap.panini.procrastaint.data.repositories.PreferenceRepository
import ap.panini.procrastaint.util.toRFC3339
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.time.Duration.Companion.minutes

@Serializable
data class GoogleEvent(
    val summary: String,

    @Transient
    val startTime: Long = 0,

    @Transient
    val endTime: Long? = null,

    val id: String,

    val start: Time = Time(startTime.toRFC3339()),

    val end: Time = Time(
        endTime?.toRFC3339()
            ?: (startTime + 10.minutes.inWholeMilliseconds).toRFC3339()
    ),

    val description: String = ""

) {

    @Serializable
    data class Time(val dateTime: String, val timeZone: String = "UTC")

    companion object {
        fun TaskMeta.getGoogleId(preferences: PreferenceRepository) =
            "$taskId${preferences.getUuid()}$metaId"

        fun getGoogleEvents(task: Task, preferences: PreferenceRepository): List<GoogleEvent> {
            return task.meta.map { meta ->
                GoogleEvent(
                    summary = task.taskInfo.title,
                    description = task.taskInfo.description,
                    id = meta.getGoogleId(preferences),
                    startTime = meta.startTime ?: Clock.System.now().toEpochMilliseconds(),
                    endTime = meta.endTime
                )
            }
        }
    }
}
