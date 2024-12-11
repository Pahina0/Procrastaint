package ap.panini.procrastaint.`data`.database

import androidx.room.RoomDatabaseConstructor

public actual object ProcrastaintDatabaseConstructor : RoomDatabaseConstructor<ProcrastaintDatabase>
    {
  actual override fun initialize(): ProcrastaintDatabase =
      ap.panini.procrastaint.`data`.database.ProcrastaintDatabase_Impl()
}
