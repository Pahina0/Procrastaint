package ap.panini.procrastaint.di

import ap.panini.procrastaint.data.AppRepository
import ap.panini.procrastaint.data.AppRepositoryImpl
import org.koin.dsl.module

fun appModule() = module {
    single<AppRepository> { AppRepositoryImpl() }
}