package ap.panini.procrastaint.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import ap.panini.procrastaint.data.database.dao.TaskDao
import ap.panini.procrastaint.data.database.typeconverter.TaskTypeConverter
import ap.panini.procrastaint.data.model.Task


@Database(entities = [Task::class], version = 1)
@TypeConverters(TaskTypeConverter::class)
abstract class ProcrastaintDatabase : RoomDatabase() {
    abstract fun getTaskDao(): TaskDao
}