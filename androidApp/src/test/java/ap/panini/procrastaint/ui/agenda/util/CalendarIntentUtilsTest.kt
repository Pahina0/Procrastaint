package ap.panini.procrastaint.ui.agenda.util

import ap.panini.procrastaint.data.entities.CalendarSource
import ap.panini.procrastaint.data.entities.ExternalCalendarEvent
import org.junit.Assert.assertEquals
import org.junit.Test

class CalendarIntentUtilsTest {

    @Test
    fun testCreateViewEventUriStringWithNumericId() {
        val event = ExternalCalendarEvent(
            id = "12345",
            title = "Team Standup",
            startTime = 1700000000000L,
            endTime = 1700003600000L,
            source = CalendarSource.DEVICE
        )
        val uriString = CalendarIntentUtils.createViewEventUriString(event)
        assertEquals("content://com.android.calendar/events/12345", uriString)
    }

    @Test
    fun testCreateViewEventUriStringWithAlphanumericIdFallsBackToTime() {
        val event = ExternalCalendarEvent(
            id = "google_cal_alphanumeric_id",
            title = "All Hands",
            startTime = 1700000000000L,
            endTime = 1700003600000L,
            source = CalendarSource.GOOGLE
        )
        val uriString = CalendarIntentUtils.createViewEventUriString(event)
        assertEquals("content://com.android.calendar/time/1700000000000", uriString)
    }
}
