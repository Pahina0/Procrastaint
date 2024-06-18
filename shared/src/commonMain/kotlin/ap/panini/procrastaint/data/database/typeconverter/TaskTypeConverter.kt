package ap.panini.procrastaint.data.database.typeconverter

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter

@ProvidedTypeConverter
class TaskTypeConverter {

    @TypeConverter
    fun timesToString(value: Set<Long>) = value.joinToString("|")

    @TypeConverter
    fun stringToLong(value: String) =
        value.split("|").map { it.toLong() }.toSet()
}
