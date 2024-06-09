package ap.panini.procrastaint.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import ap.panini.procrastaint.data.database.ProcrastaintDatabase
import ap.panini.procrastaint.data.database.typeconverter.TaskTypeConverter
import kotlinx.coroutines.Dispatchers
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
