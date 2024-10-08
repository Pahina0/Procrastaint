package ap.panini.procrastaint.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room.RoomDatabase
import ap.panini.procrastaint.data.database.ProcrastaintDatabase

actual fun getDatabaseBuilder(): RoomDatabase.Builder<ProcrastaintDatabase> {
    TODO("Not yet implemented")
}

actual fun createDataStore(): DataStore<Preferences> {
    TODO("Not yet implemented")
}
