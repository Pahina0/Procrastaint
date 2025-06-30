package ap.panini.procrastaint.data.entities

import androidx.room.Embedded
import androidx.room.Junction
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
    val completions: List<TaskCompletion> = emptyList(),

    @Relation(
        parentColumn = "taskId",
        entityColumn = "tagId",
        associateBy = Junction(TaskTagCrossRef::class)
    )
    val tags: List<TaskTag> = emptyList()
) {
    fun generateOriginalText() =
        "${taskInfo.title}${taskInfo.extractedTimePhrase} ${
            tags.joinToString(" ") { it.generateTag() }
        }".trim()
}
