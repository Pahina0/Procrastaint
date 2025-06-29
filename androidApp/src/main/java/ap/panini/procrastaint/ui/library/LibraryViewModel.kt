package ap.panini.procrastaint.ui.library

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ap.panini.procrastaint.data.entities.TaskTag
import ap.panini.procrastaint.data.repositories.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LibraryViewModel(
    private val db: TaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    @Immutable
    data class LibraryUiState(
        val tag: TaskTag = TaskTag(
            "",
            "",
            TaskTag.generateRandomColor()
        ),

        val showBottomSheet: Boolean = false,
    )

    fun showBottomSheet(show: Boolean) {
        if (!show) {
            // resets state
            _uiState.update {
                it.copy(
                    tag = TaskTag(
                        "",
                        "",
                        TaskTag.generateRandomColor()
                    )
                )
            }
        }

        _uiState.update { it.copy(showBottomSheet = show) }
    }

    fun onEdit(tag: TaskTag) {
        showBottomSheet(true)
        _uiState.update { it.copy(tag = tag) }
    }

    fun onUpdateTag(tag: TaskTag) {
        _uiState.update { it.copy(tag = tag) }
    }

    fun onSave() {
        showBottomSheet(false)
        viewModelScope.launch {
            db.upsertTaskTag(uiState.value.tag)
        }
    }

}