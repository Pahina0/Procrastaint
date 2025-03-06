package ap.panini.procrastaint.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ap.panini.procrastaint.data.repositories.CalendarRepository
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val calendarRepository: CalendarRepository
) : ViewModel() {

    fun googleLogin() {
        viewModelScope.launch {
            println(
                calendarRepository.createCalendar()
            )
        }
    }
}