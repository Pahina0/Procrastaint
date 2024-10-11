package ap.panini.procrastaint.di

import ap.panini.procrastaint.ui.MainActivityScreenModel
import ap.panini.procrastaint.ui.inbox.TaskListScreenModel
import org.koin.dsl.module

val androidModule = module {
    factory { MainActivityScreenModel(get()) }
    factory { TaskListScreenModel(get(), get()) }
}
