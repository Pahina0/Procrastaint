package ap.panini.procrastaint.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import ap.panini.procrastaint.data.database.dao.TaskDao
import ap.panini.procrastaint.data.model.Task

@Database(entities = [Task::class], version = 1)
abstract class ProcrastaintDatabase : RoomDatabase() {
    abstract fun getTaskDao(): TaskDao
}
