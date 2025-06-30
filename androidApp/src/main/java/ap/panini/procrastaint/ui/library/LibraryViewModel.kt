package ap.panini.procrastaint.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ap.panini.procrastaint.data.entities.TaskTag
import ap.panini.procrastaint.data.repositories.TaskRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class LibraryViewModel(
    private val db: TaskRepository
) : ViewModel() {

    val tags = db.getTags()
        .stateIn(
            scope = viewModelScope,
            initialValue = emptyList(), // set initial value here
            started = SharingStarted.WhileSubscribed(5000)
        )


    fun isValidTag(tag: TaskTag): Boolean {
        return runBlocking {
            val found = db.getTagOrNull(tag.title)
            if (found == null) return@runBlocking true

            return@runBlocking found.tagId == tag.tagId
        }
    }

    fun onSave(tag: TaskTag) {
        viewModelScope.launch {
            db.upsertTaskTag(tag)
        }
    }

}