package ap.panini.procrastaint.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import ap.panini.procrastaint.util.Date.formatMilliseconds
import ap.panini.procrastaint.util.Time

@Entity
data class Task(
    var title: String,
    var description: String?,

    var startTimes: Long? = null,
    var endTime: Long? = null, // only needed if you repeat

    var repeatTag: Time? = null,
    var repeatOften: Int? = null,

    var completed: Long? = null,
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    fun getTimeRangeString() =
        startTimes?.formatMilliseconds() + (
            endTime?.let { " to ${it.formatMilliseconds()}" }
                ?: ""
            )
}
