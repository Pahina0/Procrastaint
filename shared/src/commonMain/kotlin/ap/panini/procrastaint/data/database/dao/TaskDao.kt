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

    @Query("SELECT * FROM Task ORDER BY startTimes")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM Task WHERE completed IS FALSE ORDER BY startTimes")
    fun getIncompleteTasks(): Flow<List<Task>>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateTask(task: Task)
}
