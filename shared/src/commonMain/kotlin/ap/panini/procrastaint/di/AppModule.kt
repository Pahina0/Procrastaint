package ap.panini.procrastaint.di

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import ap.panini.procrastaint.data.AppRepository
import ap.panini.procrastaint.data.database.ProcrastaintDatabase
import ap.panini.procrastaint.data.database.typeconverter.TaskTypeConverter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.dsl.module

val appModule = module {
    val database = getRoomDatabase(getDatabaseBuilder())
    single { database.getTaskDao() }
    single { AppRepository(get()) }
}

fun getRoomDatabase(builder: RoomDatabase.Builder<ProcrastaintDatabase>): ProcrastaintDatabase {
    return builder
        .setDriver(BundledSQLiteDriver())
        .addTypeConverter(TaskTypeConverter())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}

expect fun getDatabaseBuilder(): RoomDatabase.Builder<ProcrastaintDatabase>
