package ap.panini.procrastaint.di

import ap.panini.procrastaint.ui.MainActivityScreenModel
import ap.panini.procrastaint.ui.upcoming.UpcomingScreenModel
import org.koin.dsl.module

val androidModule = module {
    factory { MainActivityScreenModel(get()) }
    factory { UpcomingScreenModel(get()) }
}
