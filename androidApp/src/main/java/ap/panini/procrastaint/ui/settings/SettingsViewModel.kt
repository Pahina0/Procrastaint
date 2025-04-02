package ap.panini.procrastaint.ui.settings

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ap.panini.procrastaint.data.repositories.calendars.GoogleCalendarRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val googleCalendarRepository: GoogleCalendarRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            googleCalendarRepository.isLoggedIn().collect { loggedIn ->
                _uiState.update { it.copy(googleLoggedIn = loggedIn) }
            }
        }
    }

    fun googleLogout() {
        viewModelScope.launch {
            googleCalendarRepository.logout()
        }
    }

    @Immutable
    data class SettingsUiState(
        val googleLoggedIn: Boolean = false,
    )
}
