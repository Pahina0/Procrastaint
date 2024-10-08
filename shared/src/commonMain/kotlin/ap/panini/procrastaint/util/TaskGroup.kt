package ap.panini.procrastaint.util

import ap.panini.kwhen.TimeUnit
import ap.panini.procrastaint.data.model.Task

data class TaskGroup(
    var startTimes: Set<Long>,
    var endTime: Long?, // only needed if you repeat

    var title: String,
    var description: String = "",

    var repeatTag: Time? = null,
    var repeatOften: Int = 0,
) {

    /**
     * @return null if invalid task, else the tasks separated
     */
    fun toTaskList(): List<Task>? {
        if (endTime != null) {
            if (startTimes.isEmpty() ||
                (startTimes.firstOrNull() ?: 0) > (endTime ?: Long.MAX_VALUE)
            ) {
                return null
            }
        }

        return startTimes.map {
            Task(
                startTime = it,
                endTime = endTime,
                title = title,
                description = description.run { ifBlank { null } },
                repeatTag = repeatTag,
                repeatOften = repeatOften.let { rep -> if (rep == 0) null else repeatOften }
            )
        }
    }
}

enum class Time {
    SECOND, MINUTE, HOUR, DAY, WEEK, MONTH, YEAR;

    companion object {
        fun TimeUnit.toTime() = when (this) {
            TimeUnit.SECOND -> SECOND
            TimeUnit.MINUTE -> MINUTE
            TimeUnit.HOUR -> HOUR
            TimeUnit.DAY -> DAY
            TimeUnit.WEEK -> WEEK
            TimeUnit.MONTH -> MONTH
            TimeUnit.YEAR -> YEAR
        }
    }
}
