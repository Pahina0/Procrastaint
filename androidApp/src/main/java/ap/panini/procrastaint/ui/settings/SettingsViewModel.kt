package ap.panini.procrastaint.ui.settings

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ap.panini.procrastaint.data.repositories.PreferenceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val preferences: PreferenceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                preferences.getString(PreferenceRepository.GOOGLE_REFRESH_TOKEN),
                preferences.getString(PreferenceRepository.GOOGLE_CALENDAR_ID),
                preferences.getString(PreferenceRepository.GOOGLE_REFRESH_TOKEN)
            ) { token, id, refresh ->
                token.isNotBlank() && id.isNotBlank() && refresh.isNotBlank()
            }.collectLatest { loggedIn ->
                _uiState.update { it.copy(googleLoggedIn = loggedIn) }
            }
        }
    }

    fun googleLogout() {
        viewModelScope.launch {
            preferences.setString(PreferenceRepository.GOOGLE_ACCESS_TOKEN)
            preferences.setString(PreferenceRepository.GOOGLE_CALENDAR_ID)
            preferences.setString(PreferenceRepository.GOOGLE_REFRESH_TOKEN)
        }
    }

    @Immutable
    data class SettingsUiState(
        val googleLoggedIn: Boolean = false,
    )
}
