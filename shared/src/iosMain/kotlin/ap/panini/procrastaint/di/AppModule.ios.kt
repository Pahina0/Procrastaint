package ap.panini.procrastaint.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import androidx.room.RoomDatabase
import ap.panini.procrastaint.data.database.ProcrastaintDatabase
import platform.Foundation.NSHomeDirectory
import org.koin.core.context.startKoin

fun initialize() {
    startKoin {
        modules(appModule)
    }
}

actual fun getDatabaseBuilder(): RoomDatabase.Builder<ProcrastaintDatabase> {
    val dbFilePath = NSHomeDirectory() + "/procrastaint.db"
    return Room.databaseBuilder<ProcrastaintDatabase>(
        name = dbFilePath,
    )
}

actual fun createDataStore(): DataStore<Preferences> {
    return createDataStore(producePath = { NSHomeDirectory() + "/" + DataStoreFileName })
}
