package ap.panini.procrastaint.util

import ap.panini.kwhen.TimeParser
import ap.panini.procrastaint.data.entities.TaskTag
import ap.panini.procrastaint.data.repositories.TaskRepository
import ap.panini.procrastaint.util.Time.Companion.toTime
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

class Parser(private val db: TaskRepository) {
    private var textParser = TimeParser()

    private fun reset() {
        textParser = TimeParser()
    }

    fun parse(input: String): Parsed {
        reset()

        val tagRegex = Regex("(\\s*)#(\\S+)")
        val tagMatches = tagRegex.findAll(input)

        val tags = tagMatches.map {
            val tag = it.groupValues[2] // Extract the actual tag without #
            ParsedTag(
                tag =
                    runBlocking { db.getTagOrNull(tag) } ?: TaskTag(tag),
                extractedRange = it.range
            )
        }.toList()

        val times = textParser.parse(input).map {
            val startTimes = it.startTime.map { startTime ->
                startTime.toMillisecondLocal()
            }

            ParsedTime(
                text = it.text,
                extractedRange = it.range,
                startTimes = startTimes.toSet(),
                endTime = it.endTime?.toMillisecondLocal(),
                tagsTimeStart = it.tagsTimeStart.map { mapped -> mapped.toTime() }.toSet(),
                tagsTimeEnd = it.tagsTimeEnd.map { mapped -> mapped.toTime() }.toSet(),
                repeatTag = it.repeatTag?.toTime(),
                repeatOften = it.repeatOften ?: 0,
            )
        }

        return Parsed(
            text = input,
            times = times,
            tags = tags
        )
    }

    private fun LocalDateTime.toMillisecondLocal() =
        toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
}
