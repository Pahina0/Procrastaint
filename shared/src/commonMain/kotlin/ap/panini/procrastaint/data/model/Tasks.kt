package ap.panini.procrastaint.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import ap.panini.kwhen.TimeUnit

@Entity
data class Task(
    var startTimes: Set<Long>,
    var endTime: Long?, // only needed if you repeat

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
    YEAR;

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
}
