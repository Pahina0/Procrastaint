package ap.panini.procrastaint.data.repositories

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import ap.panini.procrastaint.data.entities.CalendarSource
import ap.panini.procrastaint.data.entities.ExternalCalendarEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DeviceCalendarRepository(private val context: Context) {

    fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED
    }

    suspend fun getEvents(fromTime: Long, toTime: Long): List<ExternalCalendarEvent> =
        withContext(Dispatchers.IO) {
            if (!hasPermission()) return@withContext emptyList()

            val events = mutableListOf<ExternalCalendarEvent>()
            try {
                val builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
                ContentUris.appendId(builder, fromTime)
                ContentUris.appendId(builder, toTime)
                val uri = builder.build()

                val projection = arrayOf(
                    CalendarContract.Instances.EVENT_ID,
                    CalendarContract.Instances.TITLE,
                    CalendarContract.Instances.BEGIN,
                    CalendarContract.Instances.END,
                    CalendarContract.Instances.ALL_DAY,
                    CalendarContract.Instances.DISPLAY_COLOR,
                    CalendarContract.Instances.DESCRIPTION,
                    CalendarContract.Instances.EVENT_LOCATION,
                    CalendarContract.Instances.CALENDAR_DISPLAY_NAME
                )

                context.contentResolver.query(
                    uri,
                    projection,
                    null,
                    null,
                    "${CalendarContract.Instances.BEGIN} ASC"
                )?.use { cursor ->
                    val idCol = cursor.getColumnIndex(CalendarContract.Instances.EVENT_ID)
                    val titleCol = cursor.getColumnIndex(CalendarContract.Instances.TITLE)
                    val beginCol = cursor.getColumnIndex(CalendarContract.Instances.BEGIN)
                    val endCol = cursor.getColumnIndex(CalendarContract.Instances.END)
                    val allDayCol = cursor.getColumnIndex(CalendarContract.Instances.ALL_DAY)
                    val colorCol = cursor.getColumnIndex(CalendarContract.Instances.DISPLAY_COLOR)
                    val descCol = cursor.getColumnIndex(CalendarContract.Instances.DESCRIPTION)
                    val locCol = cursor.getColumnIndex(CalendarContract.Instances.EVENT_LOCATION)
                    val calNameCol = cursor.getColumnIndex(CalendarContract.Instances.CALENDAR_DISPLAY_NAME)

                    while (cursor.moveToNext()) {
                        val calName = if (calNameCol != -1) cursor.getString(calNameCol) else null
                        // Skip Procrastaint's own synced calendar to avoid duplicating internal tasks
                        if (calName.equals("Procrastaint", ignoreCase = true)) {
                            continue
                        }

                        val id = if (idCol != -1) cursor.getLong(idCol).toString() else ""
                        val title = if (titleCol != -1) cursor.getString(titleCol) ?: "Event" else "Event"
                        val begin = if (beginCol != -1) cursor.getLong(beginCol) else fromTime
                        val end = if (endCol != -1) cursor.getLong(endCol) else begin + 3600000L
                        val isAllDay = if (allDayCol != -1) cursor.getInt(allDayCol) == 1 else false
                        val color = if (colorCol != -1 && !cursor.isNull(colorCol)) cursor.getInt(colorCol) else null
                        val description = if (descCol != -1) cursor.getString(descCol) else null
                        val location = if (locCol != -1) cursor.getString(locCol) else null

                        events.add(
                            ExternalCalendarEvent(
                                id = id,
                                title = title,
                                startTime = begin,
                                endTime = end,
                                isAllDay = isAllDay,
                                source = CalendarSource.DEVICE,
                                color = color,
                                description = description,
                                location = location
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                // Return whatever was collected so far or empty on error/revoked permission
            }
            events
        }
}
