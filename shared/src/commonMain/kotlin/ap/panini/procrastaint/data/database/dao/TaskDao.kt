package ap.panini.procrastaint.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import ap.panini.procrastaint.data.entities.Task
import ap.panini.procrastaint.data.entities.TaskCompletion
import ap.panini.procrastaint.data.entities.TaskInfo
import ap.panini.procrastaint.data.entities.TaskMeta
import ap.panini.procrastaint.data.entities.TaskSingle
import ap.panini.procrastaint.data.entities.TaskTag
import ap.panini.procrastaint.data.entities.TaskTagCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Insert
    suspend fun insertTag(taskTag: TaskTag): Long

    @Update
    suspend fun updateTag(taskTag: TaskTag)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTagCrossRef(taskTagCrossRef: TaskTagCrossRef): Long

    @Insert
    suspend fun insertTaskInfo(taskInfo: TaskInfo): Long

    @Update
    suspend fun updateTaskInfo(taskInfo: TaskInfo)

    @Update
    suspend fun updateTaskMeta(taskMeta: TaskMeta)

    @Query("DELETE FROM TaskMeta WHERE taskId = :taskId")
    suspend fun deleteMetasForTask(taskId: Long)

    @Insert
    suspend fun insertTaskMeta(taskMeta: TaskMeta): Long

    @Insert
    suspend fun insertTaskCompletion(taskCompletion: TaskCompletion): Long

    @Delete
    suspend fun deleteTaskCompletion(taskCompletion: TaskCompletion)

    @Delete
    suspend fun deleteTask(task: TaskInfo)

    @Delete
    suspend fun deleteTag(tag: TaskTag)

    @Transaction
    @Query("""SELECT * FROM TaskTagCrossRef WHERE tagId = :tagId""")
    suspend fun getTaskTagCrossRef(tagId: Long): List<TaskTagCrossRef>

    @Transaction
    @Query("""DELETE FROM TaskTagCrossRef WHERE taskId = :taskId""")
    suspend fun deleteTagsCrossRef(taskId: Long)

    @Transaction
    @Query("""SELECT * FROM TaskTag""")
    fun getTags(): Flow<List<TaskTag>>

    @Transaction
    @Query("""SELECT * FROM TaskTag WHERE title LIKE :title || '%'""")
    suspend fun getTagsStarting(title: String): List<TaskTag>

    @Transaction
    @Query("""SELECT * FROM TaskTag WHERE title = :title""")
    suspend fun getTagOrNull(title: String): TaskTag?

    @Transaction
    @Query("""SELECT * FROM TaskTag WHERE tagId = :id""")
    suspend fun getTag(id: Long): TaskTag

    @Transaction
    @Query("""SELECT * FROM TaskInfo WHERE taskId = :id""")
    suspend fun getTask(id: Long): Task

    @Transaction
    @Query("""SELECT * FROM TaskInfo WHERE taskId = :id""")
    suspend fun getTaskOrNull(id: Long): Task?

    @Query("""SELECT * FROM TaskCompletion WHERE completionId = :id""")
    suspend fun getCompletion(id: Long): TaskCompletion

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
        LEFT JOIN TaskTagCrossRef ttcr
            ON ti.taskId = ttcr.taskId
        LEFT JOIN TaskTag tt
            ON ttcr.tagId = tt.tagId
        WHERE (
                (
                    tm.startTime IS NOT NULL
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
                    AND (:to IS NULL OR tm.startTime <= :to)
                )
                OR (
                    :includeNoTimeTasks
                    AND tm.startTime IS NULL
                )
            )
            AND (:taskId IS NULL OR ti.taskId = :taskId)
            AND (:tagId IS NULL OR tt.tagId = :tagId)
        ORDER BY tm.startTime
    """
    )
    fun getTasks(
        from: Long,
        to: Long? = null,
        taskId: Long? = null,
        tagId: Long? = null,
        includeNoTimeTasks: Boolean = false
    ): Flow<List<TaskSingle>>
}
