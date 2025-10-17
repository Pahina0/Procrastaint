package ap.panini.procrastaint.di

import ap.panini.procrastaint.data.database.ProcrastaintDatabase
import ap.panini.procrastaint.data.repositories.TaskRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object DependencyProvider : KoinComponent {
    val database: ProcrastaintDatabase by inject()
    val taskRepository: TaskRepository by inject()
}
