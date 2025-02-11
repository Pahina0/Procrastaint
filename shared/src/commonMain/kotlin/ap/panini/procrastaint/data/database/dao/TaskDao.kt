package ap.panini.procrastaint.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import ap.panini.procrastaint.data.entities.TaskCompletion
import ap.panini.procrastaint.data.entities.TaskInfo
import ap.panini.procrastaint.data.entities.TaskMeta
import ap.panini.procrastaint.data.entities.TaskSingle
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Insert
    suspend fun insertTaskInfo(taskInfo: TaskInfo): Long

    @Insert
    suspend fun insertTaskMeta(taskMeta: TaskMeta)

    @Insert
    suspend fun insertTaskCompletion(taskCompletion: TaskCompletion)

    @Delete
    suspend fun deleteTaskCompletion(taskCompletion: TaskCompletion)

    @Query(
        """
        SELECT ti.taskId, tm.metaId, tc.completionId, ti.title, ti.description, tc.completionTime AS completed, tm.startTime, tm.endTime, tm.repeatTag, tm.repeatOften, tm.allDay, COALESCE(tc.forTime, -1) AS currentEventTime
        FROM TaskInfo ti
        LEFT JOIN TaskMeta tm ON ti.taskId = tm.taskId
        LEFT JOIN TaskCompletion tc ON ti.taskId = tc.taskId AND tm.metaId = tc.metaId AND tc.completionTime <= :to
        WHERE tm.startTime IS NOT NULL 
        AND ( 
            tc.completionTime IS NULL 
            OR (
                (tm.repeatTag IS NOT NULL AND tm.repeatOften IS NOT NULL)
                AND (
                    tm.endTime IS NULL
                    OR tm.endTime >= :from
                )
            )
        )
        ORDER BY tm.startTime
    """
    )
    fun getTasksBetween(from: Long, to: Long): Flow<List<TaskSingle>>

    @Query(
        """
        SELECT ti.taskId, tm.metaId, tc.completionId, ti.title, ti.description, tc.completionTime AS completed, tm.startTime, tm.endTime, tm.repeatTag, tm.repeatOften, tm.allDay, COALESCE(tc.forTime, -1) AS currentEventTime
        FROM TaskInfo ti
        LEFT JOIN TaskMeta tm ON ti.taskId = tm.taskId
        LEFT JOIN TaskCompletion tc ON ti.taskId = tc.taskId AND tm.metaId = tc.metaId
        WHERE tc.completionTime IS NULL 
        OR (
            (tm.repeatTag IS NOT NULL AND tm.repeatOften IS NOT NULL)
            AND (
                tm.endTime IS NULL
                OR tm.endTime >= :from
            )
        )
    """
    )
    fun getAllTasks(from: Long): Flow<List<TaskSingle>>
}
