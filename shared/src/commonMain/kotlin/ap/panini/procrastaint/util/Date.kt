@file:Suppress("TooManyFunctions")

package ap.panini.procrastaint.util

import ap.panini.procrastaint.data.models.ResolvedDateTimeFormat
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
import kotlinx.datetime.number
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.concurrent.Volatile
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

const val StartOfWeek = 2
const val EndOfWeek = 6

@OptIn(ExperimentalTime::class)
fun Instant.toLocalDate(): LocalDate {
    return this.toLocalDateTime(TimeZone.currentSystemDefault()).date
}

object Date {
    private const val ROUND_NUMBER = 100000

    @Volatile
    var displayConfig: ResolvedDateTimeFormat = ResolvedDateTimeFormat(use24Hour = false, useEuropeanDate = false)

    @OptIn(ExperimentalTime::class)
    private fun now() = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

    @OptIn(ExperimentalTime::class)
    fun getTime(
        year: Int = now().year,
        month: Int = now().month.number,
        dayOfMonth: Int = now().day,
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

    @OptIn(ExperimentalTime::class)
    fun getTodayStart(): Long =
        Clock.System
            .now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .toInstant(TimeZone.currentSystemDefault())
            .minus(now().hour.hours)
            .minus(now().minute.minutes)
            .toEpochMilliseconds()
            .let { (it / ROUND_NUMBER) * ROUND_NUMBER }

    @OptIn(ExperimentalTime::class)
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
    @OptIn(ExperimentalTime::class)
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
        val day = known.contains(Time.DAY) || (smart && time.day != now.day)
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

    private fun DateTimeFormatBuilder.WithDateTime.monthAndDay(
        monthFormat: MonthNames,
        useEuropean: Boolean
    ) {
        if (useEuropean) {
            day(padding = Padding.NONE)
            char(' ')
            monthName(monthFormat)
        } else {
            monthName(monthFormat)
            char(' ')
            day(padding = Padding.NONE)
        }
    }

    private fun DateTimeFormatBuilder.WithDateTime.formatDayWithYear() {
        if (displayConfig.useEuropeanDate) {
            day()
            char('/')
            monthNumber()
            char('/')
            year()
        } else {
            date(LocalDate.Formats.ISO)
        }
    }

    private fun DateTimeFormatBuilder.WithDateTime.formatDayWithoutYear(
        monthFormat: MonthNames,
        daysAway: Int,
        useAbbreviated: Boolean
    ) {
        if (useAbbreviated) {
            monthAndDay(monthFormat, displayConfig.useEuropeanDate)
            return
        }

        when (daysAway) {
            0 -> {
                chars("Today")
            }

            1 -> {
                chars("Tomorrow")
            }

            in StartOfWeek..EndOfWeek -> {
                dayOfWeek(
                    DayOfWeekNames.ENGLISH_FULL
                )
            }

            else -> {
                monthAndDay(monthFormat, displayConfig.useEuropeanDate)
            }
        }
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
                formatDayWithYear()
            } else {
                formatDayWithoutYear(monthFormat, daysAway, useAbbreviated)
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
            monthAndDay(monthFormat, displayConfig.useEuropeanDate)
        }
    }

    private fun DateTimeFormatBuilder.WithDateTime.formatTime(
        hour: Boolean,
        minute: Boolean
    ) {
        if (hour || minute) {
            if (displayConfig.use24Hour) {
                hour()

                if (minute) {
                    char(':')
                    minute()
                }
            } else {
                amPmHour(padding = Padding.NONE)

                if (minute) {
                    char(':')
                    minute()
                }

                amPmMarker("AM", "PM")
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
fun Long.year() =
    Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.currentSystemDefault()).year

@OptIn(ExperimentalTime::class)
fun Long.month() =
    Instant.fromEpochMilliseconds(this)
        .toLocalDateTime(TimeZone.currentSystemDefault()).month.ordinal

@OptIn(ExperimentalTime::class)
fun Long.dayOfMonth() =
    Instant.fromEpochMilliseconds(this)
        .toLocalDateTime(TimeZone.currentSystemDefault()).day

@OptIn(ExperimentalTime::class)
fun Long.hour() =
    Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.currentSystemDefault()).hour

@OptIn(ExperimentalTime::class)
fun Long.minute() =
    Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.currentSystemDefault()).minute

@OptIn(ExperimentalTime::class)
fun Long.dayOfWeek() =
    Instant.fromEpochMilliseconds(this)
        .toLocalDateTime(TimeZone.currentSystemDefault()).dayOfWeek.isoDayNumber

@OptIn(ExperimentalTime::class)
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
            day()

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

private const val PaddingLength = 2
private const val PaddingChar = '0'
private const val MaxHour = 23
private const val NoonHour = 12

fun LocalDate.formatCustom(useEuropean: Boolean = Date.displayConfig.useEuropeanDate): String {
    val monthStr = month.number.toString().padStart(PaddingLength, PaddingChar)
    val dayStr = day.toString().padStart(PaddingLength, PaddingChar)
    val yearStr = year.toString()
    return if (useEuropean) "$dayStr/$monthStr/$yearStr" else "$monthStr/$dayStr/$yearStr"
}

fun LocalDate.formatToMMDDYYYY(): String = formatCustom(useEuropean = false)

fun Int.toAmPmHour(): String {
    require(this in 0..MaxHour) { "Hour must be between 0 and 23" }
    val hour = if (this == 0) NoonHour else if (this > NoonHour) this - NoonHour else this
    val ampm = if (this < NoonHour) "am" else "pm"
    return "$hour$ampm"
}
