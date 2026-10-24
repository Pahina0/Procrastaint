package ap.panini.procrastaint.util

import ap.panini.procrastaint.data.models.ResolvedDateTimeFormat
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DateFormattingTest {
    @Test
    fun test12HourVs24HourTimeFormatting() {
        // 2:30 PM (14:30) UTC
        val epochMs = 1775313000000L
        Date.displayConfig = ResolvedDateTimeFormat(use24Hour = false, useEuropeanDate = false)
        val formatted12 = with(Date) { epochMs.formatMilliseconds(setOf(Time.HOUR, Time.MINUTE), smart = false) }
        assertTrue(formatted12.contains("PM") || formatted12.contains("AM"))

        Date.displayConfig = ResolvedDateTimeFormat(use24Hour = true, useEuropeanDate = false)
        val formatted24 = with(Date) { epochMs.formatMilliseconds(setOf(Time.HOUR, Time.MINUTE), smart = false) }
        assertFalse(formatted24.contains("PM") || formatted24.contains("AM"))
        assertTrue(formatted24.matches(Regex("\\d{2}:\\d{2}")), "Expected 24-hour HH:mm, got $formatted24")
    }

    @Test
    fun testUSVsEuropeanDateReordering() {
        val epochMs = 1775313000000L
        Date.displayConfig = ResolvedDateTimeFormat(use24Hour = false, useEuropeanDate = false)
        val usDate = with(Date) {
            epochMs.formatMilliseconds(setOf(Time.MONTH, Time.DAY), smart = false, useAbbreviated = true)
        }
        assertTrue(usDate.matches(Regex("[A-Za-z]+ \\d+")), "Expected Month Day in US mode, got $usDate")

        Date.displayConfig = ResolvedDateTimeFormat(use24Hour = false, useEuropeanDate = true)
        val euDate = with(Date) {
            epochMs.formatMilliseconds(setOf(Time.MONTH, Time.DAY), smart = false, useAbbreviated = true)
        }
        assertTrue(euDate.matches(Regex("\\d+ [A-Za-z]+")), "Expected Day Month in EU mode, got $euDate")
    }

    @Test
    fun testLocalDateCustomFormat() {
        val date = LocalDate(2026, 4, 5)
        assertEquals("04/05/2026", date.formatCustom(useEuropean = false))
        assertEquals("05/04/2026", date.formatCustom(useEuropean = true))

        Date.displayConfig = ResolvedDateTimeFormat(use24Hour = false, useEuropeanDate = false)
        assertEquals("04/05/2026", date.formatCustom())

        Date.displayConfig = ResolvedDateTimeFormat(use24Hour = false, useEuropeanDate = true)
        assertEquals("05/04/2026", date.formatCustom())
    }

    @Test
    fun testFullDateFormattingWithEuropeanMode() {
        val epochMs = 1775313000000L
        Date.displayConfig = ResolvedDateTimeFormat(use24Hour = true, useEuropeanDate = true)
        val fullDate = with(Date) { epochMs.formatMilliseconds(emptySet()) }
        // Full date should start with DD/MM/YYYY
        assertTrue(
            fullDate.matches(Regex("\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}")),
            "Expected DD/MM/YYYY HH:mm in EU mode, got $fullDate"
        )
    }
}
