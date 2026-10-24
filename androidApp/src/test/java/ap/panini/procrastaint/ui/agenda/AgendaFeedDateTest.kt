package ap.panini.procrastaint.ui.agenda

import ap.panini.procrastaint.ui.agenda.components.computeAgendaDates
import ap.panini.procrastaint.ui.agenda.components.formatAgendaHeaderDate
import ap.panini.procrastaint.ui.agenda.components.isTodayDate
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class AgendaFeedDateTest {

    private val timeZone = TimeZone.currentSystemDefault()

    private class FixedClock(private val fixedInstant: Instant) : Clock {
        override fun now(): Instant = fixedInstant
    }

    @Test
    fun testInvalidOrZeroTimestampReturnsToday() {
        assertEquals("Today", formatAgendaHeaderDate(0L))
        assertEquals("Today", formatAgendaHeaderDate(-100L))
        assertTrue(isTodayDate(0L))
        assertTrue(isTodayDate(-50L))
    }

    @Test
    fun testTodayReturnsToday() {
        val todayDate = LocalDate(2026, 10, 24)
        val todayInstant = todayDate.atStartOfDayIn(timeZone)
        val fixedClock = FixedClock(todayInstant)

        val formatted = formatAgendaHeaderDate(
            dateMillis = todayInstant.toEpochMilliseconds(),
            timeZone = timeZone,
            locale = Locale.US,
            clock = fixedClock
        )
        assertEquals("Today", formatted)
        assertTrue(
            isTodayDate(
                dateMillis = todayInstant.toEpochMilliseconds(),
                timeZone = timeZone,
                clock = fixedClock
            )
        )
    }

    @Test
    fun testTomorrowReturnsTomorrow() {
        val todayDate = LocalDate(2026, 10, 24)
        val todayInstant = todayDate.atStartOfDayIn(timeZone)
        val fixedClock = FixedClock(todayInstant)

        val tomorrowDate = todayDate.plus(1, DateTimeUnit.DAY)
        val tomorrowInstant = tomorrowDate.atStartOfDayIn(timeZone)

        val formatted = formatAgendaHeaderDate(
            dateMillis = tomorrowInstant.toEpochMilliseconds(),
            timeZone = timeZone,
            locale = Locale.US,
            clock = fixedClock
        )
        assertEquals("Tomorrow", formatted)
        assertFalse(
            isTodayDate(
                dateMillis = tomorrowInstant.toEpochMilliseconds(),
                timeZone = timeZone,
                clock = fixedClock
            )
        )
    }

    @Test
    fun testUpcomingDateInCurrentYearFormatsDayAndMonth() {
        val todayDate = LocalDate(2026, 10, 24)
        val todayInstant = todayDate.atStartOfDayIn(timeZone)
        val fixedClock = FixedClock(todayInstant)

        // Tuesday, Oct 28, 2026
        val targetDate = LocalDate(2026, 10, 28)
        val targetInstant = targetDate.atStartOfDayIn(timeZone)

        val formatted = formatAgendaHeaderDate(
            dateMillis = targetInstant.toEpochMilliseconds(),
            timeZone = timeZone,
            locale = Locale.US,
            clock = fixedClock
        )
        assertEquals("Wednesday, Oct 28", formatted)
    }

    @Test
    fun testTuesdayFormatting() {
        val todayDate = LocalDate(2026, 10, 24)
        val todayInstant = todayDate.atStartOfDayIn(timeZone)
        val fixedClock = FixedClock(todayInstant)

        // Tuesday, Oct 27, 2026
        val targetDate = LocalDate(2026, 10, 27)
        val targetInstant = targetDate.atStartOfDayIn(timeZone)

        val formatted = formatAgendaHeaderDate(
            dateMillis = targetInstant.toEpochMilliseconds(),
            timeZone = timeZone,
            locale = Locale.US,
            clock = fixedClock
        )
        assertEquals("Tuesday, Oct 27", formatted)
    }

    @Test
    fun testDateInDifferentYearIncludesYear() {
        val todayDate = LocalDate(2026, 10, 24)
        val todayInstant = todayDate.atStartOfDayIn(timeZone)
        val fixedClock = FixedClock(todayInstant)

        val nextYearDate = LocalDate(2027, 5, 12)
        val nextYearInstant = nextYearDate.atStartOfDayIn(timeZone)

        val formatted = formatAgendaHeaderDate(
            dateMillis = nextYearInstant.toEpochMilliseconds(),
            timeZone = timeZone,
            locale = Locale.US,
            clock = fixedClock
        )
        assertEquals("Wednesday, May 12, 2027", formatted)
    }

    @Test
    fun testComputeAgendaDatesWhenFilterActiveReturnsOnlyTaskAndEventDates() {
        val todayDate = LocalDate(2026, 10, 24)
        val todayInstant = todayDate.atStartOfDayIn(timeZone).toEpochMilliseconds()

        val taskDate1 = LocalDate(2026, 10, 26).atStartOfDayIn(timeZone).toEpochMilliseconds()
        val eventDate1 = LocalDate(2026, 10, 28).atStartOfDayIn(timeZone).toEpochMilliseconds()

        val result = computeAgendaDates(
            todayStart = todayInstant,
            focusedDate = todayInstant,
            taskDates = setOf(taskDate1),
            eventDates = setOf(eventDate1),
            isFilterActive = true,
            timeZone = timeZone
        )

        assertEquals(listOf(taskDate1, eventDate1), result)
    }

    @Test
    fun testComputeAgendaDatesContinuousSpanBeyondOneWeek() {
        val todayDate = LocalDate(2026, 10, 24) // Saturday
        val todayInstant = todayDate.atStartOfDayIn(timeZone).toEpochMilliseconds()

        // 8 weeks = 56 days + week offset
        val result = computeAgendaDates(
            todayStart = todayInstant,
            focusedDate = todayInstant,
            taskDates = emptySet(),
            eventDates = emptySet(),
            isFilterActive = false,
            windowWeeks = 8,
            timeZone = timeZone
        )

        // Feed starts at today
        assertEquals(todayInstant, result.first())

        // Confirms it spans multiple weeks continuously
        assertTrue(result.size >= 56, "Expected at least 56 continuous days, got ${result.size}")

        // Confirms consecutive days (86400000 ms roughly, or day increments)
        for (i in 0 until result.size - 1) {
            val d1 = Instant.fromEpochMilliseconds(result[i]).toLocalDateTime(timeZone).date
            val d2 = Instant.fromEpochMilliseconds(result[i + 1]).toLocalDateTime(timeZone).date
            assertEquals(1, d1.daysUntil(d2))
        }
    }

    @Test
    fun testComputeAgendaDatesExpandsWhenFocusedDateFarInFuture() {
        val todayDate = LocalDate(2026, 10, 24)
        val todayInstant = todayDate.atStartOfDayIn(timeZone).toEpochMilliseconds()

        val futureDate = LocalDate(2026, 12, 15) // ~7 weeks out
        val futureInstant = futureDate.atStartOfDayIn(timeZone).toEpochMilliseconds()

        val result = computeAgendaDates(
            todayStart = todayInstant,
            focusedDate = futureInstant,
            taskDates = emptySet(),
            eventDates = emptySet(),
            isFilterActive = false,
            windowWeeks = 8,
            timeZone = timeZone
        )

        assertTrue(result.contains(futureInstant), "Expected future date $futureDate to be in feed")
        val lastDate = Instant.fromEpochMilliseconds(result.last()).toLocalDateTime(timeZone).date
        assertTrue(lastDate > futureDate, "Expected feed to extend beyond focused future date")
    }
}
