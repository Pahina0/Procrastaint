package ap.panini.procrastaint.ui.agenda

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

private const val DaysInWeek = 7

@OptIn(ExperimentalTime::class)
class CalendarHeaderDateTest {

    private val timeZone = TimeZone.currentSystemDefault()

    @Test
    fun testSundayStartCalculation() {
        // Sunday: Oct 18, 2026
        val sunday = LocalDate(2026, 10, 18)
        assertEquals(DayOfWeek.SUNDAY, sunday.dayOfWeek)
        val sunOffset = sunday.dayOfWeek.isoDayNumber % DaysInWeek
        assertEquals(0, sunOffset)
        val sunStart = sunday.minus(sunOffset.toLong(), DateTimeUnit.DAY)
        assertEquals(sunday, sunStart)

        // Wednesday: Oct 21, 2026
        val wednesday = LocalDate(2026, 10, 21)
        assertEquals(DayOfWeek.WEDNESDAY, wednesday.dayOfWeek)
        val wedOffset = wednesday.dayOfWeek.isoDayNumber % DaysInWeek
        assertEquals(3, wedOffset)
        val wedStart = wednesday.minus(wedOffset.toLong(), DateTimeUnit.DAY)
        assertEquals(sunday, wedStart)

        // Saturday: Oct 24, 2026
        val saturday = LocalDate(2026, 10, 24)
        assertEquals(DayOfWeek.SATURDAY, saturday.dayOfWeek)
        val satOffset = saturday.dayOfWeek.isoDayNumber % DaysInWeek
        assertEquals(6, satOffset)
        val satStart = saturday.minus(satOffset.toLong(), DateTimeUnit.DAY)
        assertEquals(sunday, satStart)
    }

    @Test
    fun testMonthGridWeekCountForOctober2026() {
        // Oct 1, 2026 is Thursday (isoDay = 4, offset = 4, grid starts Sun Sep 27)
        val firstDay = LocalDate(2026, 10, 1)
        val startDayOfWeek = firstDay.dayOfWeek.isoDayNumber % DaysInWeek
        val gridStart = firstDay.minus(startDayOfWeek.toLong(), DateTimeUnit.DAY)
        assertEquals(LocalDate(2026, 9, 27), gridStart)

        val nextMonth = firstDay.plus(1, DateTimeUnit.MONTH)
        val lastDay = nextMonth.minus(1, DateTimeUnit.DAY)
        assertEquals(LocalDate(2026, 10, 31), lastDay)

        val totalDays = gridStart.daysUntil(lastDay) + 1
        val weeks = (totalDays + DaysInWeek - 1) / DaysInWeek
        assertEquals(5, weeks)
        assertEquals(35, weeks * DaysInWeek)
    }

    @Test
    fun testMonthGridWeekCountForAugust2026() {
        // Aug 1, 2026 is Saturday (isoDay = 6, offset = 6, grid starts Sun Jul 26)
        val firstDay = LocalDate(2026, 8, 1)
        val startDayOfWeek = firstDay.dayOfWeek.isoDayNumber % DaysInWeek
        val gridStart = firstDay.minus(startDayOfWeek.toLong(), DateTimeUnit.DAY)
        assertEquals(LocalDate(2026, 7, 26), gridStart)

        val nextMonth = firstDay.plus(1, DateTimeUnit.MONTH)
        val lastDay = nextMonth.minus(1, DateTimeUnit.DAY)
        assertEquals(LocalDate(2026, 8, 31), lastDay)

        val totalDays = gridStart.daysUntil(lastDay) + 1
        val weeks = (totalDays + DaysInWeek - 1) / DaysInWeek
        assertEquals(6, weeks)
        assertEquals(42, weeks * DaysInWeek)
    }

    @Test
    fun testStartOfDayConsistency() {
        val testDate = LocalDate(2026, 10, 24)
        val cellStartOfDay = LocalDateTime(
            year = testDate.year,
            month = testDate.month,
            day = testDate.day,
            hour = 0,
            minute = 0,
            second = 0,
            nanosecond = 0
        ).toInstant(timeZone).toEpochMilliseconds()

        val computedStartOfDay = getStartOfDay(cellStartOfDay)
        assertEquals(cellStartOfDay, computedStartOfDay)

        // Midday timestamp should resolve to same start of day
        val middayTimestamp = LocalDateTime(
            year = testDate.year,
            month = testDate.month,
            day = testDate.day,
            hour = 14,
            minute = 30,
            second = 0,
            nanosecond = 0
        ).toInstant(timeZone).toEpochMilliseconds()
        assertEquals(cellStartOfDay, getStartOfDay(middayTimestamp))
    }

    @Test
    fun testMonthYearFormatting() {
        val date = LocalDate(2026, 10, 24)
        val timestamp = LocalDateTime(date.year, date.month, date.day, 10, 0, 0, 0)
            .toInstant(timeZone).toEpochMilliseconds()

        val ldt = Instant.fromEpochMilliseconds(timestamp).toLocalDateTime(timeZone)
        val monthName = ldt.month.name.lowercase().replaceFirstChar { it.titlecase() }
        val formatted = "$monthName ${ldt.year}"
        assertEquals("October 2026", formatted)
    }
}
