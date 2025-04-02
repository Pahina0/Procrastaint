package ap.panini.procrastaint.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import ap.panini.procrastaint.util.Time

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = TaskInfo::class,
            childColumns = ["taskId"],
            parentColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TaskMeta(
    val startTime: Long? = null,
    val endTime: Long? = null,
    val repeatTag: Time? = null,
    val repeatOften: Int? = null,

    @ColumnInfo(index = true)
    val taskId: Long = 0,

    @PrimaryKey(autoGenerate = true)
    val metaId: Long = 0

)
