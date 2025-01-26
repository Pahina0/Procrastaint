package ap.panini.procrastaint.util

import ap.panini.kwhen.TimeUnit
import ap.panini.procrastaint.data.entities.Task
import ap.panini.procrastaint.data.entities.TaskInfo
import ap.panini.procrastaint.data.entities.TaskMeta

/**
 * a prototype task to be converted into a list of tasks.
 * simpler than a normal task
 */
data class TaskGroup(
    val startTimes: Set<Long>,
    val endTime: Long?, // only needed if you repeat

    val title: String,
    val description: String = "",

    val repeatTag: Time? = null,
    val repeatOften: Int = 0,
) {

    /**
     * @return null if invalid task, else the tasks separated
     */
    fun toTask(): Task? {
        if (endTime != null) {
            if (startTimes.isEmpty() ||
                (startTimes.firstOrNull() ?: 0) > endTime
            ) {
                return null
            }
        }


        // no start time
        if (startTimes.isEmpty()) {
            return Task(
                taskInfo = TaskInfo(
                    title = title,
                    description = description,
                ),
                meta = listOf(
                    TaskMeta(
                        startTime = null,
                        endTime = endTime,
                        repeatTag = repeatTag,
                        repeatOften = repeatOften.let { rep -> if (rep == 0) null else repeatOften },
                    )
                ),
            )
        }

        return Task(
            taskInfo = TaskInfo(
                title = title,
                description = description
            ),
            meta = startTimes.map {
                TaskMeta(
                    startTime = it,
                    endTime = endTime,
                    repeatTag = repeatTag,
                    repeatOften = repeatOften.let { rep -> if (rep == 0) null else repeatOften }
                )
            }
        )
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
