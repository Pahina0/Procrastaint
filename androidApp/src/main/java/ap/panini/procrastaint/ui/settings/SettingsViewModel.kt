package ap.panini.procrastaint.ui.settings

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ap.panini.procrastaint.data.repositories.DeviceCalendarRepository
import ap.panini.procrastaint.data.repositories.PreferenceRepository
import ap.panini.procrastaint.data.repositories.calendars.GoogleCalendarRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val googleCalendarRepository: GoogleCalendarRepository,
    private val deviceCalendarRepository: DeviceCalendarRepository? = null,
    private val preferenceRepository: PreferenceRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            hasDeviceCalendarPermission = deviceCalendarRepository?.hasPermission() == true
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            googleCalendarRepository.isLoggedIn().collect { loggedIn ->
                _uiState.update { it.copy(googleLoggedIn = loggedIn) }
            }
        }
        preferenceRepository?.let { prefRepo ->
            viewModelScope.launch {
                prefRepo.getShowDeviceCalendar().collect { show ->
                    _uiState.update { it.copy(showDeviceCalendar = show) }
                }
            }
            viewModelScope.launch {
                prefRepo.getShowGoogleCalendar().collect { show ->
                    _uiState.update { it.copy(showGoogleCalendar = show) }
                }
            }
        }
    }

    fun updateDeviceCalendarPermission() {
        val hasPermission = deviceCalendarRepository?.hasPermission() == true
        _uiState.update { it.copy(hasDeviceCalendarPermission = hasPermission) }
    }

    fun setShowDeviceCalendar(show: Boolean) {
        viewModelScope.launch {
            preferenceRepository?.setShowDeviceCalendar(show)
        }
    }

    fun setShowGoogleCalendar(show: Boolean) {
        viewModelScope.launch {
            preferenceRepository?.setShowGoogleCalendar(show)
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
        val showGoogleCalendar: Boolean = true,
        val hasDeviceCalendarPermission: Boolean = false,
        val showDeviceCalendar: Boolean = true
    )
}
