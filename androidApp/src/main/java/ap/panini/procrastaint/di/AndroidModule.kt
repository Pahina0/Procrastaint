package ap.panini.procrastaint.di

import ap.panini.procrastaint.data.repositories.RepositoryCallback
import ap.panini.procrastaint.services.NotificationWorker
import ap.panini.procrastaint.services.SyncWorker
import ap.panini.procrastaint.ui.MainActivityViewModel
import ap.panini.procrastaint.ui.calendar.CalendarViewModel
import ap.panini.procrastaint.ui.library.LibraryViewModel
import ap.panini.procrastaint.ui.settings.SettingsViewModel
import ap.panini.procrastaint.ui.settings.auth.GoogleAuth
import ap.panini.procrastaint.ui.settings.sync.SyncViewModel
import ap.panini.procrastaint.ui.tag.TagViewModel
import ap.panini.procrastaint.ui.upcoming.UpcomingViewModel
import ap.panini.procrastaint.ui.widget.UpcomingWidgetViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.workmanager.dsl.worker
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val androidModule = module {
    viewModel { MainActivityViewModel(get(), get()) }
    viewModel { UpcomingViewModel(get()) }
    viewModel { SettingsViewModel(get()) }
    viewModel { SyncViewModel(get()) }
    viewModel { CalendarViewModel(get()) }
    viewModel { LibraryViewModel(get()) }
    viewModel { TagViewModel(it[0], get()) }

    single { UpcomingWidgetViewModel(get()) }

    single { GoogleAuth(androidApplication()) }

    single<RepositoryCallback> { WidgetUpdaterCallback(androidApplication()) }

    worker { SyncWorker(get(), get(), it.get()) }
    worker { NotificationWorker(get(), get(), it.get()) }
}
