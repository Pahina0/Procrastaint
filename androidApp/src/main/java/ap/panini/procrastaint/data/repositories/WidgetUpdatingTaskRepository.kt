package ap.panini.procrastaint.data.repositories

import ap.panini.procrastaint.data.database.dao.TaskDao
import ap.panini.procrastaint.data.entities.Task
import ap.panini.procrastaint.data.entities.TaskCompletion
import ap.panini.procrastaint.di.WidgetUpdater
import ap.panini.procrastaint.notifications.NotificationManager

class WidgetUpdatingTaskRepository(
    taskDao: TaskDao,
    calendar: NetworkCalendarRepository,
    notificationManager: NotificationManager,
    private val widgetUpdater: WidgetUpdater
) : TaskRepository(taskDao, calendar, notificationManager) {

    override suspend fun insertTask(tasks: Task): Boolean {
        val result = super.insertTask(tasks)
        widgetUpdater.updateWidget()
        return result
    }

    override suspend fun editTask(newTask: Task) {
        super.editTask(newTask)
        widgetUpdater.updateWidget()
    }

    override suspend fun editTask(oldTask: Task, newTask: Task) {
        super.editTask(oldTask, newTask)
        widgetUpdater.updateWidget()
    }

    override suspend fun deleteTask(task: Task) {
        super.deleteTask(task)
        widgetUpdater.updateWidget()
    }

    override suspend fun deleteTask(taskId: Long) {
        super.deleteTask(taskId)
        widgetUpdater.updateWidget()
    }

    override suspend fun addCompletion(taskCompletion: TaskCompletion) {
        super.addCompletion(taskCompletion)
        widgetUpdater.updateWidget()
    }

    override suspend fun removeCompletion(taskCompletion: TaskCompletion) {
        super.removeCompletion(taskCompletion)
        widgetUpdater.updateWidget()
    }

    override suspend fun deleteTagWithTasks(tag: ap.panini.procrastaint.data.entities.TaskTag) {
        super.deleteTagWithTasks(tag)
        widgetUpdater.updateWidget()
    }
}
