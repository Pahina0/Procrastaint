package ap.panini.procrastaint.data

import ap.panini.procrastaint.data.model.Task

interface AppRepository {
    //TODO change any to an actual type
    fun addTask(task: Task)

    fun getAllTasks(): List<Task>
}