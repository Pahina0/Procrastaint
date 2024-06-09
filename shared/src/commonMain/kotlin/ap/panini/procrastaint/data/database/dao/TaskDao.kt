package ap.panini.procrastaint.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ap.panini.procrastaint.data.model.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Insert
    suspend fun insertTask(task: Task)

    @Query("SELECT * FROM Task")
     fun getAllTasks(): Flow<List<Task>>
}