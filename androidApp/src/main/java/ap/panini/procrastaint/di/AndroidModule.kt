package ap.panini.procrastaint.di

import ap.panini.procrastaint.ui.MainActivityScreenModel
import org.koin.dsl.module

val androidModule = module {
    factory { MainActivityScreenModel() }
}
