package ap.panini.procrastaint.util

import ap.panini.kwhen.TimeUnit
import kotlinx.datetime.DateTimePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus

enum class Time {
    SECOND, MINUTE, HOUR, DAY, WEEK, MONTH, YEAR;

    companion object {
        fun TimeUnit.toTime() = when (this) {
            TimeUnit.SECOND -> SECOND
            TimeUnit.MINUTE -> MINUTE
            TimeUnit.HOUR -> HOUR
            TimeUnit.DAY -> DAY
            TimeUnit.WEEK -> WEEK
            TimeUnit.MONTH -> MONTH
            TimeUnit.YEAR -> YEAR
        }
    }

    override fun toString(): String = when (this) {
        SECOND -> "Second"
        MINUTE -> "Minute"
        HOUR -> "Hour"
        DAY -> "Day"
        WEEK -> "Week"
        MONTH -> "Month"
        YEAR -> "Year"
    }

    fun toTimeRepeatString(): String = when (this) {
        SECOND -> "SECONDLY"
        MINUTE -> "MINUTELY"
        HOUR -> "HOURLY"
        DAY -> "DAILY"
        WEEK -> "WEEKLY"
        MONTH -> "MONTHLY"
        YEAR -> "YEARLY"
    }

    fun incrementBy(curTime: Instant, amount: Int) = when (this) {
        SECOND -> curTime.plus(
            DateTimePeriod(seconds = amount),
            TimeZone.currentSystemDefault()
        )

        MINUTE -> curTime.plus(
            DateTimePeriod(minutes = amount),
            TimeZone.currentSystemDefault()
        )

        HOUR -> curTime.plus(
            DateTimePeriod(hours = amount),
            TimeZone.currentSystemDefault()
        )

        DAY -> curTime.plus(
            DateTimePeriod(days = amount),
            TimeZone.currentSystemDefault()
        )

        WEEK -> curTime.plus(
            DateTimePeriod(days = 7 * amount),
            TimeZone.currentSystemDefault()
        )

        MONTH -> curTime.plus(
            DateTimePeriod(months = amount),
            TimeZone.currentSystemDefault()
        )

        YEAR -> curTime.plus(
            DateTimePeriod(years = amount),
            TimeZone.currentSystemDefault()
        )
    }
}
