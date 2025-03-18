package ap.panini.procrastaint.ui.settings.sync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ap.panini.procrastaint.data.repositories.NetworkCalendarRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn

class SyncViewModel(
    private val nsRepository: NetworkCalendarRepository
) : ViewModel() {
    val syncList = nsRepository.getNetworkSyncItems()
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), 1)
}
