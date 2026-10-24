package ap.panini.procrastaint.util

actual class SystemDateTimeProvider actual constructor() {
    actual fun is24HourFormat(): Boolean = false
    actual fun isEuropeanDateFormat(): Boolean = false
}
