package ap.panini.procrastaint.ui.settings

import androidx.lifecycle.ViewModel
import ap.panini.procrastaint.data.repositories.CalendarRepository

class SettingsViewModel(
    private val calendarRepository: CalendarRepository
) : ViewModel()
