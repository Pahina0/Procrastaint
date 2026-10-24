package ap.panini.procrastaint.util

import ap.panini.procrastaint.data.repositories.PreferenceRepository
import ap.panini.procrastaint.di.createDataStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import okio.FileSystem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class ParserConfigTest {

    private fun createTestRepository(): PreferenceRepository {
        val filename = "test_parser_${Uuid.random().toHexString()}.preferences_pb"
        val tempPath = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / filename
        val dataStore = createDataStore { tempPath.toString() }
        return PreferenceRepository(dataStore)
    }

    private suspend fun waitForCondition(timeoutMs: Long = 3000, condition: () -> Boolean) {
        val start = kotlin.time.Clock.System.now().toEpochMilliseconds()
        while (!condition()) {
            if (kotlin.time.Clock.System.now().toEpochMilliseconds() - start > timeoutMs) {
                error("Condition not met within $timeoutMs ms")
            }
            delay(20)
        }
    }

    @Test
    fun testDefaultMorningHourIs9() = runBlocking {
        val repo = createTestRepository()
        val parser = Parser(preferenceRepository = repo)

        waitForCondition { parser.cachedConfig.morning == 9 }

        val parsed = parser.parse("tomorrow morning")
        assertTrue(parsed.times.isNotEmpty(), "Expected parsed times for 'tomorrow morning'")
        val startTime = parsed.times.first().startTimes.first()
        val localTime = Instant.fromEpochMilliseconds(startTime).toLocalDateTime(TimeZone.currentSystemDefault())
        assertEquals(9, localTime.hour)
    }

    @Test
    fun testDefaultParserWithoutRepository() {
        val parser = Parser()
        val parsed = parser.parse("tomorrow morning")
        assertTrue(parsed.times.isNotEmpty(), "Expected parsed times for default parser")
        val startTime = parsed.times.first().startTimes.first()
        val localTime = Instant.fromEpochMilliseconds(startTime).toLocalDateTime(TimeZone.currentSystemDefault())
        assertEquals(9, localTime.hour)
    }

    @Test
    fun testMorningHourUpdatesTo6() = runBlocking {
        val repo = createTestRepository()
        val parser = Parser(preferenceRepository = repo)

        repo.setMorningHour(6)
        waitForCondition { parser.cachedConfig.morning == 6 }

        val parsed = parser.parse("tomorrow morning")
        assertTrue(parsed.times.isNotEmpty(), "Expected parsed times for 'tomorrow morning'")
        val startTime = parsed.times.first().startTimes.first()
        val localTime = Instant.fromEpochMilliseconds(startTime).toLocalDateTime(TimeZone.currentSystemDefault())
        assertEquals(6, localTime.hour)
    }

    @Test
    fun testEuropeanDateFormatToggled() = runBlocking {
        val repo = createTestRepository()
        val parser = Parser(preferenceRepository = repo)

        // Default: US MM/DD/YYYY -> 03/04/2026 should be March 4th
        waitForCondition { !parser.cachedConfig.useEuropeanDate }
        val parsedUS = parser.parse("Doctor visit 03/04/2026")
        assertTrue(parsedUS.times.isNotEmpty(), "Expected parsed times for US date")
        val startTimeUS = parsedUS.times.first().startTimes.first()
        val localTimeUS = Instant.fromEpochMilliseconds(startTimeUS).toLocalDateTime(TimeZone.currentSystemDefault())
        assertEquals(Month.MARCH, localTimeUS.month)
        assertEquals(4, localTimeUS.day)

        // Toggle European: DD/MM/YYYY -> 03/04/2026 should be April 3rd
        repo.setUseEuropeanDate(true)
        waitForCondition { parser.cachedConfig.useEuropeanDate }

        val parsedEU = parser.parse("Doctor visit 03/04/2026")
        assertTrue(parsedEU.times.isNotEmpty(), "Expected parsed times for EU date")
        val startTimeEU = parsedEU.times.first().startTimes.first()
        val localTimeEU = Instant.fromEpochMilliseconds(startTimeEU).toLocalDateTime(TimeZone.currentSystemDefault())
        assertEquals(Month.APRIL, localTimeEU.month)
        assertEquals(3, localTimeEU.day)
    }
}
