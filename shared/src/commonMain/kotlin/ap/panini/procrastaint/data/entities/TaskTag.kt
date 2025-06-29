package ap.panini.procrastaint.data.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [Index(value = ["name"], unique = true)])
data class TaskTag(
    @PrimaryKey
    val tagId: Long,

    val name: String, // separated by / to show layers

    val color: String
)


@Entity(primaryKeys = ["taskId", "tagId"])
data class TaskTagCrossRef(
    val taskId: Long,
    val tagId: Long,
)