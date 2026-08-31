package ap.panini.procrastaint.data.repositories

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import ap.panini.procrastaint.data.entities.CalendarSource
import ap.panini.procrastaint.data.entities.ExternalCalendarEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private data class EventColumnIndices(
    val idCol: Int,
    val titleCol: Int,
    val beginCol: Int,
    val endCol: Int,
    val allDayCol: Int,
    val colorCol: Int,
    val descCol: Int,
    val locCol: Int,
    val calNameCol: Int
) {
    constructor(cursor: Cursor) : this(
        idCol = cursor.getColumnIndex(CalendarContract.Instances.EVENT_ID),
        titleCol = cursor.getColumnIndex(CalendarContract.Instances.TITLE),
        beginCol = cursor.getColumnIndex(CalendarContract.Instances.BEGIN),
        endCol = cursor.getColumnIndex(CalendarContract.Instances.END),
        allDayCol = cursor.getColumnIndex(CalendarContract.Instances.ALL_DAY),
        colorCol = cursor.getColumnIndex(CalendarContract.Instances.DISPLAY_COLOR),
        descCol = cursor.getColumnIndex(CalendarContract.Instances.DESCRIPTION),
        locCol = cursor.getColumnIndex(CalendarContract.Instances.EVENT_LOCATION),
        calNameCol = cursor.getColumnIndex(CalendarContract.Instances.CALENDAR_DISPLAY_NAME)
    )
}

class DeviceCalendarRepository(private val context: Context) {

    companion object {
        private const val DEFAULT_EVENT_DURATION_MS = 3600000L
        private val PROJECTION = arrayOf(
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
    }

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

                context.contentResolver.query(
                    builder.build(),
                    PROJECTION,
                    null,
                    null,
                    "${CalendarContract.Instances.BEGIN} ASC"
                )?.use { cursor ->
                    parseEventsFromCursor(cursor, fromTime, events)
                }
            } catch (_: Exception) {
                // Return whatever was collected so far or empty on error
            }
            events
        }

    private fun parseEventsFromCursor(
        cursor: Cursor,
        fromTime: Long,
        outEvents: MutableList<ExternalCalendarEvent>
    ) {
        val indices = EventColumnIndices(cursor)
        while (cursor.moveToNext()) {
            val event = parseSingleEvent(cursor, fromTime, indices)
            if (event != null) {
                outEvents.add(event)
            }
        }
    }

    private fun parseSingleEvent(
        cursor: Cursor,
        fromTime: Long,
        indices: EventColumnIndices
    ): ExternalCalendarEvent? {
        val calName = if (indices.calNameCol != -1) cursor.getString(indices.calNameCol) else null
        if (calName.equals("Procrastaint", ignoreCase = true)) return null

        val id = if (indices.idCol != -1) cursor.getLong(indices.idCol).toString() else ""
        val title = if (indices.titleCol != -1) cursor.getString(indices.titleCol) ?: "Event" else "Event"
        val begin = if (indices.beginCol != -1) cursor.getLong(indices.beginCol) else fromTime
        val end = if (indices.endCol != -1) cursor.getLong(indices.endCol) else begin + DEFAULT_EVENT_DURATION_MS
        val isAllDay = if (indices.allDayCol != -1) cursor.getInt(indices.allDayCol) == 1 else false
        val color = if (indices.colorCol != -1 && !cursor.isNull(indices.colorCol)) {
            cursor.getInt(indices.colorCol)
        } else {
            null
        }

        return ExternalCalendarEvent(
            id = id,
            title = title,
            startTime = begin,
            endTime = end,
            isAllDay = isAllDay,
            source = CalendarSource.DEVICE,
            color = color,
            description = if (indices.descCol != -1) cursor.getString(indices.descCol) else null,
            location = if (indices.locCol != -1) cursor.getString(indices.locCol) else null
        )
    }
}
