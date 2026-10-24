package ap.panini.procrastaint.data.models

enum class TimeFormatMode {
    SYSTEM,
    H12,
    H24
}

enum class DateFormatMode {
    SYSTEM,
    MDY,
    DMY
}

data class ResolvedDateTimeFormat(
    val use24Hour: Boolean,
    val useEuropeanDate: Boolean
)
