package ap.panini.procrastaint.util

import ap.panini.procrastaint.data.entities.Task
import ap.panini.procrastaint.data.entities.TaskInfo
import ap.panini.procrastaint.data.entities.TaskMeta
import ap.panini.procrastaint.data.entities.TaskTag

data class Parsed(
    val text: String,
    val times: List<ParsedTime>,
    val tags: List<ParsedTag>
) {
    /**
     * Text extracted returns the task after all the tags and times are removed from the string
     * it is what wolud get saved as a task
     *
     * @param timeIndex
     * @return
     */
    fun textExtracted(timeIndex: Int): String = removeRangesFromString(
        text,
        (times.getOrNull(timeIndex)?.extractedRange?.let { listOf(it) }
            ?: emptyList()
                ) + tags.map { it.extractedRange }
    )

    /**
     * @return null if invalid task, else the tasks separated
     */
    fun toTask(
        timeIndex: Int = -1,
        description: String
    ): Task? {
        val time = times.getOrNull(timeIndex)

        val endTime = time?.endTime
        val startTimes = time?.startTimes ?: emptyList()
        val repeatTag = time?.repeatTag
        val repeatOften = time?.repeatOften
        val extractedTimePhrase = time?.text ?: ""

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
                title = textExtracted(timeIndex),
                description = description,
                extractedTimePhrase = extractedTimePhrase
            ),
            meta = meta
        )
    }

    private fun removeRangesFromString(input: String, ranges: List<IntRange>): String {
        // Sort ranges by start index in descending order to avoid index shifting
        val sortedRanges = ranges.sortedByDescending { it.first }

        var result = input
        for (range in sortedRanges) {
            // Ensure the range is within the bounds of the string
            val safeStart = range.first.coerceIn(0, result.length)
            val safeEnd = range.last.coerceIn(0, result.length - 1)
            if (safeStart <= safeEnd) {
                result = result.removeRange(safeStart..safeEnd)
            }
        }

        return result.trim()
    }
}

data class ParsedTag(
    val extractedRange: IntRange,
    val tag: TaskTag,
)

data class ParsedTime(
    val text: String,
    val extractedRange: IntRange,
    val startTimes: Set<Long>,
    val endTime: Long?,
    val tagsTimeStart: Set<Time>,
    val tagsTimeEnd: Set<Time>,
    val repeatTag: Time?,
    val repeatOften: Int,
)
