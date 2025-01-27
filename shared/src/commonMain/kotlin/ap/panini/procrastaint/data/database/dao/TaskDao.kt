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

    // TODO: FIX repeating and their completions
    @Query("""
        SELECT ti.taskId, tm.metaId, tc.completionId, ti.title, ti.description, tc.completionTime AS completed, tm.startTime, tm.endTime, tm.repeatTag, tm.repeatOften, tm.allDay, COALESCE(tc.forTime, -1) AS currentEventTime
        FROM TaskInfo ti
        LEFT JOIN TaskMeta tm ON ti.taskId = tm.taskId
        LEFT JOIN TaskCompletion tc ON ti.taskId = tc.taskId AND tm.metaId = tc.metaId
        WHERE tc.completionTime IS NULL 
        OR (tm.startTime <= :to AND tm.endTime >= :from) 
        OR (tm.startTime >= :from AND tm.startTime <= :to AND tm.endTime IS NULL)
        ORDER BY tm.startTime
    """)
    fun getUpcomingTasks(from: Long, to: Long) : Flow<List<TaskSingle>>
//    @Insert
//    suspend fun insertTasks(taskInfos: List<TaskInfo>)
//
//    @Query("SELECT * FROM Task")
//    fun getAllTasks(): Flow<List<TaskInfo>>
//
//    @Query("SELECT * FROM TASK WHERE completed IS NOT NULL")
//    fun getTaskHistory(): Flow<List<TaskInfo>>
//
//    @Query("SELECT * FROM TASK WHERE completed IS NULL")
//    fun getIncompleteTasks(): Flow<List<TaskInfo>>
//
//    @Update(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun updateTask(taskInfo: TaskInfo)
//
//    @Query(
//        """
//        SELECT * FROM Task
//        WHERE completed IS NULL OR startTime > :from
//        ORDER BY startTime
//        """
//    )
//    fun getUpcomingTasksGrouped(from: Long): Flow<List<TaskInfo>>
}
