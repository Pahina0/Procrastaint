package ap.panini.procrastaint.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            TaskInfo::class,
            childColumns = ["taskId"],
            parentColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        ),

        ForeignKey(
            TaskMeta::class,
            childColumns = ["metaId"],
            parentColumns = ["metaId"],
            onDelete = ForeignKey.CASCADE
        ),

        ForeignKey(
            TaskCompletion::class,
            childColumns = ["completionId"],
            parentColumns = ["completionId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class NetworkSyncItem(
    val time: Long,

    val location: SyncData,

    val action: SyncAction,

    val metaId: Long? = null,

    val taskId: Long? = null,

    val completionId: Long? = null,

    val failCount: Int = 0,

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
) {

    enum class SyncAction {
        CREATE_CALENDAR,
        CHECK,
        UNCHECK,
        CREATE_EVENT
    }

    enum class SyncData {
        GOOGLE
        // can also have like future apple, or any other calendars
    }
}
