package ap.panini.procrastaint.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import ap.panini.procrastaint.util.Date.formatMilliseconds
import ap.panini.procrastaint.util.Time

@Entity
data class Task(
    val title: String,
    val description: String = "",

    val startTime: Long? = null,
    val endTime: Long? = null, // only needed if you repeat

    val repeatTag: Time? = null,
    val repeatOften: Int? = null,

    val completed: Long? = null,
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    fun getTimeRangeString() =
        startTime?.formatMilliseconds() + (
                endTime?.let { " to ${it.formatMilliseconds()}" }
                    ?: ""
                )
}
