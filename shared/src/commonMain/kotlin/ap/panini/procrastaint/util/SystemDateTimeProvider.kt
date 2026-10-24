package ap.panini.procrastaint.util

expect class SystemDateTimeProvider() {
    fun is24HourFormat(): Boolean
    fun isEuropeanDateFormat(): Boolean
}
