package ap.panini.procrastaint.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ap.panini.procrastaint.data.entities.NetworkSyncItem
import kotlinx.coroutines.flow.Flow

@Dao
interface NetworkSyncDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE) // as you want the first event
    suspend fun insertNetworkSyncItem(item: NetworkSyncItem): Long

    @Delete
    suspend fun deleteSyncItem(item: NetworkSyncItem)

    @Update
    suspend fun updateSyncItem(item: NetworkSyncItem)

    @Query(
        """
        DELETE FROM networkSyncItem
        WHERE taskId = :taskId
        AND metaId = :metaId
        AND time <= :time
        AND `action` = 'CHECK'
    """
    )
    suspend fun deleteChecked(taskId: Long, metaId: Long, time: Long)

    @Query(
        """
        DELETE FROM networkSyncItem
        WHERE taskId = :taskId
        AND metaId = :metaId
        AND time <= :time
        AND `action` = 'UNCHECK'
    """
    )
    suspend fun deleteUnchecked(taskId: Long, metaId: Long, time: Long)

    @Query(
        """
            DELETE FROM networkSyncItem
            WHERE taskId = :taskId
        """
    )
    suspend fun deleteTask(taskId: Long)

    @Query(
        """
            SELECT * FROM
            networkSyncItem
            ORDER BY time
        """
    )
    fun getSyncItems(): Flow<List<NetworkSyncItem>>
}
