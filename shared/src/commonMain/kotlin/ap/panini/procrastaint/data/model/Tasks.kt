package ap.panini.procrastaint.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Task(
    var startTime: Long,
    var endTime: Long,
    var title: String,
    var description: String = "",
    var repeatedIntervalsTime: Time? = null,
    var repeatedIntervalsWeeks: List<Int> = emptyList(), // still day of week
    var completed: Boolean = false,
    var subTasks: List<Int> = emptyList()
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}

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
