package ap.panini.procrastaint.data.entities

import androidx.room.Embedded
import androidx.room.Relation

data class Task(
    @Embedded
    val taskInfo: TaskInfo,

    @Relation(
        parentColumn = "taskId",
        entityColumn = "taskId"
    )
    val meta: List<TaskMeta>,

    @Relation(
        parentColumn = "taskId",
        entityColumn = "taskId"
    )
    val completions: List<TaskCompletion> = emptyList()
)