package ap.panini.procrastaint.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TaskInfo(
    val title: String,
    val description: String = "",

    @PrimaryKey(autoGenerate = true)
    var taskId: Long = 0,
) {

//    fun getTimeRangeString() =
//        startTime?.formatMilliseconds() + (
//                endTime?.let { " to ${it.formatMilliseconds()}" }
//                    ?: ""
//                )
}
