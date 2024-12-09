package ap.panini.procrastaint.ui.calendar

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

// Main data class representing a Task
data class Task(
    val id: String,
    val title: String,
    val description: String? = null,
    val dueDate: LocalDateTime? = null,
    val startDate: LocalDateTime? = null,
    val completed: LocalDateTime? = null,
    val tags: List<String> = emptyList(),
    val priority: Priority = Priority.MEDIUM
) {
    // Enum for task priority
    enum class Priority {
        LOW, MEDIUM, HIGH
    }

    // Helper method to generate a human-readable time range string
    fun getTimeRangeString(): String {
        return buildString {
            startDate?.let { start ->
                append(start.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)))

                dueDate?.let { end ->
                    append(" - ")
                    append(end.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)))
                }
            } ?: dueDate?.let {
                append(it.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)))
            }
        }
    }

    // Determine if the task is considered old
    fun isOld(): Boolean {
        return dueDate?.isBefore(LocalDateTime.now()) ?: false
    }
}

// Companion object for Task with utility methods
object TaskUtils {
    // Create a new task with default values
    fun createTask(
        title: String,
        description: String? = null,
        dueDate: LocalDateTime? = null,
        startDate: LocalDateTime? = null,
        priority: Task.Priority = Task.Priority.MEDIUM
    ): Task {
        return Task(
            id = generateUniqueId(),
            title = title,
            description = description,
            dueDate = dueDate,
            startDate = startDate,
            priority = priority
        )
    }

    // Generate a unique ID (simple implementation, replace with more robust method in production)
    private fun generateUniqueId(): String {
        return System.currentTimeMillis().toString() +
                Math.random().toString().substring(2, 5)
    }
}

// Repository for managing task-related preferences
object PreferenceRepository {
    // Preference keys for filtering tasks
    const val SHOW_COMPLETE = "show_complete"
    const val SHOW_INCOMPLETE = "show_incomplete"
    const val SHOW_OLD = "show_old"

    // Store and retrieve preferences (simplified, replace with actual persistent storage in production)
    private val preferences = mutableMapOf(
        SHOW_COMPLETE to true,
        SHOW_INCOMPLETE to true,
        SHOW_OLD to false
    )

    fun getPreference(key: String, defaultValue: Boolean = true): Boolean {
        return preferences[key] ?: defaultValue
    }

    fun setPreference(key: String, value: Boolean) {
        preferences[key] = value
    }
}

// Screen model to manage task list state and operations
class CalendarViewModelModel {
    // Represents the current state of the task list
    data class State(
        val tasks: List<Task> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null
    )

    // Configuration options for displaying tasks
    data class Options(
        val showComplete: Boolean = PreferenceRepository.getPreference(PreferenceRepository.SHOW_COMPLETE),
        val showIncomplete: Boolean = PreferenceRepository.getPreference(PreferenceRepository.SHOW_INCOMPLETE),
        val showOld: Boolean = PreferenceRepository.getPreference(PreferenceRepository.SHOW_OLD)
    )

    // Mutable state for the screen
    private var _state by mutableStateOf(State())
    val state: State
        get() = _state

    private var _options by mutableStateOf(Options())
    val options: Options
        get() = _options

    // Method to load tasks (simplified, replace with actual data source in production)
    fun loadTasks() {
        _state = _state.copy(isLoading = true)
        try {
            // Simulate task loading - replace with actual data fetching
            val tasks = listOf(
                TaskUtils.createTask(
                    title = "Team Meeting",
                    description = "Weekly team sync",
                    dueDate = LocalDateTime.now().plusDays(1),
                    priority = Task.Priority.HIGH
                ),
                TaskUtils.createTask(
                    title = "Project Deadline",
                    description = "Complete quarterly report",
                    dueDate = LocalDateTime.now().plusWeeks(2),
                    priority = Task.Priority.HIGH
                )
            )
            _state = _state.copy(
                tasks = applyFilters(tasks),
                isLoading = false
            )
        } catch (e: Exception) {
            _state = _state.copy(
                error = "Failed to load tasks",
                isLoading = false
            )
        }
    }

    // Complete a task
    fun completeTask(task: Task) {
        val updatedTasks = _state.tasks.map {
            if (it.id == task.id) {
                it.copy(completed = LocalDateTime.now())
            } else it
        }
        _state = _state.copy(tasks = applyFilters(updatedTasks))
    }

    // Change filter options
    fun changeFilterOption(key: String, value: Boolean) {
        when (key) {
            PreferenceRepository.SHOW_COMPLETE -> {
                PreferenceRepository.setPreference(key, value)
                _options = _options.copy(showComplete = value)
            }
            PreferenceRepository.SHOW_INCOMPLETE -> {
                PreferenceRepository.setPreference(key, value)
                _options = _options.copy(showIncomplete = value)
            }
            PreferenceRepository.SHOW_OLD -> {
                PreferenceRepository.setPreference(key, value)
                _options = _options.copy(showOld = value)
            }
        }
        // Reapply filters after changing options
        _state = _state.copy(tasks = applyFilters(_state.tasks))
    }

    // Apply filters based on current options
    private fun applyFilters(tasks: List<Task>): List<Task> {
        return tasks.filter { task ->
            val isCompleted = task.completed != null
            val isOld = task.isOld()

            (options.showComplete && isCompleted) ||
                    (options.showIncomplete && !isCompleted) ||
                    (options.showOld && isOld)
        }
    }
}