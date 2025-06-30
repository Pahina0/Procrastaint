package ap.panini.procrastaint.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import ap.panini.procrastaint.data.entities.Task
import ap.panini.procrastaint.data.entities.TaskCompletion
import ap.panini.procrastaint.data.entities.TaskInfo
import ap.panini.procrastaint.data.entities.TaskMeta
import ap.panini.procrastaint.data.entities.TaskSingle
import ap.panini.procrastaint.data.entities.TaskTag
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTag(taskTag: TaskTag): Long

    @Insert
    suspend fun insertTaskInfo(taskInfo: TaskInfo): Long

    @Insert
    suspend fun insertTaskMeta(taskMeta: TaskMeta): Long

    @Insert
    suspend fun insertTaskCompletion(taskCompletion: TaskCompletion): Long

    @Delete
    suspend fun deleteTaskCompletion(taskCompletion: TaskCompletion)

    @Delete
    suspend fun deleteTask(task: TaskInfo)

    @Transaction
    @Query("""SELECT * FROM TaskTag""")
    fun getTags(): Flow<List<TaskTag>>

    @Transaction
    @Query("""SELECT * FROM TaskTag WHERE title = :title""")
    suspend fun getTagOrNull(title: String): TaskTag?

    @Transaction
    @Query("""SELECT * FROM TaskInfo WHERE taskId = :id""")
    suspend fun getTask(id: Long): Task

    @Transaction
    @Query("""SELECT * FROM TaskInfo WHERE taskId = :id""")
    suspend fun getTaskOrNull(id: Long): Task?

    @Query("""SELECT * FROM TaskCompletion WHERE completionId = :id""")
    fun getCompletion(id: Long): TaskCompletion

    @Query(
        """
        SELECT ti.taskId,
            tm.metaId,
            tc.completionId,
            ti.title,
            ti.description,
            tc.completionTime AS completed,
            tm.startTime,
            tm.endTime,
            tm.repeatTag,
            tm.repeatOften,
            COALESCE(tc.forTime, - 1) AS currentEventTime
        FROM TaskInfo ti
        LEFT JOIN TaskMeta tm
            ON ti.taskId = tm.taskId
        LEFT JOIN TaskCompletion tc
            ON ti.taskId = tc.taskId
                AND tm.metaId = tc.metaId
        WHERE (tm.startTime IS NOT NULL
            AND (
                tm.startTime >= :from
                OR (
                    (
                        tm.repeatTag IS NOT NULL
                        AND tm.repeatOften IS NOT NULL
                        )
                    AND (
                        tm.endTime IS NULL
                        OR tm.endTime >= :from
                        )
                    )
                )
            AND tm.startTime <= :to)
            AND ti.taskId = :taskId
        ORDER BY tm.startTime
    """
    )
    fun getTasksBetweenFiltered(from: Long, to: Long, taskId: Long): Flow<List<TaskSingle>>

    @Query(
        """
        SELECT ti.taskId,
            tm.metaId,
            tc.completionId,
            ti.title,
            ti.description,
            tc.completionTime AS completed,
            tm.startTime,
            tm.endTime,
            tm.repeatTag,
            tm.repeatOften,
            COALESCE(tc.forTime, - 1) AS currentEventTime
        FROM TaskInfo ti
        LEFT JOIN TaskMeta tm
            ON ti.taskId = tm.taskId
        LEFT JOIN TaskCompletion tc
            ON ti.taskId = tc.taskId
                AND tm.metaId = tc.metaId
        WHERE tm.startTime IS NOT NULL
            AND (
                tm.startTime >= :from
                OR (
                    (
                        tm.repeatTag IS NOT NULL
                        AND tm.repeatOften IS NOT NULL
                        )
                    AND (
                        tm.endTime IS NULL
                        OR tm.endTime >= :from
                        )
                    )
                )
            AND tm.startTime <= :to
        ORDER BY tm.startTime
    """
    )
    fun getTasksBetween(from: Long, to: Long): Flow<List<TaskSingle>>

    @Query(
        """
        SELECT ti.taskId,
            tm.metaId,
            tc.completionId,
            ti.title,
            ti.description,
            tc.completionTime AS completed,
            tm.startTime,
            tm.endTime,
            tm.repeatTag,
            tm.repeatOften,
            COALESCE(tc.forTime, - 1) AS currentEventTime
        FROM TaskInfo ti
        LEFT JOIN TaskMeta tm
            ON ti.taskId = tm.taskId
        LEFT JOIN TaskCompletion tc
            ON ti.taskId = tc.taskId
                AND tm.metaId = tc.metaId
        WHERE tc.completionTime IS NULL
            OR (
                (
                    tm.repeatTag IS NOT NULL
                    AND tm.repeatOften IS NOT NULL
                    )
                AND (
                    tm.endTime IS NULL
                    OR tm.endTime >= :from
                    )
                )
    """
    )
    fun getAllTasks(from: Long): Flow<List<TaskSingle>>
}
