package ap.panini.procrastaint.ui.widget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ap.panini.procrastaint.data.entities.TaskSingle
import ap.panini.procrastaint.data.repositories.TaskRepository
import ap.panini.procrastaint.util.Date
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn

class AddButtonWidgetViewModel(
    taskRepository: TaskRepository
) : ViewModel() {

    val upcomingTasks: StateFlow<List<TaskSingle>> =
        taskRepository.getTasks(from = Date.getTodayStart(), to = Date.getTodayStart() + 7 * 24 * 60 * 60 * 1000L)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
}
