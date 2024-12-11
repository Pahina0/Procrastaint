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

  public override suspend fun insertTasks(tasks: List<Task>): Unit = performSuspending(__db, false,
      true) { _connection ->
    __insertAdapterOfTask.insert(_connection, tasks)
  }

  public override suspend fun updateTask(task: Task): Unit = performSuspending(__db, false, true) {
      _connection ->
    __updateAdapterOfTask.handle(_connection, task)
  }

  public override fun getAllTasks(): Flow<List<Task>> {
    val _sql: String = "SELECT * FROM Task"
    return createFlow(__db, false, arrayOf("Task")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _cursorIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _cursorIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _cursorIndexOfStartTime: Int = getColumnIndexOrThrow(_stmt, "startTime")
        val _cursorIndexOfEndTime: Int = getColumnIndexOrThrow(_stmt, "endTime")
        val _cursorIndexOfRepeatTag: Int = getColumnIndexOrThrow(_stmt, "repeatTag")
        val _cursorIndexOfRepeatOften: Int = getColumnIndexOrThrow(_stmt, "repeatOften")
        val _cursorIndexOfCompleted: Int = getColumnIndexOrThrow(_stmt, "completed")
        val _cursorIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _result: MutableList<Task> = mutableListOf()
        while (_stmt.step()) {
          val _item: Task
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_cursorIndexOfTitle)
          val _tmpDescription: String?
          if (_stmt.isNull(_cursorIndexOfDescription)) {
            _tmpDescription = null
          } else {
            _tmpDescription = _stmt.getText(_cursorIndexOfDescription)
          }
          val _tmpStartTime: Long?
          if (_stmt.isNull(_cursorIndexOfStartTime)) {
            _tmpStartTime = null
          } else {
            _tmpStartTime = _stmt.getLong(_cursorIndexOfStartTime)
          }
          val _tmpEndTime: Long?
          if (_stmt.isNull(_cursorIndexOfEndTime)) {
            _tmpEndTime = null
          } else {
            _tmpEndTime = _stmt.getLong(_cursorIndexOfEndTime)
          }
          val _tmpRepeatTag: Time?
          if (_stmt.isNull(_cursorIndexOfRepeatTag)) {
            _tmpRepeatTag = null
          } else {
            _tmpRepeatTag = __Time_stringToEnum(_stmt.getText(_cursorIndexOfRepeatTag))
          }
          val _tmpRepeatOften: Int?
          if (_stmt.isNull(_cursorIndexOfRepeatOften)) {
            _tmpRepeatOften = null
          } else {
            _tmpRepeatOften = _stmt.getLong(_cursorIndexOfRepeatOften).toInt()
          }
          val _tmpCompleted: Long?
          if (_stmt.isNull(_cursorIndexOfCompleted)) {
            _tmpCompleted = null
          } else {
            _tmpCompleted = _stmt.getLong(_cursorIndexOfCompleted)
          }
          _item =
              Task(_tmpTitle,_tmpDescription,_tmpStartTime,_tmpEndTime,_tmpRepeatTag,_tmpRepeatOften,_tmpCompleted)
          _item.id = _stmt.getLong(_cursorIndexOfId).toInt()
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getTaskHistory(): Flow<List<Task>> {
    val _sql: String = "SELECT * FROM TASK WHERE completed IS NOT NULL"
    return createFlow(__db, false, arrayOf("TASK")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _cursorIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _cursorIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _cursorIndexOfStartTime: Int = getColumnIndexOrThrow(_stmt, "startTime")
        val _cursorIndexOfEndTime: Int = getColumnIndexOrThrow(_stmt, "endTime")
        val _cursorIndexOfRepeatTag: Int = getColumnIndexOrThrow(_stmt, "repeatTag")
        val _cursorIndexOfRepeatOften: Int = getColumnIndexOrThrow(_stmt, "repeatOften")
        val _cursorIndexOfCompleted: Int = getColumnIndexOrThrow(_stmt, "completed")
        val _cursorIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _result: MutableList<Task> = mutableListOf()
        while (_stmt.step()) {
          val _item: Task
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_cursorIndexOfTitle)
          val _tmpDescription: String?
          if (_stmt.isNull(_cursorIndexOfDescription)) {
            _tmpDescription = null
          } else {
            _tmpDescription = _stmt.getText(_cursorIndexOfDescription)
          }
          val _tmpStartTime: Long?
          if (_stmt.isNull(_cursorIndexOfStartTime)) {
            _tmpStartTime = null
          } else {
            _tmpStartTime = _stmt.getLong(_cursorIndexOfStartTime)
          }
          val _tmpEndTime: Long?
          if (_stmt.isNull(_cursorIndexOfEndTime)) {
            _tmpEndTime = null
          } else {
            _tmpEndTime = _stmt.getLong(_cursorIndexOfEndTime)
          }
          val _tmpRepeatTag: Time?
          if (_stmt.isNull(_cursorIndexOfRepeatTag)) {
            _tmpRepeatTag = null
          } else {
            _tmpRepeatTag = __Time_stringToEnum(_stmt.getText(_cursorIndexOfRepeatTag))
          }
          val _tmpRepeatOften: Int?
          if (_stmt.isNull(_cursorIndexOfRepeatOften)) {
            _tmpRepeatOften = null
          } else {
            _tmpRepeatOften = _stmt.getLong(_cursorIndexOfRepeatOften).toInt()
          }
          val _tmpCompleted: Long?
          if (_stmt.isNull(_cursorIndexOfCompleted)) {
            _tmpCompleted = null
          } else {
            _tmpCompleted = _stmt.getLong(_cursorIndexOfCompleted)
          }
          _item =
              Task(_tmpTitle,_tmpDescription,_tmpStartTime,_tmpEndTime,_tmpRepeatTag,_tmpRepeatOften,_tmpCompleted)
          _item.id = _stmt.getLong(_cursorIndexOfId).toInt()
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

