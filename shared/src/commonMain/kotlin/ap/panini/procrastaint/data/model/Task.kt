package ap.panini.procrastaint.data.model

enum class Time {
    SECOND,
    MINUTE,
    HOUR,
    DAY,
    WEEK,
    MONTH,
    YEAR,
}

enum class DayOfWeek {
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY,
    SUNDAY
}

data class Task(
    var startTime: Long,
    var endTime: Long,
    var title: String,
    var description: String = "",
    var repeatedIntervalsTime: Time? = null,
    var repeatedIntervalsWeeks: List<DayOfWeek> = emptyList(),
    var completed: Boolean = false,
    var subTasks: List<Task> = emptyList()
)
