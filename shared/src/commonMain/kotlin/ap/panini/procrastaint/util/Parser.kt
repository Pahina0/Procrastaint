package ap.panini.procrastaint.util

import ap.panini.kwhen.TimeParser
import ap.panini.procrastaint.util.Time.Companion.toTime
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

class Parser {
    private val textParser = TimeParser()

    fun parse(input: String): List<Parsed> =
        textParser.parse(input).run {
            map {
                val startTimes = it.startTime.map { startTime ->
                    startTime.toMillisecondLocal()
                }
                Parsed(
                    it.text,
                    it.range,
                    startTimes.toSet(),
                    it.endTime?.toMillisecondLocal(),
                    it.tagsTimeStart.map { mapped -> mapped.toTime() }.toSet(),
                    it.tagsTimeEnd.map { mapped -> mapped.toTime() }.toSet(),
                    repeatTag = it.repeatTag?.toTime(),
                    repeatOften = it.repeatOften ?: 0
                )
            }
        }

    private fun LocalDateTime.toMillisecondLocal() =
        toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
}

data class Parsed(
    val text: String,
    val extractedRange: IntRange,
    val startTimes: Set<Long>,
    val endTime: Long?,
    val tagsTimeStart: Set<Time>,
    val tagsTimeEnd: Set<Time>,
    val repeatTag: Time?,
    val repeatOften: Int
)
