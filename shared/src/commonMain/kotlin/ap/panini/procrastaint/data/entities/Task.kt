package ap.panini.procrastaint.data.entities

data class Task(
    val taskInfo: TaskInfo,

    val meta: List<TaskMeta>,

    val completions: List<TaskCompletion> = emptyList()
)
