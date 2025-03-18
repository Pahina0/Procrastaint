package ap.panini.procrastaint.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import ap.panini.procrastaint.data.entities.NetworkSyncItem
import kotlinx.coroutines.flow.Flow

@Dao
interface NetworkSyncDao {
    @Insert
    suspend fun insertNetworkSyncItem(item: NetworkSyncItem): Long

    @Delete
    suspend fun deleteSyncItem(item: NetworkSyncItem)

    @Query(
        """
            SELECT * FROM
            networkSyncItem
            ORDER BY time
        """
    )
    fun getSyncItems(): Flow<List<NetworkSyncItem>>

}