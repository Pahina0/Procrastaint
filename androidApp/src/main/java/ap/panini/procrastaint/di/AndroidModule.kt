package ap.panini.procrastaint.di

import ap.panini.procrastaint.ui.MainActivityViewModel
import ap.panini.procrastaint.ui.inbox.UpcomingViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val androidModule = module {
    viewModel { MainActivityViewModel(get()) }
    viewModel { UpcomingViewModel(get()) }
}
