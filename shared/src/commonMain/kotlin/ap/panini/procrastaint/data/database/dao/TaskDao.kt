package ap.panini.procrastaint.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ap.panini.procrastaint.data.model.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Insert
    suspend fun insertTasks(tasks: List<Task>)

    @Query("SELECT * FROM Task")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM TASK WHERE completed IS NOT NULL")
    fun getTaskHistory(): Flow<List<Task>>

    @Query("SELECT * FROM TASK WHERE completed IS NULL")
    fun getIncompleteTasks(): Flow<List<Task>>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateTask(task: Task)

    @Query(
        """
        SELECT * FROM Task 
        WHERE completed IS NULL OR startTime > :from
        ORDER BY startTime
        """
    )
    fun getUpcomingTasksGrouped(from: Long): Flow<List<Task>>
}
