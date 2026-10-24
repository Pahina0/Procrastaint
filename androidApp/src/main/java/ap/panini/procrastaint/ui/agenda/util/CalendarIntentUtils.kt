package ap.panini.procrastaint.ui.agenda.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import ap.panini.procrastaint.data.entities.ExternalCalendarEvent

object CalendarIntentUtils {

    private const val CALENDAR_EVENTS_URI_PREFIX = "content://com.android.calendar/events"
    private const val CALENDAR_TIME_URI_PREFIX = "content://com.android.calendar/time"

    /**
     * Constructs the content URI string to view an external calendar event.
     * Uses the event ID if numeric, otherwise falls back to viewing the calendar at the event's start time.
     */
    fun createViewEventUriString(event: ExternalCalendarEvent): String {
        val numericId = event.id.toLongOrNull()
        return if (numericId != null) {
            "$CALENDAR_EVENTS_URI_PREFIX/$numericId"
        } else {
            "$CALENDAR_TIME_URI_PREFIX/${event.startTime}"
        }
    }

    /**
     * Creates an [Intent] to view an external calendar event.
     */
    fun createViewEventIntent(event: ExternalCalendarEvent): Intent {
        val uriString = createViewEventUriString(event)
        return Intent(Intent.ACTION_VIEW, Uri.parse(uriString))
    }

    /**
     * Attempts to launch the native calendar app to view the given event.
     * Returns true if successfully launched, false if failed.
     */
    fun launchCalendarEvent(context: Context, event: ExternalCalendarEvent): Boolean {
        return try {
            val intent = createViewEventIntent(event).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            try {
                val fallbackIntent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_APP_CALENDAR)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallbackIntent)
                true
            } catch (_: Exception) {
                false
            }
        }
    }
}
