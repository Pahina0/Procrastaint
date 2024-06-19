package ap.panini.procrastaint.util

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeFormatBuilder
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

object Date {
    private fun now() = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

    fun getTime(
        year: Int = now().year,
        month: Int = now().monthNumber,
        dayOfMonth: Int = now().dayOfMonth,
        hour: Int = now().hour,
        minute: Int = now().minute
    ): Long = LocalDateTime(
        year,
        month,
        dayOfMonth,
        hour,
        minute,
        0,
        0
    ).toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()

    fun Long.year() =
        Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.currentSystemDefault()).year

    fun Long.month() =
        Instant.fromEpochMilliseconds(this)
            .toLocalDateTime(TimeZone.currentSystemDefault()).monthNumber

    fun Long.dayOfMonth() =
        Instant.fromEpochMilliseconds(this)
            .toLocalDateTime(TimeZone.currentSystemDefault()).dayOfMonth

    fun Long.hour() =
        Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.currentSystemDefault()).hour

    fun Long.minute() =
        Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.currentSystemDefault()).minute

    /**
     * @param known The known values from the time to display
     * @param smart If true, displays more information based on mismatched month, day, or year
     */
    fun Long.formatMilliseconds(known: Set<Time> = emptySet(), smart: Boolean = true): String {
        val now = now()
        val time =
            Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.currentSystemDefault())

        if (known.isEmpty()) {
            val formatter = LocalDateTime.Format {
                formatDate(year = true, month = true, week = true, day = true)
                char(' ')
                formatTime(hour = true, minute = true)
            }

            return time.format(formatter)
        }

        val year = known.contains(Time.YEAR) || (smart && time.year != now.year)
        val month = known.contains(Time.MONTH) || (smart && time.month != now.month)
        val week = known.contains(Time.WEEK)
        val day = known.contains(Time.DAY) || (smart && time.dayOfMonth != now.dayOfMonth)
        val hour = known.contains(Time.HOUR)
        val minute = known.contains(Time.MINUTE)

        val formatter = LocalDateTime.Format {
            formatDate(year, month, week, day)

            val showDate = month || day || year || week
            val showTime = hour || minute

            if (showDate && showTime) {
                char(' ')
            }

            formatTime(hour, minute)
        }

        return time.format(formatter)
    }

    private fun DateTimeFormatBuilder.WithDateTime.formatDate(
        year: Boolean,
        month: Boolean,
        week: Boolean,
        day: Boolean,
    ) {
        if (day) {
            if (year) {
                date(LocalDate.Formats.ISO)
            } else {
                monthName(MonthNames.ENGLISH_ABBREVIATED)
                char(' ')
                dayOfMonth(padding = Padding.NONE)
            }
        } else if (month) {
            monthName(MonthNames.ENGLISH_ABBREVIATED)
            if (year) {
                char(' ')
                year()
            }
        } else if (year) {
            // has year only
            year()
        } else if (week) {
            monthName(MonthNames.ENGLISH_ABBREVIATED)
            char(' ')
            dayOfMonth(padding = Padding.NONE)
        }
    }

    private fun DateTimeFormatBuilder.WithDateTime.formatTime(
        hour: Boolean,
        minute: Boolean
    ) {
        if (hour || minute) {
            amPmHour(padding = Padding.NONE)

            if (minute) {
                char(':')
                minute()
            }

            amPmMarker("AM", "PM")
        }
    }
}
