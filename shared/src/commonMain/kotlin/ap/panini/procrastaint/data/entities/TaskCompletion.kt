package ap.panini.procrastaint.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = TaskInfo::class,
            childColumns = ["taskId"],
            parentColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TaskMeta::class,
            childColumns = ["metaId"],
            parentColumns = ["metaId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TaskCompletion(
    val completionTime: Long,

    val forTime: Long,

    @ColumnInfo(index = true)
    val taskId: Long = 0,

    @ColumnInfo(index = true)
    val metaId: Long = 0,

    @PrimaryKey(autoGenerate = true)
    val completionId: Long = 0,
)
