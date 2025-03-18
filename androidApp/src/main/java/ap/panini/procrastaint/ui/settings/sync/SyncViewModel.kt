package ap.panini.procrastaint.ui.settings.sync

import android.content.ContentResolver
import android.os.Bundle
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ap.panini.procrastaint.data.entities.NetworkSyncItem
import ap.panini.procrastaint.data.repositories.NetworkCalendarRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SyncViewModel(
    private val nsRepository: NetworkCalendarRepository
) : ViewModel() {


    private val _uiState = MutableStateFlow(SyncUiState())
    val uiState: StateFlow<SyncUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            nsRepository.getNetworkSyncItems().collectLatest { latest ->
                _uiState.update { it.copy(syncList = latest) }
            }
        }
    }


    fun sync() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }

            // TODO: do sync here

            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    @Immutable
    data class SyncUiState(
        val isRefreshing: Boolean = false,
        val syncList: List<NetworkSyncItem> = emptyList()
    )
}
