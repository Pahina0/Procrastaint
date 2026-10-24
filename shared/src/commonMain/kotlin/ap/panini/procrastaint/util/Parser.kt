package ap.panini.procrastaint.util

import ap.panini.kwhen.TimeParser
import ap.panini.kwhen.configs.ENConfig
import ap.panini.procrastaint.data.entities.TaskTag
import ap.panini.procrastaint.data.repositories.PreferenceRepository
import ap.panini.procrastaint.data.repositories.TaskRepository
import ap.panini.procrastaint.util.Time.Companion.toTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.concurrent.Volatile
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class Parser(
    private val db: TaskRepository? = null,
    private val preferenceRepository: PreferenceRepository? = null,
    coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) {
    @Volatile
    internal var cachedConfig: ENConfig = ENConfig()
        private set

    private var textParser = TimeParser()

    init {
        preferenceRepository?.let { repo ->
            coroutineScope.launch {
                repo.getKwhenConfig().collect { config ->
                    cachedConfig = config
                }
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun reset() {
        textParser = TimeParser(
            cachedConfig.copy(
                relativeTo = Clock.System.now().toEpochMilliseconds(),
                timeZone = TimeZone.currentSystemDefault()
            )
        )
    }

    fun parse(input: String): Parsed {
        reset()

        val tagRegex = Regex("(\\s*)#(\\S+)")
        val tagMatches = tagRegex.findAll(input)

        val tags = tagMatches.map {
            val tag = it.groupValues[2] // Extract the actual tag without #
            ParsedTag(
                tag =
                db?.let { repository -> runBlocking { repository.getTagOrNull(tag) } } ?: TaskTag(tag),
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

    @OptIn(ExperimentalTime::class)
    private fun LocalDateTime.toMillisecondLocal() =
        toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
}
