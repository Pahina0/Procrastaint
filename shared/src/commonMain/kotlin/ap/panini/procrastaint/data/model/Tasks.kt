package ap.panini.procrastaint.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Task(
    var startTimes: Set<Long>,
    var endTime: Long,

    var title: String,
    var description: String = "",

    var completed: Boolean = false,

    val repeatTag: Time? = null,
    val repeatOften: Int = 0
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
