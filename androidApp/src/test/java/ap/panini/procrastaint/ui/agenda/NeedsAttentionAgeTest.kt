package ap.panini.procrastaint.ui.agenda

import ap.panini.procrastaint.data.models.ResolvedDateTimeFormat
import ap.panini.procrastaint.ui.agenda.components.formatOverdueAge
import ap.panini.procrastaint.util.Date
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class NeedsAttentionAgeTest {

    private val timeZone = TimeZone.currentSystemDefault()
    private val originalConfig = Date.displayConfig

    @BeforeTest
    fun setUp() {
        Date.displayConfig = ResolvedDateTimeFormat(useEuropeanDate = false)
    }

    @AfterTest
    fun tearDown() {
        Date.displayConfig = originalConfig
    }

    @Test
    fun testInvalidOrZeroTimestampReturnsOverdue() {
        assertEquals("Overdue", formatOverdueAge(0L, timeZone))
        assertEquals("Overdue", formatOverdueAge(-100L, timeZone))
    }

    @Test
    fun testTodayReturnsToday() {
        val today = Clock.System.todayIn(timeZone)
        val todayMs = today.atStartOfDayIn(timeZone).toEpochMilliseconds()
        assertEquals("Today", formatOverdueAge(todayMs, timeZone))
    }

    @Test
    fun testYesterdayReturnsYesterday() {
        val today = Clock.System.todayIn(timeZone)
        val yesterday = today.minus(1, DateTimeUnit.DAY)
        val yesterdayMs = yesterday.atStartOfDayIn(timeZone).toEpochMilliseconds()
        assertEquals("Yesterday", formatOverdueAge(yesterdayMs, timeZone))
    }

    @Test
    fun testDaysAgoInCurrentYearFormatsMonthAndDay() {
        val today = Clock.System.todayIn(timeZone)
        val threeDaysAgo = today.minus(3, DateTimeUnit.DAY)
        if (threeDaysAgo.year == today.year) {
            val threeDaysAgoMs = threeDaysAgo.atStartOfDayIn(timeZone).toEpochMilliseconds()
            val monthAbbr = threeDaysAgo.month.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)
            val expected = "$monthAbbr ${threeDaysAgo.day}"
            assertEquals(expected, formatOverdueAge(threeDaysAgoMs, timeZone))
        }
    }

    @Test
    fun testEuropeanDateFormat() {
        Date.displayConfig = ResolvedDateTimeFormat(useEuropeanDate = true)
        val today = Clock.System.todayIn(timeZone)
        val fourDaysAgo = today.minus(4, DateTimeUnit.DAY)
        if (fourDaysAgo.year == today.year) {
            val fourDaysAgoMs = fourDaysAgo.atStartOfDayIn(timeZone).toEpochMilliseconds()
            val monthAbbr = fourDaysAgo.month.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)
            val expected = "${fourDaysAgo.day} $monthAbbr"
            assertEquals(expected, formatOverdueAge(fourDaysAgoMs, timeZone))
        }
    }

    @Test
    fun testPastYearIncludesYear() {
        Date.displayConfig = ResolvedDateTimeFormat(useEuropeanDate = false)
        val pastDate = LocalDate(2023, 5, 15)
        val pastMs = pastDate.atStartOfDayIn(timeZone).toEpochMilliseconds()
        assertEquals("May 15, 2023", formatOverdueAge(pastMs, timeZone))

        Date.displayConfig = ResolvedDateTimeFormat(useEuropeanDate = true)
        assertEquals("15 May, 2023", formatOverdueAge(pastMs, timeZone))
    }
}
