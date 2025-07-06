package ap.panini.procrastaint.util

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.DateTimeFormatBuilder
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

object Date {
    private const val ROUND_NUMBER = 100000
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

    fun getTodayStart(): Long =
        Clock.System
            .now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .toInstant(TimeZone.currentSystemDefault())
            .minus(now().hour.hours)
            .minus(now().minute.minutes)
            .toEpochMilliseconds()
            .let { (it / ROUND_NUMBER) * ROUND_NUMBER }

    fun Long.toDayOfWeek(): String {
        val time =
            Instant.fromEpochMilliseconds(this)
                .toLocalDateTime(TimeZone.currentSystemDefault())
        return time.format(
            LocalDateTime.Format {
                dayOfWeek(DayOfWeekNames.ENGLISH_ABBREVIATED)
            }
        )
    }

    /**
     * @param known The known values from the time to display
     * @param smart If true, displays more information based on mismatched month, day, or year
     * @param useAbbreviated whether or not to use abbreviated text for month
     */
    fun Long.formatMilliseconds(
        known: Set<Time> = emptySet(),
        smart: Boolean = true,
        useAbbreviated: Boolean = false
    ): String {
        val now = now()
        val time =
            Instant.fromEpochMilliseconds(this)
                .toLocalDateTime(TimeZone.currentSystemDefault())

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

        val daysAway = now.date.daysUntil(time.date)

        val formatter = LocalDateTime.Format {
            formatDate(year, month, week, day, daysAway, useAbbreviated)

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
        daysAway: Int = 0,
        useAbbreviated: Boolean = false
    ) {

        val monthFormat =
            with(MonthNames) { if (useAbbreviated) ENGLISH_ABBREVIATED else ENGLISH_FULL }

        if (day) {
            if (year) {
                date(LocalDate.Formats.ISO)
            } else {
                if (useAbbreviated) {
                    monthName(monthFormat)
                    char(' ')
                    dayOfMonth(padding = Padding.NONE)
                    return
                }

                when (daysAway) {
                    0 -> {
                        chars("Today")
                    }

                    1 -> {
                        chars("Tomorrow")
                    }

                    in 2..6 -> {
                        dayOfWeek(
                            DayOfWeekNames.ENGLISH_FULL
                        )
                    }

                    else -> {
                        monthName(monthFormat)
                        char(' ')
                        dayOfMonth(padding = Padding.NONE)
                    }
                }
            }
        } else if (month) {
            monthName(monthFormat)
            if (year) {
                char(' ')
                year()
            }
        } else if (year) {
            // has year only
            year()
        } else if (week) {
            monthName(monthFormat)
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

fun Long.dayOfWeek() =
    Instant.fromEpochMilliseconds(this)
        .toLocalDateTime(TimeZone.currentSystemDefault()).dayOfWeek.isoDayNumber

fun Long.toRFC3339(includeFiller: Boolean = true) =
    Instant.fromEpochMilliseconds(this).format(
        DateTimeComponents.Format {
            year()

            if (includeFiller) {
                char('-')
            }
            monthNumber()
            if (includeFiller) {
                char('-')
            }
            dayOfMonth()

            char('T')
            hour()
            if (includeFiller) {
                char(':')
            }
            minute()
            if (includeFiller) {
                char(':')
            }
            second()
            char('Z')
        },
    )
