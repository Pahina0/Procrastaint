package ap.panini.procrastaint.di

import ap.panini.procrastaint.ui.MainActivityViewModel
import ap.panini.procrastaint.ui.calendar.CalendarViewModel
import ap.panini.procrastaint.ui.settings.SettingsViewModel
import ap.panini.procrastaint.ui.upcoming.UpcomingViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val androidModule = module {
    viewModel { MainActivityViewModel(get()) }
    viewModel { UpcomingViewModel(get()) }
    viewModel { SettingsViewModel(get()) }
    viewModel { params -> CalendarViewModel(time = params.get(), get()) }
}
