package ap.panini.procrastaint.data

interface AppRepository {
    //TODO change any to an actual type
    fun addTask(Any: task)

    fun getAllTasks(): List<Any>
}