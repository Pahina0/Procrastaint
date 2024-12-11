package ap.panini.procrastaint.`data`.database.dao

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import ap.panini.procrastaint.`data`.model.Task
import ap.panini.procrastaint.util.Time
import javax.`annotation`.processing.Generated
import kotlin.IllegalArgumentException
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class TaskDao_Impl(
  __db: RoomDatabase,
) : TaskDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfTask: EntityInsertAdapter<Task>

  private val __updateAdapterOfTask: EntityDeleteOrUpdateAdapter<Task>
  init {
    this.__db = __db
    this.__insertAdapterOfTask = object : EntityInsertAdapter<Task>() {
      protected override fun createQuery(): String =
          "INSERT OR ABORT INTO `Task` (`title`,`description`,`startTime`,`endTime`,`repeatTag`,`repeatOften`,`completed`,`id`) VALUES (?,?,?,?,?,?,?,nullif(?, 0))"

      protected override fun bind(statement: SQLiteStatement, entity: Task) {
        statement.bindText(1, entity.title)
        val _tmpDescription: String? = entity.description
        if (_tmpDescription == null) {
          statement.bindNull(2)
        } else {
          statement.bindText(2, _tmpDescription)
        }
        val _tmpStartTime: Long? = entity.startTime
        if (_tmpStartTime == null) {
          statement.bindNull(3)
        } else {
          statement.bindLong(3, _tmpStartTime)
        }
        val _tmpEndTime: Long? = entity.endTime
        if (_tmpEndTime == null) {
          statement.bindNull(4)
        } else {
          statement.bindLong(4, _tmpEndTime)
        }
        val _tmpRepeatTag: Time? = entity.repeatTag
        if (_tmpRepeatTag == null) {
          statement.bindNull(5)
        } else {
          statement.bindText(5, __Time_enumToString(_tmpRepeatTag))
        }
        val _tmpRepeatOften: Int? = entity.repeatOften
        if (_tmpRepeatOften == null) {
          statement.bindNull(6)
        } else {
          statement.bindLong(6, _tmpRepeatOften.toLong())
        }
        val _tmpCompleted: Long? = entity.completed
        if (_tmpCompleted == null) {
          statement.bindNull(7)
        } else {
          statement.bindLong(7, _tmpCompleted)
        }
        statement.bindLong(8, entity.id.toLong())
      }
    }
    this.__updateAdapterOfTask = object : EntityDeleteOrUpdateAdapter<Task>() {
      protected override fun createQuery(): String =
          "UPDATE OR REPLACE `Task` SET `title` = ?,`description` = ?,`startTime` = ?,`endTime` = ?,`repeatTag` = ?,`repeatOften` = ?,`completed` = ?,`id` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: Task) {
        statement.bindText(1, entity.title)
        val _tmpDescription: String? = entity.description
        if (_tmpDescription == null) {
          statement.bindNull(2)
        } else {
          statement.bindText(2, _tmpDescription)
        }
        val _tmpStartTime: Long? = entity.startTime
        if (_tmpStartTime == null) {
          statement.bindNull(3)
        } else {
          statement.bindLong(3, _tmpStartTime)
        }
        val _tmpEndTime: Long? = entity.endTime
        if (_tmpEndTime == null) {
          statement.bindNull(4)
        } else {
          statement.bindLong(4, _tmpEndTime)
        }
        val _tmpRepeatTag: Time? = entity.repeatTag
        if (_tmpRepeatTag == null) {
          statement.bindNull(5)
        } else {
          statement.bindText(5, __Time_enumToString(_tmpRepeatTag))
        }
        val _tmpRepeatOften: Int? = entity.repeatOften
        if (_tmpRepeatOften == null) {
          statement.bindNull(6)
        } else {
          statement.bindLong(6, _tmpRepeatOften.toLong())
        }
        val _tmpCompleted: Long? = entity.completed
        if (_tmpCompleted == null) {
          statement.bindNull(7)
        } else {
          statement.bindLong(7, _tmpCompleted)
        }
        statement.bindLong(8, entity.id.toLong())
        statement.bindLong(9, entity.id.toLong())
      }
    }
  }

