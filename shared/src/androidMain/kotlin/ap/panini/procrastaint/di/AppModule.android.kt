package ap.panini.procrastaint.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import androidx.room.RoomDatabase
import ap.panini.procrastaint.data.database.ProcrastaintDatabase
import org.koin.mp.KoinPlatform.getKoin

actual fun getDatabaseBuilder(): RoomDatabase.Builder<ProcrastaintDatabase> {
    val ctx: Context = getKoin().get()
    val appContext = ctx.applicationContext
    val dbFile = appContext.getDatabasePath("procrastaint.db")
    return Room.databaseBuilder<ProcrastaintDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}

actual fun createDataStore(): DataStore<Preferences> {
    val ctx: Context = getKoin().get()
    return createDataStore(producePath = { ctx.filesDir.resolve(DataStoreFileName).absolutePath })
}
