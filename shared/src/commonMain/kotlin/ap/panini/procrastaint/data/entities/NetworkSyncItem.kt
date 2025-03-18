package ap.panini.procrastaint.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class NetworkSyncItem(
    val time: Long,

    val location: SyncData,

    val action: SyncAction,

    val metaId: Long? = null,

    val taskId: Long? = null,

    val completionId: Long? = null,

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