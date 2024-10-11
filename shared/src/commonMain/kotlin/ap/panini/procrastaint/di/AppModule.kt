package ap.panini.procrastaint.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import ap.panini.procrastaint.data.database.ProcrastaintDatabase
import ap.panini.procrastaint.data.repositories.AppRepository
import ap.panini.procrastaint.data.repositories.PreferenceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import okio.Path.Companion.toPath
import org.koin.dsl.module

val appModule = module {
    val database = getRoomDatabase(getDatabaseBuilder())
    single { database.getTaskDao() }
    single { createDataStore() }
    single { AppRepository(get()) }
    single { PreferenceRepository(get()) }
}

fun getRoomDatabase(builder: RoomDatabase.Builder<ProcrastaintDatabase>): ProcrastaintDatabase {
    return builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}

expect fun getDatabaseBuilder(): RoomDatabase.Builder<ProcrastaintDatabase>

// https://developer.android.com/kotlin/multiplatform/datastore
fun createDataStore(producePath: () -> String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(
        produceFile = { producePath().toPath() }
    )

internal const val DataStoreFileName = "procrastaint.preferences_pb"

expect fun createDataStore(): DataStore<Preferences>
