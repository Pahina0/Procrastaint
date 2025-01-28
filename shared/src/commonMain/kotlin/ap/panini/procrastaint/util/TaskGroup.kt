package ap.panini.procrastaint.util

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

        val meta = if (startTimes.isEmpty()) {
            listOf(
                TaskMeta(
                    startTime = null,
                    endTime = endTime,
                    repeatTag = repeatTag,
                    repeatOften = repeatOften.let { rep -> if (rep == 0) null else repeatOften },
                )
            )
        } else {
            startTimes.map {
                TaskMeta(
                    startTime = it,
                    endTime = endTime,
                    repeatTag = repeatTag,
                    repeatOften = repeatOften.let { rep -> if (rep == 0) null else repeatOften }
                )
            }
        }

        // no start time
        return Task(
            taskInfo = TaskInfo(
                title = title,
                description = description,
            ),
            meta = meta
        )
    }
}
